package net.wurstclient.mixin;

import net.minecraft.class_1309;
import net.minecraft.class_922;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_922.class})
public abstract class LivingEntityRendererMixin {
    @Inject(method={"method_4055"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_310;method_1551()Lnet/minecraft/class_310;", ordinal=0)}, cancellable=true)
    private void shouldForceLabel(class_1309 entity, double distanceSq, CallbackInfoReturnable<Boolean> cir) {
        if (WurstClient.INSTANCE.getHax().nameTagsHack.shouldForcePlayerNametags()) {
            cir.setReturnValue((Object)true);
        }
    }
}
