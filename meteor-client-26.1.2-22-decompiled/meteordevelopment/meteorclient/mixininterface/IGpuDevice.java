package meteordevelopment.meteorclient.mixininterface;

import com.mojang.blaze3d.systems.RenderPassBackend;

public interface IGpuDevice {
    public void meteor$pushScissor(int var1, int var2, int var3, int var4);

    public void meteor$popScissor();

    @Deprecated
    public void meteor$onCreateRenderPass(RenderPassBackend var1);
}
