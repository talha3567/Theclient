package net.wurstclient.mixin;

import net.minecraft.class_11398;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_7285;
import net.minecraft.class_9779;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_11398.class})
public class AtmosphericFogModifierMixin {
    @Inject(method={"method_42591"}, at={@At(value="TAIL")}, cancellable=true)
    private void onApplyStartEndModifier(class_7285 data, class_4184 camera, class_638 world, float viewDistance, class_9779 tickCounter, CallbackInfo ci) {
        if (!WurstClient.INSTANCE.getHax().noFogHack.isEnabled()) {
            return;
        }
        data.field_60582 = 1000000.0f;
        data.field_60584 = 1000000.0f;
    }
}
