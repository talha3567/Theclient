package meteordevelopment.meteorclient.utils.entity;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.function.BiFunction;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DamageUtils {
    public static final RaycastFactory HIT_FACTORY = (context, blockPos) -> {
        BlockState blockState = MeteorClient.mc.level.getBlockState(blockPos);
        if (blockState.getBlock().getExplosionResistance() < 600.0f) {
            return null;
        }
        return blockState.getCollisionShape((BlockGetter)MeteorClient.mc.level, blockPos).clip(context.start(), context.end(), blockPos);
    };

    private DamageUtils() {
    }

    public static float crystalDamage(LivingEntity target, Vec3 targetPos, AABB targetBox, Vec3 explosionPos, RaycastFactory raycastFactory) {
        return DamageUtils.explosionDamage(target, targetPos, targetBox, explosionPos, 12.0f, raycastFactory);
    }

    public static float bedDamage(LivingEntity target, Vec3 targetPos, AABB targetBox, Vec3 explosionPos, RaycastFactory raycastFactory) {
        return DamageUtils.explosionDamage(target, targetPos, targetBox, explosionPos, 10.0f, raycastFactory);
    }

    public static float anchorDamage(LivingEntity target, Vec3 targetPos, AABB targetBox, Vec3 explosionPos, RaycastFactory raycastFactory) {
        return DamageUtils.explosionDamage(target, targetPos, targetBox, explosionPos, 10.0f, raycastFactory);
    }

    public static float explosionDamage(LivingEntity target, Vec3 targetPos, AABB targetBox, Vec3 explosionPos, float power, RaycastFactory raycastFactory) {
        double modDistance = PlayerUtils.distance(targetPos.x, targetPos.y, targetPos.z, explosionPos.x, explosionPos.y, explosionPos.z);
        if (modDistance > (double)power) {
            return 0.0f;
        }
        double exposure = DamageUtils.getExposure(explosionPos, targetBox, raycastFactory);
        double impact = (1.0 - modDistance / (double)power) * exposure;
        float damage = (int)((impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0);
        return DamageUtils.calculateReductions(damage, (Entity)target, MeteorClient.mc.level.damageSources().explosion(null));
    }

    public static float crystalDamage(LivingEntity target, Vec3 crystal, boolean predictMovement, BlockPos obsidianPos) {
        return DamageUtils.overridingExplosionDamage(target, crystal, 12.0f, predictMovement, obsidianPos, Blocks.OBSIDIAN.defaultBlockState());
    }

    public static float crystalDamage(LivingEntity target, Vec3 crystal) {
        return DamageUtils.explosionDamage(target, crystal, 12.0f, false);
    }

    public static float bedDamage(LivingEntity target, Vec3 bed) {
        return DamageUtils.explosionDamage(target, bed, 10.0f, false);
    }

    public static float anchorDamage(LivingEntity target, Vec3 anchor) {
        return DamageUtils.overridingExplosionDamage(target, anchor, 10.0f, false, BlockPos.containing((Position)anchor), Blocks.AIR.defaultBlockState());
    }

    private static float overridingExplosionDamage(LivingEntity target, Vec3 explosionPos, float power, boolean predictMovement, BlockPos overridePos, BlockState overrideState) {
        return DamageUtils.explosionDamage(target, explosionPos, power, predictMovement, DamageUtils.getOverridingHitFactory(overridePos, overrideState));
    }

    private static float explosionDamage(LivingEntity target, Vec3 explosionPos, float power, boolean predictMovement) {
        return DamageUtils.explosionDamage(target, explosionPos, power, predictMovement, HIT_FACTORY);
    }

    private static float explosionDamage(LivingEntity target, Vec3 explosionPos, float power, boolean predictMovement, RaycastFactory raycastFactory) {
        Player player;
        if (target == null) {
            return 0.0f;
        }
        if (target instanceof Player && EntityUtils.getGameMode(player = (Player)target) == GameType.CREATIVE && !(player instanceof FakePlayerEntity)) {
            return 0.0f;
        }
        Vec3 position = predictMovement ? target.position().add(target.getDeltaMovement()) : target.position();
        AABB box = target.getBoundingBox();
        if (predictMovement) {
            box = box.move(target.getDeltaMovement());
        }
        return DamageUtils.explosionDamage(target, position, box, explosionPos, power, raycastFactory);
    }

    public static RaycastFactory getOverridingHitFactory(BlockPos overridePos, BlockState overrideState) {
        return (context, blockPos) -> {
            BlockState blockState;
            if (blockPos.equals((Object)overridePos)) {
                blockState = overrideState;
            } else {
                blockState = MeteorClient.mc.level.getBlockState(blockPos);
                if (blockState.getBlock().getExplosionResistance() < 600.0f) {
                    return null;
                }
            }
            return blockState.getCollisionShape((BlockGetter)MeteorClient.mc.level, blockPos).clip(context.start(), context.end(), blockPos);
        };
    }

    public static float getAttackDamage(LivingEntity attacker, Entity target) {
        DamageSource damageSource;
        float itemDamage = (float)attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (attacker instanceof Player) {
            Player player = (Player)attacker;
            damageSource = MeteorClient.mc.level.damageSources().playerAttack(player);
        } else {
            damageSource = MeteorClient.mc.level.damageSources().mobAttack(attacker);
        }
        DamageSource damageSource2 = damageSource;
        float damage = DamageUtils.modifyAttackDamage(attacker, target, attacker.getWeaponItem(), damageSource2, itemDamage);
        return DamageUtils.calculateReductions(damage, target, damageSource2);
    }

    public static float getAttackDamage(LivingEntity attacker, Entity target, ItemStack weapon) {
        DamageSource damageSource;
        AttributeInstance original = attacker.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance copy = new AttributeInstance(Attributes.ATTACK_DAMAGE, attributeInstance -> {});
        copy.setBaseValue(original.getBaseValue());
        for (AttributeModifier modifier2 : original.getModifiers()) {
            copy.addTransientModifier(modifier2);
        }
        copy.removeModifier(Item.BASE_ATTACK_DAMAGE_ID);
        ItemAttributeModifiers attributeModifiers = (ItemAttributeModifiers)weapon.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (attributeModifiers != null) {
            attributeModifiers.forEach(EquipmentSlot.MAINHAND, (entry, modifier) -> {
                if (entry == Attributes.ATTACK_DAMAGE) {
                    copy.addOrUpdateTransientModifier(modifier);
                }
            });
        }
        float itemDamage = (float)copy.getValue();
        if (attacker instanceof Player) {
            Player player = (Player)attacker;
            damageSource = MeteorClient.mc.level.damageSources().playerAttack(player);
        } else {
            damageSource = MeteorClient.mc.level.damageSources().mobAttack(attacker);
        }
        DamageSource damageSource2 = damageSource;
        float damage = DamageUtils.modifyAttackDamage(attacker, target, weapon, damageSource2, itemDamage);
        return DamageUtils.calculateReductions(damage, target, damageSource2);
    }

    private static float modifyAttackDamage(LivingEntity attacker, Entity target, ItemStack weapon, DamageSource damageSource, float damage) {
        int smite;
        int impaling;
        int baneOfArthropods;
        Object2IntOpenHashMap enchantments = new Object2IntOpenHashMap();
        Utils.getEnchantments(weapon, (Object2IntMap<Holder<Enchantment>>)enchantments);
        float enchantDamage = 0.0f;
        int sharpness = Utils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)enchantments, (ResourceKey<Enchantment>)Enchantments.SHARPNESS);
        if (sharpness > 0) {
            enchantDamage += 1.0f + 0.5f * (float)(sharpness - 1);
        }
        if ((baneOfArthropods = Utils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)enchantments, (ResourceKey<Enchantment>)Enchantments.BANE_OF_ARTHROPODS)) > 0 && target.typeHolder().is(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) {
            enchantDamage += 2.5f * (float)baneOfArthropods;
        }
        if ((impaling = Utils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)enchantments, (ResourceKey<Enchantment>)Enchantments.IMPALING)) > 0 && target.typeHolder().is(EntityTypeTags.SENSITIVE_TO_IMPALING)) {
            enchantDamage += 2.5f * (float)impaling;
        }
        if ((smite = Utils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)enchantments, (ResourceKey<Enchantment>)Enchantments.SMITE)) > 0 && target.typeHolder().is(EntityTypeTags.SENSITIVE_TO_SMITE)) {
            enchantDamage += 2.5f * (float)smite;
        }
        if (attacker instanceof Player) {
            MaceItem item;
            float bonusDamage;
            Player playerEntity = (Player)attacker;
            float charge = playerEntity.getAttackStrengthScale(0.5f);
            damage *= 0.2f + charge * charge * 0.8f;
            enchantDamage *= charge;
            Item item2 = weapon.getItem();
            if (item2 instanceof MaceItem && (bonusDamage = (item = (MaceItem)item2).getAttackDamageBonus(target, damage, damageSource)) > 0.0f) {
                int density = Utils.getEnchantmentLevel(weapon, (ResourceKey<Enchantment>)Enchantments.DENSITY);
                if (density > 0) {
                    bonusDamage += (float)(0.5 * attacker.fallDistance);
                }
                damage += bonusDamage;
            }
            if (!(!(charge > 0.9f) || !(attacker.fallDistance > 0.0) || attacker.onGround() || attacker.onClimbable() || attacker.isInWater() || attacker.hasEffect(MobEffects.BLINDNESS) || attacker.isPassenger())) {
                damage *= 1.5f;
            }
        }
        return damage + enchantDamage;
    }

    public static float fallDamage(LivingEntity entity) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (player.getAbilities().flying) {
                return 0.0f;
            }
        }
        if (entity.hasEffect(MobEffects.SLOW_FALLING) || entity.hasEffect(MobEffects.LEVITATION)) {
            return 0.0f;
        }
        int surface = MeteorClient.mc.level.getChunkAt(entity.blockPosition()).getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING).getFirstAvailable(entity.getBlockX() & 0xF, entity.getBlockZ() & 0xF);
        if (entity.getBlockY() >= surface) {
            return DamageUtils.fallDamageReductions(entity, surface);
        }
        BlockHitResult raycastResult = MeteorClient.mc.level.clip(new ClipContext(entity.position(), new Vec3(entity.getX(), (double)MeteorClient.mc.level.getMinY(), entity.getZ()), ClipContext.Block.COLLIDER, ClipContext.Fluid.WATER, (Entity)entity));
        if (raycastResult.getType() == HitResult.Type.MISS) {
            return 0.0f;
        }
        return DamageUtils.fallDamageReductions(entity, raycastResult.getBlockPos().getY());
    }

    private static float fallDamageReductions(LivingEntity entity, int surface) {
        int fallHeight = (int)(entity.getY() - (double)surface + entity.fallDistance - 3.0);
        @Nullable MobEffectInstance jumpBoostInstance = entity.getEffect(MobEffects.JUMP_BOOST);
        if (jumpBoostInstance != null) {
            fallHeight -= jumpBoostInstance.getAmplifier() + 1;
        }
        return DamageUtils.calculateReductions(fallHeight, (Entity)entity, MeteorClient.mc.level.damageSources().fall());
    }

    public static float calculateReductions(float damage, Entity entity, DamageSource damageSource) {
        if (damageSource.scalesWithDifficulty()) {
            switch (MeteorClient.mc.level.getDifficulty()) {
                case EASY: {
                    damage = Math.min(damage / 2.0f + 1.0f, damage);
                    break;
                }
                case HARD: {
                    damage *= 1.5f;
                }
            }
        }
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            damage = CombatRules.getDamageAfterAbsorb((LivingEntity)livingEntity, (float)damage, (DamageSource)damageSource, (float)DamageUtils.getArmor(livingEntity), (float)((float)livingEntity.getAttributeValue(Attributes.ARMOR_TOUGHNESS)));
            damage = DamageUtils.resistanceReduction(livingEntity, damage);
            damage = DamageUtils.protectionReduction(livingEntity, damage, damageSource);
        }
        return Math.max(damage, 0.0f);
    }

    private static float getArmor(LivingEntity entity) {
        return (float)Math.floor(entity.getAttributeValue(Attributes.ARMOR));
    }

    private static float protectionReduction(LivingEntity player, float damage, DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return damage;
        }
        int damageProtection = 0;
        for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
            int featherFalling;
            int projectileProtection;
            int blastProtection;
            int fireProtection;
            ItemStack stack = player.getItemBySlot(slot);
            Object2IntOpenHashMap enchantments = new Object2IntOpenHashMap();
            Utils.getEnchantments(stack, (Object2IntMap<Holder<Enchantment>>)enchantments);
            int protection = Utils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)enchantments, (ResourceKey<Enchantment>)Enchantments.PROTECTION);
            if (protection > 0) {
                damageProtection += protection;
            }
            if ((fireProtection = Utils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)enchantments, (ResourceKey<Enchantment>)Enchantments.FIRE_PROTECTION)) > 0 && source.is(DamageTypeTags.IS_FIRE)) {
                damageProtection += 2 * fireProtection;
            }
            if ((blastProtection = Utils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)enchantments, (ResourceKey<Enchantment>)Enchantments.BLAST_PROTECTION)) > 0 && source.is(DamageTypeTags.IS_EXPLOSION)) {
                damageProtection += 2 * blastProtection;
            }
            if ((projectileProtection = Utils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)enchantments, (ResourceKey<Enchantment>)Enchantments.PROJECTILE_PROTECTION)) > 0 && source.is(DamageTypeTags.IS_PROJECTILE)) {
                damageProtection += 2 * projectileProtection;
            }
            if ((featherFalling = Utils.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)enchantments, (ResourceKey<Enchantment>)Enchantments.FEATHER_FALLING)) <= 0 || !source.is(DamageTypeTags.IS_FALL)) continue;
            damageProtection += 3 * featherFalling;
        }
        return CombatRules.getDamageAfterMagicAbsorb((float)damage, (float)damageProtection);
    }

    private static float resistanceReduction(LivingEntity player, float damage) {
        MobEffectInstance resistance = player.getEffect(MobEffects.RESISTANCE);
        if (resistance != null) {
            int lvl = resistance.getAmplifier() + 1;
            damage *= 1.0f - (float)lvl * 0.2f;
        }
        return Math.max(damage, 0.0f);
    }

    private static float getExposure(Vec3 source, AABB box, RaycastFactory raycastFactory) {
        double xDiff = box.maxX - box.minX;
        double yDiff = box.maxY - box.minY;
        double zDiff = box.maxZ - box.minZ;
        double xStep = 1.0 / (xDiff * 2.0 + 1.0);
        double yStep = 1.0 / (yDiff * 2.0 + 1.0);
        double zStep = 1.0 / (zDiff * 2.0 + 1.0);
        if (xStep > 0.0 && yStep > 0.0 && zStep > 0.0) {
            int misses = 0;
            int hits = 0;
            double xOffset = (1.0 - Math.floor(1.0 / xStep) * xStep) * 0.5;
            double zOffset = (1.0 - Math.floor(1.0 / zStep) * zStep) * 0.5;
            xStep *= xDiff;
            yStep *= yDiff;
            zStep *= zDiff;
            double startX = box.minX + xOffset;
            double startY = box.minY;
            double startZ = box.minZ + zOffset;
            double endX = box.maxX + xOffset;
            double endY = box.maxY;
            double endZ = box.maxZ + zOffset;
            for (double x = startX; x <= endX; x += xStep) {
                for (double y = startY; y <= endY; y += yStep) {
                    for (double z = startZ; z <= endZ; z += zStep) {
                        Vec3 position = new Vec3(x, y, z);
                        if (DamageUtils.raycast(new ExposureRaycastContext(position, source), raycastFactory) == null) {
                            ++misses;
                        }
                        ++hits;
                    }
                }
            }
            return (float)misses / (float)hits;
        }
        return 0.0f;
    }

    private static BlockHitResult raycast(ExposureRaycastContext context, RaycastFactory raycastFactory) {
        return (BlockHitResult)BlockGetter.traverseBlocks((Vec3)context.start, (Vec3)context.end, (Object)context, (BiFunction)raycastFactory, exposureRaycastContext -> null);
    }

    @FunctionalInterface
    public static interface RaycastFactory
    extends BiFunction<ExposureRaycastContext, BlockPos, BlockHitResult> {
    }

    public record ExposureRaycastContext(Vec3 start, Vec3 end) {
    }
}
