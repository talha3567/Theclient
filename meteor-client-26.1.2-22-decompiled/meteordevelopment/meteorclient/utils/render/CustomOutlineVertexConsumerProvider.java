package meteordevelopment.meteorclient.utils.render;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import meteordevelopment.meteorclient.utils.render.NoopVertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

public class CustomOutlineVertexConsumerProvider
implements MultiBufferSource {
    private final MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate((ByteBufferBuilder)new ByteBufferBuilder(1536));

    public VertexConsumer getBuffer(RenderType layer) {
        if (layer.isOutline()) {
            return new CustomVertexConsumer(this.immediate.getBuffer(layer));
        }
        Optional optional = layer.outline();
        if (optional.isPresent()) {
            return new CustomVertexConsumer(this.immediate.getBuffer((RenderType)optional.get()));
        }
        return NoopVertexConsumer.INSTANCE;
    }

    public void draw() {
        this.immediate.endBatch();
    }

    private record CustomVertexConsumer(VertexConsumer consumer) implements VertexConsumer
    {
        public VertexConsumer addVertex(float x, float y, float z) {
            this.consumer.addVertex(x, y, z);
            return this;
        }

        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            this.consumer.setColor(red, green, blue, alpha);
            return this;
        }

        public VertexConsumer setColor(int argb) {
            this.consumer.setColor(argb);
            return this;
        }

        public VertexConsumer setUv(float u, float v) {
            this.consumer.setUv(u, v);
            return this;
        }

        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        public VertexConsumer setNormal(float x, float y, float z) {
            return this;
        }

        public VertexConsumer setLineWidth(float width) {
            return this;
        }
    }
}
