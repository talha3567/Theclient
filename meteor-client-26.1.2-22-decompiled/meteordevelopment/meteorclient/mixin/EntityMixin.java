package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.EntityMoveEvent;
import meteordevelopment.meteorclient.events.entity.player.JumpVelocityMultiplierEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixin.PlayerAccessor;
import meteordevelopment.meteorclient.mixininterface.ICamera;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.Hitboxes;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.systems.modules.movement.Jesus;
import meteordevelopment.meteorclient.systems.modules.movement.NoFall;
import meteordevelopment.meteorclient.systems.modules.movement.NoSlow;
import meteordevelopment.meteorclient.systems.modules.movement.Velocity;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.systems.modules.render.ESP;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.systems.modules.world.HighwayBuilder;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={Entity.class})
public abstract class EntityMixin {
    @Inject(method={"isInWater", "isInLava"}, at={@At(value="HEAD")}, cancellable=true)
    private void onIsInFluid(CallbackInfoReturnable<Boolean> cir) {
        if (this != MeteorClient.mc.player) {
            return;
        }
        Flight flight = Modules.get().get(Flight.class);
        NoSlow noSlow = Modules.get().get(NoSlow.class);
        if (flight.isActive() || noSlow.fluidDrag()) {
            cir.setReturnValue((Object)false);
        }
    }

    @Inject(method={"onAboveBubbleColumn", "onInsideBubbleColumn"}, at={@At(value="HEAD")})
    private void onBubbleColumn(CallbackInfo ci) {
        if (this != MeteorClient.mc.player) {
            return;
        }
        Jesus jesus = Modules.get().get(Jesus.class);
        if (jesus.isActive()) {
            jesus.isInBubbleColumn = true;
        }
    }

    @ModifyExpressionValue(method={"updateSwimming"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;isUnderWater()Z")})
    private boolean isSubmergedInWater(boolean submerged) {
        if (this != MeteorClient.mc.player) {
            return submerged;
        }
        if (Modules.get().get(NoSlow.class).fluidDrag()) {
            return false;
        }
        if (Modules.get().get(Flight.class).isActive()) {
            return false;
        }
        return submerged;
    }

    @ModifyArgs(method={"push(Lnet/minecraft/world/entity/Entity;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;push(DDD)V"))
    private void onPushAwayFrom(Args args, Entity entity) {
        Velocity velocity = Modules.get().get(Velocity.class);
        if (this == MeteorClient.mc.player && velocity.isActive() && velocity.entityPush.get().booleanValue()) {
            double multiplier = velocity.entityPushAmount.get();
            args.set(0, (Object)((Double)args.get(0) * multiplier));
            args.set(2, (Object)((Double)args.get(2) * multiplier));
        } else if (entity instanceof FakePlayerEntity) {
            FakePlayerEntity player = (FakePlayerEntity)entity;
            if (player.doNotPush) {
                args.set(0, (Object)0.0);
                args.set(2, (Object)0.0);
            }
        }
    }

    @ModifyReturnValue(method={"getBlockJumpFactor"}, at={@At(value="RETURN")})
    private float onGetBlockJumpFactor(float original) {
        if (this == MeteorClient.mc.player) {
            JumpVelocityMultiplierEvent event = MeteorClient.EVENT_BUS.post(JumpVelocityMultiplierEvent.get());
            return original * event.multiplier;
        }
        return original;
    }

    @Inject(method={"move"}, at={@At(value="HEAD")})
    private void onMove(MoverType moverType, Vec3 delta, CallbackInfo ci) {
        if (this == MeteorClient.mc.player) {
            MeteorClient.EVENT_BUS.post(PlayerMoveEvent.get(moverType, delta));
        } else {
            MeteorClient.EVENT_BUS.post(EntityMoveEvent.get((Entity)this, delta));
        }
    }

    @ModifyExpressionValue(method={"getBlockSpeedFactor"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;")})
    private Block modifyBlockSpeedFactor(Block original) {
        if (this != MeteorClient.mc.player) {
            return original;
        }
        if (original == Blocks.SOUL_SAND && Modules.get().get(NoSlow.class).soulSand()) {
            return Blocks.STONE;
        }
        if (original == Blocks.HONEY_BLOCK && Modules.get().get(NoSlow.class).honeyBlock()) {
            return Blocks.STONE;
        }
        return original;
    }

    @ModifyReturnValue(method={"isInvisibleTo"}, at={@At(value="RETURN")})
    private boolean isInvisibleToCanceller(boolean original) {
        if (!Utils.canUpdate()) {
            return original;
        }
        ESP esp = Modules.get().get(ESP.class);
        if (Modules.get().get(NoRender.class).noInvisibility() || esp.isActive() && !esp.shouldSkip((Entity)this)) {
            return false;
        }
        return original;
    }

    @Inject(method={"isCurrentlyGlowing"}, at={@At(value="HEAD")}, cancellable=true)
    private void isCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (Modules.get().get(NoRender.class).noGlowing()) {
            cir.setReturnValue((Object)false);
        }
    }

    @Inject(method={"getPickRadius"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetPickRadius(CallbackInfoReturnable<Float> cir) {
        double v = Modules.get().get(Hitboxes.class).getEntityValue((Entity)this);
        if (v != 0.0) {
            cir.setReturnValue((Object)Float.valueOf((float)v));
        }
    }

    @Inject(method={"isInvisibleTo"}, at={@At(value="HEAD")}, cancellable=true)
    private void onIsInvisibleTo(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player == null) {
            cir.setReturnValue((Object)false);
        }
    }

    @Inject(method={"getPose"}, at={@At(value="HEAD")}, cancellable=true)
    private void getPoseHook(CallbackInfoReturnable<Pose> cir) {
        if (this != MeteorClient.mc.player) {
            return;
        }
        if (Modules.get().get(ElytraFly.class).canPacketEfly()) {
            cir.setReturnValue((Object)Pose.FALL_FLYING);
        }
    }

    @ModifyReturnValue(method={"getPose"}, at={@At(value="RETURN")})
    private Pose modifyGetPose(Pose original) {
        if (this != MeteorClient.mc.player) {
            return original;
        }
        if (original == Pose.CROUCHING && !MeteorClient.mc.player.isShiftKeyDown() && ((PlayerAccessor)MeteorClient.mc.player).meteor$canChangeIntoPose(Pose.STANDING)) {
            return Pose.STANDING;
        }
        return original;
    }

    @ModifyReturnValue(method={"isSuppressingBounce"}, at={@At(value="RETURN")})
    private boolean cancelBounce(boolean original) {
        return Modules.get().get(NoFall.class).cancelBounce() || original;
    }

    @Inject(method={"turn"}, at={@At(value="HEAD")}, cancellable=true)
    private void updateTurn(double xo, double yo, CallbackInfo ci) {
        if (this != MeteorClient.mc.player) {
            return;
        }
        Freecam freecam = Modules.get().get(Freecam.class);
        FreeLook freeLook = Modules.get().get(FreeLook.class);
        if (freecam.isActive()) {
            freecam.changeLookDirection(xo * 0.15, yo * 0.15);
            ci.cancel();
        } else if (Modules.get().isActive(HighwayBuilder.class)) {
            Camera camera = MeteorClient.mc.gameRenderer.getMainCamera();
            ((ICamera)camera).meteor$setRot((double)camera.yRot() + xo * 0.15, (double)camera.xRot() + yo * 0.15);
            ci.cancel();
        } else if (freeLook.cameraMode()) {
            freeLook.cameraYaw += (float)(xo / (double)freeLook.sensitivity.get().floatValue());
            freeLook.cameraPitch += (float)(yo / (double)freeLook.sensitivity.get().floatValue());
            if (Math.abs(freeLook.cameraPitch) > 90.0f) {
                freeLook.cameraPitch = freeLook.cameraPitch > 0.0f ? 90.0f : -90.0f;
            }
            ci.cancel();
        }
    }
}
