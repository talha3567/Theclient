package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.GetFovEvent;
import meteordevelopment.meteorclient.mixininterface.ICamera;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.CameraTweaks;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.systems.modules.world.HighwayBuilder;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={Camera.class})
public abstract class CameraMixin
implements ICamera {
    @Shadow
    private boolean detached;
    @Shadow
    private float yRot;
    @Shadow
    private float xRot;

    @Shadow
    protected abstract void setRotation(float var1, float var2);

    @Inject(method={"getFluidInCamera"}, at={@At(value="HEAD")}, cancellable=true)
    private void getSubmergedFluidState(CallbackInfoReturnable<FogType> cir) {
        if (Modules.get().get(NoRender.class).noLiquidOverlay()) {
            cir.setReturnValue((Object)FogType.NONE);
        }
    }

    @ModifyVariable(method={"getMaxZoom"}, at=@At(value="HEAD"), argsOnly=true, name={"cameraDist"})
    private float modifyGetMaxZoom(float cameraDist) {
        if (Modules.get().get(Freecam.class).isActive()) {
            return 0.0f;
        }
        CameraTweaks cameraTweaks = Modules.get().get(CameraTweaks.class);
        return cameraTweaks.isActive() ? (float)cameraTweaks.distance : cameraDist;
    }

    @Inject(method={"getMaxZoom"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetMaxZoom(float cameraDist, CallbackInfoReturnable<Float> cir) {
        if (Modules.get().get(CameraTweaks.class).clip()) {
            cir.setReturnValue((Object)Float.valueOf(cameraDist));
        }
    }

    @Inject(method={"alignWithEntity"}, at={@At(value="TAIL")})
    private void onAlignWithEntityTail(float partialTicks, CallbackInfo ci) {
        if (Modules.get().isActive(Freecam.class)) {
            this.detached = true;
        }
    }

    @ModifyArgs(method={"alignWithEntity"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/Camera;setPosition(DDD)V"))
    private void onAlignSetPosArgs(Args args, @Local(argsOnly=true, name={"partialTicks"}) float partialTicks) {
        Freecam freecam = Modules.get().get(Freecam.class);
        if (freecam.isActive()) {
            args.set(0, (Object)freecam.getX(partialTicks));
            args.set(1, (Object)freecam.getY(partialTicks));
            args.set(2, (Object)freecam.getZ(partialTicks));
        }
    }

    @ModifyArgs(method={"alignWithEntity"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/Camera;setRotation(FF)V"))
    private void onAlignSetRotationArgs(Args args, @Local(argsOnly=true, name={"partialTicks"}) float partialTicks) {
        Freecam freecam = Modules.get().get(Freecam.class);
        FreeLook freeLook = Modules.get().get(FreeLook.class);
        if (freecam.isActive()) {
            args.set(0, (Object)Float.valueOf((float)freecam.getYaw(partialTicks)));
            args.set(1, (Object)Float.valueOf((float)freecam.getPitch(partialTicks)));
        } else if (Modules.get().isActive(HighwayBuilder.class)) {
            args.set(0, (Object)Float.valueOf(this.yRot));
            args.set(1, (Object)Float.valueOf(this.xRot));
        } else if (freeLook.isActive()) {
            args.set(0, (Object)Float.valueOf(freeLook.cameraYaw));
            args.set(1, (Object)Float.valueOf(freeLook.cameraPitch));
        }
    }

    @ModifyReturnValue(method={"calculateFov"}, at={@At(value="RETURN")})
    private float modifyFov(float original) {
        return MeteorClient.EVENT_BUS.post(GetFovEvent.get((float)original)).fov;
    }

    @Override
    public void meteor$setRot(double yaw, double pitch) {
        this.setRotation((float)yaw, (float)Mth.clamp((double)pitch, (double)-90.0, (double)90.0));
    }
}
