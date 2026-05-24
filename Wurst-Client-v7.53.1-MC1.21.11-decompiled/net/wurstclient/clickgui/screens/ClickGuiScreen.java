package net.wurstclient.clickgui.screens;

import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4068;
import net.minecraft.class_437;
import net.wurstclient.clickgui.ClickGui;

public final class ClickGuiScreen
extends class_437 {
    private final ClickGui gui;

    public ClickGuiScreen(ClickGui gui) {
        super((class_2561)class_2561.method_43470((String)""));
        this.gui = gui;
    }

    public boolean method_25421() {
        return false;
    }

    public boolean method_25402(class_11909 context, boolean doubleClick) {
        this.gui.handleMouseClick(context);
        return super.method_25402(context, doubleClick);
    }

    public boolean method_25406(class_11909 context) {
        this.gui.handleMouseRelease(context.comp_4798(), context.comp_4799(), context.method_74245());
        return super.method_25406(context);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.gui.handleMouseScroll(mouseX, mouseY, verticalAmount);
        return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
        }
        this.gui.render(context, mouseX, mouseY, partialTicks);
    }

    public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
    }
}
