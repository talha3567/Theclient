package net.wurstclient.mixin;

import net.minecraft.class_9779;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_9779.class_9781.class})
public abstract class RenderTickCounterDynamicMixin {
    @Shadow
    public float field_51958;

    @Inject(method={"method_60639"}, at={@At(value="FIELD", target="Lnet/minecraft/class_9779$class_9781;field_51962:J", opcode=181, ordinal=0)})
    public void onBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        this.field_51958 *= WurstClient.INSTANCE.getHax().timerHack.getTimerSpeed();
    }
}
