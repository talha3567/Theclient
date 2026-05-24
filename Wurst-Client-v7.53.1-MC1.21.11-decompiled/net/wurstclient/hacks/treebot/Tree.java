package net.wurstclient.hacks.treebot;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_4587;
import net.wurstclient.util.RenderUtils;

public class Tree {
    private final class_2338 stump;
    private final ArrayList<class_2338> logs;

    public Tree(class_2338 stump, ArrayList<class_2338> logs) {
        this.stump = stump;
        this.logs = logs;
    }

    public void draw(class_4587 matrixStack) {
        int green = -2147418368;
        class_238 box = new class_238(class_2338.field_10980).method_1011(0.0625);
        class_238 stumpBox = box.method_996(this.stump);
        RenderUtils.drawCrossBox(matrixStack, stumpBox, green, false);
        List<class_238> logBoxes = this.logs.stream().map(pos -> box.method_996(pos)).toList();
        RenderUtils.drawOutlinedBoxes(matrixStack, logBoxes, green, false);
    }

    public class_2338 getStump() {
        return this.stump;
    }

    public ArrayList<class_2338> getLogs() {
        return this.logs;
    }
}
