package net.wurstclient.clickgui.screens;

import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.wurstclient.settings.TextFieldSetting;

public final class EditTextFieldScreen
extends class_437 {
    private final class_437 prevScreen;
    private final TextFieldSetting setting;
    private class_342 valueField;
    private class_4185 doneButton;

    public EditTextFieldScreen(class_437 prevScreen, TextFieldSetting setting) {
        super((class_2561)class_2561.method_43470((String)""));
        this.prevScreen = prevScreen;
        this.setting = setting;
    }

    public void method_25426() {
        int x1 = this.field_22789 / 2 - 100;
        int y1 = 60;
        int y2 = this.field_22790 / 3 * 2;
        class_327 tr = this.field_22787.field_1772;
        this.valueField = new class_342(tr, x1, y1, 200, 20, (class_2561)class_2561.method_43470((String)""));
        this.valueField.method_1880(Integer.MAX_VALUE);
        this.valueField.method_1852(this.setting.getValue());
        this.valueField.method_1875(0);
        this.method_25429((class_364)this.valueField);
        this.method_25395((class_364)this.valueField);
        this.valueField.method_25365(true);
        this.doneButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Done"), b -> this.done()).method_46434(x1, y2, 200, 20).method_46431();
        this.method_37063((class_364)this.doneButton);
    }

    private void done() {
        String value = this.valueField.method_1882();
        this.setting.setValue(value);
        this.field_22787.method_1507(this.prevScreen);
    }

    public boolean method_25404(class_11908 context) {
        switch (context.comp_4795()) {
            case 257: {
                this.done();
                break;
            }
            case 256: {
                this.field_22787.method_1507(this.prevScreen);
            }
        }
        return super.method_25404(context);
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        context.method_25300(this.field_22787.field_1772, this.setting.getName(), this.field_22789 / 2, 20, -1);
        this.valueField.method_25394(context, mouseX, mouseY, partialTicks);
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
        }
    }

    public boolean method_25421() {
        return false;
    }

    public boolean method_25422() {
        return false;
    }
}
