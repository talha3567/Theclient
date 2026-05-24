package net.wurstclient.hacks.newchunks;

import java.util.Set;
import net.minecraft.class_1921;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_4588;
import net.wurstclient.WurstRenderLayers;
import net.wurstclient.hacks.newchunks.NewChunksChunkRenderer;
import net.wurstclient.util.RegionPos;
import net.wurstclient.util.RenderUtils;

public final class NewChunksSquareRenderer
implements NewChunksChunkRenderer {
    @Override
    public void buildBuffer(class_4588 buffer, Set<class_1923> chunks, int drawDistance) {
        class_1923 camChunkPos = new class_1923(RenderUtils.getCameraBlockPos());
        RegionPos region = RegionPos.of(camChunkPos);
        for (class_1923 chunkPos : chunks) {
            if (chunkPos.method_24022(camChunkPos) > drawDistance) continue;
            class_2338 blockPos = chunkPos.method_35231(-region.x(), 0, -region.z());
            float x1 = (float)blockPos.method_10263() + 0.5f;
            float x2 = x1 + 15.0f;
            float z1 = (float)blockPos.method_10260() + 0.5f;
            float z2 = z1 + 15.0f;
            int color = -1;
            buffer.method_22912(x1, 0.0f, z1).method_39415(color);
            buffer.method_22912(x2, 0.0f, z1).method_39415(color);
            buffer.method_22912(x2, 0.0f, z2).method_39415(color);
            buffer.method_22912(x1, 0.0f, z2).method_39415(color);
        }
    }

    @Override
    public class_1921 getLayer() {
        return WurstRenderLayers.ESP_QUADS_NO_CULLING;
    }
}
