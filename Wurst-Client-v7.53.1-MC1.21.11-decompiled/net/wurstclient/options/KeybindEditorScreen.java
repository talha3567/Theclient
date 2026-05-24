package net.wurstclient.options;

import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.wurstclient.WurstClient;
import net.wurstclient.keybinds.Keybind;
import net.wurstclient.options.PressAKeyCallback;
import net.wurstclient.options.PressAKeyScreen;

public final class KeybindEditorScreen
extends class_437
implements PressAKeyCallback {
    private final class_437 prevScreen;
    private String key;
    private final String oldKey;
    private final String oldCommands;
    private class_342 commandField;

    public KeybindEditorScreen(class_437 prevScreen) {
        super((class_2561)class_2561.method_43470((String)""));
        this.prevScreen = prevScreen;
        this.key = "NONE";
        this.oldKey = null;
        this.oldCommands = null;
    }

    public KeybindEditorScreen(class_437 prevScreen, String key, String commands) {
        super((class_2561)class_2561.method_43470((String)""));
        this.prevScreen = prevScreen;
        this.key = key;
        this.oldKey = key;
        this.oldCommands = commands;
    }

    public void method_25426() {
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Change Key"), b -> this.field_22787.method_1507((class_437)new PressAKeyScreen(this))).method_46434(this.field_22789 / 2 - 100, 60, 200, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Save"), b -> this.save()).method_46434(this.field_22789 / 2 - 100, this.field_22790 / 4 + 72, 200, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Cancel"), b -> this.field_22787.method_1507(this.prevScreen)).method_46434(this.field_22789 / 2 - 100, this.field_22790 / 4 + 96, 200, 20).method_46431());
        this.commandField = new class_342(this.field_22793, this.field_22789 / 2 - 100, 100, 200, 20, (class_2561)class_2561.method_43470((String)""));
        this.commandField.method_1880(65536);
        this.method_25429((class_364)this.commandField);
        this.method_25395((class_364)this.commandField);
        this.commandField.method_25365(true);
        if (this.oldCommands != null) {
            this.commandField.method_1852(this.oldCommands);
        }
    }

    private void save() {
        if (this.oldKey != null) {
            WurstClient.INSTANCE.getKeybinds().remove(this.oldKey);
        }
        WurstClient.INSTANCE.getKeybinds().add(this.key, this.commandField.method_1882());
        this.field_22787.method_1507(this.prevScreen);
    }

    public boolean method_25402(class_11909 context, boolean doubleClick) {
        this.commandField.method_25402(context, doubleClick);
        return super.method_25402(context, doubleClick);
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        context.method_25300(this.field_22793, (this.oldKey != null ? "Edit" : "Add") + " Keybind", this.field_22789 / 2, 20, -1);
        context.method_25303(this.field_22793, "Key: " + Keybind.getDisplayKey(this.key), this.field_22789 / 2 - 100, 47, -986896);
        context.method_25303(this.field_22793, "Commands (separated by ';')", this.field_22789 / 2 - 100, 87, -986896);
        this.commandField.method_25394(context, mouseX, mouseY, partialTicks);
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
        }
    }

    public void method_25419() {
        this.field_22787.method_1507(this.prevScreen);
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }
}
