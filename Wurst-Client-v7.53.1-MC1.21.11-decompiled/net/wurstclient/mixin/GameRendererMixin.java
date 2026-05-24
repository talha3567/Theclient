package net.wurstclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_1309;
import net.minecraft.class_4587;
import net.minecraft.class_757;
import net.wurstclient.WurstClient;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.CameraTransformViewBobbingListener;
import net.wurstclient.hacks.FullbrightHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_757.class})
public abstract class GameRendererMixin
implements AutoCloseable {
    @WrapOperation(method={"method_3188"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_757;method_3186(Lnet/minecraft/class_4587;F)V", ordinal=0)})
    private void onBobView(class_757 instance, class_4587 matrices, float tickDelta, Operation<Void> original) {
        CameraTransformViewBobbingListener.CameraTransformViewBobbingEvent event = new CameraTransformViewBobbingListener.CameraTransformViewBobbingEvent();
        EventManager.fire(event);
        if (!event.isCancelled()) {
            original.call(new Object[]{instance, matrices, Float.valueOf(tickDelta)});
        }
    }

    @ModifyReturnValue(method={"method_3196"}, at={@At(value="RETURN")})
    private float onGetFov(float original) {
        return WurstClient.INSTANCE.getOtfs().zoomOtf.changeFovBasedOnZoom(original);
    }

    @WrapOperation(method={"method_3188"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_3532;method_16439(FFF)F", ordinal=0)})
    private float onRenderWorldNauseaLerp(float delta, float start, float end, Operation<Float> original) {
        if (!WurstClient.INSTANCE.getHax().antiWobbleHack.isEnabled()) {
            return ((Float)original.call(new Object[]{Float.valueOf(delta), Float.valueOf(start), Float.valueOf(end)})).floatValue();
        }
        return 0.0f;
    }

    @Inject(method={"method_3174"}, at={@At(value="HEAD")}, cancellable=true)
    private static void onGetNightVisionStrength(class_1309 entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
        FullbrightHack fullbright = WurstClient.INSTANCE.getHax().fullbrightHack;
        if (fullbright.isNightVisionActive()) {
            cir.setReturnValue((Object)Float.valueOf(fullbright.getNightVisionStrength()));
        }
    }

    @Inject(method={"method_3198"}, at={@At(value="HEAD")}, cancellable=true)
    private void onTiltViewWhenHurt(class_4587 matrices, float tickDelta, CallbackInfo ci) {
        if (WurstClient.INSTANCE.getHax().noHurtcamHack.isEnabled()) {
            ci.cancel();
        }
    }
}
