package net.wurstclient.commands;

import net.minecraft.class_746;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.events.ChatInputListener;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.ChatUtils;
import org.apache.commons.lang3.StringUtils;

public final class AnnoyCmd
extends Command
implements ChatInputListener {
    private final CheckboxSetting rcMode = new CheckboxSetting("RC mode", "Remote control mode. Re-enables a bug that allows .annoy to run Wurst commands. Not recommended for security reasons, but until we have a proper remote control feature, this is at least better than nothing.", false);
    private boolean enabled;
    private String target;

    public AnnoyCmd() {
        super("annoy", "Annoys a player by repeating everything they say.", ".annoy <player>", "Turn off: .annoy");
        this.addSetting(this.rcMode);
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length > 0) {
            String newTarget = String.join((CharSequence)" ", args);
            if (this.enabled && this.target != null && this.target.equalsIgnoreCase(newTarget)) {
                this.disable();
                return;
            }
            if (this.enabled) {
                this.disable();
            }
            this.enable(args);
        } else {
            if (!this.enabled) {
                throw new CmdError(".annoy is already turned off.");
            }
            this.disable();
        }
    }

    private void enable(String[] args) throws CmdException {
        if (args.length < 1) {
            throw new CmdSyntaxError();
        }
        this.target = String.join((CharSequence)" ", args);
        ChatUtils.message("Now annoying " + this.target + ".");
        class_746 player = AnnoyCmd.MC.field_1724;
        if (player != null && this.target.equals(player.method_5477().getString())) {
            ChatUtils.warning("Annoying yourself is a bad idea!");
        }
        EVENTS.add(ChatInputListener.class, this);
        this.enabled = true;
    }

    private void disable() {
        EVENTS.remove(ChatInputListener.class, this);
        if (this.target != null) {
            ChatUtils.message("No longer annoying " + this.target + ".");
            this.target = null;
        }
        this.enabled = false;
    }

    @Override
    public void onReceivedMessage(ChatInputListener.ChatInputEvent event) {
        String message = event.getComponent().getString();
        if (message.startsWith("\u00a7c[\u00a76Wurst\u00a7c]\u00a7r ")) {
            return;
        }
        String prefix1 = this.target + ">";
        if (message.contains("<" + prefix1) || message.contains(prefix1)) {
            this.repeat(message, prefix1);
            return;
        }
        String prefix2 = this.target + ":";
        if (message.contains("] " + prefix2) || message.contains("]" + prefix2)) {
            this.repeat(message, prefix2);
        }
    }

    private void repeat(String message, String prefix) {
        int beginIndex = message.indexOf(prefix) + prefix.length();
        String repeated = message.substring(beginIndex).trim();
        repeated = StringUtils.normalizeSpace((String)repeated);
        if (this.rcMode.isChecked() && repeated.startsWith(".")) {
            WURST.getCmdProcessor().process(repeated.substring(1));
        } else {
            MC.method_1562().method_45729(repeated);
        }
    }
}
