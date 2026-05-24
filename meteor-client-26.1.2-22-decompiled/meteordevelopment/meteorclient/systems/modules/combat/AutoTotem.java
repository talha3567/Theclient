package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class AutoTotem
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;
    private final Setting<Integer> delay;
    private final Setting<Integer> health;
    private final Setting<Boolean> elytra;
    private final Setting<Boolean> fall;
    private final Setting<Boolean> explosion;
    public boolean locked;
    private int totems;
    private int ticks;

    public AutoTotem() {
        super(Categories.Combat, "auto-totem", "Automatically equips a totem in your offhand.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Determines when to hold a totem, strict will always hold.")).defaultValue(Mode.Smart)).build());
        this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("The ticks between slot movements.")).defaultValue(0)).min(0).build());
        this.health = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("health")).description("The health to hold a totem at.")).defaultValue(10)).range(0, 36).sliderMax(36).visible(() -> this.mode.get() == Mode.Smart)).build());
        this.elytra = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("elytra")).description("Will always hold a totem when flying with elytra.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart)).build());
        this.fall = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fall")).description("Will hold a totem when fall damage could kill you.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart)).build());
        this.explosion = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("explosion")).description("Will hold a totem when explosion damage could kill you.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart)).build());
    }

    @EventHandler(priority=200)
    private void onTick(TickEvent.Pre event) {
        FindItemResult result = InvUtils.find(Items.TOTEM_OF_UNDYING);
        this.totems = result.count();
        if (this.totems <= 0) {
            this.locked = false;
        } else if (this.ticks >= this.delay.get()) {
            boolean low = this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount() - PlayerUtils.possibleHealthReductions(this.explosion.get(), this.fall.get()) <= (float)this.health.get().intValue();
            boolean ely = this.elytra.get() != false && this.mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA && this.mc.player.isFallFlying();
            boolean bl = this.locked = this.mode.get() == Mode.Strict || this.mode.get() == Mode.Smart && (low || ely);
            if (this.locked && this.mc.player.getOffhandItem().getItem() != Items.TOTEM_OF_UNDYING) {
                InvUtils.move().from(result.slot()).toOffhand();
            }
            this.ticks = 0;
            return;
        }
        ++this.ticks;
    }

    @EventHandler(priority=100)
    private void onReceivePacket(PacketEvent.Receive event) {
        Packet<?> packet = event.packet;
        if (!(packet instanceof ClientboundEntityEventPacket)) {
            return;
        }
        ClientboundEntityEventPacket p = (ClientboundEntityEventPacket)packet;
        if (p.getEventId() != 35) {
            return;
        }
        Entity entity = p.getEntity((Level)this.mc.level);
        if (entity == null || !entity.equals((Object)this.mc.player)) {
            return;
        }
        this.ticks = 0;
    }

    public boolean isLocked() {
        return this.isActive() && this.locked;
    }

    @Override
    public String getInfoString() {
        return String.valueOf(this.totems);
    }

    public static enum Mode {
        Smart,
        Strict;

    }
}
