package net.wurstclient.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.class_1275;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2165;
import net.minecraft.class_243;
import net.minecraft.class_5568;
import net.wurstclient.WurstClient;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.VelocityFromEntityCollisionListener;
import net.wurstclient.events.VelocityFromFluidListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_1297.class})
public abstract class EntityMixin
implements class_1275,
class_5568,
class_2165 {
    @WrapWithCondition(method={"method_5692"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1297;method_18799(Lnet/minecraft/class_243;)V", opcode=182, ordinal=0)}, require=0)
    private boolean shouldSetVelocity(class_1297 instance, class_243 velocity) {
        VelocityFromFluidListener.VelocityFromFluidEvent event = new VelocityFromFluidListener.VelocityFromFluidEvent(instance);
        EventManager.fire(event);
        return !event.isCancelled();
    }

    @Inject(method={"method_5697"}, at={@At(value="HEAD")}, cancellable=true)
    private void onPushAwayFrom(class_1297 entity, CallbackInfo ci) {
        VelocityFromEntityCollisionListener.VelocityFromEntityCollisionEvent event = new VelocityFromEntityCollisionListener.VelocityFromEntityCollisionEvent((class_1297)this);
        EventManager.fire(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_5756"}, at={@At(value="RETURN")}, cancellable=true)
    private void onIsInvisibleTo(class_1657 player, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }
        if (WurstClient.INSTANCE.getHax().trueSightHack.shouldBeVisible((class_1297)this)) {
            cir.setReturnValue((Object)false);
        }
    }
}
