package net.wurstclient.hacks.newchunks;

import java.util.function.Consumer;
import net.minecraft.class_1921;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.BufferWithLayer;
import net.wurstclient.util.RenderUtils;

public final class NewChunksRenderer {
    private final BufferWithLayer[] vertexBuffers = new BufferWithLayer[4];
    private final SliderSetting altitude;
    private final SliderSetting opacity;
    private final ColorSetting newChunksColor;
    private final ColorSetting oldChunksColor;

    public NewChunksRenderer(SliderSetting altitude, SliderSetting opacity, ColorSetting newChunksColor, ColorSetting oldChunksColor) {
        this.altitude = altitude;
        this.opacity = opacity;
        this.newChunksColor = newChunksColor;
        this.oldChunksColor = oldChunksColor;
    }

    public void updateBuffer(int i, class_1921 layer, Consumer<class_4588> callback) {
        this.vertexBuffers[i] = BufferWithLayer.createAndUpload(layer, callback);
    }

    public void closeBuffers() {
        for (int i = 0; i < this.vertexBuffers.length; ++i) {
            if (this.vertexBuffers[i] == null) continue;
            this.vertexBuffers[i].close();
            this.vertexBuffers[i] = null;
        }
    }

    public void render(class_4587 matrixStack, float partialTicks) {
        matrixStack.method_22903();
        RenderUtils.applyRegionalRenderOffset(matrixStack);
        float alpha = this.opacity.getValueF();
        double altitudeD = this.altitude.getValue();
        for (int i = 0; i < this.vertexBuffers.length; ++i) {
            BufferWithLayer buffer = this.vertexBuffers[i];
            if (buffer == null) continue;
            matrixStack.method_22903();
            if (i == 0 || i == 2) {
                matrixStack.method_22904(0.0, altitudeD, 0.0);
            }
            float[] rgb = i < 2 ? this.newChunksColor.getColorF() : this.oldChunksColor.getColorF();
            buffer.draw(matrixStack, rgb, alpha);
            matrixStack.method_22909();
        }
        matrixStack.method_22909();
    }
}
