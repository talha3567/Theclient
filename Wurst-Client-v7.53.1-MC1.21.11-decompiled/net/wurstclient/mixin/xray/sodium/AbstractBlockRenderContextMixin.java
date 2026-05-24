package net.wurstclient.mixin.xray.sodium;

import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.XRayHack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets={"net/caffeinemc/mods/sodium/client/render/model/AbstractBlockRenderContext"})
public class AbstractBlockRenderContextMixin {
    @Shadow
    protected class_2680 state;
    @Shadow
    protected class_2338 pos;

    @Inject(method={"isFaceCulled(Lnet/minecraft/class_2350;)Z"}, at={@At(value="HEAD")}, cancellable=true, remap=false, require=0)
    private void onIsFaceCulled(@Nullable class_2350 face, CallbackInfoReturnable<Boolean> cir) {
        XRayHack xray = WurstClient.INSTANCE.getHax().xRayHack;
        Boolean shouldDrawSide = xray.shouldDrawSide(this.state, this.pos);
        if (shouldDrawSide != null) {
            cir.setReturnValue((Object)(shouldDrawSide == false ? 1 : 0));
        }
    }
}
