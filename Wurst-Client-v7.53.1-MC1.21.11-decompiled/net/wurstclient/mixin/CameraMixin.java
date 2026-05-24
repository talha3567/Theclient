package net.wurstclient.mixin;

import net.minecraft.class_4184;
import net.minecraft.class_5636;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.CameraDistanceHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_4184.class})
public abstract class CameraMixin {
    @ModifyVariable(method={"method_19318"}, at=@At(value="HEAD"), argsOnly=true)
    private float changeClipToSpaceDistance(float desiredCameraDistance) {
        CameraDistanceHack cameraDistance = WurstClient.INSTANCE.getHax().cameraDistanceHack;
        if (cameraDistance.isEnabled()) {
            return cameraDistance.getDistance();
        }
        return desiredCameraDistance;
    }

    @Inject(method={"method_19318"}, at={@At(value="HEAD")}, cancellable=true)
    private void onClipToSpace(float desiredCameraDistance, CallbackInfoReturnable<Float> cir) {
        if (WurstClient.INSTANCE.getHax().cameraNoClipHack.isEnabled()) {
            cir.setReturnValue((Object)Float.valueOf(desiredCameraDistance));
        }
    }

    @Inject(method={"method_19334"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetSubmersionType(CallbackInfoReturnable<class_5636> cir) {
        if (WurstClient.INSTANCE.getHax().noOverlayHack.isEnabled()) {
            cir.setReturnValue((Object)class_5636.field_27888);
        }
    }
}
