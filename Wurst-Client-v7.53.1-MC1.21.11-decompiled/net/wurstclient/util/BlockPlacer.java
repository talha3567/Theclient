package net.wurstclient.util;

import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.wurstclient.WurstClient;
import net.wurstclient.mixinterface.IMinecraftClient;
import net.wurstclient.util.BlockBreaker;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RotationUtils;

public final class BlockPlacer
extends Enum<BlockPlacer> {
    private static final WurstClient WURST;
    private static final class_310 MC;
    private static final IMinecraftClient IMC;
    private static final /* synthetic */ BlockPlacer[] $VALUES;

    public static BlockPlacer[] values() {
        return (BlockPlacer[])$VALUES.clone();
    }

    public static BlockPlacer valueOf(String name) {
        return Enum.valueOf(BlockPlacer.class, name);
    }

    public static boolean placeOneBlock(class_2338 pos) {
        BlockPlacingParams params = BlockPlacer.getBlockPlacingParams(pos);
        if (params == null || params.requiresSneaking()) {
            return false;
        }
        WURST.getRotationFaker().faceVectorPacket(params.hitVec);
        IMC.getInteractionManager().rightClickBlock(params.neighbor, params.side, params.hitVec);
        return true;
    }

    public static BlockPlacingParams getBlockPlacingParams(class_2338 pos) {
        if (BlockUtils.canBeClicked(pos) && BlockUtils.getState(pos).method_45474()) {
            BlockBreaker.BlockBreakingParams breakParams = BlockBreaker.getBlockBreakingParams(pos);
            boolean requiresSneaking = BlockUtils.isInteractive(BlockUtils.getState(pos));
            if (breakParams == null) {
                return null;
            }
            return new BlockPlacingParams(pos, breakParams.side(), breakParams.hitVec(), breakParams.distanceSq(), breakParams.lineOfSight(), requiresSneaking);
        }
        class_2350[] sides = class_2350.values();
        class_243[] hitVecs = new class_243[sides.length];
        for (int i = 0; i < sides.length; ++i) {
            class_2338 neighbor = pos.method_10093(sides[i]);
            class_2680 state = BlockUtils.getState(neighbor);
            class_265 shape = state.method_26218((class_1922)BlockPlacer.MC.field_1687, neighbor);
            if (shape.method_1110() || state.method_45474()) continue;
            class_238 box = shape.method_1107();
            class_243 halfSize = new class_243(box.field_1320 - box.field_1323, box.field_1325 - box.field_1322, box.field_1324 - box.field_1321).method_1021(0.5);
            class_243 center = class_243.method_24954((class_2382)neighbor).method_1019(box.method_1005());
            class_2382 dirVec = sides[i].method_10153().method_62675();
            class_243 relHitVec = new class_243(halfSize.field_1352 * (double)dirVec.method_10263(), halfSize.field_1351 * (double)dirVec.method_10264(), halfSize.field_1350 * (double)dirVec.method_10260());
            hitVecs[i] = center.method_1019(relHitVec);
        }
        class_243 eyesPos = RotationUtils.getEyesPos();
        class_243 posVec = class_243.method_24953((class_2382)pos);
        double distanceSqToPosVec = eyesPos.method_1025(posVec);
        double[] distancesSq = new double[sides.length];
        boolean[] linesOfSight = new boolean[sides.length];
        boolean[] interactive = new boolean[sides.length];
        for (int i = 0; i < sides.length; ++i) {
            if (hitVecs[i] == null) {
                distancesSq[i] = Double.MAX_VALUE;
                continue;
            }
            distancesSq[i] = eyesPos.method_1025(hitVecs[i]);
            class_2338 neighbor = pos.method_10093(sides[i]);
            interactive[i] = BlockUtils.isInteractive(BlockUtils.getState(neighbor));
            if (distancesSq[i] <= distanceSqToPosVec) continue;
            linesOfSight[i] = BlockUtils.hasLineOfSight(eyesPos, hitVecs[i]);
        }
        class_2350 side = sides[0];
        for (int i = 1; i < sides.length; ++i) {
            int bestSide = side.ordinal();
            if (hitVecs[i] == null) continue;
            if (interactive[bestSide] && !interactive[i]) {
                side = sides[i];
                continue;
            }
            if (!interactive[bestSide] && interactive[i]) continue;
            if (!linesOfSight[bestSide] && linesOfSight[i]) {
                side = sides[i];
                continue;
            }
            if (linesOfSight[bestSide] && !linesOfSight[i] || !(distancesSq[i] > distancesSq[bestSide])) continue;
            side = sides[i];
        }
        if (hitVecs[side.ordinal()] == null) {
            return null;
        }
        return new BlockPlacingParams(pos.method_10093(side), side.method_10153(), hitVecs[side.ordinal()], distancesSq[side.ordinal()], linesOfSight[side.ordinal()], interactive[side.ordinal()]);
    }

    private static /* synthetic */ BlockPlacer[] $values() {
        return new BlockPlacer[0];
    }

    static {
        $VALUES = BlockPlacer.$values();
        WURST = WurstClient.INSTANCE;
        MC = WurstClient.MC;
        IMC = WurstClient.IMC;
    }

    public record BlockPlacingParams(class_2338 neighbor, class_2350 side, class_243 hitVec, double distanceSq, boolean lineOfSight, boolean requiresSneaking) {
        public class_3965 toHitResult() {
            return new class_3965(this.hitVec, this.side, this.neighbor, false);
        }
    }
}
