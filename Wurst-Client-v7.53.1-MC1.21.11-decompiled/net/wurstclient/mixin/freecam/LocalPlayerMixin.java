package net.wurstclient.mixin.freecam;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import java.util.function.Predicate;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3959;
import net.minecraft.class_3966;
import net.minecraft.class_638;
import net.minecraft.class_742;
import net.minecraft.class_746;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.FreecamHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_746.class})
public abstract class LocalPlayerMixin
extends class_742 {
    private LocalPlayerMixin(WurstClient wurst, class_638 world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method={"method_5715"}, at={@At(value="HEAD")}, cancellable=true)
    private void onIsShiftKeyDown(CallbackInfoReturnable<Boolean> cir) {
        if (WurstClient.INSTANCE.getHax().freecamHack.isMovingCamera()) {
            cir.setReturnValue((Object)false);
        }
    }

    public void method_5872(double deltaYaw, double deltaPitch) {
        FreecamHack freecam = WurstClient.INSTANCE.getHax().freecamHack;
        if (freecam.isMovingCamera()) {
            freecam.turn(deltaYaw, deltaPitch);
            return;
        }
        super.method_5872(deltaYaw, deltaPitch);
    }

    @WrapOperation(method={"method_76763"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1297;method_5745(DFZ)Lnet/minecraft/class_239;")})
    private static class_239 modifyBlockRaycast(class_1297 player, double maxDist, float partialTicks, boolean includeFluids, Operation<class_239> original) {
        FreecamHack freecam = WurstClient.INSTANCE.getHax().freecamHack;
        if (!freecam.isClickingFromCamera()) {
            return (class_239)original.call(new Object[]{player, maxDist, Float.valueOf(partialTicks), includeFluids});
        }
        class_243 camStart = freecam.getCamPos(partialTicks);
        class_243 camEnd = camStart.method_1019(freecam.getScaledCamDir(maxDist));
        return player.method_73183().method_17742(new class_3959(camStart, camEnd, class_3959.class_3960.field_17559, includeFluids ? class_3959.class_242.field_1347 : class_3959.class_242.field_1348, player));
    }

    @WrapOperation(method={"method_76763"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1675;method_18075(Lnet/minecraft/class_1297;Lnet/minecraft/class_243;Lnet/minecraft/class_243;Lnet/minecraft/class_238;Ljava/util/function/Predicate;D)Lnet/minecraft/class_3966;")})
    private static class_3966 modifyEntityRaycast(class_1297 instance, class_243 start, class_243 end, class_238 bounds, Predicate<class_1297> predicate, double maxDistSq, Operation<class_3966> original, @Local(ordinal=0) double maxDist) {
        FreecamHack freecam = WurstClient.INSTANCE.getHax().freecamHack;
        if (!freecam.isClickingFromCamera()) {
            return (class_3966)original.call(new Object[]{instance, start, end, bounds, predicate, maxDistSq});
        }
        class_243 camStart = freecam.getCamPos(1.0f);
        class_243 scaledCamDir = freecam.getScaledCamDir(maxDist);
        class_243 camEnd = camStart.method_1019(scaledCamDir);
        class_238 camBounds = class_1299.field_6097.method_18386().method_30757(camStart).method_18804(scaledCamDir).method_1014(1.0);
        return (class_3966)original.call(new Object[]{instance, camStart, camEnd, camBounds, predicate, maxDistSq});
    }
}
