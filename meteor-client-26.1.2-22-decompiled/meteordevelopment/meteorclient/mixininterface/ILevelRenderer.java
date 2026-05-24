package meteordevelopment.meteorclient.mixininterface;

import com.mojang.blaze3d.pipeline.RenderTarget;

public interface ILevelRenderer {
    public void meteor$pushEntityOutlineFramebuffer(RenderTarget var1);

    public void meteor$popEntityOutlineFramebuffer();
}
