package net.wurstclient.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_2338;
import net.wurstclient.util.RegionPos;

public final class BlockVertexCompiler
extends Enum<BlockVertexCompiler> {
    private static final /* synthetic */ BlockVertexCompiler[] $VALUES;

    public static BlockVertexCompiler[] values() {
        return (BlockVertexCompiler[])$VALUES.clone();
    }

    public static BlockVertexCompiler valueOf(String name) {
        return Enum.valueOf(BlockVertexCompiler.class, name);
    }

    public static ArrayList<int[]> compile(HashSet<class_2338> blocks) {
        return blocks.parallelStream().flatMap(pos -> BlockVertexCompiler.getVertices(pos, blocks)).collect(Collectors.toCollection(ArrayList::new));
    }

    public static ArrayList<int[]> compile(HashSet<class_2338> blocks, RegionPos region) {
        return blocks.parallelStream().flatMap(pos -> BlockVertexCompiler.getVertices(pos, blocks)).map(v -> BlockVertexCompiler.applyRegionOffset(v, region)).collect(Collectors.toCollection(ArrayList::new));
    }

    private static int[] applyRegionOffset(int[] vertex, RegionPos region) {
        vertex[0] = vertex[0] - region.x();
        vertex[2] = vertex[2] - region.z();
        return vertex;
    }

    private static Stream<int[]> getVertices(class_2338 pos, HashSet<class_2338> matchingBlocks) {
        Stream.Builder<int[]> builder = Stream.builder();
        if (!matchingBlocks.contains(pos.method_10074())) {
            builder.accept(BlockVertexCompiler.getVertex(pos, 0, 0, 0));
            builder.accept(BlockVertexCompiler.getVertex(pos, 1, 0, 0));
            builder.accept(BlockVertexCompiler.getVertex(pos, 1, 0, 1));
            builder.accept(BlockVertexCompiler.getVertex(pos, 0, 0, 1));
        }
        if (!matchingBlocks.contains(pos.method_10084())) {
            builder.accept(BlockVertexCompiler.getVertex(pos, 0, 1, 0));
            builder.accept(BlockVertexCompiler.getVertex(pos, 0, 1, 1));
            builder.accept(BlockVertexCompiler.getVertex(pos, 1, 1, 1));
            builder.accept(BlockVertexCompiler.getVertex(pos, 1, 1, 0));
        }
        if (!matchingBlocks.contains(pos.method_10095())) {
            builder.accept(BlockVertexCompiler.getVertex(pos, 0, 0, 0));
            builder.accept(BlockVertexCompiler.getVertex(pos, 0, 1, 0));
            builder.accept(BlockVertexCompiler.getVertex(pos, 1, 1, 0));
            builder.accept(BlockVertexCompiler.getVertex(pos, 1, 0, 0));
        }
        if (!matchingBlocks.contains(pos.method_10078())) {
            builder.accept(BlockVertexCompiler.getVertex(pos, 1, 0, 0));
            builder.accept(BlockVertexCompiler.getVertex(pos, 1, 1, 0));
            builder.accept(BlockVertexCompiler.getVertex(pos, 1, 1, 1));
            builder.accept(BlockVertexCompiler.getVertex(pos, 1, 0, 1));
        }
        if (!matchingBlocks.contains(pos.method_10072())) {
            builder.accept(BlockVertexCompiler.getVertex(pos, 0, 0, 1));
            builder.accept(BlockVertexCompiler.getVertex(pos, 1, 0, 1));
            builder.accept(BlockVertexCompiler.getVertex(pos, 1, 1, 1));
            builder.accept(BlockVertexCompiler.getVertex(pos, 0, 1, 1));
        }
        if (!matchingBlocks.contains(pos.method_10067())) {
            builder.accept(BlockVertexCompiler.getVertex(pos, 0, 0, 0));
            builder.accept(BlockVertexCompiler.getVertex(pos, 0, 0, 1));
            builder.accept(BlockVertexCompiler.getVertex(pos, 0, 1, 1));
            builder.accept(BlockVertexCompiler.getVertex(pos, 0, 1, 0));
        }
        return builder.build();
    }

    private static int[] getVertex(class_2338 pos, int x, int y, int z) {
        return new int[]{pos.method_10263() + x, pos.method_10264() + y, pos.method_10260() + z};
    }

    private static /* synthetic */ BlockVertexCompiler[] $values() {
        return new BlockVertexCompiler[0];
    }

    static {
        $VALUES = BlockVertexCompiler.$values();
    }
}
