package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.CanWalkOnFluidEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.HighJump;
import meteordevelopment.meteorclient.systems.modules.movement.Sprint;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightModes;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes.Bounce;
import meteordevelopment.meteorclient.systems.modules.player.NoStatusEffects;
import meteordevelopment.meteorclient.systems.modules.render.HandView;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntity.class})
public abstract class LivingEntityMixin
extends Entity {
    @Unique
    private boolean previousElytra = false;

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @ModifyReturnValue(method={"canStandOnFluid"}, at={@At(value="RETURN")})
    private boolean onCanWalkOnFluid(boolean original, FluidState fluid) {
        if (this != MeteorClient.mc.player) {
            return original;
        }
        CanWalkOnFluidEvent event = MeteorClient.EVENT_BUS.post(CanWalkOnFluidEvent.get(fluid));
        return event.walkOnFluid;
    }

    @Inject(method={"spawnItemParticles"}, at={@At(value="HEAD")}, cancellable=true)
    private void spawnItemParticles(ItemStack itemStack, int count, CallbackInfo ci) {
        NoRender noRender = Modules.get().get(NoRender.class);
        if (noRender.noEatParticles() && itemStack.getComponents().has(DataComponents.FOOD)) {
            ci.cancel();
        }
    }

    @ModifyVariable(method={"swing(Lnet/minecraft/world/InteractionHand;)V"}, at=@At(value="HEAD"), argsOnly=true, name={"hand"})
    private InteractionHand setHand(InteractionHand hand) {
        if (this != MeteorClient.mc.player) {
            return hand;
        }
        HandView handView = Modules.get().get(HandView.class);
        if (handView.isActive()) {
            if (handView.swingMode.get() == HandView.SwingMode.None) {
                return hand;
            }
            return handView.swingMode.get() == HandView.SwingMode.Offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        }
        return hand;
    }

    @ModifyExpressionValue(method={"getCurrentSwingDuration"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/component/SwingAnimation;duration()I")})
    private int getHandSwingDuration(int original) {
        if (this != MeteorClient.mc.player) {
            return original;
        }
        return Modules.get().get(HandView.class).isActive() && MeteorClient.mc.options.getCameraType().isFirstPerson() ? Modules.get().get(HandView.class).swingSpeed.get() : original;
    }

    @ModifyReturnValue(method={"isFallFlying"}, at={@At(value="RETURN")})
    private boolean isGlidingHook(boolean original) {
        if (this != MeteorClient.mc.player) {
            return original;
        }
        if (Modules.get().get(ElytraFly.class).canPacketEfly()) {
            return true;
        }
        return original;
    }

    @Inject(method={"isFallFlying"}, at={@At(value="TAIL")}, cancellable=true)
    public void recastOnLand(CallbackInfoReturnable<Boolean> cir) {
        boolean elytra = (Boolean)cir.getReturnValue();
        ElytraFly elytraFly = Modules.get().get(ElytraFly.class);
        if (this.previousElytra && !elytra && elytraFly.isActive() && elytraFly.flightMode.get() == ElytraFlightModes.Bounce) {
            cir.setReturnValue((Object)Bounce.recastElytra(MeteorClient.mc.player));
        }
        this.previousElytra = elytra;
    }

    @ModifyReturnValue(method={"hasEffect"}, at={@At(value="RETURN")})
    private boolean hasEffect(boolean original, Holder<MobEffect> effect) {
        if (effect == null || effect.value() == null) {
            return original;
        }
        if (Modules.get().get(NoStatusEffects.class).shouldBlock((MobEffect)effect.value())) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method={"jumpFromGround"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;getYRot()F")})
    private float modifyGetYaw(float original) {
        if (this != MeteorClient.mc.player) {
            return original;
        }
        if (!Modules.get().get(Sprint.class).rageSprint()) {
            return original;
        }
        float forward = Math.signum(MeteorClient.mc.player.zza);
        float strafe = 90.0f * Math.signum(MeteorClient.mc.player.xxa);
        if (forward != 0.0f) {
            strafe *= forward * 0.5f;
        }
        original -= strafe;
        if (forward < 0.0f) {
            original -= 180.0f;
        }
        return original;
    }

    @ModifyConstant(method={"jumpFromGround"}, constant={@Constant(floatValue=1.0E-5f)})
    private float modifyJumpConstant(float original) {
        if (this != MeteorClient.mc.player) {
            return original;
        }
        if (!Modules.get().isActive(HighJump.class)) {
            return original;
        }
        return -1.0f;
    }

    @ModifyExpressionValue(method={"jumpFromGround"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;isSprinting()Z")})
    private boolean modifyIsSprinting(boolean original) {
        if (this != MeteorClient.mc.player) {
            return original;
        }
        if (!Modules.get().get(Sprint.class).rageSprint()) {
            return original;
        }
        return original && (Math.abs(MeteorClient.mc.player.zza) > 1.0E-5f || Math.abs(MeteorClient.mc.player.xxa) > 1.0E-5f);
    }
}
