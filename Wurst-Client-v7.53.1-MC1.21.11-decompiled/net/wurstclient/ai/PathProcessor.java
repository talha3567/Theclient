package net.wurstclient.ai;

import java.util.ArrayList;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.wurstclient.WurstClient;
import net.wurstclient.ai.PathPos;
import net.wurstclient.mixinterface.IKeyMapping;

public abstract class PathProcessor {
    protected static final WurstClient WURST = WurstClient.INSTANCE;
    protected static final class_310 MC = WurstClient.MC;
    private static final class_304[] CONTROLS = new class_304[]{PathProcessor.MC.field_1690.field_1894, PathProcessor.MC.field_1690.field_1881, PathProcessor.MC.field_1690.field_1849, PathProcessor.MC.field_1690.field_1913, PathProcessor.MC.field_1690.field_1903, PathProcessor.MC.field_1690.field_1832};
    protected final ArrayList<PathPos> path;
    protected int index;
    protected boolean done;
    protected int ticksOffPath;

    public PathProcessor(ArrayList<PathPos> path) {
        if (path.isEmpty()) {
            throw new IllegalStateException("There is no path!");
        }
        this.path = path;
    }

    public abstract void process();

    public abstract boolean canBreakBlocks();

    public final int getIndex() {
        return this.index;
    }

    public final boolean isDone() {
        return this.done;
    }

    public final int getTicksOffPath() {
        return this.ticksOffPath;
    }

    protected final void facePosition(class_2338 pos) {
        WURST.getRotationFaker().faceVectorClientIgnorePitch(class_243.method_24953((class_2382)pos));
    }

    public static final void lockControls() {
        for (class_304 key : CONTROLS) {
            key.method_23481(false);
        }
        PathProcessor.MC.field_1724.method_5728(false);
    }

    public static final void releaseControls() {
        for (class_304 key : CONTROLS) {
            IKeyMapping.get(key).resetPressedState();
        }
    }
}
