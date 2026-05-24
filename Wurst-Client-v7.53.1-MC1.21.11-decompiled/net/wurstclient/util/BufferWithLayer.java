package net.wurstclient.util;

import java.util.function.Consumer;
import net.minecraft.class_1921;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.wurstclient.util.EasyVertexBuffer;

public record BufferWithLayer(EasyVertexBuffer buffer, class_1921 layer) implements AutoCloseable
{
    public static BufferWithLayer createAndUpload(class_1921 layer, Consumer<class_4588> callback) {
        return new BufferWithLayer(EasyVertexBuffer.createAndUpload(layer.method_23033(), layer.method_23031(), callback), layer);
    }

    public void draw(class_4587 matrixStack) {
        this.buffer.draw(matrixStack, this.layer);
    }

    public void draw(class_4587 matrixStack, float red, float green, float blue, float alpha) {
        this.buffer.draw(matrixStack, this.layer, red, green, blue, alpha);
    }

    public void draw(class_4587 matrixStack, float[] rgba) {
        this.buffer.draw(matrixStack, this.layer, rgba);
    }

    public void draw(class_4587 matrixStack, float[] rgb, float alpha) {
        this.buffer.draw(matrixStack, this.layer, rgb, alpha);
    }

    public void draw(class_4587 matrixStack, int argb) {
        this.buffer.draw(matrixStack, this.layer, argb);
    }

    @Override
    public void close() {
        this.buffer.close();
    }
}
