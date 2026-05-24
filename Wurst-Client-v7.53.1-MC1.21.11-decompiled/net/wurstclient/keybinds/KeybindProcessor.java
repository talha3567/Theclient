package net.wurstclient.keybinds;

import net.minecraft.class_1041;
import net.minecraft.class_11908;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.screens.ClickGuiScreen;
import net.wurstclient.command.CmdProcessor;
import net.wurstclient.events.KeyPressListener;
import net.wurstclient.events.MouseButtonPressListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hack.HackList;
import net.wurstclient.keybinds.KeybindList;
import net.wurstclient.util.ChatUtils;

public final class KeybindProcessor
implements KeyPressListener,
MouseButtonPressListener {
    private final HackList hax;
    private final KeybindList keybinds;
    private final CmdProcessor cmdProcessor;

    public KeybindProcessor(HackList hax, KeybindList keybinds, CmdProcessor cmdProcessor) {
        this.hax = hax;
        this.keybinds = keybinds;
        this.cmdProcessor = cmdProcessor;
    }

    @Override
    public void onKeyPress(KeyPressListener.KeyPressEvent event) {
        if (event.getAction() != 1) {
            return;
        }
        if (!this.isKeybindProcessingAllowed()) {
            return;
        }
        String keyName = this.getKeyName(event);
        String cmds = this.keybinds.getCommands(keyName);
        if (cmds == null) {
            return;
        }
        this.processCmds(cmds);
    }

    @Override
    public void onMouseButtonPress(MouseButtonPressListener.MouseButtonPressEvent event) {
        if (event.getAction() != 1) {
            return;
        }
        if (!this.isKeybindProcessingAllowed()) {
            return;
        }
        String keyName = this.getMouseButtonName(event);
        String cmds = this.keybinds.getCommands(keyName);
        if (cmds == null) {
            return;
        }
        this.processCmds(cmds);
    }

    private boolean isKeybindProcessingAllowed() {
        if (class_3675.method_15987((class_1041)WurstClient.MC.method_22683(), (int)292)) {
            return false;
        }
        class_437 screen = WurstClient.MC.field_1755;
        return screen == null || screen instanceof ClickGuiScreen;
    }

    private String getKeyName(KeyPressListener.KeyPressEvent event) {
        int keyCode = event.getKeyCode();
        int scanCode = event.getScanCode();
        return class_3675.method_15985((class_11908)new class_11908(keyCode, scanCode, event.getModifiers())).method_1441();
    }

    private String getMouseButtonName(MouseButtonPressListener.MouseButtonPressEvent event) {
        return class_3675.class_307.field_1672.method_1447(event.getButton()).method_1441();
    }

    private void processCmds(String cmds) {
        cmds = cmds.replace(";", "\u00a7").replace("\u00a7\u00a7", ";");
        for (String cmd : cmds.split("\u00a7")) {
            this.processCmd(cmd.trim());
        }
    }

    private void processCmd(String cmd) {
        if (cmd.startsWith(".")) {
            this.cmdProcessor.process(cmd.substring(1));
        } else if (cmd.contains(" ")) {
            this.cmdProcessor.process(cmd);
        } else {
            Hack hack = this.hax.getHackByName(cmd);
            if (hack == null) {
                this.cmdProcessor.process(cmd);
                return;
            }
            if (!hack.isEnabled() && this.hax.tooManyHaxHack.isEnabled() && this.hax.tooManyHaxHack.isBlocked(hack)) {
                ChatUtils.error(hack.getName() + " is blocked by TooManyHax.");
                return;
            }
            hack.setEnabled(!hack.isEnabled());
        }
    }
}
