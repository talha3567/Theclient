package net.wurstclient.mixin;

import net.minecraft.class_1291;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_6880;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_1309.class})
public class LivingEntityMixin {
    @Inject(method={"method_66279"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetEffectFadeFactor(class_6880<class_1291> registryEntry, float delta, CallbackInfoReturnable<Float> cir) {
        if (registryEntry != class_1294.field_38092) {
            return;
        }
        if (WurstClient.INSTANCE.getHax().antiBlindHack.isEnabled()) {
            cir.setReturnValue((Object)Float.valueOf(0.0f));
        }
    }
}
