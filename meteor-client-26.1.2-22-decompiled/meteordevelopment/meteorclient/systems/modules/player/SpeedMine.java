package meteordevelopment.meteorclient.systems.modules.player;

import java.util.List;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.MultiPlayerGameModeAccessor;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

public class SpeedMine
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Mode> mode;
    private final Setting<List<Block>> blocks;
    private final Setting<ListMode> blocksFilter;
    public final Setting<Double> modifier;
    private final Setting<Integer> hasteAmplifier;
    private final Setting<Boolean> instamine;
    private final Setting<Boolean> grimBypass;

    public SpeedMine() {
        super(Categories.Player, "speed-mine", "Allows you to quickly mine blocks.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).defaultValue(Mode.Damage)).onChanged(mode -> this.removeHaste())).build());
        this.blocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blocks")).description("Selected blocks.")).filter((Block block) -> block.defaultDestroyTime() > 0.0f).visible(() -> this.mode.get() != Mode.Haste)).build());
        this.blocksFilter = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("blocks-filter")).description("How to use the blocks setting.")).defaultValue(ListMode.Blacklist)).visible(() -> this.mode.get() != Mode.Haste)).build());
        this.modifier = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("modifier")).description("Mining speed modifier. An additional value of 0.2 is equivalent to one haste level (1.2 = haste 1).")).defaultValue(1.4).visible(() -> this.mode.get() == Mode.Normal)).min(0.0).build());
        this.hasteAmplifier = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("haste-amplifier")).description("What value of haste to give you. Above 2 not recommended.")).defaultValue(2)).min(1).visible(() -> this.mode.get() == Mode.Haste)).onChanged(n -> this.removeHaste())).build());
        this.instamine = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("instamine")).description("Whether or not to instantly mine blocks under certain conditions.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Damage)).build());
        this.grimBypass = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("grim-bypass")).description("Bypasses Grim's fastbreak check, working as of 2.3.58")).defaultValue(false)).visible(() -> this.mode.get() == Mode.Damage)).build());
    }

    @Override
    public void onDeactivate() {
        this.removeHaste();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) {
            return;
        }
        if (this.mode.get() == Mode.Haste) {
            MobEffectInstance haste = this.mc.player.getEffect(MobEffects.HASTE);
            if (haste == null || haste.getAmplifier() <= this.hasteAmplifier.get() - 1) {
                this.mc.player.addEffect(new MobEffectInstance(MobEffects.HASTE, -1, this.hasteAmplifier.get() - 1, false, false, false), null);
            }
        } else if (this.mode.get() == Mode.Damage) {
            MultiPlayerGameModeAccessor im = (MultiPlayerGameModeAccessor)this.mc.gameMode;
            float progress = im.meteor$getBreakingProgress();
            BlockPos pos = im.meteor$getCurrentBreakingBlockPos();
            if (pos == null || progress <= 0.0f) {
                return;
            }
            if (progress + this.mc.level.getBlockState(pos).getDestroyProgress((Player)this.mc.player, (BlockGetter)this.mc.level, pos) >= 0.7f) {
                im.meteor$setDestroyProgress(1.0f);
            }
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Send event) {
        ServerboundPlayerActionPacket packet;
        if (this.mode.get() != Mode.Damage || !this.grimBypass.get().booleanValue()) {
            return;
        }
        Packet<?> packet2 = event.packet;
        if (packet2 instanceof ServerboundPlayerActionPacket && (packet = (ServerboundPlayerActionPacket)packet2).getAction() == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
            this.mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, packet.getPos().above(), packet.getDirection()));
        }
    }

    private void removeHaste() {
        if (!Utils.canUpdate()) {
            return;
        }
        MobEffectInstance haste = this.mc.player.getEffect(MobEffects.HASTE);
        if (haste != null && !haste.showIcon()) {
            this.mc.player.removeEffect(MobEffects.HASTE);
        }
    }

    public boolean filter(Block block) {
        if (this.blocksFilter.get() == ListMode.Blacklist && !this.blocks.get().contains(block)) {
            return true;
        }
        return this.blocksFilter.get() == ListMode.Whitelist && this.blocks.get().contains(block);
    }

    public boolean instamine() {
        return this.isActive() && this.mode.get() == Mode.Damage && this.instamine.get() != false;
    }

    public static enum Mode {
        Normal,
        Haste,
        Damage;

    }

    public static enum ListMode {
        Whitelist,
        Blacklist;

    }
}
