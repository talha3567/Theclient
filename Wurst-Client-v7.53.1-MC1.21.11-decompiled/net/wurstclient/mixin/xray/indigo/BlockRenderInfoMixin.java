package net.wurstclient.mixin.xray.indigo;

import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.XRayHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets={"net/fabricmc/fabric/impl/client/indigo/renderer/render/BlockRenderInfo"}, remap=false)
public abstract class BlockRenderInfoMixin {
    @Shadow
    public class_2338 blockPos;
    @Shadow
    public class_2680 blockState;

    @Inject(method={"shouldDrawSide"}, at={@At(value="HEAD")}, require=0, cancellable=true)
    private void onShouldDrawSide(class_2350 face, CallbackInfoReturnable<Boolean> cir) {
        XRayHack xray = WurstClient.INSTANCE.getHax().xRayHack;
        Boolean shouldDrawSide = xray.shouldDrawSide(this.blockState, this.blockPos);
        if (shouldDrawSide != null) {
            cir.setReturnValue((Object)shouldDrawSide);
        }
    }
}
