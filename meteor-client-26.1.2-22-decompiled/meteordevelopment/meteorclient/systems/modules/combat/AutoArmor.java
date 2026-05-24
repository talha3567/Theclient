package meteordevelopment.meteorclient.systems.modules.combat;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnchantmentListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.ChestSwap;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.Equippable;

public class AutoArmor
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Protection> preferredProtection;
    private final Setting<Integer> delay;
    private final Setting<Set<ResourceKey<Enchantment>>> avoidedEnchantments;
    private final Setting<Boolean> blastLeggings;
    private final Setting<Boolean> antiBreak;
    private final Setting<Boolean> ignoreElytra;
    private final Object2IntMap<Holder<Enchantment>> enchantments;
    private final ArmorPiece[] armorPieces;
    private final ArmorPiece helmet;
    private final ArmorPiece chestplate;
    private final ArmorPiece leggings;
    private final ArmorPiece boots;
    private int timer;

    public AutoArmor() {
        super(Categories.Combat, "auto-armor", "Automatically equips armor.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.preferredProtection = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("preferred-protection")).description("Which type of protection to prefer.")).defaultValue(Protection.Protection)).build());
        this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("swap-delay")).description("The delay between equipping armor pieces.")).defaultValue(1)).min(0).sliderMax(5).build());
        this.avoidedEnchantments = this.sgGeneral.add(((EnchantmentListSetting.Builder)((EnchantmentListSetting.Builder)new EnchantmentListSetting.Builder().name("avoided-enchantments")).description("Enchantments that should be avoided.")).defaultValue(Enchantments.BINDING_CURSE, Enchantments.FROST_WALKER).build());
        this.blastLeggings = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("blast-prot-leggings")).description("Uses blast protection for leggings regardless of preferred protection.")).defaultValue(true)).build());
        this.antiBreak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-break")).description("Takes off armor if it is about to break.")).defaultValue(false)).build());
        this.ignoreElytra = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-elytra")).description("Will not replace your elytra if you have it equipped.")).defaultValue(true)).build());
        this.enchantments = new Object2IntOpenHashMap();
        this.armorPieces = new ArmorPiece[4];
        this.helmet = new ArmorPiece(this, EquipmentSlot.HEAD);
        this.chestplate = new ArmorPiece(this, EquipmentSlot.CHEST);
        this.leggings = new ArmorPiece(this, EquipmentSlot.LEGS);
        this.boots = new ArmorPiece(this, EquipmentSlot.FEET);
        this.armorPieces[0] = this.helmet;
        this.armorPieces[1] = this.chestplate;
        this.armorPieces[2] = this.leggings;
        this.armorPieces[3] = this.boots;
    }

    @Override
    public void onActivate() {
        this.timer = 0;
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (this.timer > 0) {
            --this.timer;
            return;
        }
        for (ArmorPiece armorPiece : this.armorPieces) {
            armorPiece.reset();
        }
        block7: for (int i = 0; i < this.mc.player.getInventory().getNonEquipmentItems().size(); ++i) {
            ItemStack itemStack = this.mc.player.getInventory().getItem(i);
            if (itemStack.isEmpty() || !this.isArmor(itemStack) || this.antiBreak.get().booleanValue() && itemStack.isDamageableItem() && itemStack.getMaxDamage() - itemStack.getDamageValue() <= 10) continue;
            Utils.getEnchantments(itemStack, this.enchantments);
            if (this.hasAvoidedEnchantment()) continue;
            switch (this.getItemSlotId(itemStack)) {
                case 0: {
                    this.boots.add(itemStack, i);
                    continue block7;
                }
                case 1: {
                    this.leggings.add(itemStack, i);
                    continue block7;
                }
                case 2: {
                    this.chestplate.add(itemStack, i);
                    continue block7;
                }
                case 3: {
                    this.helmet.add(itemStack, i);
                }
            }
        }
        for (ArmorPiece armorPiece : this.armorPieces) {
            armorPiece.calculate();
        }
        Arrays.sort(this.armorPieces, Comparator.comparingInt(ArmorPiece::getSortScore));
        for (ArmorPiece armorPiece : this.armorPieces) {
            armorPiece.apply();
        }
    }

    private boolean hasAvoidedEnchantment() {
        for (Holder enchantment : this.enchantments.keySet()) {
            if (!enchantment.is(this.avoidedEnchantments.get()::contains)) continue;
            return true;
        }
        return false;
    }

    private int getItemSlotId(ItemStack itemStack) {
        if (itemStack.has(DataComponents.GLIDER)) {
            return 2;
        }
        return ((Equippable)itemStack.get(DataComponents.EQUIPPABLE)).slot().getIndex();
    }

    private int getScore(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        }
        int score = 0;
        ResourceKey protection = this.preferredProtection.get().enchantment;
        if (this.isArmor(itemStack) && this.blastLeggings.get().booleanValue() && this.getItemSlotId(itemStack) == 1) {
            protection = Enchantments.BLAST_PROTECTION;
        }
        score += 3 * Utils.getEnchantmentLevel(this.enchantments, protection);
        score += Utils.getEnchantmentLevel(this.enchantments, (ResourceKey<Enchantment>)Enchantments.PROTECTION);
        score += Utils.getEnchantmentLevel(this.enchantments, (ResourceKey<Enchantment>)Enchantments.BLAST_PROTECTION);
        score += Utils.getEnchantmentLevel(this.enchantments, (ResourceKey<Enchantment>)Enchantments.FIRE_PROTECTION);
        score += Utils.getEnchantmentLevel(this.enchantments, (ResourceKey<Enchantment>)Enchantments.PROJECTILE_PROTECTION);
        score += Utils.getEnchantmentLevel(this.enchantments, (ResourceKey<Enchantment>)Enchantments.UNBREAKING);
        score += 2 * Utils.getEnchantmentLevel(this.enchantments, (ResourceKey<Enchantment>)Enchantments.MENDING);
        if (itemStack.has(DataComponents.ATTRIBUTE_MODIFIERS)) {
            ItemAttributeModifiers component = (ItemAttributeModifiers)itemStack.get(DataComponents.ATTRIBUTE_MODIFIERS);
            for (ItemAttributeModifiers.Entry modifier : component.modifiers()) {
                if (modifier.attribute() != Attributes.ARMOR && modifier.attribute() != Attributes.ARMOR_TOUGHNESS) continue;
                double e = modifier.modifier().amount();
                score += (switch (modifier.modifier().operation()) {
                    default -> throw new MatchException(null, null);
                    case AttributeModifier.Operation.ADD_VALUE -> (int)e;
                    case AttributeModifier.Operation.ADD_MULTIPLIED_BASE -> (int)(e * this.mc.player.getAttributeBaseValue(modifier.attribute()));
                    case AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL -> (int)(e * (double)score);
                });
            }
        }
        return score;
    }

    private boolean cannotSwap() {
        return this.timer > 0;
    }

    private void swap(int from, int armorSlotId) {
        InvUtils.move().from(from).toArmor(armorSlotId);
        this.timer = this.delay.get();
    }

    private void moveToEmpty(int armorSlotId) {
        for (int i = 0; i < this.mc.player.getInventory().getNonEquipmentItems().size(); ++i) {
            if (!this.mc.player.getInventory().getItem(i).isEmpty()) continue;
            InvUtils.move().fromArmor(armorSlotId).to(i);
            this.timer = this.delay.get();
            break;
        }
    }

    private boolean isArmor(ItemStack itemStack) {
        return itemStack.is(ItemTags.FOOT_ARMOR) || itemStack.is(ItemTags.LEG_ARMOR) || itemStack.is(ItemTags.CHEST_ARMOR) || itemStack.is(ItemTags.HEAD_ARMOR);
    }

    public static enum Protection {
        Protection((ResourceKey<Enchantment>)Enchantments.PROTECTION),
        BlastProtection((ResourceKey<Enchantment>)Enchantments.BLAST_PROTECTION),
        FireProtection((ResourceKey<Enchantment>)Enchantments.FIRE_PROTECTION),
        ProjectileProtection((ResourceKey<Enchantment>)Enchantments.PROJECTILE_PROTECTION);

        private final ResourceKey<Enchantment> enchantment;

        private Protection(ResourceKey<Enchantment> enchantment) {
            this.enchantment = enchantment;
        }
    }

    private class ArmorPiece {
        private final EquipmentSlot slot;
        private int bestSlot;
        private int bestScore;
        private int score;
        private int durability;
        final /* synthetic */ AutoArmor this$0;

        public ArmorPiece(AutoArmor autoArmor, EquipmentSlot slot) {
            AutoArmor autoArmor2 = autoArmor;
            Objects.requireNonNull(autoArmor2);
            this.this$0 = autoArmor2;
            this.slot = slot;
        }

        public void reset() {
            this.bestSlot = -1;
            this.bestScore = -1;
            this.score = -1;
            this.durability = Integer.MAX_VALUE;
        }

        public void add(ItemStack itemStack, int slot) {
            int score = this.this$0.getScore(itemStack);
            if (score > this.bestScore) {
                this.bestScore = score;
                this.bestSlot = slot;
            }
        }

        public void calculate() {
            if (this.this$0.cannotSwap()) {
                return;
            }
            ItemStack itemStack = ((AutoArmor)this.this$0).mc.player.getItemBySlot(this.slot);
            if ((this.this$0.ignoreElytra.get().booleanValue() || Modules.get().isActive(ChestSwap.class)) && itemStack.getItem() == Items.ELYTRA) {
                this.score = Integer.MAX_VALUE;
                return;
            }
            Utils.getEnchantments(itemStack, this.this$0.enchantments);
            if (this.this$0.enchantments.containsKey((Object)Enchantments.BINDING_CURSE)) {
                this.score = Integer.MAX_VALUE;
                return;
            }
            this.score = this.this$0.getScore(itemStack);
            this.score = this.decreaseScoreByAvoidedEnchantments(this.score);
            this.score = this.applyAntiBreakScore(this.score, itemStack);
            if (!itemStack.isEmpty()) {
                this.durability = itemStack.getMaxDamage() - itemStack.getDamageValue();
            }
        }

        public int getSortScore() {
            if (this.this$0.antiBreak.get().booleanValue() && this.durability <= 10) {
                return -1;
            }
            return this.bestScore;
        }

        public void apply() {
            if (this.this$0.cannotSwap() || this.score == Integer.MAX_VALUE) {
                return;
            }
            if (this.bestScore > this.score) {
                this.this$0.swap(this.bestSlot, this.slot.getIndex());
            } else if (this.this$0.antiBreak.get().booleanValue() && this.durability <= 10) {
                this.this$0.moveToEmpty(this.slot.getIndex());
            }
        }

        private int decreaseScoreByAvoidedEnchantments(int score) {
            for (ResourceKey<Enchantment> enchantment : this.this$0.avoidedEnchantments.get()) {
                score -= 2 * this.this$0.enchantments.getInt(enchantment);
            }
            return score;
        }

        private int applyAntiBreakScore(int score, ItemStack itemStack) {
            if (this.this$0.antiBreak.get().booleanValue() && itemStack.isDamageableItem() && itemStack.getMaxDamage() - itemStack.getDamageValue() <= 10) {
                return -1;
            }
            return score;
        }
    }
}
