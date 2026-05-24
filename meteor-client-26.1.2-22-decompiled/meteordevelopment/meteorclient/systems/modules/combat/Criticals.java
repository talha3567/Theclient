package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IServerboundMovePlayerPacket;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MaceItem;

public class Criticals
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgMace;
    private final Setting<Mode> mode;
    private final Setting<Boolean> ka;
    private final Setting<Boolean> mace;
    private final Setting<Double> extraHeight;
    private ServerboundAttackPacket attackPacket;
    private ServerboundSwingPacket swingPacket;
    private boolean sendPackets;
    private int sendTimer;
    private double lastY;
    private boolean waitingForPeak;

    public Criticals() {
        super(Categories.Combat, "criticals", "Performs critical attacks when you hit your target.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgMace = this.settings.createGroup("Mace");
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("The mode on how Criticals will function.")).defaultValue(Mode.Packet)).build());
        this.ka = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-killaura")).description("Only performs crits when using killaura.")).defaultValue(false)).visible(() -> this.mode.get() != Mode.None)).build());
        this.mace = this.sgMace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("smash-attack")).description("Will always perform smash attacks when using a mace.")).defaultValue(true)).build());
        this.extraHeight = this.sgMace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("additional-height")).description("The amount of additional height to spoof. More height means more damage.")).defaultValue(0.0).min(0.0).sliderRange(0.0, 100.0).visible(this.mace::get)).build());
    }

    @Override
    public void onActivate() {
        this.attackPacket = null;
        this.swingPacket = null;
        this.sendPackets = false;
        this.sendTimer = 0;
        this.lastY = 0.0;
        this.waitingForPeak = false;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        Packet<?> packet = event.packet;
        if (packet instanceof ServerboundAttackPacket) {
            int n;
            ServerboundAttackPacket serverboundAttackPacket = (ServerboundAttackPacket)packet;
            try {
                int n2 = n = serverboundAttackPacket.entityId();
            }
            catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }
            int entityId = n;
            if (this.mace.get().booleanValue() && this.mc.player.getMainHandItem().getItem() instanceof MaceItem) {
                if (this.mc.player.isFallFlying()) {
                    return;
                }
                this.sendPacket(0.0);
                this.sendPacket(1.501 + this.extraHeight.get());
                this.sendPacket(0.0);
                return;
            }
            if (this.skipCrit()) {
                return;
            }
            Entity entity = this.mc.level.getEntity(entityId);
            if (!(entity instanceof LivingEntity)) return;
            if (entity != Modules.get().get(KillAura.class).getTarget() && this.ka.get().booleanValue()) {
                return;
            }
            switch (this.mode.get().ordinal()) {
                case 1: {
                    this.sendPacket(0.0625);
                    this.sendPacket(0.0);
                    return;
                }
                case 2: {
                    this.sendPacket(8.0E-7);
                    this.sendPacket(0.0);
                    return;
                }
                case 3: {
                    this.sendPacket(0.11);
                    this.sendPacket(0.1100013579);
                    this.sendPacket(1.3579E-6);
                    return;
                }
                case 4: 
                case 5: {
                    if (this.sendPackets) return;
                    this.sendPackets = true;
                    this.attackPacket = (ServerboundAttackPacket)event.packet;
                    if (this.mode.get() == Mode.Jump) {
                        this.mc.player.jumpFromGround();
                        this.waitingForPeak = true;
                        this.lastY = this.mc.player.getY();
                    } else {
                        ((IVec3)this.mc.player.getDeltaMovement()).meteor$setY(0.25);
                        this.sendTimer = 4;
                    }
                    event.cancel();
                    return;
                }
            }
            return;
        }
        packet = event.packet;
        if (!(packet instanceof ServerboundSwingPacket)) return;
        ServerboundSwingPacket serverboundSwingPacket = (ServerboundSwingPacket)packet;
        if (this.mode.get() == Mode.Packet) return;
        if (this.skipCrit()) {
            return;
        }
        if (!this.sendPackets) return;
        if (this.swingPacket != null) return;
        this.swingPacket = serverboundSwingPacket;
        event.cancel();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.sendPackets) {
            if (this.mode.get() == Mode.Jump && this.waitingForPeak) {
                double currentY = this.mc.player.getY();
                if (currentY <= this.lastY) {
                    this.waitingForPeak = false;
                    this.sendTimer = 0;
                }
                this.lastY = currentY;
                return;
            }
            if (this.sendTimer <= 0) {
                if (this.attackPacket == null || this.swingPacket == null) {
                    this.sendPackets = false;
                    return;
                }
                this.mc.getConnection().send((Packet)this.attackPacket);
                this.mc.getConnection().send((Packet)this.swingPacket);
                this.attackPacket = null;
                this.swingPacket = null;
                this.sendPackets = false;
            } else {
                --this.sendTimer;
            }
        }
    }

    private void sendPacket(double height) {
        double x = this.mc.player.getX();
        double y = this.mc.player.getY();
        double z = this.mc.player.getZ();
        ServerboundMovePlayerPacket.Pos packet = new ServerboundMovePlayerPacket.Pos(x, y + height, z, false, false);
        ((IServerboundMovePlayerPacket)packet).meteor$setTag(1337);
        this.mc.player.connection.send((Packet)packet);
    }

    private boolean skipCrit() {
        if (EntityUtils.isInCobweb((Entity)this.mc.player) && (this.mode.get() == Mode.Jump || this.mode.get() == Mode.MiniJump)) {
            return true;
        }
        return !this.mc.player.onGround() || this.mc.player.isInWater() || this.mc.player.isInLava() || this.mc.player.onClimbable();
    }

    @Override
    public String getInfoString() {
        return this.mode.get().name();
    }

    public static enum Mode {
        None,
        Packet,
        UpdatedNCP,
        OldNCP,
        Jump,
        MiniJump;

    }
}
