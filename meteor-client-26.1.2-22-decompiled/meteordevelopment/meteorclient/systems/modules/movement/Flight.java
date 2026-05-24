package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.LocalPlayerAccessor;
import meteordevelopment.meteorclient.mixin.ServerboundMovePlayerPacketAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class Flight
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgAntiKick;
    private final Setting<Mode> mode;
    private final Setting<Double> speed;
    private final Setting<Boolean> verticalSpeedMatch;
    private final Setting<Boolean> noSneak;
    private final Setting<AntiKickMode> antiKickMode;
    private final Setting<Integer> delay;
    private final Setting<Integer> offTime;
    private int delayLeft;
    private int offLeft;
    private boolean flip;
    private float lastYaw;
    private double lastPacketY;

    public Flight() {
        super(Categories.Movement, "flight", "FLYYYY! No Fall is recommended with this module.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgAntiKick = this.settings.createGroup("Anti Kick");
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("The mode for Flight.")).defaultValue(Mode.Abilities)).onChanged(mode -> {
            if (!this.isActive() || !Utils.canUpdate()) {
                return;
            }
            this.abilitiesOff();
        })).build());
        this.speed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("speed")).description("Your speed when flying.")).defaultValue(0.1).min(0.0).build());
        this.verticalSpeedMatch = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("vertical-speed-match")).description("Matches your vertical speed to your horizontal speed, otherwise uses vanilla ratio.")).defaultValue(false)).build());
        this.noSneak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("no-sneak")).description("Prevents you from sneaking while flying.")).defaultValue(false)).visible(() -> this.mode.get() == Mode.Velocity)).build());
        this.antiKickMode = this.sgAntiKick.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("The mode for anti kick.")).defaultValue(AntiKickMode.Packet)).build());
        this.delay = this.sgAntiKick.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("The amount of delay, in ticks, between flying down a bit and return to original position")).defaultValue(20)).min(1).sliderMax(200).build());
        this.offTime = this.sgAntiKick.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("off-time")).description("The amount of delay, in ticks, to fly down a bit to reset floating ticks.")).defaultValue(1)).min(1).sliderRange(1, 20).build());
        this.delayLeft = this.delay.get();
        this.offLeft = this.offTime.get();
        this.lastPacketY = Double.MAX_VALUE;
    }

    @Override
    public void onActivate() {
        if (this.mode.get() == Mode.Abilities && !this.mc.player.isSpectator()) {
            this.mc.player.getAbilities().flying = true;
            if (this.mc.player.getAbilities().instabuild) {
                return;
            }
            this.mc.player.getAbilities().mayfly = true;
        }
    }

    @Override
    public void onDeactivate() {
        if (this.mode.get() == Mode.Abilities && !this.mc.player.isSpectator()) {
            this.abilitiesOff();
        }
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        float currentYaw = this.mc.player.getYRot();
        if (this.mc.player.fallDistance >= 3.0 && currentYaw == this.lastYaw && this.mc.player.getDeltaMovement().length() < 0.003) {
            this.mc.player.setYRot(currentYaw + (float)(this.flip ? 1 : -1));
            this.flip = !this.flip;
        }
        this.lastYaw = currentYaw;
    }

    @EventHandler
    private void onPostTick(TickEvent.Post event) {
        if (this.delayLeft > 0) {
            --this.delayLeft;
        }
        if (this.offLeft <= 0 && this.delayLeft <= 0) {
            this.delayLeft = this.delay.get();
            this.offLeft = this.offTime.get();
            if (this.antiKickMode.get() == AntiKickMode.Packet) {
                ((LocalPlayerAccessor)this.mc.player).meteor$setPositionReminder(20);
            }
        } else if (this.delayLeft <= 0) {
            boolean shouldReturn = false;
            if (this.antiKickMode.get() == AntiKickMode.Normal) {
                if (this.mode.get() == Mode.Abilities) {
                    this.abilitiesOff();
                    shouldReturn = true;
                }
            } else if (this.antiKickMode.get() == AntiKickMode.Packet && this.offLeft == this.offTime.get()) {
                ((LocalPlayerAccessor)this.mc.player).meteor$setPositionReminder(20);
            }
            --this.offLeft;
            if (shouldReturn) {
                return;
            }
        }
        if (this.mc.player.getYRot() != this.lastYaw) {
            this.mc.player.setYRot(this.lastYaw);
        }
        switch (this.mode.get().ordinal()) {
            case 1: {
                this.mc.player.getAbilities().flying = false;
                this.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
                Vec3 playerVelocity = this.mc.player.getDeltaMovement();
                if (this.mc.options.keyJump.isDown()) {
                    playerVelocity = playerVelocity.add(0.0, this.speed.get() * (double)(this.verticalSpeedMatch.get() != false ? 10.0f : 5.0f), 0.0);
                }
                if (this.mc.options.keyShift.isDown()) {
                    playerVelocity = playerVelocity.subtract(0.0, this.speed.get() * (double)(this.verticalSpeedMatch.get() != false ? 10.0f : 5.0f), 0.0);
                }
                this.mc.player.setDeltaMovement(playerVelocity);
                if (!this.noSneak.get().booleanValue()) break;
                this.mc.player.setOnGround(false);
                break;
            }
            case 0: {
                if (this.mc.player.isSpectator()) {
                    return;
                }
                this.mc.player.getAbilities().setFlyingSpeed(this.speed.get().floatValue());
                this.mc.player.getAbilities().flying = true;
                if (this.mc.player.getAbilities().instabuild) {
                    return;
                }
                this.mc.player.getAbilities().mayfly = true;
            }
        }
    }

    private void antiKickPacket(ServerboundMovePlayerPacket packet, double currentY) {
        if (this.delayLeft <= 0 && this.lastPacketY != Double.MAX_VALUE && this.shouldFlyDown(currentY, this.lastPacketY) && EntityUtils.isOnAir((Entity)this.mc.player)) {
            ((ServerboundMovePlayerPacketAccessor)packet).meteor$setY(this.lastPacketY - 0.0313);
        } else {
            this.lastPacketY = currentY;
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        ServerboundMovePlayerPacket packet;
        block6: {
            block5: {
                Packet<?> packet2 = event.packet;
                if (!(packet2 instanceof ServerboundMovePlayerPacket)) break block5;
                packet = (ServerboundMovePlayerPacket)packet2;
                if (this.antiKickMode.get() == AntiKickMode.Packet) break block6;
            }
            return;
        }
        double currentY = packet.getY(Double.MAX_VALUE);
        if (currentY != Double.MAX_VALUE) {
            this.antiKickPacket(packet, currentY);
        } else {
            Object fullPacket = packet.hasRotation() ? new ServerboundMovePlayerPacket.PosRot(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), packet.getYRot(0.0f), packet.getXRot(0.0f), packet.isOnGround(), this.mc.player.horizontalCollision) : new ServerboundMovePlayerPacket.Pos(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), packet.isOnGround(), this.mc.player.horizontalCollision);
            event.cancel();
            this.antiKickPacket((ServerboundMovePlayerPacket)fullPacket, this.mc.player.getY());
            this.mc.getConnection().send((Packet)fullPacket);
        }
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        ClientboundPlayerAbilitiesPacket packet;
        block3: {
            block2: {
                Packet<?> packet2 = event.packet;
                if (!(packet2 instanceof ClientboundPlayerAbilitiesPacket)) break block2;
                packet = (ClientboundPlayerAbilitiesPacket)packet2;
                if (this.mode.get() == Mode.Abilities) break block3;
            }
            return;
        }
        event.cancel();
        this.mc.player.getAbilities().invulnerable = packet.isInvulnerable();
        this.mc.player.getAbilities().instabuild = packet.canInstabuild();
        this.mc.player.getAbilities().setWalkingSpeed(packet.getWalkingSpeed());
    }

    private boolean shouldFlyDown(double currentY, double lastY) {
        if (currentY >= lastY) {
            return true;
        }
        return lastY - currentY < 0.0313;
    }

    private void abilitiesOff() {
        this.mc.player.getAbilities().flying = false;
        this.mc.player.getAbilities().setFlyingSpeed(0.05f);
        if (this.mc.player.getAbilities().instabuild) {
            return;
        }
        this.mc.player.getAbilities().mayfly = false;
    }

    public float getFlyingSpeed() {
        if (!this.isActive() || this.mode.get() != Mode.Velocity) {
            return -1.0f;
        }
        return this.speed.get().floatValue() * (this.mc.player.isSprinting() ? 15.0f : 10.0f);
    }

    public boolean noSneak() {
        return this.isActive() && this.mode.get() == Mode.Velocity && this.noSneak.get() != false;
    }

    public static enum Mode {
        Abilities,
        Velocity;

    }

    public static enum AntiKickMode {
        Normal,
        Packet,
        None;

    }
}
