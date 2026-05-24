package net.wurstclient.util;

import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.class_1297;
import net.minecraft.class_151;
import net.minecraft.class_1657;
import net.minecraft.class_1922;
import net.minecraft.class_2199;
import net.minecraft.class_2238;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2260;
import net.minecraft.class_2288;
import net.minecraft.class_2304;
import net.minecraft.class_2315;
import net.minecraft.class_2331;
import net.minecraft.class_2338;
import net.minecraft.class_2363;
import net.minecraft.class_2377;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_2406;
import net.minecraft.class_243;
import net.minecraft.class_2478;
import net.minecraft.class_2480;
import net.minecraft.class_2515;
import net.minecraft.class_259;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2769;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3708;
import net.minecraft.class_3711;
import net.minecraft.class_3713;
import net.minecraft.class_3715;
import net.minecraft.class_3718;
import net.minecraft.class_3748;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4739;
import net.minecraft.class_7923;
import net.minecraft.class_8886;
import net.wurstclient.WurstClient;
import net.wurstclient.util.MathUtils;
import net.wurstclient.util.RotationUtils;

public final class BlockUtils
extends Enum<BlockUtils> {
    private static final class_310 MC;
    private static final /* synthetic */ BlockUtils[] $VALUES;

    public static BlockUtils[] values() {
        return (BlockUtils[])$VALUES.clone();
    }

    public static BlockUtils valueOf(String name) {
        return Enum.valueOf(BlockUtils.class, name);
    }

    public static class_2680 getState(class_2338 pos) {
        return BlockUtils.MC.field_1687.method_8320(pos);
    }

    public static class_2248 getBlock(class_2338 pos) {
        return BlockUtils.getState(pos).method_26204();
    }

    public static int getId(class_2338 pos) {
        return class_2248.method_9507((class_2680)BlockUtils.getState(pos));
    }

    public static String getName(class_2338 pos) {
        return BlockUtils.getName(BlockUtils.getBlock(pos));
    }

    public static String getName(class_2248 block) {
        return class_7923.field_41175.method_10221((Object)block).toString();
    }

    public static class_2248 getBlockFromName(String name) {
        try {
            return (class_2248)class_7923.field_41175.method_63535(class_2960.method_60654((String)name));
        }
        catch (class_151 e) {
            return class_2246.field_10124;
        }
    }

    public static class_2248 getBlockFromNameOrID(String nameOrId) {
        if (MathUtils.isInteger(nameOrId)) {
            class_2680 state = (class_2680)class_2248.field_10651.method_10200(Integer.parseInt(nameOrId));
            if (state == null) {
                return null;
            }
            return state.method_26204();
        }
        try {
            class_2960 id = class_2960.method_60654((String)nameOrId);
            if (!class_7923.field_41175.method_10250(id)) {
                return null;
            }
            return (class_2248)class_7923.field_41175.method_63535(id);
        }
        catch (class_151 e) {
            return null;
        }
    }

    public static float getHardness(class_2338 pos) {
        return BlockUtils.getState(pos).method_26165((class_1657)BlockUtils.MC.field_1724, (class_1922)BlockUtils.MC.field_1687, pos);
    }

    public static boolean isUnbreakable(class_2338 pos) {
        return BlockUtils.getBlock(pos).method_36555() < 0.0f;
    }

    private static class_265 getOutlineShape(class_2338 pos) {
        return BlockUtils.getState(pos).method_26218((class_1922)BlockUtils.MC.field_1687, pos);
    }

    public static class_238 getBoundingBox(class_2338 pos) {
        return BlockUtils.getOutlineShape(pos).method_1107().method_996(pos);
    }

    public static boolean canBeClicked(class_2338 pos) {
        return BlockUtils.getOutlineShape(pos) != class_259.method_1073();
    }

    public static boolean isOpaqueFullCube(class_2338 pos) {
        return BlockUtils.getState(pos).method_26216();
    }

    public static class_3965 raycast(class_243 from, class_243 to, class_3959.class_242 fluidHandling) {
        class_3959 context = new class_3959(from, to, class_3959.class_3960.field_17558, fluidHandling, (class_1297)BlockUtils.MC.field_1724);
        return BlockUtils.MC.field_1687.method_17742(context);
    }

    public static class_3965 raycast(class_243 from, class_243 to) {
        return BlockUtils.raycast(from, to, class_3959.class_242.field_1348);
    }

    public static boolean hasLineOfSight(class_243 from, class_243 to) {
        return BlockUtils.raycast(from, to).method_17783() == class_239.class_240.field_1333;
    }

    public static boolean hasLineOfSight(class_243 to) {
        return BlockUtils.raycast(RotationUtils.getEyesPos(), to).method_17783() == class_239.class_240.field_1333;
    }

    public static Stream<class_238> getBlockCollisions(class_238 box) {
        Iterable blockCollisions = BlockUtils.MC.field_1687.method_20812((class_1297)BlockUtils.MC.field_1724, box);
        return StreamSupport.stream(blockCollisions.spliterator(), false).flatMap(shape -> shape.method_1090().stream()).filter(shapeBox -> shapeBox.method_994(box));
    }

    public static ArrayList<class_2338> getAllInBox(class_2338 from, class_2338 to) {
        ArrayList<class_2338> blocks = new ArrayList<class_2338>();
        class_2338 min = new class_2338(Math.min(from.method_10263(), to.method_10263()), Math.min(from.method_10264(), to.method_10264()), Math.min(from.method_10260(), to.method_10260()));
        class_2338 max = new class_2338(Math.max(from.method_10263(), to.method_10263()), Math.max(from.method_10264(), to.method_10264()), Math.max(from.method_10260(), to.method_10260()));
        for (int x = min.method_10263(); x <= max.method_10263(); ++x) {
            for (int y = min.method_10264(); y <= max.method_10264(); ++y) {
                for (int z = min.method_10260(); z <= max.method_10260(); ++z) {
                    blocks.add(new class_2338(x, y, z));
                }
            }
        }
        return blocks;
    }

    public static ArrayList<class_2338> getAllInBox(class_2338 center, int range) {
        return BlockUtils.getAllInBox(center.method_10069(-range, -range, -range), center.method_10069(range, range, range));
    }

    public static Stream<class_2338> getAllInBoxStream(class_2338 from, class_2338 to) {
        class_2338 min = new class_2338(Math.min(from.method_10263(), to.method_10263()), Math.min(from.method_10264(), to.method_10264()), Math.min(from.method_10260(), to.method_10260()));
        class_2338 max = new class_2338(Math.max(from.method_10263(), to.method_10263()), Math.max(from.method_10264(), to.method_10264()), Math.max(from.method_10260(), to.method_10260()));
        Stream<class_2338> stream = Stream.iterate(min, pos -> {
            int x = pos.method_10263();
            int y = pos.method_10264();
            int z = pos.method_10260();
            if (++x > max.method_10263()) {
                x = min.method_10263();
                ++y;
            }
            if (y > max.method_10264()) {
                y = min.method_10264();
                ++z;
            }
            if (z > max.method_10260()) {
                throw new IllegalStateException("Stream limit didn't work.");
            }
            return new class_2338(x, y, z);
        });
        int limit = (max.method_10263() - min.method_10263() + 1) * (max.method_10264() - min.method_10264() + 1) * (max.method_10260() - min.method_10260() + 1);
        return stream.limit(limit);
    }

    public static Stream<class_2338> getAllInBoxStream(class_2338 center, int range) {
        return BlockUtils.getAllInBoxStream(center.method_10069(-range, -range, -range), center.method_10069(range, range, range));
    }

    public static boolean isInteractive(class_2680 state) {
        if (state == null) {
            return false;
        }
        class_2248 block = state.method_26204();
        if (block instanceof class_4739 || block instanceof class_3708 || block instanceof class_2480) {
            return true;
        }
        if (block instanceof class_2199 || block instanceof class_3711 || block instanceof class_2304 || block instanceof class_2331 || block instanceof class_3713 || block instanceof class_2406 || block instanceof class_3718) {
            return true;
        }
        if (block instanceof class_2363 || block instanceof class_2260 || block instanceof class_2315 || block instanceof class_2377 || block instanceof class_8886) {
            return true;
        }
        if (block instanceof class_2238) {
            return true;
        }
        if (block instanceof class_2478) {
            return true;
        }
        if (block instanceof class_3715) {
            return (Boolean)state.method_11654((class_2769)class_2741.field_17393);
        }
        if (block instanceof class_2288 || block instanceof class_2515 || block instanceof class_3748) {
            return BlockUtils.MC.field_1724.method_7338();
        }
        return false;
    }

    private static /* synthetic */ BlockUtils[] $values() {
        return new BlockUtils[0];
    }

    static {
        $VALUES = BlockUtils.$values();
        MC = WurstClient.MC;
    }
}
