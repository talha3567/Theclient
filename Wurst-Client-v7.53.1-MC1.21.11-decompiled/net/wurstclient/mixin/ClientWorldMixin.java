package net.wurstclient.mixin;

import net.minecraft.class_1802;
import net.minecraft.class_1934;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_638.class})
public class ClientWorldMixin {
    @Shadow
    @Final
    private class_310 field_3729;

    @Inject(method={"method_35752"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetBlockParticle(CallbackInfoReturnable<class_2248> cir) {
        if (!WurstClient.INSTANCE.getHax().barrierEspHack.isEnabled()) {
            return;
        }
        if (this.field_3729.field_1761.method_2920() == class_1934.field_9220 && this.field_3729.field_1724.method_6047().method_7909() == class_1802.field_30904) {
            return;
        }
        cir.setReturnValue((Object)class_2246.field_10499);
    }
}
