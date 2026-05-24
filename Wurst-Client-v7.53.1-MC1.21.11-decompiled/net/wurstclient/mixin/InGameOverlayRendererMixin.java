package net.wurstclient.mixin;

import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4603;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_4603.class})
public class InGameOverlayRendererMixin {
    @ModifyConstant(method={"method_23070"}, constant={@Constant(floatValue=-0.3f)})
    private static float getFireOffset(float original) {
        return original - WurstClient.INSTANCE.getHax().noFireOverlayHack.getOverlayOffset();
    }

    @Inject(method={"method_23069"}, at={@At(value="HEAD")}, cancellable=true)
    private static void onRenderUnderwaterOverlay(class_310 client, class_4587 matrices, class_4597 vertexConsumerProvider, CallbackInfo ci) {
        if (WurstClient.INSTANCE.getHax().noOverlayHack.isEnabled()) {
            ci.cancel();
        }
    }
}
