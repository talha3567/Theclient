package meteordevelopment.meteorclient.mixin;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import com.mojang.blaze3d.systems.RenderPassBackend;
import meteordevelopment.meteorclient.mixininterface.IGpuDevice;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={GpuDevice.class})
public abstract class GpuDeviceMixin
implements IGpuDevice {
    @Shadow
    @Final
    private GpuDeviceBackend backend;

    @Override
    public void meteor$pushScissor(int x, int y, int width, int height) {
        ((IGpuDevice)this.backend).meteor$pushScissor(x, y, width, height);
    }

    @Override
    public void meteor$popScissor() {
        ((IGpuDevice)this.backend).meteor$popScissor();
    }

    @Override
    public void meteor$onCreateRenderPass(RenderPassBackend backend) {
        ((IGpuDevice)this.backend).meteor$onCreateRenderPass(backend);
    }
}
