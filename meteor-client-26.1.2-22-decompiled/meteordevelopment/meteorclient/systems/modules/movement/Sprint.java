package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.GUIMove;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.Entity;

public class Sprint
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Mode> mode;
    private final Setting<Boolean> keepSprint;
    private final Setting<Boolean> unsprintOnHit;
    public final Setting<Boolean> unsprintInWater;
    private final Setting<Boolean> permaSprint;

    public Sprint() {
        super(Categories.Movement, "sprint", "Automatically sprints.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("sprint-mode")).description("What mode of sprinting.")).defaultValue(Mode.Strict)).build());
        this.keepSprint = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("keep-sprint")).description("Whether to keep sprinting after attacking.")).defaultValue(false)).build());
        this.unsprintOnHit = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("unsprint-on-hit")).description("Whether to stop sprinting before attacking, to ensure you get crits and sweep attacks.")).defaultValue(false)).build());
        this.unsprintInWater = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("unsprint-in-water")).description("Whether to stop sprinting when in water.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Rage)).build());
        this.permaSprint = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sprint-while-stationary")).description("Sprint even when not moving.")).defaultValue(false)).visible(() -> this.mode.get() == Mode.Rage)).build());
    }

    @EventHandler(priority=100)
    private void onTickMovement(TickEvent.Post event) {
        if (this.unsprintInWater.get().booleanValue() && this.mc.player.isInWater()) {
            return;
        }
        this.mc.player.setSprinting(this.shouldSprint());
    }

    @EventHandler(priority=100)
    private void onPacketSend(PacketEvent.Send event) {
        if (!this.unsprintOnHit.get().booleanValue()) {
            return;
        }
        if (!(event.packet instanceof ServerboundAttackPacket)) {
            return;
        }
        this.mc.getConnection().send((Packet)new ServerboundPlayerCommandPacket((Entity)this.mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
        this.mc.player.setSprinting(false);
    }

    @EventHandler
    private void onPacketSent(PacketEvent.Sent event) {
        if (!this.unsprintOnHit.get().booleanValue() || !this.keepSprint.get().booleanValue()) {
            return;
        }
        if (!(event.packet instanceof ServerboundAttackPacket)) {
            return;
        }
        if (!this.shouldSprint() || this.mc.player.isSprinting()) {
            return;
        }
        this.mc.getConnection().send((Packet)new ServerboundPlayerCommandPacket((Entity)this.mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
        this.mc.player.setSprinting(true);
    }

    public boolean shouldSprint() {
        if (this.mc.screen != null && !Modules.get().get(GUIMove.class).sprint.get().booleanValue()) {
            return false;
        }
        float movement = this.mode.get() == Mode.Rage ? Math.abs(this.mc.player.zza) + Math.abs(this.mc.player.xxa) : this.mc.player.zza;
        double d = movement;
        double d2 = this.mc.player.isUnderWater() ? (double)1.0E-5f : 0.8;
        if (d <= d2 && (this.mode.get() == Mode.Strict || !this.permaSprint.get().booleanValue())) {
            return false;
        }
        boolean strictSprint = !this.mc.player.isInShallowWater() && !this.mc.player.isMobilityRestricted() && this.mc.player.isPassenger() ? this.mc.player.getVehicle().canSprint() && this.mc.player.getVehicle().isLocalInstanceAuthoritative() : this.mc.player.getFoodData().hasEnoughFood() && (!this.mc.player.horizontalCollision || this.mc.player.minorHorizontalCollision);
        return this.isActive() && (this.mode.get() == Mode.Rage || strictSprint);
    }

    public boolean rageSprint() {
        return this.isActive() && this.mode.get() == Mode.Rage;
    }

    public boolean unsprintInWater() {
        return this.isActive() && this.unsprintInWater.get() != false;
    }

    public boolean stopSprinting() {
        return !this.isActive() || this.keepSprint.get() == false;
    }

    public static enum Mode {
        Strict,
        Rage;

    }
}
