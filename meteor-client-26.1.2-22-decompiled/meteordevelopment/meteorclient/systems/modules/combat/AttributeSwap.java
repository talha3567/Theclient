package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.entity.player.DoAttackEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AttributeSwap
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgSwappingOptions;
    private final SettingGroup sgSwordEnchants;
    private final SettingGroup sgMaceEnchants;
    private final SettingGroup sgSpearEnchants;
    private final SettingGroup sgOtherEnchants;
    private final SettingGroup sgWeapon;
    private final Setting<Mode> mode;
    private final Setting<Integer> targetSlot;
    private final Setting<Boolean> swapOnMiss;
    private final Setting<Boolean> swapBack;
    private final Setting<Integer> swapBackDelay;
    private final Setting<Boolean> smartShieldBreak;
    private final Setting<Boolean> smartDurability;
    private final Setting<Boolean> swordSwapping;
    private final Setting<Boolean> maceSwapping;
    private final Setting<Boolean> spearSwapping;
    private final Setting<Boolean> otherSwapping;
    private final Setting<Boolean> enchantFireAspect;
    private final Setting<Boolean> enchantLooting;
    private final Setting<Boolean> enchantSharpness;
    private final Setting<Boolean> enchantSmite;
    private final Setting<Boolean> enchantBaneOfArthropods;
    private final Setting<Boolean> enchantSweepingEdge;
    private final Setting<Boolean> regularMace;
    private final Setting<Boolean> enchantDensity;
    private final Setting<Boolean> enchantBreach;
    private final Setting<Boolean> enchantWindBurst;
    private final Setting<Boolean> enchantImpaling;
    private final Setting<Boolean> enchantLunge;
    private final Setting<Boolean> spearHitbox;
    private final Setting<Boolean> excludeLungeFromHitbox;
    private final Setting<Boolean> onlyOnWeapon;
    private final Setting<Boolean> sword;
    private final Setting<Boolean> axe;
    private final Setting<Boolean> pickaxe;
    private final Setting<Boolean> shovel;
    private final Setting<Boolean> hoe;
    private final Setting<Boolean> mace;
    private final Setting<Boolean> trident;
    private int backTimer;
    private boolean awaitingBack;

    public AttributeSwap() {
        super(Categories.Combat, "attribute-swap", "Swaps to a target slot when you attack.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgSwappingOptions = this.settings.createGroup("Swapping Options");
        this.sgSwordEnchants = this.settings.createGroup("Sword Enchants");
        this.sgMaceEnchants = this.settings.createGroup("Mace Enchants");
        this.sgSpearEnchants = this.settings.createGroup("Spear Enchants");
        this.sgOtherEnchants = this.settings.createGroup("Other Enchants");
        this.sgWeapon = this.settings.createGroup("Weapon Options");
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("The mode to use.")).defaultValue(Mode.Simple)).build());
        this.targetSlot = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("target-slot")).description("Hotbar slot to swap to (1-9).")).defaultValue(1)).range(1, 9).visible(() -> this.mode.get() == Mode.Simple)).build());
        this.swapOnMiss = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swap-on-miss")).description("Whether to swap on a missed attack. Useful for quickly lunging with spears.")).defaultValue(false)).visible(() -> this.mode.get() == Mode.Simple)).build());
        this.swapBack = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swap-back")).description("Swap back to the original slot after a delay.")).defaultValue(true)).build());
        this.swapBackDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("swap-back-delay")).description("Delay in ticks before swapping back.")).defaultValue(2)).min(0).max(100).sliderRange(0, 20).visible(this.swapBack::get)).build());
        this.smartShieldBreak = this.sgSwappingOptions.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("shield-breaker")).description("Automatically swaps to an axe if the target is blocking.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart)).build());
        this.smartDurability = this.sgSwappingOptions.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("durability-saver")).description("Swaps to a non-damageable item to save durability on the main weapon.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart)).build());
        this.swordSwapping = this.sgSwappingOptions.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sword-swapping")).description("Enables smart swapping for sword enchantments.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart)).build());
        this.maceSwapping = this.sgSwappingOptions.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("mace-swapping")).description("Enables smart swapping for mace enchantments.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart)).build());
        this.spearSwapping = this.sgSwappingOptions.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("spear-swapping")).description("Enables smart swapping for spear enchantments.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart)).build());
        this.otherSwapping = this.sgSwappingOptions.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("other-swapping")).description("Enables smart swapping for other enchantments like Impaling.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart)).build());
        this.enchantFireAspect = this.sgSwordEnchants.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fire-aspect")).description("Swaps to an item with Fire Aspect to set the target on fire, if target isn't already on fire")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart && this.swordSwapping.get() != false)).build());
        this.enchantLooting = this.sgSwordEnchants.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("looting")).description("Swaps to an item with Looting for better drops or more experience. Only prefers for mobs (but fire aspect is priority)")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart && this.swordSwapping.get() != false)).build());
        this.enchantSharpness = this.sgSwordEnchants.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sharpness")).description("Swaps to an item with Sharpness for increased damage against all entities.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart && this.swordSwapping.get() != false)).build());
        this.enchantSmite = this.sgSwordEnchants.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("smite")).description("Swaps to an item with Smite for increased damage against undead mobs.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart && this.swordSwapping.get() != false)).build());
        this.enchantBaneOfArthropods = this.sgSwordEnchants.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("bane-of-arthropods")).description("Swaps to an item with Bane of Arthropods for increased damage against arthropods.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart && this.swordSwapping.get() != false)).build());
        this.enchantSweepingEdge = this.sgSwordEnchants.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sweeping-edge")).description("Swaps to an item with Sweeping Edge for increased sweeping attack damage.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart && this.swordSwapping.get() != false)).build());
        this.regularMace = this.sgMaceEnchants.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("regular-mace")).description("Swaps to a regular Mace when falling if no better option is available.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart && this.maceSwapping.get() != false)).build());
        this.enchantDensity = this.sgMaceEnchants.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("density")).description("Swaps to a Mace with Density to deal increased damage when falling.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart && this.maceSwapping.get() != false)).build());
        this.enchantBreach = this.sgMaceEnchants.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("breach")).description("Swaps to a Mace with Breach to reduce the target's armor effectiveness.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart && this.maceSwapping.get() != false)).build());
        this.enchantWindBurst = this.sgMaceEnchants.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("wind-burst")).description("Swaps to a Mace with Wind Burst to launch up when hitting while falling.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart && this.maceSwapping.get() != false)).build());
        this.enchantImpaling = this.sgOtherEnchants.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("impaling")).description("Swaps to an item with Impaling for increased damage against aquatic mobs.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart && this.otherSwapping.get() != false)).build());
        this.enchantLunge = this.sgSpearEnchants.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("lunge")).description("Swaps to a spear with Lunge for traveling.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart && this.spearSwapping.get() != false)).build());
        this.spearHitbox = this.sgSpearEnchants.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("hitbox")).description("Swaps to a spear for extended reach when target is far.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart && this.spearSwapping.get() != false)).build());
        this.excludeLungeFromHitbox = this.sgSpearEnchants.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("exclude-lunge-from-hitbox")).description("Don't use lunge-enchanted spears for hitbox extension.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Smart && this.spearSwapping.get() != false && this.spearHitbox.get() != false)).build());
        this.onlyOnWeapon = this.sgWeapon.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-on-weapon")).description("Only swaps when holding a selected weapon in hand.")).defaultValue(false)).build());
        this.sword = this.sgWeapon.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sword")).description("Works while holding a sword.")).defaultValue(true)).visible(this.onlyOnWeapon::get)).build());
        this.axe = this.sgWeapon.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("axe")).description("Works while holding an axe.")).defaultValue(true)).visible(this.onlyOnWeapon::get)).build());
        this.pickaxe = this.sgWeapon.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pickaxe")).description("Works while holding a pickaxe.")).defaultValue(true)).visible(this.onlyOnWeapon::get)).build());
        this.shovel = this.sgWeapon.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("shovel")).description("Works while holding a shovel.")).defaultValue(true)).visible(this.onlyOnWeapon::get)).build());
        this.hoe = this.sgWeapon.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("hoe")).description("Works while holding a hoe.")).defaultValue(true)).visible(this.onlyOnWeapon::get)).build());
        this.mace = this.sgWeapon.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("mace")).description("Works while holding a mace.")).defaultValue(true)).visible(this.onlyOnWeapon::get)).build());
        this.trident = this.sgWeapon.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("trident")).description("Works while holding a trident.")).defaultValue(true)).visible(this.onlyOnWeapon::get)).build());
    }

    @Override
    public void onDeactivate() {
        this.backTimer = 0;
        this.awaitingBack = false;
    }

    @EventHandler
    private void onAttack(DoAttackEvent event) {
        if (this.mc.hitResult.getType() == HitResult.Type.BLOCK || !this.canSwapByWeapon()) {
            return;
        }
        if (this.mode.get() == Mode.Smart && this.spearSwapping.get().booleanValue()) {
            int lungeSlot;
            Entity target;
            if (this.spearHitbox.get().booleanValue() && (target = this.getTargetEntity()) != null) {
                if ((double)this.mc.player.distanceTo(target) <= this.mc.player.entityInteractionRange() + 0.5) {
                    return;
                }
                int spearSlot = this.getSmartSpearSlot(false);
                if (spearSlot != -1) {
                    this.doSwap(spearSlot);
                    return;
                }
            }
            if (this.enchantLunge.get().booleanValue() && (lungeSlot = this.getSmartSpearSlot(true)) != -1) {
                this.doSwap(lungeSlot);
                return;
            }
        }
        if (this.mode.get() == Mode.Smart || !this.swapOnMiss.get().booleanValue()) {
            return;
        }
        this.doSwap(this.targetSlot.get() - 1);
    }

    @EventHandler
    private void onAttackEntity(AttackEntityEvent event) {
        if (!this.canSwapByWeapon() || this.mode.get() == Mode.Simple && this.swapOnMiss.get().booleanValue()) {
            return;
        }
        this.performSwap(event.entity);
    }

    private void performSwap(Entity target) {
        if (this.awaitingBack) {
            return;
        }
        if (this.mode.get() == Mode.Simple) {
            this.doSwap(this.targetSlot.get() - 1);
        } else {
            this.doSwap(this.getSmartSlot(target));
        }
    }

    private void doSwap(int slotIndex) {
        if (this.awaitingBack) {
            return;
        }
        if (slotIndex < 0 || slotIndex > 8) {
            return;
        }
        if (slotIndex == this.mc.player.getInventory().getSelectedSlot()) {
            return;
        }
        if (!InvUtils.swap(slotIndex, this.swapBack.get())) {
            return;
        }
        this.awaitingBack = this.swapBack.get();
        if (this.awaitingBack) {
            this.backTimer = this.swapBackDelay.get();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!this.awaitingBack) {
            return;
        }
        if (this.backTimer-- > 0) {
            return;
        }
        InvUtils.swapBack();
        this.awaitingBack = false;
    }

    private boolean canSwapByWeapon() {
        if (!this.onlyOnWeapon.get().booleanValue()) {
            return true;
        }
        return InvUtils.testInMainHand(item -> this.sword.get() != false && item.is(ItemTags.SWORDS) || this.axe.get() != false && item.is(ItemTags.AXES) || this.pickaxe.get() != false && item.is(ItemTags.PICKAXES) || this.shovel.get() != false && item.is(ItemTags.SHOVELS) || this.hoe.get() != false && item.is(ItemTags.HOES) || this.mace.get() != false && item.getItem() instanceof MaceItem || this.trident.get() != false && item.getItem() instanceof TridentItem);
    }

    private int getSmartSlot(Entity target) {
        LivingEntity living;
        ItemStack currentStack = this.mc.player.getMainHandItem();
        if (target != null && this.smartShieldBreak.get().booleanValue() && target instanceof LivingEntity && (living = (LivingEntity)target).isBlocking()) {
            if (currentStack.getItem() instanceof AxeItem) {
                return -1;
            }
            int axeSlot = InvUtils.findInHotbar(item -> item.getItem() instanceof AxeItem).slot();
            if (axeSlot != -1) {
                return axeSlot;
            }
        }
        boolean isFalling = this.mc.player.fallDistance > 1.5;
        boolean durability = this.smartDurability.get();
        boolean isLiving = target instanceof LivingEntity;
        boolean isPlayer = target instanceof Player;
        boolean isOnFire = target != null && target.isOnFire();
        boolean isUndead = target != null && target.typeHolder().is(EntityTypeTags.SENSITIVE_TO_SMITE);
        boolean isArthropod = target != null && target.typeHolder().is(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS);
        boolean isAquatic = target != null && target.typeHolder().is(EntityTypeTags.SENSITIVE_TO_IMPALING);
        boolean hasFireResistance = isLiving && (((LivingEntity)target).hasEffect(MobEffects.FIRE_RESISTANCE) || this.hasFireProtectionArmor((LivingEntity)target));
        double armor = isLiving ? ((LivingEntity)target).getAttributeValue(Attributes.ARMOR) : 0.0;
        float health = isLiving ? ((LivingEntity)target).getHealth() : 0.0f;
        int bestSlot = -1;
        double bestScore = this.getItemScore(currentStack, isFalling, durability, isLiving, isPlayer, isOnFire, hasFireResistance, isUndead, isArthropod, isAquatic, armor, health);
        for (int i = 0; i < 9; ++i) {
            double score;
            ItemStack stack;
            if (i == this.mc.player.getInventory().getSelectedSlot() || (stack = this.mc.player.getInventory().getItem(i)).isEmpty() && !durability || !((score = this.getItemScore(stack, isFalling, durability, isLiving, isPlayer, isOnFire, hasFireResistance, isUndead, isArthropod, isAquatic, armor, health)) > bestScore)) continue;
            bestScore = score;
            bestSlot = i;
        }
        return bestSlot;
    }

    private int getSmartSpearSlot(boolean requireLunge) {
        for (int i = 0; i < 9; ++i) {
            boolean hasLunge;
            ItemStack stack;
            if (i == this.mc.player.getInventory().getSelectedSlot() || !(stack = this.mc.player.getInventory().getItem(i)).is(ItemTags.SPEARS)) continue;
            boolean bl = hasLunge = Utils.getEnchantmentLevel(stack, (ResourceKey<Enchantment>)Enchantments.LUNGE) > 0;
            if (requireLunge && !hasLunge || !requireLunge && this.excludeLungeFromHitbox.get().booleanValue() && hasLunge) continue;
            return i;
        }
        return -1;
    }

    private Entity getTargetEntity() {
        double maxDistance = 7.0;
        Vec3 start = this.mc.player.getEyePosition(1.0f);
        Vec3 look = this.mc.player.getViewVector(1.0f);
        Vec3 end = start.add(look.scale(maxDistance));
        AABB box = this.mc.player.getBoundingBox().expandTowards(look.scale(maxDistance)).inflate(1.0);
        Entity target = null;
        double closestDistance = maxDistance * maxDistance;
        for (Entity entity : this.mc.level.getEntities((Entity)this.mc.player, box, e -> !e.isSpectator() && e.isPickable())) {
            double distSq;
            AABB expandedBox = entity.getBoundingBox().inflate(0.15);
            if (!expandedBox.clip(start, end).isPresent() || !((distSq = start.distanceToSqr(entity.getX(), entity.getY(), entity.getZ())) < closestDistance)) continue;
            closestDistance = distSq;
            target = entity;
        }
        return target;
    }

    private double getItemScore(ItemStack stack, boolean isFalling, boolean durability, boolean isLiving, boolean isPlayer, boolean isOnFire, boolean hasFireResistance, boolean isUndead, boolean isArthropod, boolean isAquatic, double armor, float health) {
        double score = 0.0;
        if (durability) {
            score += this.getDurabilityScore(stack);
        }
        if (stack.isEmpty()) {
            return score;
        }
        return score += this.getCombatScore(stack, isFalling, isLiving, isPlayer, isOnFire, hasFireResistance, isUndead, isArthropod, isAquatic, armor, health);
    }

    private double getDurabilityScore(ItemStack stack) {
        if (!stack.isDamageableItem()) {
            return 4.0;
        }
        int unbreaking = Utils.getEnchantmentLevel(stack, (ResourceKey<Enchantment>)Enchantments.UNBREAKING);
        if (unbreaking > 0) {
            return (double)unbreaking * 0.05;
        }
        return 0.0;
    }

    private double getCombatScore(ItemStack stack, boolean isFalling, boolean isLiving, boolean isPlayer, boolean isOnFire, boolean hasFireResistance, boolean isUndead, boolean isArthropod, boolean isAquatic, double armor, float health) {
        double score = 0.0;
        if (this.swordSwapping.get().booleanValue()) {
            score += this.getFireAspectScore(stack, isOnFire, hasFireResistance);
            score += this.getLootingScore(stack, isPlayer, isLiving, isOnFire, health);
            score += this.getSharpnessScore(stack, isOnFire);
            score += this.getSmiteScore(stack, isUndead, isOnFire);
            score += this.getBaneOfArthropodsScore(stack, isArthropod, isOnFire);
            score += this.getSweepingEdgeScore(stack);
        }
        if (this.maceSwapping.get().booleanValue()) {
            score += this.getBreachScore(stack, isLiving, armor);
            score += this.getDensityScore(stack, isFalling);
            score += this.getWindBurstScore(stack, isFalling);
            score += this.getMaceScore(stack, isFalling);
        }
        if (this.otherSwapping.get().booleanValue()) {
            score += this.getImpalingScore(stack, isAquatic);
        }
        return score;
    }

    private double getFireAspectScore(ItemStack stack, boolean isOnFire, boolean hasFireResistance) {
        if (!this.enchantFireAspect.get().booleanValue() || isOnFire || hasFireResistance) {
            return 0.0;
        }
        int level = Utils.getEnchantmentLevel(stack, (ResourceKey<Enchantment>)Enchantments.FIRE_ASPECT);
        return level > 0 ? 30.0 : 0.0;
    }

    private double getLootingScore(ItemStack stack, boolean isPlayer, boolean isLiving, boolean isOnFire, float health) {
        if (!this.enchantLooting.get().booleanValue() || isPlayer) {
            return 0.0;
        }
        int level = Utils.getEnchantmentLevel(stack, (ResourceKey<Enchantment>)Enchantments.LOOTING);
        if (level > 0) {
            boolean execute = isLiving && health < 20.0f || isOnFire;
            return level * (execute ? 10 : 5);
        }
        return 0.0;
    }

    private double getSharpnessScore(ItemStack stack, boolean isOnFire) {
        if (!this.enchantSharpness.get().booleanValue()) {
            return 0.0;
        }
        int level = Utils.getEnchantmentLevel(stack, (ResourceKey<Enchantment>)Enchantments.SHARPNESS);
        if (level > 0) {
            double baseScore = (1.0 + 0.5 * (double)(level - 1)) * 3.0;
            return isOnFire ? baseScore * 1.5 : baseScore;
        }
        return 0.0;
    }

    private double getSmiteScore(ItemStack stack, boolean isUndead, boolean isOnFire) {
        if (!this.enchantSmite.get().booleanValue() || !isUndead) {
            return 0.0;
        }
        int level = Utils.getEnchantmentLevel(stack, (ResourceKey<Enchantment>)Enchantments.SMITE);
        if (level > 0) {
            double baseScore = level * 5;
            return isOnFire ? baseScore * 1.5 : baseScore;
        }
        return 0.0;
    }

    private double getBaneOfArthropodsScore(ItemStack stack, boolean isArthropod, boolean isOnFire) {
        if (!this.enchantBaneOfArthropods.get().booleanValue() || !isArthropod) {
            return 0.0;
        }
        int level = Utils.getEnchantmentLevel(stack, (ResourceKey<Enchantment>)Enchantments.BANE_OF_ARTHROPODS);
        if (level > 0) {
            double baseScore = level * 5;
            return isOnFire ? baseScore * 1.5 : baseScore;
        }
        return 0.0;
    }

    private double getSweepingEdgeScore(ItemStack stack) {
        if (!this.enchantSweepingEdge.get().booleanValue()) {
            return 0.0;
        }
        int level = Utils.getEnchantmentLevel(stack, (ResourceKey<Enchantment>)Enchantments.SWEEPING_EDGE);
        if (level > 0) {
            return level * 3;
        }
        return 0.0;
    }

    private double getImpalingScore(ItemStack stack, boolean isAquatic) {
        if (!this.enchantImpaling.get().booleanValue() || !isAquatic) {
            return 0.0;
        }
        int level = Utils.getEnchantmentLevel(stack, (ResourceKey<Enchantment>)Enchantments.IMPALING);
        if (level > 0) {
            return level * 5;
        }
        return 0.0;
    }

    private double getBreachScore(ItemStack stack, boolean isLiving, double armor) {
        if (!this.enchantBreach.get().booleanValue() || !isLiving || armor <= 0.0) {
            return 0.0;
        }
        int level = Utils.getEnchantmentLevel(stack, (ResourceKey<Enchantment>)Enchantments.BREACH);
        if (level > 0) {
            return (double)level * armor * 0.3;
        }
        return 0.0;
    }

    private double getDensityScore(ItemStack stack, boolean isFalling) {
        if (!this.enchantDensity.get().booleanValue() || !isFalling) {
            return 0.0;
        }
        int level = Utils.getEnchantmentLevel(stack, (ResourceKey<Enchantment>)Enchantments.DENSITY);
        if (level > 0) {
            return 50.0 + (double)level * this.mc.player.fallDistance * 2.0;
        }
        return 0.0;
    }

    private double getWindBurstScore(ItemStack stack, boolean isFalling) {
        if (!this.enchantWindBurst.get().booleanValue() || !isFalling) {
            return 0.0;
        }
        int level = Utils.getEnchantmentLevel(stack, (ResourceKey<Enchantment>)Enchantments.WIND_BURST);
        if (level > 0) {
            return level * 20;
        }
        return 0.0;
    }

    private double getMaceScore(ItemStack stack, boolean isFalling) {
        if (!this.regularMace.get().booleanValue() || !isFalling) {
            return 0.0;
        }
        if (stack.getItem() instanceof MaceItem) {
            return 40.0;
        }
        return 0.0;
    }

    private boolean hasFireProtectionArmor(LivingEntity entity) {
        for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
            int fireProtection;
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty() || (fireProtection = Utils.getEnchantmentLevel(stack, (ResourceKey<Enchantment>)Enchantments.FIRE_PROTECTION)) <= 0) continue;
            return true;
        }
        return false;
    }

    public static enum Mode {
        Simple,
        Smart;

    }
}
