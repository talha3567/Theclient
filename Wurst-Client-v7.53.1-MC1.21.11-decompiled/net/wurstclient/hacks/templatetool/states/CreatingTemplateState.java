package net.wurstclient.hacks.templatetool.states;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.wurstclient.hacks.TemplateToolHack;
import net.wurstclient.hacks.templatetool.TemplateToolState;
import net.wurstclient.hacks.templatetool.states.ChooseNameState;
import net.wurstclient.util.RenderUtils;

public final class CreatingTemplateState
extends TemplateToolState {
    private int totalBlocks;
    private int blocksPerTick;
    private float progress;
    private ArrayDeque<class_2338> unsortedBlocks;
    private TreeSet<class_2338> sortingHelper;

    @Override
    public void onEnter(TemplateToolHack hack) {
        this.totalBlocks = hack.getNonEmptyBlocks().size();
        this.blocksPerTick = class_3532.method_15340((int)(this.totalBlocks / 15), (int)1, (int)1024);
        this.unsortedBlocks = new ArrayDeque<class_2338>(hack.getNonEmptyBlocks().keySet());
        class_2338 origin = hack.getOriginPos();
        this.sortingHelper = new TreeSet<class_2338>(Comparator.comparingDouble(pos -> pos.method_10262((class_2382)origin)).thenComparing(pos -> pos));
    }

    @Override
    public void onUpdate(TemplateToolHack hack) {
        if (!this.unsortedBlocks.isEmpty()) {
            for (int i = 0; i < this.blocksPerTick && !this.unsortedBlocks.isEmpty(); ++i) {
                this.sortingHelper.add(this.unsortedBlocks.removeLast());
            }
            this.progress = (float)this.sortingHelper.size() / (float)this.totalBlocks;
            return;
        }
        LinkedHashSet<class_2338> sortedBlocks = hack.getSortedBlocks();
        if (sortedBlocks.isEmpty() && !this.sortingHelper.isEmpty()) {
            class_2338 first = this.sortingHelper.first();
            sortedBlocks.add(first);
            this.sortingHelper.remove(first);
        }
        for (int i = 0; i < this.blocksPerTick && !this.sortingHelper.isEmpty(); ++i) {
            class_2338 current = this.sortingHelper.first();
            double dCurrent = Double.MAX_VALUE;
            for (class_2338 pos : this.sortingHelper) {
                double dPos = sortedBlocks.getLast().method_10262((class_2382)pos);
                if (dPos >= dCurrent) continue;
                for (class_2350 side : class_2350.values()) {
                    class_2338 next = pos.method_10093(side);
                    if (!sortedBlocks.contains(next)) continue;
                    current = pos;
                    dCurrent = dPos;
                }
            }
            sortedBlocks.add(current);
            this.sortingHelper.remove(current);
        }
        this.progress = (float)this.sortingHelper.size() / (float)this.totalBlocks;
        if (sortedBlocks.size() == this.totalBlocks) {
            hack.setState(new ChooseNameState());
        }
    }

    @Override
    public void onRender(TemplateToolHack hack, class_4587 matrixStack, float partialTicks) {
        int black = Integer.MIN_VALUE;
        int green30 = 1291910912;
        class_2338 start = hack.getStartPos();
        class_2338 end = hack.getEndPos();
        class_238 bounds = class_238.method_54784((class_2338)start, (class_2338)end).method_1011(0.0625);
        double scannerX = class_3532.method_16436((double)this.progress, (double)bounds.field_1323, (double)bounds.field_1320);
        class_238 scanner = bounds.method_35574(scannerX).method_35577(scannerX);
        RenderUtils.drawOutlinedBox(matrixStack, scanner, black, true);
        RenderUtils.drawSolidBox(matrixStack, scanner, green30, true);
        List<class_238> boxes = hack.getSortedBlocks().reversed().stream().map(pos -> new class_238(pos).method_1011(0.0625)).limit(1024L).toList();
        RenderUtils.drawOutlinedBoxes(matrixStack, boxes, black, false);
    }

    @Override
    protected String getMessage(TemplateToolHack hack) {
        return "Creating template...";
    }
}
