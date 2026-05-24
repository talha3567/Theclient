package net.wurstclient.mixin.xray.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_1920;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_3610;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.XRayHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets={"net/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/DefaultFluidRenderer"})
public class DefaultFluidRendererMixin {
    @Inject(method={"isFullBlockFluidOccluded(Lnet/minecraft/class_1920;Lnet/minecraft/class_2338;Lnet/minecraft/class_2350;Lnet/minecraft/class_2680;Lnet/minecraft/class_3610;)Z"}, at={@At(value="HEAD")}, cancellable=true, remap=false, require=0)
    private void onIsFullBlockFluidOccluded(class_1920 world, class_2338 pos, class_2350 dir, class_2680 state, class_3610 fluid, CallbackInfoReturnable<Boolean> cir) {
        XRayHack xray = WurstClient.INSTANCE.getHax().xRayHack;
        Boolean shouldDrawSide = xray.shouldDrawSide(state, null);
        if (shouldDrawSide != null) {
            cir.setReturnValue((Object)(shouldDrawSide == false ? 1 : 0));
        }
    }

    @Inject(method={"isSideExposed(Lnet/minecraft/class_1920;IIILnet/minecraft/class_2350;F)Z"}, at={@At(value="HEAD")}, cancellable=true, remap=false, require=0)
    private void onIsSideExposed(class_1920 world, int x, int y, int z, class_2350 dir, float height, CallbackInfoReturnable<Boolean> cir) {
        XRayHack xray = WurstClient.INSTANCE.getHax().xRayHack;
        class_2338 pos = new class_2338(x, y, z);
        class_2680 state = world.method_8320(pos);
        Boolean shouldDrawSide = xray.shouldDrawSide(state, null);
        if (shouldDrawSide == null) {
            return;
        }
        class_2338 nPos = pos.method_10081(dir.method_62675());
        class_2680 neighborState = world.method_8320(nPos);
        cir.setReturnValue((Object)(!neighborState.method_26227().method_15772().method_15780(state.method_26227().method_15772()) && shouldDrawSide != false ? 1 : 0));
    }

    @Inject(method={"isFullBlockFluidSideVisible(Lnet/minecraft/class_1922;Lnet/minecraft/class_2338;Lnet/minecraft/class_2350;Lnet/minecraft/class_3610;)Z"}, at={@At(value="HEAD")}, cancellable=true, remap=false, require=0)
    private void onIsFullBlockFluidSideVisible(class_1922 world, class_2338 pos, class_2350 dir, class_3610 fluid, CallbackInfoReturnable<Boolean> cir) {
        XRayHack xray = WurstClient.INSTANCE.getHax().xRayHack;
        class_2680 state = fluid.method_15759();
        Boolean shouldDrawSide = xray.shouldDrawSide(state, null);
        if (shouldDrawSide == null) {
            return;
        }
        class_2338 nPos = pos.method_10081(dir.method_62675());
        class_2680 neighborState = world.method_8320(nPos);
        cir.setReturnValue((Object)(!neighborState.method_26227().method_15772().method_15780(fluid.method_15772()) && shouldDrawSide != false ? 1 : 0));
    }

    @Inject(method={"isFluidSideExposed(Lnet/minecraft/class_1920;Lnet/minecraft/class_2680;Lnet/minecraft/class_2338;Lnet/minecraft/class_2350;F)Z"}, at={@At(value="HEAD")}, cancellable=true, remap=false, require=0)
    private void onIsFluidSideExposed(class_1920 world, class_2680 state, class_2338 neighborPos, class_2350 dir, float height, CallbackInfoReturnable<Boolean> cir) {
        XRayHack xray = WurstClient.INSTANCE.getHax().xRayHack;
        Boolean shouldDrawSide = xray.shouldDrawSide(state, null);
        if (shouldDrawSide == null) {
            return;
        }
        class_2680 neighborState = world.method_8320(neighborPos);
        cir.setReturnValue((Object)(!neighborState.method_26227().method_15772().method_15780(state.method_26227().method_15772()) && shouldDrawSide != false ? 1 : 0));
    }

    @Inject(method={"getUpFaceExposureByNeighbors(Lnet/minecraft/class_1920;Lnet/minecraft/class_2338;Lnet/minecraft/class_3610;)I"}, at={@At(value="HEAD")}, cancellable=true, remap=false, require=0)
    private void onGetUpFaceExposureByNeighbors(class_1920 level, class_2338 pos, class_3610 fluidState, CallbackInfoReturnable<Integer> cir) {
        XRayHack xray = WurstClient.INSTANCE.getHax().xRayHack;
        Boolean shouldDrawSide = xray.shouldDrawSide(fluidState.method_15759(), null);
        if (shouldDrawSide != null) {
            cir.setReturnValue((Object)(shouldDrawSide != false ? 3 : 0));
        }
    }

    @ModifyExpressionValue(method={"updateQuad"}, at={@At(value="INVOKE", target="Lnet/caffeinemc/mods/sodium/api/util/ColorARGB;toABGR(I)I")}, remap=false, require=0)
    private int onUpdateQuad(int original, @Local(argsOnly=true) class_2338 pos, @Local(argsOnly=true) class_3610 state) {
        XRayHack xray = WurstClient.INSTANCE.getHax().xRayHack;
        if (!xray.isOpacityMode() || xray.isVisible(state.method_15759().method_26204(), pos)) {
            return original;
        }
        return original & xray.getOpacityColorMask();
    }
}
