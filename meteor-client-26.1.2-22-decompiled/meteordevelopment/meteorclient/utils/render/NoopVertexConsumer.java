package meteordevelopment.meteorclient.utils.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class NoopVertexConsumer
implements VertexConsumer {
    public static final NoopVertexConsumer INSTANCE = new NoopVertexConsumer();

    private NoopVertexConsumer() {
    }

    public VertexConsumer addVertex(float x, float y, float z) {
        return this;
    }

    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return this;
    }

    public VertexConsumer setColor(int argb) {
        return this;
    }

    public VertexConsumer setUv(float u, float v) {
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
