package net.wurstclient.util.chunk;

import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.class_1923;
import net.minecraft.class_2586;
import net.minecraft.class_2596;
import net.minecraft.class_2626;
import net.minecraft.class_2637;
import net.minecraft.class_2672;
import net.minecraft.class_2791;
import net.minecraft.class_2818;
import net.minecraft.class_310;
import net.minecraft.class_4076;
import net.wurstclient.WurstClient;

public final class ChunkUtils
extends Enum<ChunkUtils> {
    private static final class_310 MC;
    private static final /* synthetic */ ChunkUtils[] $VALUES;

    public static ChunkUtils[] values() {
        return (ChunkUtils[])$VALUES.clone();
    }

    public static ChunkUtils valueOf(String name) {
        return Enum.valueOf(ChunkUtils.class, name);
    }

    public static Stream<class_2586> getLoadedBlockEntities() {
        return ChunkUtils.getLoadedChunks().flatMap(chunk -> chunk.method_12214().values().stream());
    }

    public static int getManhattanDistance(class_1923 a, class_1923 b) {
        return Math.abs(a.field_9181 - b.field_9181) + Math.abs(a.field_9180 - b.field_9180);
    }

    public static class_1923 getAffectedChunk(class_2596<?> packet) {
        if (packet instanceof class_2626) {
            class_2626 p = (class_2626)packet;
            return new class_1923(p.method_11309());
        }
        if (packet instanceof class_2637) {
            class_2637 p = (class_2637)packet;
            return p.field_26345.method_18692();
        }
        if (packet instanceof class_2672) {
            class_2672 p = (class_2672)packet;
            return new class_1923(p.method_11523(), p.method_11524());
        }
        return null;
    }

    public static Stream<class_2818> getLoadedChunks() {
        int radius = Math.max(2, ChunkUtils.MC.field_1690.method_38521()) + 3;
        int diameter = radius * 2 + 1;
        class_1923 center = ChunkUtils.MC.field_1724.method_31476();
        class_1923 min = new class_1923(center.field_9181 - radius, center.field_9180 - radius);
        class_1923 max = new class_1923(center.field_9181 + radius, center.field_9180 + radius);
        Stream<class_2818> stream = Stream.iterate(min, pos -> {
            int x = pos.field_9181;
            int z = pos.field_9180;
            if (++x > max.field_9181) {
                x = min.field_9181;
                ++z;
            }
            if (z > max.field_9180) {
                throw new IllegalStateException("Stream limit didn't work.");
            }
            return new class_1923(x, z);
        }).limit(diameter * diameter).filter(c -> ChunkUtils.MC.field_1687.method_8393(c.field_9181, c.field_9180)).map(c -> ChunkUtils.MC.field_1687.method_8497(c.field_9181, c.field_9180)).filter(Objects::nonNull);
        return stream;
    }

    public static int getHighestNonEmptySectionYOffset(class_2791 chunk) {
        int i = chunk.method_12040();
        if (i == -1) {
            return chunk.method_31607();
        }
        return class_4076.method_18688((int)chunk.method_31604(i));
    }

    private static /* synthetic */ ChunkUtils[] $values() {
        return new ChunkUtils[0];
    }

    static {
        $VALUES = ChunkUtils.$values();
        MC = WurstClient.MC;
    }
}
