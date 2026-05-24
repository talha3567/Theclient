package net.wurstclient.hacks.templatetool.states;

import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_4587;
import net.wurstclient.hacks.TemplateToolHack;
import net.wurstclient.hacks.templatetool.SelectPositionState;
import net.wurstclient.hacks.templatetool.TemplateToolState;
import net.wurstclient.hacks.templatetool.states.SelectBoxEndState;
import net.wurstclient.util.RenderUtils;

public final class SelectBoxStartState
extends SelectPositionState {
    @Override
    public void onRender(TemplateToolHack hack, class_4587 matrixStack, float partialTicks) {
        super.onRender(hack, matrixStack, partialTicks);
        class_2338 start = hack.getStartPos();
        if (start == null) {
            return;
        }
        class_238 box = new class_238(start).method_1011(0.0625);
        int black = Integer.MIN_VALUE;
        int green15 = 637599488;
        RenderUtils.drawOutlinedBox(matrixStack, box, black, false);
        RenderUtils.drawSolidBox(matrixStack, box, green15, false);
    }

    @Override
    protected String getDefaultMessage() {
        return "Select start position.";
    }

    @Override
    protected class_2338 getSelectedPos(TemplateToolHack hack) {
        return hack.getStartPos();
    }

    @Override
    protected void setSelectedPos(TemplateToolHack hack, class_2338 pos) {
        hack.setStartPos(pos);
    }

    @Override
    protected TemplateToolState getNextState() {
        return new SelectBoxEndState();
    }
}
