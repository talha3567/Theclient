package meteordevelopment.meteorclient.renderer;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import java.nio.ByteBuffer;
import net.minecraft.client.renderer.DynamicUniformStorage;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public class MeshUniforms {
    public static final int SIZE = new Std140SizeCalculator().putMat4f().putMat4f().get();
    private static final Data DATA = new Data();
    private static final DynamicUniformStorage<Data> STORAGE = new DynamicUniformStorage("Meteor - Mesh UBO", SIZE, 16);

    public static void flipFrame() {
        STORAGE.endFrame();
    }

    public static GpuBufferSlice write(Matrix4f proj, Matrix4f modelView) {
        MeshUniforms.DATA.proj = proj;
        MeshUniforms.DATA.modelView = modelView;
        return STORAGE.writeUniform((DynamicUniformStorage.DynamicUniform)DATA);
    }

    private static final class Data
    implements DynamicUniformStorage.DynamicUniform {
        private Matrix4f proj;
        private Matrix4f modelView;

        private Data() {
        }

        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer((ByteBuffer)buffer).putMat4f((Matrix4fc)this.proj).putMat4f((Matrix4fc)this.modelView);
        }

        public boolean equals(Object o) {
            return false;
        }
    }
}
