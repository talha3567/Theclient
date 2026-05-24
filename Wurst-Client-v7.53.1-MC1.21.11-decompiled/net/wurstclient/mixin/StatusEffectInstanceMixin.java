package net.wurstclient.mixin;

import net.minecraft.class_1293;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_1293.class})
public abstract class StatusEffectInstanceMixin
implements Comparable<class_1293> {
    @Inject(method={"method_5588"}, at={@At(value="HEAD")}, cancellable=true)
    private void onUpdateDuration(CallbackInfo ci) {
        if (WurstClient.INSTANCE.getHax().potionSaverHack.isFrozen()) {
            ci.cancel();
        }
    }
}
