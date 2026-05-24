package net.wurstclient.util.chunk;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_4588;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.settings.ChunkAreaSetting;
import net.wurstclient.util.EasyVertexBuffer;
import net.wurstclient.util.chunk.AbstractChunkCoordinator;
import net.wurstclient.util.chunk.ChunkSearcher;
import net.wurstclient.util.chunk.ChunkUtils;

public final class ChunkVertexBufferCoordinator
extends AbstractChunkCoordinator {
    private final HashMap<class_1923, EasyVertexBuffer> buffers = new HashMap();
    private final Renderer renderer;
    private final VertexFormat.class_5596 drawMode;
    private final VertexFormat format;

    public ChunkVertexBufferCoordinator(BiPredicate<class_2338, class_2680> query, VertexFormat.class_5596 drawMode, VertexFormat format, Renderer renderer, ChunkAreaSetting area) {
        super(query, area);
        this.renderer = Objects.requireNonNull(renderer);
        this.drawMode = drawMode;
        this.format = format;
    }

    @Override
    public void onReceivedPacket(PacketInputListener.PacketInputEvent event) {
        class_1923 center = ChunkUtils.getAffectedChunk(event.getPacket());
        if (center == null) {
            return;
        }
        for (int x = center.field_9181 - 1; x <= center.field_9181 + 1; ++x) {
            for (int z = center.field_9180 - 1; z <= center.field_9180 + 1; ++z) {
                this.chunksToUpdate.add(new class_1923(x, z));
            }
        }
    }

    @Override
    protected void onRemove(ChunkSearcher searcher) {
        EasyVertexBuffer buffer = this.buffers.remove(searcher.getPos());
        if (buffer != null) {
            buffer.close();
        }
    }

    @Override
    public void reset() {
        super.reset();
        this.buffers.values().forEach(EasyVertexBuffer::close);
        this.buffers.clear();
    }

    public Set<Map.Entry<class_1923, EasyVertexBuffer>> getBuffers() {
        for (ChunkSearcher searcher : this.searchers.values()) {
            this.buildBuffer(searcher);
        }
        return Collections.unmodifiableSet(this.buffers.entrySet());
    }

    private void buildBuffer(ChunkSearcher searcher) {
        if (this.buffers.containsKey(searcher.getPos())) {
            return;
        }
        EasyVertexBuffer vertexBuffer = EasyVertexBuffer.createAndUpload(this.drawMode, this.format, buffer -> this.renderer.buildBuffer((class_4588)buffer, searcher, searcher.getMatchesList()));
        this.buffers.put(searcher.getPos(), vertexBuffer);
    }

    public static interface Renderer {
        public void buildBuffer(class_4588 var1, ChunkSearcher var2, List<ChunkSearcher.Result> var3);
    }
}
