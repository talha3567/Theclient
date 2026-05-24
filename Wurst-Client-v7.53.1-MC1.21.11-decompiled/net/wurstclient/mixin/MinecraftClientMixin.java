package net.wurstclient.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.File;
import java.nio.file.Path;
import net.minecraft.class_239;
import net.minecraft.class_310;
import net.minecraft.class_320;
import net.minecraft.class_3678;
import net.minecraft.class_3966;
import net.minecraft.class_4093;
import net.minecraft.class_542;
import net.minecraft.class_636;
import net.minecraft.class_746;
import net.minecraft.class_7853;
import net.wurstclient.WurstClient;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.HandleBlockBreakingListener;
import net.wurstclient.events.HandleInputListener;
import net.wurstclient.events.LeftClickListener;
import net.wurstclient.events.RightClickListener;
import net.wurstclient.mixinterface.IClientPlayerInteractionManager;
import net.wurstclient.mixinterface.ILocalPlayer;
import net.wurstclient.mixinterface.IMinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_310.class})
public abstract class MinecraftClientMixin
extends class_4093<Runnable>
implements class_3678,
IMinecraftClient {
    @Shadow
    @Final
    public File field_1697;
    @Shadow
    public class_636 field_1761;
    @Shadow
    public class_746 field_1724;
    @Unique
    private YggdrasilAuthenticationService wurstAuthenticationService;
    private class_320 wurstSession;
    private class_7853 wurstProfileKeys;

    private MinecraftClientMixin(WurstClient wurst, String name) {
        super(name);
    }

    @Inject(method={"<init>"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_7497;method_44143(Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;Ljava/io/File;)Lnet/minecraft/class_7497;", shift=At.Shift.AFTER)})
    private void captureAuthenticationService(class_542 args, CallbackInfo ci, @Local YggdrasilAuthenticationService yggdrasilAuthenticationService) {
        this.wurstAuthenticationService = yggdrasilAuthenticationService;
    }

    @Inject(method={"method_1574"}, at={@At(value="FIELD", target="Lnet/minecraft/class_310;field_18175:Lnet/minecraft/class_4071;", ordinal=0)})
    private void onHandleInputEvents(CallbackInfo ci) {
        if (this.field_1724 == null) {
            return;
        }
        EventManager.fire(HandleInputListener.HandleInputEvent.INSTANCE);
    }

    @Inject(method={"method_1536"}, at={@At(value="FIELD", target="Lnet/minecraft/class_310;field_1765:Lnet/minecraft/class_239;", ordinal=0)}, cancellable=true)
    private void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
        LeftClickListener.LeftClickEvent event = new LeftClickListener.LeftClickEvent();
        EventManager.fire(event);
        if (event.isCancelled()) {
            cir.setReturnValue((Object)false);
        }
    }

    @Inject(method={"method_1583"}, at={@At(value="FIELD", target="Lnet/minecraft/class_310;field_1752:I", ordinal=0)}, cancellable=true)
    private void onDoItemUse(CallbackInfo ci) {
        RightClickListener.RightClickEvent event = new RightClickListener.RightClickEvent();
        EventManager.fire(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_1511"}, at={@At(value="HEAD")})
    private void onDoItemPick(CallbackInfo ci) {
        if (!WurstClient.INSTANCE.isEnabled()) {
            return;
        }
        class_239 hitResult = WurstClient.MC.field_1765;
        if (!(hitResult instanceof class_3966)) {
            return;
        }
        class_3966 eHitResult = (class_3966)hitResult;
        WurstClient.INSTANCE.getFriends().middleClick(eHitResult.method_17782());
    }

    @Inject(method={"method_1590"}, at={@At(value="HEAD")}, cancellable=true)
    private void onHandleBlockBreaking(boolean breaking, CallbackInfo ci) {
        HandleBlockBreakingListener.HandleBlockBreakingEvent event = new HandleBlockBreakingListener.HandleBlockBreakingEvent();
        EventManager.fire(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_1548"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetSession(CallbackInfoReturnable<class_320> cir) {
        if (this.wurstSession != null) {
            cir.setReturnValue((Object)this.wurstSession);
        }
    }

    @Inject(method={"method_53462"}, at={@At(value="RETURN")}, cancellable=true)
    public void onGetGameProfile(CallbackInfoReturnable<GameProfile> cir) {
        if (this.wurstSession == null) {
            return;
        }
        GameProfile oldProfile = (GameProfile)cir.getReturnValue();
        GameProfile newProfile = new GameProfile(this.wurstSession.method_44717(), this.wurstSession.method_1676(), oldProfile.properties());
        cir.setReturnValue((Object)newProfile);
    }

    @Inject(method={"method_43590"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetProfileKeys(CallbackInfoReturnable<class_7853> cir) {
        if (WurstClient.INSTANCE.getOtfs().noChatReportsOtf.isActive()) {
            cir.setReturnValue((Object)class_7853.field_40800);
        }
        if (this.wurstProfileKeys == null) {
            return;
        }
        cir.setReturnValue((Object)this.wurstProfileKeys);
    }

    @Inject(method={"method_47596"}, at={@At(value="HEAD")}, cancellable=true)
    private void onIsTelemetryEnabledByApi(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue((Object)(!WurstClient.INSTANCE.getOtfs().noTelemetryOtf.isEnabled() ? 1 : 0));
    }

    @Inject(method={"method_47595"}, at={@At(value="HEAD")}, cancellable=true)
    private void onIsOptionalTelemetryEnabledByApi(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue((Object)(!WurstClient.INSTANCE.getOtfs().noTelemetryOtf.isEnabled() ? 1 : 0));
    }

    @Override
    public ILocalPlayer getPlayer() {
        return (ILocalPlayer)this.field_1724;
    }

    @Override
    public IClientPlayerInteractionManager getInteractionManager() {
        return (IClientPlayerInteractionManager)this.field_1761;
    }

    @Override
    public class_320 getWurstSession() {
        return this.wurstSession;
    }

    @Override
    public void setWurstSession(class_320 session) {
        this.wurstSession = session;
        if (session == null) {
            this.wurstProfileKeys = null;
            return;
        }
        String accessToken = session.method_1674();
        boolean isOffline = accessToken == null || accessToken.isBlank() || accessToken.equals("0") || accessToken.equals("null");
        UserApiService userApiService = isOffline ? UserApiService.OFFLINE : this.wurstAuthenticationService.createUserApiService(accessToken);
        this.wurstProfileKeys = class_7853.method_46532((UserApiService)userApiService, (class_320)session, (Path)this.field_1697.toPath());
    }
}
