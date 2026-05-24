package net.wurstclient.clickgui.screens;

import net.minecraft.class_11908;
import net.minecraft.class_1799;
import net.minecraft.class_1935;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.wurstclient.settings.BlockSetting;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RenderUtils;
import org.joml.Matrix3x2fStack;

public final class EditBlockScreen
extends class_437 {
    private final class_437 prevScreen;
    private final BlockSetting setting;
    private class_342 blockField;
    private class_4185 doneButton;

    public EditBlockScreen(class_437 prevScreen, BlockSetting setting) {
        super((class_2561)class_2561.method_43470((String)""));
        this.prevScreen = prevScreen;
        this.setting = setting;
    }

    public void method_25426() {
        int x1 = this.field_22789 / 2 - 100;
        int y1 = 59;
        int y2 = this.field_22790 / 3 * 2;
        class_327 tr = this.field_22787.field_1772;
        String valueString = this.setting.getBlockName();
        this.blockField = new class_342(tr, x1, y1, 178, 20, (class_2561)class_2561.method_43470((String)""));
        this.blockField.method_1852(valueString);
        this.blockField.method_1875(0);
        this.blockField.method_1880(256);
        this.method_25429((class_364)this.blockField);
        this.method_25395((class_364)this.blockField);
        this.blockField.method_25365(true);
        this.doneButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Done"), b -> this.done()).method_46434(x1, y2, 200, 20).method_46431();
        this.method_37063((class_364)this.doneButton);
    }

    private void done() {
        String nameOrId = this.blockField.method_1882();
        class_2248 block = BlockUtils.getBlockFromNameOrID(nameOrId);
        if (block != null) {
            this.setting.setBlock(block);
        }
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
        Matrix3x2fStack matrixStack = context.method_51448();
        class_327 tr = this.field_22787.field_1772;
        context.method_25300(tr, this.setting.getName(), this.field_22789 / 2, 20, -1);
        this.blockField.method_25394(context, mouseX, mouseY, partialTicks);
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
        }
        context.field_59826.method_71067();
        matrixStack.pushMatrix();
        matrixStack.translate((float)(-64 + this.field_22789 / 2 - 100), 115.0f);
        boolean lblAbove = !this.blockField.method_1882().isEmpty() || this.blockField.method_25370();
        String lblText = lblAbove ? "Block ID or number:" : "block ID or number";
        int lblX = lblAbove ? 50 : 68;
        int lblY = lblAbove ? -66 : -50;
        int lblColor = lblAbove ? -986896 : -8355712;
        context.method_25303(tr, lblText, lblX, lblY, lblColor);
        int border = this.blockField.method_25370() ? -1 : -6250336;
        int black = -16777216;
        context.method_25294(48, -56, 64, -36, border);
        context.method_25294(49, -55, 65, -37, black);
        context.method_25294(242, -56, 246, -36, border);
        context.method_25294(241, -55, 245, -37, black);
        matrixStack.popMatrix();
        String nameOrId = this.blockField.method_1882();
        class_2248 blockToAdd = BlockUtils.getBlockFromNameOrID(nameOrId);
        if (blockToAdd == null) {
            blockToAdd = class_2246.field_10124;
        }
        RenderUtils.drawItem(context, new class_1799((class_1935)blockToAdd), -64 + this.field_22789 / 2 - 100 + 52, 63, false);
    }

    public boolean method_25421() {
        return false;
    }

    public boolean method_25422() {
        return false;
    }
}
