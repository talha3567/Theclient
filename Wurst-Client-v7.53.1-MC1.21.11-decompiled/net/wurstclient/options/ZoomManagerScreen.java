package net.wurstclient.options;

import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.wurstclient.WurstClient;
import net.wurstclient.options.PressAKeyCallback;
import net.wurstclient.options.PressAKeyScreen;
import net.wurstclient.other_features.ZoomOtf;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;

public class ZoomManagerScreen
extends class_437
implements PressAKeyCallback {
    private class_437 prevScreen;
    private class_4185 scrollButton;

    public ZoomManagerScreen(class_437 par1GuiScreen) {
        super((class_2561)class_2561.method_43470((String)""));
        this.prevScreen = par1GuiScreen;
    }

    public void method_25426() {
        WurstClient wurst = WurstClient.INSTANCE;
        ZoomOtf zoom = wurst.getOtfs().zoomOtf;
        SliderSetting level = zoom.getLevelSetting();
        CheckboxSetting scroll = zoom.getScrollSetting();
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Back"), b -> this.field_22787.method_1507(this.prevScreen)).method_46434(this.field_22789 / 2 - 100, this.field_22790 / 4 + 144 - 16, 200, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Zoom Key: ").method_10852(zoom.getTranslatedKeybindName()), b -> this.field_22787.method_1507((class_437)new PressAKeyScreen(this))).method_46434(this.field_22789 / 2 - 79, this.field_22790 / 4 + 24 - 16, 158, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"More"), b -> level.increaseValue()).method_46434(this.field_22789 / 2 - 79, this.field_22790 / 4 + 72 - 16, 50, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Less"), b -> level.decreaseValue()).method_46434(this.field_22789 / 2 - 25, this.field_22790 / 4 + 72 - 16, 50, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Default"), b -> level.setValue(level.getDefaultValue())).method_46434(this.field_22789 / 2 + 29, this.field_22790 / 4 + 72 - 16, 50, 20).method_46431());
        this.scrollButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)("Use Mouse Wheel: " + this.onOrOff(scroll.isChecked()))), b -> this.toggleScroll()).method_46434(this.field_22789 / 2 - 79, this.field_22790 / 4 + 96 - 16, 158, 20).method_46431();
        this.method_37063((class_364)this.scrollButton);
    }

    private void toggleScroll() {
        CheckboxSetting scroll;
        ZoomOtf zoom = WurstClient.INSTANCE.getOtfs().zoomOtf;
        scroll.setChecked(!(scroll = zoom.getScrollSetting()).isChecked());
        this.scrollButton.method_25355((class_2561)class_2561.method_43470((String)("Use Mouse Wheel: " + this.onOrOff(scroll.isChecked()))));
    }

    private String onOrOff(boolean on) {
        return on ? "ON" : "OFF";
    }

    public void method_25419() {
        this.field_22787.method_1507(this.prevScreen);
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        ZoomOtf zoom = WurstClient.INSTANCE.getOtfs().zoomOtf;
        SliderSetting level = zoom.getLevelSetting();
        context.method_25300(this.field_22793, "Zoom Manager", this.field_22789 / 2, 40, -1);
        context.method_25303(this.field_22793, "Zoom Level: " + level.getValueString(), this.field_22789 / 2 - 75, this.field_22790 / 4 + 44, -986896);
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void setKey(String key) {
        WurstClient.INSTANCE.getOtfs().zoomOtf.setBoundKey(key);
    }
}
