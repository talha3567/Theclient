package net.wurstclient.mixin;

import net.minecraft.class_1935;
import net.minecraft.class_2248;
import net.wurstclient.WurstClient;
import net.wurstclient.hack.HackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_2248.class})
public abstract class BlockMixin
implements class_1935 {
    @Inject(method={"method_23349"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetVelocityMultiplier(CallbackInfoReturnable<Float> cir) {
        HackList hax = WurstClient.INSTANCE.getHax();
        if (hax == null || !hax.noSlowdownHack.isEnabled()) {
            return;
        }
        if (cir.getReturnValueF() < 1.0f) {
            cir.setReturnValue((Object)Float.valueOf(1.0f));
        }
    }
}
