package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class AutoEXP
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;
    private final Setting<Boolean> replenish;
    private final Setting<Boolean> onlyGround;
    private final Setting<Integer> slot;
    private final Setting<Integer> minThreshold;
    private final Setting<Integer> maxThreshold;
    private int repairingI;

    public AutoEXP() {
        super(Categories.Combat, "auto-exp", "Automatically repairs your armor and tools in pvp.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Which items to repair.")).defaultValue(Mode.Both)).build());
        this.replenish = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("replenish")).description("Automatically replenishes exp into a selected hotbar slot.")).defaultValue(true)).build());
        this.onlyGround = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-on-ground")).description("Only throw when the player is on the ground.")).defaultValue(false)).build());
        this.slot = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("exp-slot")).description("The slot to replenish exp into.")).visible(this.replenish::get)).defaultValue(6)).range(1, 9).sliderRange(1, 9).build());
        this.minThreshold = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("min-threshold")).description("The minimum durability percentage that an item needs to fall to, to be repaired.")).defaultValue(30)).range(1, 100).sliderRange(1, 100).build());
        this.maxThreshold = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("max-threshold")).description("The maximum durability percentage to repair items to.")).defaultValue(80)).range(1, 100).sliderRange(1, 100).build());
    }

    @Override
    public void onActivate() {
        this.repairingI = -1;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.onlyGround.get().booleanValue() && !this.mc.player.onGround()) {
            return;
        }
        if (this.repairingI == -1) {
            if (this.mode.get() != Mode.Hands) {
                for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
                    ItemStack stack = this.mc.player.getItemBySlot(slot);
                    if (!this.needsRepair(stack, this.minThreshold.get().intValue())) continue;
                    this.repairingI = 36 + slot.getIndex();
                    break;
                }
            }
            if (this.mode.get() != Mode.Armor && this.repairingI == -1) {
                for (InteractionHand hand : InteractionHand.values()) {
                    if (!this.needsRepair(this.mc.player.getItemInHand(hand), this.minThreshold.get().intValue())) continue;
                    this.repairingI = hand == InteractionHand.MAIN_HAND ? this.mc.player.getInventory().getSelectedSlot() : 40;
                    break;
                }
            }
        }
        if (this.repairingI != -1) {
            if (!this.needsRepair(this.mc.player.getInventory().getItem(this.repairingI), this.maxThreshold.get().intValue())) {
                this.repairingI = -1;
                return;
            }
            FindItemResult exp = InvUtils.find(Items.EXPERIENCE_BOTTLE);
            if (exp.found()) {
                if (!exp.isHotbar() && !exp.isOffhand()) {
                    if (!this.replenish.get().booleanValue()) {
                        return;
                    }
                    InvUtils.move().from(exp.slot()).toHotbar(this.slot.get() - 1);
                }
                Rotations.rotate((double)this.mc.player.getYRot(), 90.0, () -> {
                    if (exp.getHand() != null) {
                        this.mc.gameMode.useItem((Player)this.mc.player, exp.getHand());
                    } else {
                        InvUtils.swap(exp.slot(), true);
                        this.mc.gameMode.useItem((Player)this.mc.player, InteractionHand.MAIN_HAND);
                        InvUtils.swapBack();
                    }
                });
            }
        }
    }

    private boolean needsRepair(ItemStack itemStack, double threshold) {
        if (itemStack.isEmpty() || !Utils.hasEnchantments(itemStack, Enchantments.MENDING)) {
            return false;
        }
        return (double)(itemStack.getMaxDamage() - itemStack.getDamageValue()) / (double)itemStack.getMaxDamage() * 100.0 <= threshold;
    }

    public static enum Mode {
        Armor,
        Hands,
        Both;

    }
}
