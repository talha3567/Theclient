package net.wurstclient.util;

import java.util.Comparator;
import java.util.function.Function;
import net.minecraft.class_1268;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2846;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_634;
import net.wurstclient.WurstClient;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RotationUtils;

public final class BlockBreaker
extends Enum<BlockBreaker> {
    private static final WurstClient WURST;
    private static final class_310 MC;
    private static final /* synthetic */ BlockBreaker[] $VALUES;

    public static BlockBreaker[] values() {
        return (BlockBreaker[])$VALUES.clone();
    }

    public static BlockBreaker valueOf(String name) {
        return Enum.valueOf(BlockBreaker.class, name);
    }

    public static boolean breakOneBlock(class_2338 pos) {
        BlockBreakingParams params = BlockBreaker.getBlockBreakingParams(pos);
        if (params == null) {
            return false;
        }
        return BlockBreaker.breakOneBlock(params);
    }

    public static boolean breakOneBlock(BlockBreakingParams params) {
        WURST.getRotationFaker().faceVectorPacket(params.hitVec);
        if (!BlockBreaker.MC.field_1761.method_2902(params.pos, params.side)) {
            return false;
        }
        SwingHandSetting.SwingHand.SERVER.swing(class_1268.field_5808);
        return true;
    }

    public static BlockBreakingParams getBlockBreakingParams(class_2338 pos) {
        return BlockBreaker.getBlockBreakingParams(RotationUtils.getEyesPos(), pos);
    }

    public static BlockBreakingParams getBlockBreakingParams(class_243 eyes, class_2338 pos) {
        class_2350[] sides = class_2350.values();
        class_2680 state = BlockUtils.getState(pos);
        class_265 shape = state.method_26218((class_1922)BlockBreaker.MC.field_1687, pos);
        if (shape.method_1110()) {
            return null;
        }
        class_238 box = shape.method_1107();
        class_243 halfSize = new class_243(box.field_1320 - box.field_1323, box.field_1325 - box.field_1322, box.field_1324 - box.field_1321).method_1021(0.5);
        class_243 center = class_243.method_24954((class_2382)pos).method_1019(box.method_1005());
        class_243[] hitVecs = new class_243[sides.length];
        for (int i = 0; i < sides.length; ++i) {
            class_2382 dirVec = sides[i].method_62675();
            class_243 relHitVec = new class_243(halfSize.field_1352 * (double)dirVec.method_10263(), halfSize.field_1351 * (double)dirVec.method_10264(), halfSize.field_1350 * (double)dirVec.method_10260());
            hitVecs[i] = center.method_1019(relHitVec);
        }
        double distanceSqToCenter = eyes.method_1025(center);
        double[] distancesSq = new double[sides.length];
        boolean[] linesOfSight = new boolean[sides.length];
        for (int i = 0; i < sides.length; ++i) {
            distancesSq[i] = eyes.method_1025(hitVecs[i]);
            if (distancesSq[i] >= distanceSqToCenter) continue;
            linesOfSight[i] = BlockUtils.hasLineOfSight(eyes, hitVecs[i]);
        }
        class_2350 side = sides[0];
        for (int i = 1; i < sides.length; ++i) {
            int bestSide = side.ordinal();
            if (!linesOfSight[bestSide] && linesOfSight[i]) {
                side = sides[i];
                continue;
            }
            if (linesOfSight[bestSide] && !linesOfSight[i] || !(distancesSq[i] < distancesSq[bestSide])) continue;
            side = sides[i];
        }
        return new BlockBreakingParams(pos, side, hitVecs[side.ordinal()], distancesSq[side.ordinal()], linesOfSight[side.ordinal()]);
    }

    public static Comparator<BlockBreakingParams> comparingParams() {
        return Comparator.comparing(BlockBreakingParams::lineOfSight).reversed().thenComparing(params -> params.distanceSq);
    }

    public static <T> Comparator<T> comparingParams(Function<T, BlockBreakingParams> keyExtractor) {
        return Comparator.comparing(keyExtractor, BlockBreaker.comparingParams());
    }

    public static void breakBlocksWithPacketSpam(Iterable<class_2338> blocks) {
        class_243 eyesPos = RotationUtils.getEyesPos();
        class_634 netHandler = BlockBreaker.MC.field_1724.field_3944;
        block0: for (class_2338 pos : blocks) {
            class_243 posVec = class_243.method_24953((class_2382)pos);
            double distanceSqPosVec = eyesPos.method_1025(posVec);
            for (class_2350 side : class_2350.values()) {
                class_243 hitVec = posVec.method_1019(class_243.method_24954((class_2382)side.method_62675()).method_1021(0.5));
                if (eyesPos.method_1025(hitVec) >= distanceSqPosVec) continue;
                netHandler.method_52787((class_2596)new class_2846(class_2846.class_2847.field_12968, pos, side));
                netHandler.method_52787((class_2596)new class_2846(class_2846.class_2847.field_12973, pos, side));
                continue block0;
            }
        }
    }

    private static /* synthetic */ BlockBreaker[] $values() {
        return new BlockBreaker[0];
    }

    static {
        $VALUES = BlockBreaker.$values();
        WURST = WurstClient.INSTANCE;
        MC = WurstClient.MC;
    }

    public record BlockBreakingParams(class_2338 pos, class_2350 side, class_243 hitVec, double distanceSq, boolean lineOfSight) {
        public class_3965 toHitResult() {
            return new class_3965(this.hitVec, this.side, this.pos, false);
        }
    }
}
