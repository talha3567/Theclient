package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Pattern;
import net.minecraft.class_3544;
import net.minecraft.class_640;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.ChatInputListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.TextFieldSetting;
import net.wurstclient.util.ChatUtils;

@SearchTags(value={"mass tpa"})
@DontSaveState
public final class MassTpaHack
extends Hack
implements UpdateListener,
ChatInputListener {
    private static final Pattern ALLOWED_COMMANDS = Pattern.compile("^/+[a-zA-Z0-9_\\-]+$");
    private final TextFieldSetting commandSetting = new TextFieldSetting("Command", "The command to use for teleporting.\nExamples: /tp, /tpa, /tpahere, /tpo", "/tpa", s -> s.length() < 64 && ALLOWED_COMMANDS.matcher((CharSequence)s).matches());
    private final SliderSetting delay = new SliderSetting("Delay", "The delay between each teleportation request.", 20.0, 1.0, 200.0, 1.0, SliderSetting.ValueDisplay.INTEGER.withSuffix(" ticks").withLabel(1.0, "1 tick"));
    private final CheckboxSetting ignoreErrors = new CheckboxSetting("Ignore errors", "Whether to ignore messages from the server telling you that the teleportation command isn't valid or that you don't have permission to use it.", false);
    private final CheckboxSetting stopWhenAccepted = new CheckboxSetting("Stop when accepted", "Whether to stop sending more teleportation requests when someone accepts one of them.", true);
    private final Random random = new Random();
    private final ArrayList<String> players = new ArrayList();
    private String command;
    private int index;
    private int timer;

    public MassTpaHack() {
        super("MassTPA");
        this.setCategory(Category.CHAT);
        this.addSetting(this.commandSetting);
        this.addSetting(this.delay);
        this.addSetting(this.ignoreErrors);
        this.addSetting(this.stopWhenAccepted);
    }

    @Override
    protected void onEnable() {
        this.players.clear();
        this.index = 0;
        this.timer = 0;
        this.command = this.commandSetting.getValue().substring(1);
        String playerName = MC.method_1548().method_1676();
        for (class_640 info : MassTpaHack.MC.field_1724.field_3944.method_2880()) {
            String name = info.method_2966().name();
            if ((name = class_3544.method_15440((String)name)).equalsIgnoreCase(playerName)) continue;
            this.players.add(name);
        }
        Collections.shuffle(this.players, this.random);
        EVENTS.add(ChatInputListener.class, this);
        EVENTS.add(UpdateListener.class, this);
        if (this.players.isEmpty()) {
            ChatUtils.error("Couldn't find any players.");
            this.setEnabled(false);
        }
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(ChatInputListener.class, this);
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        if (this.timer > 0) {
            --this.timer;
            return;
        }
        if (this.index >= this.players.size()) {
            this.setEnabled(false);
            return;
        }
        MC.method_1562().method_45730(this.command + " " + this.players.get(this.index));
        ++this.index;
        this.timer = this.delay.getValueI() - 1;
    }

    @Override
    public void onReceivedMessage(ChatInputListener.ChatInputEvent event) {
        String message = event.getComponent().getString().toLowerCase();
        if (message.startsWith("\u00a7c[\u00a76wurst\u00a7c]")) {
            return;
        }
        if (message.contains("/help") || message.contains("permission")) {
            if (this.ignoreErrors.isChecked()) {
                return;
            }
            event.cancel();
            ChatUtils.error("This server doesn't have a " + this.command.toUpperCase() + " command.");
            this.setEnabled(false);
        } else if (message.contains("accepted") && message.contains("request") || message.contains("akzeptiert") && message.contains("anfrage")) {
            if (!this.stopWhenAccepted.isChecked()) {
                return;
            }
            event.cancel();
            ChatUtils.message("Someone accepted your " + this.command.toUpperCase() + " request. Stopping.");
            this.setEnabled(false);
        }
    }
}
