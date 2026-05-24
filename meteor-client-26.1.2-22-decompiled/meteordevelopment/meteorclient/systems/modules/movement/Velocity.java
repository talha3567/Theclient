package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ClientboundSetEntityMotionPacketAccessor;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;

public class Velocity
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Boolean> knockback;
    public final Setting<Double> knockbackHorizontal;
    public final Setting<Double> knockbackVertical;
    public final Setting<Boolean> explosions;
    public final Setting<Double> explosionsHorizontal;
    public final Setting<Double> explosionsVertical;
    public final Setting<Boolean> liquids;
    public final Setting<Double> liquidsHorizontal;
    public final Setting<Double> liquidsVertical;
    public final Setting<Boolean> entityPush;
    public final Setting<Double> entityPushAmount;
    public final Setting<Boolean> blocks;
    public final Setting<Boolean> sinking;
    public final Setting<Boolean> fishing;

    public Velocity() {
        super(Categories.Movement, "velocity", "Prevents you from being moved by external forces.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.knockback = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("knockback")).description("Modifies the amount of knockback you take from attacks.")).defaultValue(true)).build());
        this.knockbackHorizontal = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("knockback-horizontal")).description("How much horizontal knockback you will take.")).defaultValue(0.0).sliderMax(1.0).visible(this.knockback::get)).build());
        this.knockbackVertical = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("knockback-vertical")).description("How much vertical knockback you will take.")).defaultValue(0.0).sliderMax(1.0).visible(this.knockback::get)).build());
        this.explosions = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("explosions")).description("Modifies your knockback from explosions.")).defaultValue(true)).build());
        this.explosionsHorizontal = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("explosions-horizontal")).description("How much velocity you will take from explosions horizontally.")).defaultValue(0.0).sliderMax(1.0).visible(this.explosions::get)).build());
        this.explosionsVertical = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("explosions-vertical")).description("How much velocity you will take from explosions vertically.")).defaultValue(0.0).sliderMax(1.0).visible(this.explosions::get)).build());
        this.liquids = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("liquids")).description("Modifies the amount you are pushed by flowing liquids.")).defaultValue(true)).build());
        this.liquidsHorizontal = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("liquids-horizontal")).description("How much velocity you will take from liquids horizontally.")).defaultValue(0.0).sliderMax(1.0).visible(this.liquids::get)).build());
        this.liquidsVertical = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("liquids-vertical")).description("How much velocity you will take from liquids vertically.")).defaultValue(0.0).sliderMax(1.0).visible(this.liquids::get)).build());
        this.entityPush = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("entity-push")).description("Modifies the amount you are pushed by entities.")).defaultValue(true)).build());
        this.entityPushAmount = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("entity-push-amount")).description("How much you will be pushed.")).defaultValue(0.0).sliderMax(1.0).visible(this.entityPush::get)).build());
        this.blocks = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("blocks")).description("Prevents you from being pushed out of blocks.")).defaultValue(true)).build());
        this.sinking = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sinking")).description("Prevents you from sinking in liquids.")).defaultValue(false)).build());
        this.fishing = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fishing")).description("Prevents you from being pulled by fishing rods.")).defaultValue(false)).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!this.sinking.get().booleanValue()) {
            return;
        }
        if (this.mc.options.keyJump.isDown() || this.mc.options.keyShift.isDown()) {
            return;
        }
        if ((this.mc.player.isInWater() || this.mc.player.isInLava()) && this.mc.player.getDeltaMovement().y < 0.0) {
            ((IVec3)this.mc.player.getDeltaMovement()).meteor$setY(0.0);
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        ClientboundSetEntityMotionPacket packet;
        Packet<?> packet2;
        if (this.knockback.get().booleanValue() && (packet2 = event.packet) instanceof ClientboundSetEntityMotionPacket && (packet = (ClientboundSetEntityMotionPacket)packet2).id() == this.mc.player.getId()) {
            double velX = (packet.movement().x() - this.mc.player.getDeltaMovement().x) * this.knockbackHorizontal.get();
            double velY = (packet.movement().y() - this.mc.player.getDeltaMovement().y) * this.knockbackVertical.get();
            double velZ = (packet.movement().z() - this.mc.player.getDeltaMovement().z) * this.knockbackHorizontal.get();
            ((ClientboundSetEntityMotionPacketAccessor)packet).meteor$setMovement(new Vec3(velX + this.mc.player.getDeltaMovement().x, velY + this.mc.player.getDeltaMovement().y, velZ + this.mc.player.getDeltaMovement().z));
        }
    }

    public double getHorizontal(Setting<Double> setting) {
        return this.isActive() ? setting.get() : 1.0;
    }

    public double getVertical(Setting<Double> setting) {
        return this.isActive() ? setting.get() : 1.0;
    }
}
