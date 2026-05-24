package net.wurstclient.util;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.class_12246;
import net.minecraft.class_12250;
import net.minecraft.class_1921;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_9801;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public final class EasyVertexBuffer
implements AutoCloseable {
    private final RenderSystem.class_5590 shapeIndexBuffer;
    private final GpuBuffer vertexBuffer;
    private final int indexCount;

    public static EasyVertexBuffer createAndUpload(VertexFormat.class_5596 drawMode, VertexFormat format, Consumer<class_4588> callback) {
        class_287 bufferBuilder = class_289.method_1348().method_60827(drawMode, format);
        callback.accept((class_4588)bufferBuilder);
        try (class_9801 buffer = bufferBuilder.method_60794();){
            if (buffer == null) {
                EasyVertexBuffer easyVertexBuffer = new EasyVertexBuffer(drawMode);
                return easyVertexBuffer;
            }
            EasyVertexBuffer easyVertexBuffer = new EasyVertexBuffer(buffer, drawMode);
            return easyVertexBuffer;
        }
    }

    private EasyVertexBuffer(class_9801 buffer, VertexFormat.class_5596 drawMode) {
        class_9801.class_4574 drawParams = buffer.method_60822();
        this.shapeIndexBuffer = RenderSystem.getSequentialBuffer((VertexFormat.class_5596)drawParams.comp_752());
        this.indexCount = drawParams.comp_751();
        this.vertexBuffer = RenderSystem.getDevice().createBuffer(null, 40, buffer.method_60818());
    }

    private EasyVertexBuffer(VertexFormat.class_5596 drawMode) {
        this.shapeIndexBuffer = null;
        this.indexCount = 0;
        this.vertexBuffer = null;
    }

    public void draw(class_4587 matrixStack, class_1921 layer) {
        this.draw(matrixStack, layer, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void draw(class_4587 matrixStack, class_1921 layer, int argb) {
        float alpha = (float)(argb >> 24 & 0xFF) / 255.0f;
        float red = (float)(argb >> 16 & 0xFF) / 255.0f;
        float green = (float)(argb >> 8 & 0xFF) / 255.0f;
        float blue = (float)(argb & 0xFF) / 255.0f;
        this.draw(matrixStack, layer, red, green, blue, alpha);
    }

    public void draw(class_4587 matrixStack, class_1921 layer, float[] rgba) {
        this.draw(matrixStack, layer, rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    public void draw(class_4587 matrixStack, class_1921 layer, float[] rgb, float alpha) {
        this.draw(matrixStack, layer, rgb[0], rgb[1], rgb[2], alpha);
    }

    public void draw(class_4587 matrixStack, class_1921 layer, float red, float green, float blue, float alpha) {
        if (this.vertexBuffer == null) {
            return;
        }
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.mul((Matrix4fc)matrixStack.method_23760().method_23761());
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)new Vector4f(red, green, blue, alpha), (Vector3fc)new Vector3f(), (Matrix4fc)class_12250.field_64068.method_76030());
        class_276 framebuffer = class_12246.field_63983.method_75921();
        RenderPipeline pipeline = layer.method_73243();
        GpuBuffer indexBuffer = this.shapeIndexBuffer.method_68274(this.indexCount);
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "something from Wurst", framebuffer.method_71639(), OptionalInt.empty(), framebuffer.method_71640(), OptionalDouble.empty());){
            renderPass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms((RenderPass)renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setVertexBuffer(0, this.vertexBuffer);
            renderPass.setIndexBuffer(indexBuffer, this.shapeIndexBuffer.method_31924());
            renderPass.drawIndexed(0, 0, this.indexCount, 1);
        }
        modelViewStack.popMatrix();
    }

    @Override
    public void close() {
        if (this.vertexBuffer != null) {
            this.vertexBuffer.close();
        }
    }
}
