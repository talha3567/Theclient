package meteordevelopment.meteorclient.systems.modules.movement.elytrafly;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightModes;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ElytraFlightMode {
    protected final Minecraft mc;
    protected final ElytraFly elytraFly = Modules.get().get(ElytraFly.class);
    private final ElytraFlightModes type;
    protected boolean lastJumpPressed;
    protected boolean incrementJumpTimer;
    protected boolean lastForwardPressed;
    protected int jumpTimer;
    protected double velX;
    protected double velY;
    protected double velZ;
    protected double ticksLeft;
    protected Vec3 forward;
    protected Vec3 right;
    protected double acceleration;

    public ElytraFlightMode(ElytraFlightModes type) {
        this.mc = Minecraft.getInstance();
        this.type = type;
    }

    public void onTick() {
        ItemStack chestStack;
        FindItemResult fireworks;
        if (this.elytraFly.autoReplenish.get().booleanValue() && (fireworks = InvUtils.find(Items.FIREWORK_ROCKET)).found() && !fireworks.isHotbar()) {
            InvUtils.move().from(fireworks.slot()).toHotbar(this.elytraFly.replenishSlot.get() - 1);
        }
        if (this.elytraFly.replace.get().booleanValue() && (chestStack = this.mc.player.getItemBySlot(EquipmentSlot.CHEST)).getItem() == Items.ELYTRA && chestStack.getMaxDamage() - chestStack.getDamageValue() <= this.elytraFly.replaceDurability.get()) {
            FindItemResult elytra = InvUtils.find(stack -> stack.getMaxDamage() - stack.getDamageValue() > this.elytraFly.replaceDurability.get() && stack.getItem() == Items.ELYTRA);
            InvUtils.move().from(elytra.slot()).toArmor(2);
        }
    }

    public void onPreTick() {
    }

    public void onPacketSend(PacketEvent.Send event) {
    }

    public void onPacketReceive(PacketEvent.Receive event) {
    }

    public void onPlayerMove() {
    }

    public void onActivate() {
        this.lastJumpPressed = false;
        this.jumpTimer = 0;
        this.ticksLeft = 0.0;
        this.acceleration = 0.0;
    }

    public void onDeactivate() {
    }

    public void autoTakeoff() {
        if (this.incrementJumpTimer) {
            ++this.jumpTimer;
        }
        boolean jumpPressed = this.mc.options.keyJump.isDown();
        if (this.elytraFly.autoTakeOff.get().booleanValue() && this.elytraFly.flightMode.get() != ElytraFlightModes.Pitch40 && this.elytraFly.flightMode.get() != ElytraFlightModes.Bounce || !this.elytraFly.manualTakeoff.get().booleanValue() && this.elytraFly.flightMode.get() == ElytraFlightModes.Bounce && jumpPressed) {
            if (!this.lastJumpPressed && !this.mc.player.isFallFlying()) {
                this.jumpTimer = 0;
                this.incrementJumpTimer = true;
            }
            if (this.jumpTimer >= 8) {
                this.jumpTimer = 0;
                this.incrementJumpTimer = false;
                this.mc.player.setJumping(false);
                this.mc.player.setSprinting(true);
                this.mc.player.jumpFromGround();
                this.mc.getConnection().send((Packet)new ServerboundPlayerCommandPacket((Entity)this.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            }
        }
        this.lastJumpPressed = jumpPressed;
    }

    public void handleAutopilot() {
        if (!this.mc.player.isFallFlying()) {
            return;
        }
        if (this.elytraFly.autoPilot.get().booleanValue() && this.mc.player.getY() > this.elytraFly.autoPilotMinimumHeight.get() && this.elytraFly.flightMode.get() != ElytraFlightModes.Bounce) {
            this.mc.options.keyUp.setDown(true);
            this.lastForwardPressed = true;
        }
        if (this.elytraFly.useFireworks.get().booleanValue()) {
            if (this.ticksLeft <= 0.0) {
                this.ticksLeft = this.elytraFly.autoPilotFireworkDelay.get() * 20.0;
                FindItemResult itemResult = InvUtils.findInHotbar(Items.FIREWORK_ROCKET);
                if (!itemResult.found()) {
                    return;
                }
                if (itemResult.isOffhand()) {
                    this.mc.gameMode.useItem((Player)this.mc.player, InteractionHand.OFF_HAND);
                    this.mc.player.swing(InteractionHand.OFF_HAND);
                } else {
                    InvUtils.swap(itemResult.slot(), true);
                    this.mc.gameMode.useItem((Player)this.mc.player, InteractionHand.MAIN_HAND);
                    this.mc.player.swing(InteractionHand.MAIN_HAND);
                    InvUtils.swapBack();
                }
            }
            this.ticksLeft -= 1.0;
        }
    }

    public void handleHorizontalSpeed(PlayerMoveEvent event) {
        boolean a = false;
        boolean b = false;
        if (this.mc.options.keyUp.isDown()) {
            this.velX += this.forward.x * this.getSpeed() * 10.0;
            this.velZ += this.forward.z * this.getSpeed() * 10.0;
            a = true;
        } else if (this.mc.options.keyDown.isDown()) {
            this.velX -= this.forward.x * this.getSpeed() * 10.0;
            this.velZ -= this.forward.z * this.getSpeed() * 10.0;
            a = true;
        }
        if (this.mc.options.keyRight.isDown()) {
            this.velX += this.right.x * this.getSpeed() * 10.0;
            this.velZ += this.right.z * this.getSpeed() * 10.0;
            b = true;
        } else if (this.mc.options.keyLeft.isDown()) {
            this.velX -= this.right.x * this.getSpeed() * 10.0;
            this.velZ -= this.right.z * this.getSpeed() * 10.0;
            b = true;
        }
        if (a && b) {
            double diagonal = 1.0 / Math.sqrt(2.0);
            this.velX *= diagonal;
            this.velZ *= diagonal;
        }
    }

    public void handleVerticalSpeed(PlayerMoveEvent event) {
        if (this.mc.options.keyJump.isDown()) {
            this.velY += 0.5 * this.elytraFly.verticalSpeed.get();
        } else if (this.mc.options.keyShift.isDown()) {
            this.velY -= 0.5 * this.elytraFly.verticalSpeed.get();
        }
    }

    public void handleFallMultiplier() {
        if (this.velY < 0.0) {
            this.velY *= this.elytraFly.fallMultiplier.get().doubleValue();
        } else if (this.velY > 0.0) {
            this.velY = 0.0;
        }
    }

    public void handleAcceleration() {
        if (this.elytraFly.acceleration.get().booleanValue()) {
            if (!PlayerUtils.isMoving()) {
                this.acceleration = 0.0;
            }
            this.acceleration = Math.min(this.acceleration + this.elytraFly.accelerationMin.get() + this.elytraFly.accelerationStep.get() * 0.1, this.elytraFly.horizontalSpeed.get());
        } else {
            this.acceleration = 0.0;
        }
    }

    public void zeroAcceleration() {
        this.acceleration = 0.0;
    }

    protected double getSpeed() {
        return this.elytraFly.acceleration.get() != false ? this.acceleration : this.elytraFly.horizontalSpeed.get();
    }

    public String getHudString() {
        return this.type.name();
    }
}
