package net.wurstclient.hacks.templatetool.states;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_4587;
import net.wurstclient.hacks.TemplateToolHack;
import net.wurstclient.hacks.templatetool.SelectPositionState;
import net.wurstclient.hacks.templatetool.TemplateToolState;
import net.wurstclient.hacks.templatetool.states.ScanningAreaState;
import net.wurstclient.util.RenderUtils;

public final class SelectBoxEndState
extends SelectPositionState {
    @Override
    public void onRender(TemplateToolHack hack, class_4587 matrixStack, float partialTicks) {
        super.onRender(hack, matrixStack, partialTicks);
        class_2338 start = hack.getStartPos();
        class_2338 end = hack.getEndPos();
        List<class_238> selections = Stream.of(start, end).filter(Objects::nonNull).map(pos -> new class_238(pos).method_1011(0.0625)).toList();
        int black = Integer.MIN_VALUE;
        int green15 = 637599488;
        RenderUtils.drawOutlinedBoxes(matrixStack, selections, black, false);
        RenderUtils.drawSolidBoxes(matrixStack, selections, green15, false);
    }

    @Override
    protected String getDefaultMessage() {
        return "Select end position.";
    }

    @Override
    protected class_2338 getSelectedPos(TemplateToolHack hack) {
        return hack.getEndPos();
    }

    @Override
    protected void setSelectedPos(TemplateToolHack hack, class_2338 pos) {
        hack.setEndPos(pos);
    }

    @Override
    protected TemplateToolState getNextState() {
        return new ScanningAreaState();
    }
}
