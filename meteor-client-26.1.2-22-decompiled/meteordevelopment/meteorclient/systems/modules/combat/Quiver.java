package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.BlockBehaviourAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StatusEffectListSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Quiver
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgSafety;
    private final Setting<List<MobEffect>> effects;
    private final Setting<Integer> cooldown;
    private final Setting<Boolean> checkEffects;
    private final Setting<Boolean> silentBow;
    private final Setting<Boolean> chatInfo;
    private final Setting<Boolean> onlyInHoles;
    private final Setting<Boolean> onlyOnGround;
    private final Setting<Double> minHealth;
    private final List<Integer> arrowSlots;
    private FindItemResult bow;
    private boolean wasMainhand;
    private boolean wasHotbar;
    private int timer;
    private int prevSlot;
    private final BlockPos.MutableBlockPos testPos;

    public Quiver() {
        super(Categories.Combat, "quiver", "Shoots arrows at yourself.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgSafety = this.settings.createGroup("Safety");
        this.effects = this.sgGeneral.add(((StatusEffectListSetting.Builder)((StatusEffectListSetting.Builder)new StatusEffectListSetting.Builder().name("effects")).description("Which effects to shoot you with.")).defaultValue((MobEffect)MobEffects.STRENGTH.value()).build());
        this.cooldown = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("cooldown")).description("How many ticks between shooting effects (19 minimum for NCP).")).defaultValue(10)).range(0, 40).sliderRange(0, 40).build());
        this.checkEffects = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("check-effects")).description("Won't shoot you with effects you already have.")).defaultValue(true)).build());
        this.silentBow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("silent-bow")).description("Takes a bow from your inventory to quiver.")).defaultValue(true)).build());
        this.chatInfo = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("chat-info")).description("Sends info about quiver checks in chat.")).defaultValue(false)).build());
        this.onlyInHoles = this.sgSafety.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-in-holes")).description("Only quiver when you're in a hole.")).defaultValue(true)).build());
        this.onlyOnGround = this.sgSafety.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-on-ground")).description("Only quiver when you're on the ground.")).defaultValue(true)).build());
        this.minHealth = this.sgSafety.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("min-health")).description("How much health you must have to quiver.")).defaultValue(10.0).range(0.0, 36.0).sliderRange(0.0, 36.0).build());
        this.arrowSlots = new ArrayList<Integer>();
        this.testPos = new BlockPos.MutableBlockPos();
    }

    @Override
    public void onActivate() {
        this.bow = InvUtils.find(Items.BOW);
        if (!this.shouldQuiver()) {
            return;
        }
        this.mc.options.keyUse.setDown(false);
        this.mc.gameMode.releaseUsingItem((Player)this.mc.player);
        this.prevSlot = this.bow.slot();
        this.wasHotbar = this.bow.isHotbar();
        this.timer = 0;
        if (!this.bow.isMainHand()) {
            if (this.wasHotbar) {
                InvUtils.swap(this.bow.slot(), true);
            } else {
                InvUtils.move().from(this.mc.player.getInventory().getSelectedSlot()).to(this.prevSlot);
            }
        } else {
            this.wasMainhand = true;
        }
        this.arrowSlots.clear();
        ArrayList<MobEffect> usedEffects = new ArrayList<MobEffect>();
        for (int i = this.mc.player.getInventory().getContainerSize(); i > 0; --i) {
            Iterator effects;
            ItemStack item;
            if (i == this.mc.player.getInventory().getSelectedSlot() || (item = this.mc.player.getInventory().getItem(i)).getItem() != Items.TIPPED_ARROW || !(effects = ((PotionContents)item.getItem().components().get(DataComponents.POTION_CONTENTS)).getAllEffects().iterator()).hasNext()) continue;
            MobEffect effect = (MobEffect)((MobEffectInstance)effects.next()).getEffect().value();
            if (!this.effects.get().contains(effect) || usedEffects.contains(effect) || this.hasEffect(effect) && this.checkEffects.get().booleanValue()) continue;
            usedEffects.add(effect);
            this.arrowSlots.add(i);
        }
    }

    @Override
    public void onDeactivate() {
        if (!this.wasMainhand) {
            if (this.wasHotbar) {
                InvUtils.swapBack();
            } else {
                InvUtils.move().from(this.mc.player.getInventory().getSelectedSlot()).to(this.prevSlot);
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        this.bow = InvUtils.find(Items.BOW);
        if (!this.shouldQuiver()) {
            return;
        }
        if (this.arrowSlots.isEmpty()) {
            this.toggle();
            return;
        }
        if (this.timer > 0) {
            --this.timer;
            return;
        }
        boolean charging = this.mc.options.keyUse.isDown();
        if (!charging) {
            InvUtils.move().from(this.arrowSlots.getFirst()).to(9);
            this.mc.options.keyUse.setDown(true);
        } else if ((double)BowItem.getPowerForTime((int)this.mc.player.getTicksUsingItem()) >= 0.12) {
            int targetSlot = this.arrowSlots.getFirst();
            this.arrowSlots.removeFirst();
            this.mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Rot(this.mc.player.getYRot(), -90.0f, this.mc.player.onGround(), this.mc.player.horizontalCollision));
            this.mc.options.keyUse.setDown(false);
            this.mc.gameMode.releaseUsingItem((Player)this.mc.player);
            if (targetSlot != 9) {
                InvUtils.move().from(9).to(targetSlot);
            }
            this.timer = this.cooldown.get();
        }
    }

    private boolean shouldQuiver() {
        if (!this.bow.found() || !this.bow.isHotbar() && !this.silentBow.get().booleanValue()) {
            if (this.chatInfo.get().booleanValue()) {
                this.error("Couldn't find a usable bow, disabling.", new Object[0]);
            }
            this.toggle();
            return false;
        }
        if (!this.headIsOpen()) {
            if (this.chatInfo.get().booleanValue()) {
                this.error("Not enough space to quiver, disabling.", new Object[0]);
            }
            this.toggle();
            return false;
        }
        if ((double)EntityUtils.getTotalHealth((LivingEntity)this.mc.player) < this.minHealth.get()) {
            if (this.chatInfo.get().booleanValue()) {
                this.error("Not enough health to quiver, disabling.", new Object[0]);
            }
            this.toggle();
            return false;
        }
        if (this.onlyOnGround.get().booleanValue() && !this.mc.player.onGround()) {
            if (this.chatInfo.get().booleanValue()) {
                this.error("You are not on the ground, disabling.", new Object[0]);
            }
            this.toggle();
            return false;
        }
        if (this.onlyInHoles.get().booleanValue() && !this.isSurrounded((Player)this.mc.player)) {
            if (this.chatInfo.get().booleanValue()) {
                this.error("You are not in a hole, disabling.", new Object[0]);
            }
            this.toggle();
            return false;
        }
        return true;
    }

    private boolean headIsOpen() {
        this.testPos.set((Vec3i)this.mc.player.blockPosition().offset(0, 1, 0));
        BlockState pos1 = this.mc.level.getBlockState((BlockPos)this.testPos);
        if (((BlockBehaviourAccessor)pos1.getBlock()).meteor$isHasCollision()) {
            return false;
        }
        this.testPos.offset(0, 1, 0);
        BlockState pos2 = this.mc.level.getBlockState((BlockPos)this.testPos);
        return !((BlockBehaviourAccessor)pos2.getBlock()).meteor$isHasCollision();
    }

    private boolean hasEffect(MobEffect effect) {
        for (MobEffectInstance statusEffect : this.mc.player.getActiveEffects()) {
            if (!((MobEffect)statusEffect.getEffect().value()).equals(effect)) continue;
            return true;
        }
        return false;
    }

    private boolean isSurrounded(Player target) {
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP || dir == Direction.DOWN) continue;
            this.testPos.set((Vec3i)target.blockPosition()).relative(dir);
            Block block = this.mc.level.getBlockState((BlockPos)this.testPos).getBlock();
            if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK || block == Blocks.RESPAWN_ANCHOR || block == Blocks.CRYING_OBSIDIAN || block == Blocks.NETHERITE_BLOCK) continue;
            return false;
        }
        return true;
    }
}
