package net.wurstclient.mixin.xray;

import net.minecraft.class_11515;
import net.minecraft.class_2680;
import net.minecraft.class_3610;
import net.minecraft.class_4696;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_4696.class})
public abstract class ItemBlockRenderTypesMixin {
    @Inject(method={"method_23679"}, at={@At(value="HEAD")}, cancellable=true)
    private static void onGetBlockLayer(class_2680 state, CallbackInfoReturnable<class_11515> cir) {
        if (!WurstClient.INSTANCE.getHax().xRayHack.isOpacityMode()) {
            return;
        }
        cir.setReturnValue((Object)class_11515.field_60926);
    }

    @Inject(method={"method_23680"}, at={@At(value="HEAD")}, cancellable=true)
    private static void onGetFluidLayer(class_3610 state, CallbackInfoReturnable<class_11515> cir) {
        if (!WurstClient.INSTANCE.getHax().xRayHack.isOpacityMode()) {
            return;
        }
        cir.setReturnValue((Object)class_11515.field_60926);
    }
}
