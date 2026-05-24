package net.wurstclient.hacks.templatetool.states;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2680;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.wurstclient.hacks.TemplateToolHack;
import net.wurstclient.hacks.templatetool.TemplateToolState;
import net.wurstclient.hacks.templatetool.states.SelectOriginState;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RenderUtils;

public final class ScanningAreaState
extends TemplateToolState {
    private int totalBlocks;
    private int blocksPerTick;
    private Iterator<class_2338> iterator;
    private int scannedBlocks;
    private float progress;

    @Override
    public void onEnter(TemplateToolHack hack) {
        class_2338 start = hack.getStartPos();
        class_2338 end = hack.getEndPos();
        int lengthX = Math.abs(start.method_10263() - end.method_10263()) + 1;
        int lengthY = Math.abs(start.method_10264() - end.method_10264()) + 1;
        int lengthZ = Math.abs(start.method_10260() - end.method_10260()) + 1;
        this.totalBlocks = lengthX * lengthY * lengthZ;
        this.blocksPerTick = class_3532.method_15340((int)(this.totalBlocks / 30), (int)1, (int)1024);
        this.iterator = BlockUtils.getAllInBox(start, end).iterator();
    }

    @Override
    public void onUpdate(TemplateToolHack hack) {
        for (int i = 0; i < this.blocksPerTick && this.iterator.hasNext(); ++i) {
            ++this.scannedBlocks;
            class_2338 pos = this.iterator.next();
            class_2680 state = BlockUtils.getState(pos);
            if (state.method_45474()) continue;
            hack.getNonEmptyBlocks().put(pos, state);
        }
        this.progress = (float)this.scannedBlocks / (float)this.totalBlocks;
        if (!this.iterator.hasNext()) {
            hack.setState(new SelectOriginState());
        }
    }

    @Override
    public void onRender(TemplateToolHack hack, class_4587 matrixStack, float partialTicks) {
        int black = Integer.MIN_VALUE;
        int green15 = 637599488;
        int green30 = 1291910912;
        LinkedHashMap<class_2338, class_2680> blocks = hack.getNonEmptyBlocks();
        int offset = Math.max(0, blocks.size() - this.blocksPerTick);
        List<class_238> boxes = blocks.keySet().stream().skip(offset).map(pos -> new class_238(pos).method_1014(0.005)).toList();
        RenderUtils.drawOutlinedBoxes(matrixStack, boxes, black, true);
        RenderUtils.drawSolidBoxes(matrixStack, boxes, green15, true);
        class_2338 start = hack.getStartPos();
        class_2338 end = hack.getEndPos();
        class_238 bounds = class_238.method_54784((class_2338)start, (class_2338)end).method_1011(0.0625);
        double scannerX = class_3532.method_16436((double)this.progress, (double)bounds.field_1323, (double)bounds.field_1320);
        class_238 scanner = bounds.method_35574(scannerX).method_35577(scannerX);
        RenderUtils.drawOutlinedBox(matrixStack, scanner, black, true);
        RenderUtils.drawSolidBox(matrixStack, scanner, green30, true);
    }

    @Override
    protected String getMessage(TemplateToolHack hack) {
        return "Scanning area...";
    }
}
