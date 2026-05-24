package net.wurstclient.mixin;

import net.minecraft.class_6628;
import net.minecraft.class_7965;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_6628.class})
public class TelemetryManagerMixin {
    @Inject(method={"method_51796"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetSender(CallbackInfoReturnable<class_7965> cir) {
        if (!WurstClient.INSTANCE.getOtfs().noTelemetryOtf.isEnabled()) {
            return;
        }
        cir.setReturnValue((Object)class_7965.field_41434);
    }
}
