package meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightModes;
import meteordevelopment.meteorclient.systems.modules.player.Rotation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class Bounce
extends ElytraFlightMode {
    boolean rubberbanded = false;
    int tickDelay;
    double prevFov;

    public Bounce() {
        super(ElytraFlightModes.Bounce);
        this.tickDelay = this.elytraFly.restartDelay.get();
    }

    @Override
    public void onTick() {
        super.onTick();
        if (this.mc.options.keyJump.isDown() && !this.mc.player.isFallFlying() && !this.elytraFly.manualTakeoff.get().booleanValue()) {
            this.mc.getConnection().send((Packet)new ServerboundPlayerCommandPacket((Entity)this.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        }
        if (Bounce.checkConditions(this.mc.player)) {
            if (!this.rubberbanded) {
                if (this.prevFov != 0.0 && !this.elytraFly.sprint.get().booleanValue()) {
                    this.mc.options.fovEffectScale().set((Object)0.0);
                }
                if (this.elytraFly.autoJump.get().booleanValue()) {
                    this.mc.options.keyJump.setDown(true);
                }
                this.mc.options.keyUp.setDown(true);
                this.mc.player.setYRot(this.getYawDirection());
                if (this.elytraFly.lockPitch.get().booleanValue()) {
                    this.mc.player.setXRot(this.elytraFly.pitch.get().floatValue());
                }
            }
            if (!this.elytraFly.sprint.get().booleanValue()) {
                if (this.mc.player.isFallFlying()) {
                    this.mc.player.setSprinting(this.mc.player.onGround());
                } else {
                    this.mc.player.setSprinting(true);
                }
            }
            if (this.rubberbanded && this.elytraFly.restart.get().booleanValue()) {
                if (this.tickDelay > 0) {
                    --this.tickDelay;
                } else {
                    this.mc.getConnection().send((Packet)new ServerboundPlayerCommandPacket((Entity)this.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
                    this.rubberbanded = false;
                    this.tickDelay = this.elytraFly.restartDelay.get();
                }
            }
        }
    }

    @Override
    public void onPreTick() {
        super.onPreTick();
        if (Bounce.checkConditions(this.mc.player) && this.elytraFly.sprint.get().booleanValue()) {
            this.mc.player.setSprinting(true);
        }
    }

    private void unpress() {
        this.mc.options.keyUp.setDown(false);
        if (this.elytraFly.autoJump.get().booleanValue()) {
            this.mc.options.keyJump.setDown(false);
        }
    }

    @Override
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof ClientboundPlayerPositionPacket) {
            this.rubberbanded = true;
            this.mc.player.stopFallFlying();
        }
    }

    @Override
    public void onPacketSend(PacketEvent.Send event) {
        ServerboundPlayerCommandPacket playerCommandPacket;
        Packet<?> packet = event.packet;
        if (packet instanceof ServerboundPlayerCommandPacket && (playerCommandPacket = (ServerboundPlayerCommandPacket)packet).getAction().equals((Object)ServerboundPlayerCommandPacket.Action.START_FALL_FLYING) && !this.elytraFly.sprint.get().booleanValue()) {
            this.mc.player.setSprinting(true);
        }
    }

    public static boolean recastElytra(LocalPlayer player) {
        if (Bounce.checkConditions(player) && Bounce.startGliding(player)) {
            player.connection.send((Packet)new ServerboundPlayerCommandPacket((Entity)player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            return true;
        }
        return false;
    }

    public static boolean checkConditions(LocalPlayer player) {
        BlockState blockState = player.getInBlockState();
        boolean isClimbing = blockState.is(BlockTags.CLIMBABLE) && !blockState.is(BlockTags.CAN_GLIDE_THROUGH);
        return !player.getAbilities().flying && !player.isPassenger() && !isClimbing && !player.isInWater() && !player.hasEffect(MobEffects.LEVITATION);
    }

    private static boolean startGliding(LocalPlayer player) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            if (!LivingEntity.canGlideUsing((ItemStack)player.getItemBySlot(equipmentSlot), (EquipmentSlot)equipmentSlot)) continue;
            MeteorClient.mc.executeIfPossible(() -> ((LocalPlayer)player).startFallFlying());
            return true;
        }
        return false;
    }

    private float getYawDirection() {
        return switch (this.elytraFly.yawLockMode.get()) {
            default -> throw new MatchException(null, null);
            case Rotation.LockMode.None -> this.mc.player.getYRot();
            case Rotation.LockMode.Smart -> (float)Math.round((this.mc.player.getYRot() + 1.0f) / 45.0f) * 45.0f;
            case Rotation.LockMode.Simple -> this.elytraFly.yaw.get().floatValue();
        };
    }

    @Override
    public void onActivate() {
        this.prevFov = (Double)this.mc.options.fovEffectScale().get();
    }

    @Override
    public void onDeactivate() {
        this.unpress();
        this.rubberbanded = false;
        if (this.prevFov != 0.0 && !this.elytraFly.sprint.get().booleanValue()) {
            this.mc.options.fovEffectScale().set((Object)this.prevFov);
        }
    }
}
