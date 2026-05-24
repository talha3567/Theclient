package meteordevelopment.meteorclient.renderer.text;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;

public class VanillaTextRenderer
implements TextRenderer {
    public static final VanillaTextRenderer INSTANCE = new VanillaTextRenderer();
    private final ByteBufferBuilder buffer = new ByteBufferBuilder(2048);
    private final MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate((ByteBufferBuilder)this.buffer);
    private final PoseStack matrices = new PoseStack();
    private final Matrix4f emptyMatrix = new Matrix4f();
    public double scale = 2.0;
    public boolean scaleIndividually;
    private boolean building;
    private double alpha = 1.0;

    private VanillaTextRenderer() {
    }

    @Override
    public void setAlpha(double a) {
        this.alpha = a;
    }

    @Override
    public double getWidth(String text, int length, boolean shadow) {
        if (text.isEmpty()) {
            return 0.0;
        }
        if (length != text.length()) {
            text = text.substring(0, length);
        }
        return (double)(MeteorClient.mc.font.width(text) + (shadow ? 1 : 0)) * this.scale;
    }

    @Override
    public double getHeight(boolean shadow) {
        Objects.requireNonNull(MeteorClient.mc.font);
        return (double)(9 + (shadow ? 1 : 0)) * this.scale;
    }

    @Override
    public void begin(double scale, boolean scaleOnly, boolean big) {
        if (this.building) {
            throw new RuntimeException("VanillaTextRenderer.begin() called twice");
        }
        this.scale = scale * 2.0;
        this.building = true;
    }

    @Override
    public double render(String text, double x, double y, Color color, boolean shadow) {
        boolean wasBuilding = this.building;
        if (!wasBuilding) {
            this.begin();
        }
        x += 0.5 * this.scale;
        y += 0.5 * this.scale;
        int preA = color.a;
        color.a = (int)((double)color.a / 255.0 * this.alpha * 255.0);
        Matrix4f matrix = this.emptyMatrix;
        if (this.scaleIndividually) {
            this.matrices.pushPose();
            this.matrices.scale((float)this.scale, (float)this.scale, 1.0f);
            matrix = this.matrices.last().pose();
        }
        MeteorClient.mc.font.drawInBatch(text, (float)(x / this.scale), (float)(y / this.scale), color.getPacked(), shadow, (Matrix4fc)matrix, (MultiBufferSource)this.immediate, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        double x2 = x / this.scale + (double)MeteorClient.mc.font.width(text);
        if (this.scaleIndividually) {
            this.matrices.popPose();
        }
        color.a = preA;
        if (!wasBuilding) {
            this.end();
        }
        return (x2 - 1.0) * this.scale;
    }

    @Override
    public boolean isBuilding() {
        return this.building;
    }

    @Override
    public void end() {
        if (!this.building) {
            throw new RuntimeException("VanillaTextRenderer.end() called without calling begin()");
        }
        Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushMatrix();
        if (!this.scaleIndividually) {
            matrixStack.scale((float)this.scale, (float)this.scale, 1.0f);
        }
        this.immediate.endBatch();
        matrixStack.popMatrix();
        this.scale = 2.0;
        this.building = false;
    }
}
