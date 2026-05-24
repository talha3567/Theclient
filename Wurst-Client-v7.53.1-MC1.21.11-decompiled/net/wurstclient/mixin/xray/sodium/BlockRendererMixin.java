package net.wurstclient.mixin.xray.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.XRayHack;
import net.wurstclient.mixin.xray.sodium.AbstractBlockRenderContextMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets={"net/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer"}, remap=false)
public class BlockRendererMixin
extends AbstractBlockRenderContextMixin {
    @ModifyExpressionValue(method={"bufferQuad(Lnet/caffeinemc/mods/sodium/client/render/model/MutableQuadViewImpl;[FLnet/caffeinemc/mods/sodium/client/render/chunk/terrain/material/Material;)V"}, at={@At(value="INVOKE", target="Lnet/caffeinemc/mods/sodium/client/render/model/MutableQuadViewImpl;baseColor(I)I")}, require=0)
    private int onBufferQuad(int original) {
        XRayHack xray = WurstClient.INSTANCE.getHax().xRayHack;
        if (!xray.isOpacityMode() || xray.isVisible(this.state.method_26204(), this.pos)) {
            return original;
        }
        return original & xray.getOpacityColorMask();
    }
}
