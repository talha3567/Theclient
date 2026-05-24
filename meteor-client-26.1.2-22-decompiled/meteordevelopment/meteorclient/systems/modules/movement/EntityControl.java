package meteordevelopment.meteorclient.systems.modules.movement;

import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.events.entity.EntityMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.phys.Vec3;

public class EntityControl
extends Module {
    private final SettingGroup sgControl;
    private final SettingGroup sgSpeed;
    private final SettingGroup sgFlight;
    private final List<EntityType<?>> list;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<Boolean> spoofSaddle;
    private final Setting<Boolean> maxJump;
    public final Setting<Boolean> lockYaw;
    private final Setting<Boolean> cancelServerPackets;
    private final Setting<Boolean> speed;
    private final Setting<Double> horizontalSpeed;
    private final Setting<Boolean> onlyOnGround;
    private final Setting<Boolean> inWater;
    private final Setting<Boolean> flight;
    private final Setting<Double> verticalSpeed;
    private final Setting<Double> fallSpeed;
    private final Setting<Boolean> antiKick;
    private final Setting<Integer> delay;
    private int delayLeft;
    private double lastPacketY;
    private boolean sentPacket;

    public EntityControl() {
        super(Categories.Movement, "entity-control", "Lets you control rideable entities without a saddle.", "entity-speed", "entity-fly", "boat-fly");
        this.sgControl = this.settings.createGroup("Control");
        this.sgSpeed = this.settings.createGroup("Speed");
        this.sgFlight = this.settings.createGroup("Flight");
        this.list = BuiltInRegistries.ENTITY_TYPE.stream().filter(entityType -> EntityUtils.isRideable(entityType) && entityType != EntityType.MINECART && entityType != EntityType.LLAMA && entityType != EntityType.TRADER_LLAMA).toList();
        this.entities = this.sgControl.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Target entities.")).filter(entityType -> EntityUtils.isRideable(entityType) && entityType != EntityType.MINECART && entityType != EntityType.LLAMA && entityType != EntityType.TRADER_LLAMA).defaultValue((EntityType[])this.list.toArray(EntityType[]::new)).build());
        this.spoofSaddle = this.sgControl.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("spoof-saddle*")).description("Lets you control rideable entities without them being saddled. Only works on older server versions.")).defaultValue(false)).build());
        this.maxJump = this.sgControl.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("max-jump")).description("Sets jump power to maximum.")).defaultValue(true)).build());
        this.lockYaw = this.sgControl.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("lock-yaw")).description("Locks the Entity's yaw.")).defaultValue(true)).build());
        this.cancelServerPackets = this.sgControl.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("cancel-server-packets")).description("Cancels incoming vehicle move packets. WILL desync you from the server if you make an invalid movement.")).defaultValue(false)).build());
        this.speed = this.sgSpeed.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("speed")).description("Makes you go faster horizontally when riding entities.")).defaultValue(false)).build());
        this.horizontalSpeed = this.sgSpeed.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("horizontal-speed")).description("Horizontal speed in blocks per second.")).defaultValue(10.0).min(0.0).sliderMax(50.0).visible(this.speed::get)).build());
        this.onlyOnGround = this.sgSpeed.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-on-ground")).description("Use speed only when standing on a block.")).defaultValue(false)).visible(this.speed::get)).build());
        this.inWater = this.sgSpeed.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("in-water")).description("Use speed when in water.")).defaultValue(true)).visible(this.speed::get)).build());
        this.flight = this.sgFlight.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fly")).description("Allows you to fly with entities.")).defaultValue(false)).build());
        this.verticalSpeed = this.sgFlight.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("vertical-speed")).description("Vertical speed in blocks per second.")).defaultValue(6.0).min(0.0).sliderMax(20.0).visible(this.flight::get)).build());
        this.fallSpeed = this.sgFlight.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("fall-speed")).description("How fast you will fall in blocks per second. Set to a small value to prevent fly kicks.")).defaultValue(0.0).min(0.0).visible(this.flight::get)).build());
        this.antiKick = this.sgFlight.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-fly-kick")).description("Whether to prevent the server from kicking you for flying.")).defaultValue(true)).visible(this.flight::get)).build());
        this.delay = this.sgFlight.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("The amount of delay, in ticks, between flying down a bit and return to original position")).defaultValue(40)).min(1).sliderMax(80).visible(() -> this.flight.get() != false && this.antiKick.get() != false)).build());
        this.lastPacketY = Double.MAX_VALUE;
        this.sentPacket = false;
    }

    @Override
    public void onActivate() {
        this.delayLeft = this.delay.get();
        this.sentPacket = false;
        this.lastPacketY = Double.MAX_VALUE;
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (this.sentPacket && this.mc.player.getVehicle() != null) {
            ServerboundMoveVehiclePacket packet = ServerboundMoveVehiclePacket.fromEntity((Entity)this.mc.player.getVehicle());
            ((IVec3)packet.position()).meteor$setY(this.lastPacketY);
            this.mc.getConnection().send((Packet)packet);
            this.sentPacket = false;
        }
        --this.delayLeft;
    }

    @EventHandler
    private void onEntityMove(EntityMoveEvent event) {
        Entity entity = event.entity;
        if (event.entity.getControllingPassenger() != this.mc.player || !this.entities.get().contains(entity.getType())) {
            return;
        }
        double velX = entity.getDeltaMovement().x;
        double velY = entity.getDeltaMovement().y;
        double velZ = entity.getDeltaMovement().z;
        if (this.speed.get().booleanValue() && (!this.onlyOnGround.get().booleanValue() || entity.onGround() || entity.isFlyingVehicle()) && (this.inWater.get().booleanValue() || !entity.isInWater())) {
            Vec3 vel = PlayerUtils.getHorizontalVelocity(this.horizontalSpeed.get());
            velX = vel.x;
            velZ = vel.z;
        }
        if (this.flight.get().booleanValue()) {
            velY = 0.0;
            if (Input.isPressed(this.mc.options.keyJump)) {
                velY += this.verticalSpeed.get() / 20.0;
            }
            velY = Input.isPressed(this.mc.options.keySprint) ? (velY -= this.verticalSpeed.get() / 20.0) : (velY -= this.fallSpeed.get() / 20.0);
        }
        if (this.lockYaw.get().booleanValue()) {
            entity.setYRot(this.mc.player.getYRot());
        }
        ((IVec3)event.movement).meteor$set(velX, velY, velZ);
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        ServerboundMoveVehiclePacket packet;
        block5: {
            block4: {
                Packet<?> packet2 = event.packet;
                if (!(packet2 instanceof ServerboundMoveVehiclePacket)) break block4;
                packet = (ServerboundMoveVehiclePacket)packet2;
                if (this.antiKick.get().booleanValue()) break block5;
            }
            return;
        }
        double currentY = packet.position().y;
        if (this.delayLeft <= 0 && !this.sentPacket && this.shouldFlyDown(currentY) && EntityUtils.isOnAir(this.mc.player.getVehicle()) && !this.mc.player.getVehicle().isFlyingVehicle()) {
            ((IVec3)packet.position()).meteor$setY(this.lastPacketY - 0.0313);
            this.sentPacket = true;
            this.delayLeft = this.delay.get();
        }
        this.lastPacketY = currentY;
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof ClientboundMoveVehiclePacket && this.cancelServerPackets.get().booleanValue()) {
            event.cancel();
        }
    }

    private boolean shouldFlyDown(double currentY) {
        if (currentY >= this.lastPacketY) {
            return true;
        }
        return this.lastPacketY - currentY < 0.0313;
    }

    public boolean spoofSaddle() {
        return this.isActive() && this.spoofSaddle.get() != false;
    }

    public boolean maxJump() {
        return this.isActive() && this.maxJump.get() != false;
    }

    public boolean cancelJump() {
        if (!(this.mc.player.getVehicle() instanceof PlayerRideableJumping)) {
            return false;
        }
        return this.isActive() && this.entities.get().contains(this.mc.player.getVehicle().getType()) && this.flight.get() != false;
    }
}
