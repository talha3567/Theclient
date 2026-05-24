package meteordevelopment.meteorclient.utils.render.postprocess;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import java.nio.ByteBuffer;
import net.minecraft.client.renderer.DynamicUniformStorage;

public class OutlineUniforms {
    private static final int UNIFORM_SIZE = new Std140SizeCalculator().putInt().putFloat().putInt().putFloat().get();
    private static final DynamicUniformStorage<Data> STORAGE = new DynamicUniformStorage("Meteor - Outline UBO", UNIFORM_SIZE, 16);

    public static void flipFrame() {
        STORAGE.endFrame();
    }

    public static GpuBufferSlice write(int width, float fillOpacity, int shapeMode, float glowMultiplier) {
        return STORAGE.writeUniform((DynamicUniformStorage.DynamicUniform)new Data(width, fillOpacity, shapeMode, glowMultiplier));
    }

    private record Data(int width, float fillOpacity, int shapeMode, float glowMultiplier) implements DynamicUniformStorage.DynamicUniform
    {
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer((ByteBuffer)buffer).putInt(this.width).putFloat(this.fillOpacity).putInt(this.shapeMode).putFloat(this.glowMultiplier);
        }
    }
}
