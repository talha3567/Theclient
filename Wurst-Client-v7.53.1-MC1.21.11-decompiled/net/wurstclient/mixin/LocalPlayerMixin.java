package net.wurstclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1313;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_638;
import net.minecraft.class_6880;
import net.minecraft.class_742;
import net.minecraft.class_744;
import net.minecraft.class_746;
import net.wurstclient.InputFaker;
import net.wurstclient.WurstClient;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.AirStrafingSpeedListener;
import net.wurstclient.events.IsPlayerInWaterListener;
import net.wurstclient.events.KnockbackListener;
import net.wurstclient.events.PlayerMoveListener;
import net.wurstclient.events.PostMotionListener;
import net.wurstclient.events.PreMotionListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.HackList;
import net.wurstclient.mixinterface.ILocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_746.class})
public abstract class LocalPlayerMixin
extends class_742
implements ILocalPlayer {
    @Shadow
    @Final
    protected class_310 field_3937;
    private class_437 tempCurrentScreen;

    private LocalPlayerMixin(WurstClient wurst, class_638 world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method={"method_5773"}, at={@At(value="HEAD")})
    private void onTickHead(CallbackInfo ci) {
        InputFaker.swapIfNeeded();
    }

    @Inject(method={"method_5773"}, at={@At(value="RETURN")})
    private void onTickReturn(CallbackInfo ci) {
        InputFaker.restoreIfNeeded();
    }

    @Inject(method={"method_5842"}, at={@At(value="HEAD")})
    private void onRideTickHead(CallbackInfo ci) {
        InputFaker.swapIfNeeded();
    }

    @Inject(method={"method_5842"}, at={@At(value="RETURN")})
    private void onRideTickReturn(CallbackInfo ci) {
        InputFaker.restoreIfNeeded();
    }

    @Inject(method={"method_5773"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_742;method_5773()V", ordinal=0)})
    private void onTick(CallbackInfo ci) {
        try (InputFaker.TempRealInput ignore = new InputFaker.TempRealInput();){
            EventManager.fire(UpdateListener.UpdateEvent.INSTANCE);
        }
    }

    @WrapOperation(method={"method_6007"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_744;method_20622()Z", ordinal=0)})
    private boolean wrapHasForwardMovement(class_744 input, Operation<Boolean> original) {
        if (WurstClient.INSTANCE.getHax().autoSprintHack.shouldOmniSprint()) {
            return input.method_3128().method_35584() > 1.0E-5f;
        }
        return (Boolean)original.call(new Object[]{input});
    }

    @WrapOperation(method={"method_6007"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_746;method_75409()Z", ordinal=0)})
    private boolean wrapTickMovementItemUse(class_746 instance, Operation<Boolean> original) {
        if (WurstClient.INSTANCE.getHax().noSlowdownHack.isEnabled()) {
            return false;
        }
        return (Boolean)original.call(new Object[]{instance});
    }

    @WrapOperation(method={"method_67270"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_746;method_6115()Z", ordinal=0)})
    private boolean wrapModifyInputItemUse(class_746 instance, Operation<Boolean> original) {
        if (WurstClient.INSTANCE.getHax().noSlowdownHack.isEnabled()) {
            return false;
        }
        return (Boolean)original.call(new Object[]{instance});
    }

    @Inject(method={"method_3136"}, at={@At(value="HEAD")})
    private void onSendMovementPacketsHEAD(CallbackInfo ci) {
        EventManager.fire(PreMotionListener.PreMotionEvent.INSTANCE);
    }

    @Inject(method={"method_3136"}, at={@At(value="TAIL")})
    private void onSendMovementPacketsTAIL(CallbackInfo ci) {
        EventManager.fire(PostMotionListener.PostMotionEvent.INSTANCE);
    }

    @Inject(method={"method_5784"}, at={@At(value="HEAD")})
    private void onMove(class_1313 type, class_243 offset, CallbackInfo ci) {
        EventManager.fire(PlayerMoveListener.PlayerMoveEvent.INSTANCE);
    }

    @Inject(method={"method_3149"}, at={@At(value="HEAD")}, cancellable=true)
    private void onIsAutoJumpEnabled(CallbackInfoReturnable<Boolean> cir) {
        if (!WurstClient.INSTANCE.getHax().stepHack.isAutoJumpAllowed()) {
            cir.setReturnValue((Object)false);
        }
    }

    @Inject(method={"method_60887"}, at={@At(value="FIELD", target="Lnet/minecraft/class_310;field_1755:Lnet/minecraft/class_437;", opcode=180, ordinal=0)})
    private void beforeTickNausea(boolean fromPortalEffect, CallbackInfo ci) {
        if (!WurstClient.INSTANCE.getHax().portalGuiHack.isEnabled()) {
            return;
        }
        this.tempCurrentScreen = this.field_3937.field_1755;
        this.field_3937.field_1755 = null;
    }

    @Inject(method={"method_60887"}, at={@At(value="FIELD", target="Lnet/minecraft/class_746;field_44911:F", opcode=180, ordinal=1)})
    private void afterTickNausea(boolean fromPortalEffect, CallbackInfo ci) {
        if (this.tempCurrentScreen == null) {
            return;
        }
        this.field_3937.field_1755 = this.tempCurrentScreen;
        this.tempCurrentScreen = null;
    }

    @Inject(method={"method_74047"}, at={@At(value="HEAD")}, cancellable=true)
    private void onCanSprint(boolean allowTouchingWater, CallbackInfoReturnable<Boolean> cir) {
        if (WurstClient.INSTANCE.getHax().autoSprintHack.shouldSprintHungry()) {
            cir.setReturnValue((Object)true);
        }
    }

    protected float method_49484() {
        AirStrafingSpeedListener.AirStrafingSpeedEvent event = new AirStrafingSpeedListener.AirStrafingSpeedEvent(super.method_49484());
        EventManager.fire(event);
        return event.getSpeed();
    }

    public void method_5750(class_243 vec) {
        KnockbackListener.KnockbackEvent event = new KnockbackListener.KnockbackEvent(vec.field_1352, vec.field_1351, vec.field_1350);
        EventManager.fire(event);
        super.method_5750(new class_243(event.getX(), event.getY(), event.getZ()));
    }

    public boolean method_5799() {
        boolean inWater = super.method_5799();
        IsPlayerInWaterListener.IsPlayerInWaterEvent event = new IsPlayerInWaterListener.IsPlayerInWaterEvent(inWater);
        EventManager.fire(event);
        return event.isInWater();
    }

    @Override
    public boolean isTouchingWaterBypass() {
        return super.method_5799();
    }

    protected float method_6106() {
        return super.method_6106() + WurstClient.INSTANCE.getHax().highJumpHack.getAdditionalJumpMotion();
    }

    protected boolean method_21825() {
        return super.method_21825() || WurstClient.INSTANCE.getHax().safeWalkHack.isEnabled();
    }

    protected class_243 method_18796(class_243 movement, class_1313 type) {
        class_243 result = super.method_18796(movement, type);
        if (movement != null) {
            WurstClient.INSTANCE.getHax().safeWalkHack.onClipAtLedge(!movement.equals((Object)result));
        }
        return result;
    }

    public boolean method_6059(class_6880<class_1291> effect) {
        HackList hax = WurstClient.INSTANCE.getHax();
        if (effect == class_1294.field_5925 && hax.fullbrightHack.isNightVisionActive()) {
            return true;
        }
        if (effect == class_1294.field_5902 && hax.noLevitationHack.isEnabled()) {
            return false;
        }
        if (effect == class_1294.field_5919 && hax.antiBlindHack.isEnabled()) {
            return false;
        }
        if (effect == class_1294.field_38092 && hax.antiBlindHack.isEnabled()) {
            return false;
        }
        return super.method_6059(effect);
    }

    public class_1293 method_6112(class_6880<class_1291> effect) {
        HackList hax = WurstClient.INSTANCE.getHax();
        if (effect == class_1294.field_5902 && hax.noLevitationHack.isEnabled()) {
            return null;
        }
        return super.method_6112(effect);
    }

    public float method_49476() {
        return WurstClient.INSTANCE.getHax().stepHack.adjustStepHeight(super.method_49476());
    }

    public double method_55754() {
        HackList hax = WurstClient.INSTANCE.getHax();
        if (hax == null || !hax.reachHack.isEnabled()) {
            return super.method_55754();
        }
        return hax.reachHack.getReachDistance();
    }

    public double method_55755() {
        HackList hax = WurstClient.INSTANCE.getHax();
        if (hax == null || !hax.reachHack.isEnabled()) {
            return super.method_55755();
        }
        return hax.reachHack.getReachDistance();
    }

    @WrapOperation(method={"method_76763"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1297;method_5745(DFZ)Lnet/minecraft/class_239;", ordinal=0)})
    private static class_239 liquidsRaycast(class_1297 instance, double maxDistance, float tickDelta, boolean includeFluids, Operation<class_239> original) {
        if (!WurstClient.INSTANCE.getHax().liquidsHack.isEnabled()) {
            return (class_239)original.call(new Object[]{instance, maxDistance, Float.valueOf(tickDelta), includeFluids});
        }
        return (class_239)original.call(new Object[]{instance, maxDistance, Float.valueOf(tickDelta), true});
    }
}
