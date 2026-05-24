package net.wurstclient.hacks.templatetool;

import net.minecraft.class_1041;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_3675;
import net.minecraft.class_3965;
import net.minecraft.class_4587;
import net.wurstclient.hacks.TemplateToolHack;
import net.wurstclient.hacks.templatetool.TemplateToolState;
import net.wurstclient.util.RenderUtils;

public abstract class SelectPositionState
extends TemplateToolState {
    private class_2338 crosshairBlock;

    @Override
    public final void onUpdate(TemplateToolHack hack) {
        this.crosshairBlock = this.getCrosshairBlock();
        if (SelectPositionState.MC.field_1690.field_1904.method_1434() && this.crosshairBlock != null) {
            this.setSelectedPos(hack, this.crosshairBlock);
            return;
        }
        if (this.isPressingEnter() && this.getSelectedPos(hack) != null) {
            hack.setState(this.getNextState());
        }
    }

    private class_2338 getCrosshairBlock() {
        class_239 class_2392 = SelectPositionState.MC.field_1765;
        if (!(class_2392 instanceof class_3965)) {
            return null;
        }
        class_3965 bHitResult = (class_3965)class_2392;
        class_2338 pos = bHitResult.method_17777();
        if (SelectPositionState.MC.field_1690.field_1832.method_1434()) {
            pos = pos.method_10093(bHitResult.method_17780());
        }
        return pos;
    }

    private boolean isPressingEnter() {
        return class_3675.method_15987((class_1041)MC.method_22683(), (int)257);
    }

    @Override
    public void onRender(TemplateToolHack hack, class_4587 matrixStack, float partialTicks) {
        if (this.crosshairBlock == null) {
            return;
        }
        class_238 box = new class_238(this.crosshairBlock).method_1011(0.0625);
        int black = Integer.MIN_VALUE;
        int gray = 641744960;
        RenderUtils.drawOutlinedBox(matrixStack, box, black, false);
        RenderUtils.drawSolidBox(matrixStack, box, gray, false);
    }

    @Override
    protected final String getMessage(TemplateToolHack hack) {
        if (this.getSelectedPos(hack) != null) {
            return "Press enter to confirm, or select a different position.";
        }
        return this.getDefaultMessage();
    }

    protected abstract String getDefaultMessage();

    protected abstract class_2338 getSelectedPos(TemplateToolHack var1);

    protected abstract void setSelectedPos(TemplateToolHack var1, class_2338 var2);

    protected abstract TemplateToolState getNextState();
}
