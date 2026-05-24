package net.wurstclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import net.minecraft.class_10017;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_897;
import net.minecraft.class_898;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.HealthTagsHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_897.class})
public abstract class EntityRendererMixin<T extends class_1297, S extends class_10017> {
    @WrapOperation(method={"method_62354"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_898;method_23168(Lnet/minecraft/class_1297;)D")})
    private double fakeSquaredDistanceToCamera(class_898 dispatcher, class_1297 entity, Operation<Double> original, @Share(value="actualDistanceSq") LocalDoubleRef actualDistanceSq) {
        actualDistanceSq.set(((Double)original.call(new Object[]{dispatcher, entity})).doubleValue());
        if (WurstClient.INSTANCE.getHax().nameTagsHack.isUnlimitedRange()) {
            return 0.0;
        }
        return actualDistanceSq.get();
    }

    @Inject(method={"method_62354"}, at={@At(value="TAIL")})
    private void restoreSquaredDistanceToCamera(T entity, S state, float tickDelta, CallbackInfo ci, @Share(value="actualDistanceSq") LocalDoubleRef actualDistanceSq) {
        ((class_10017)state).field_53332 = actualDistanceSq.get();
    }

    @Inject(method={"method_62354"}, at={@At(value="TAIL")})
    private void addHealthToDisplayName(T entity, S state, float tickProgress, CallbackInfo ci) {
        if (((class_10017)state).field_53337 == null) {
            return;
        }
        if (!(entity instanceof class_1309)) {
            return;
        }
        class_1309 le = (class_1309)entity;
        HealthTagsHack healthTags = WurstClient.INSTANCE.getHax().healthTagsHack;
        if (!healthTags.isEnabled()) {
            return;
        }
        ((class_10017)state).field_53337 = healthTags.addHealth(le, ((class_10017)state).field_53337.method_27661());
    }
}
