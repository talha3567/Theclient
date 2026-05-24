package net.wurstclient.mixin.xray.indigo;

import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.XRayHack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets={"net/fabricmc/fabric/impl/client/indigo/renderer/render/AbstractTerrainRenderContext"}, remap=false)
public abstract class AbstractTerrainRenderContextMixin {
    @Shadow
    @Final
    private BlockRenderInfo blockInfo;

    @Inject(method={"shadeQuad"}, at={@At(value="RETURN")}, require=0)
    private void onShadeQuad(MutableQuadViewImpl quad, boolean ao, boolean emissive, boolean vanillaShade, CallbackInfo ci) {
        XRayHack xray = WurstClient.INSTANCE.getHax().xRayHack;
        if (!xray.isOpacityMode() || xray.isVisible(this.blockInfo.blockState.method_26204(), this.blockInfo.blockPos)) {
            return;
        }
        for (int i = 0; i < 4; ++i) {
            quad.color(i, quad.color(i) & xray.getOpacityColorMask());
        }
    }
}
