package meteordevelopment.meteorclient.utils.misc;

import java.util.function.Supplier;
import meteordevelopment.meteorclient.utils.render.DisplayItemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public enum MyPotion {
    Swiftness((Holder<Potion>)Potions.SWIFTNESS, Items.NETHER_WART, Items.SUGAR),
    SwiftnessLong((Holder<Potion>)Potions.LONG_SWIFTNESS, Items.NETHER_WART, Items.SUGAR, Items.REDSTONE),
    SwiftnessStrong((Holder<Potion>)Potions.STRONG_SWIFTNESS, Items.NETHER_WART, Items.SUGAR, Items.GLOWSTONE_DUST),
    Slowness((Holder<Potion>)Potions.SLOWNESS, Items.NETHER_WART, Items.SUGAR, Items.FERMENTED_SPIDER_EYE),
    SlownessLong((Holder<Potion>)Potions.LONG_SLOWNESS, Items.NETHER_WART, Items.SUGAR, Items.FERMENTED_SPIDER_EYE, Items.REDSTONE),
    SlownessStrong((Holder<Potion>)Potions.STRONG_SLOWNESS, Items.NETHER_WART, Items.SUGAR, Items.FERMENTED_SPIDER_EYE, Items.GLOWSTONE_DUST),
    JumpBoost((Holder<Potion>)Potions.LEAPING, Items.NETHER_WART, Items.RABBIT_FOOT),
    JumpBoostLong((Holder<Potion>)Potions.LONG_LEAPING, Items.NETHER_WART, Items.RABBIT_FOOT, Items.REDSTONE),
    JumpBoostStrong((Holder<Potion>)Potions.STRONG_LEAPING, Items.NETHER_WART, Items.RABBIT_FOOT, Items.GLOWSTONE_DUST),
    Strength((Holder<Potion>)Potions.STRENGTH, Items.NETHER_WART, Items.BLAZE_POWDER),
    StrengthLong((Holder<Potion>)Potions.LONG_STRENGTH, Items.NETHER_WART, Items.BLAZE_POWDER, Items.REDSTONE),
    StrengthStrong((Holder<Potion>)Potions.STRONG_STRENGTH, Items.NETHER_WART, Items.BLAZE_POWDER, Items.GLOWSTONE_DUST),
    Healing((Holder<Potion>)Potions.HEALING, Items.NETHER_WART, Items.GLISTERING_MELON_SLICE),
    HealingStrong((Holder<Potion>)Potions.STRONG_HEALING, Items.NETHER_WART, Items.GLISTERING_MELON_SLICE, Items.GLOWSTONE_DUST),
    Harming((Holder<Potion>)Potions.HARMING, Items.NETHER_WART, Items.GLISTERING_MELON_SLICE, Items.FERMENTED_SPIDER_EYE),
    HarmingStrong((Holder<Potion>)Potions.STRONG_HARMING, Items.NETHER_WART, Items.GLISTERING_MELON_SLICE, Items.FERMENTED_SPIDER_EYE, Items.GLOWSTONE_DUST),
    Poison((Holder<Potion>)Potions.POISON, Items.NETHER_WART, Items.SPIDER_EYE),
    PoisonLong((Holder<Potion>)Potions.LONG_POISON, Items.NETHER_WART, Items.SPIDER_EYE, Items.REDSTONE),
    PoisonStrong((Holder<Potion>)Potions.STRONG_POISON, Items.NETHER_WART, Items.SPIDER_EYE, Items.GLOWSTONE_DUST),
    Regeneration((Holder<Potion>)Potions.REGENERATION, Items.NETHER_WART, Items.GHAST_TEAR),
    RegenerationLong((Holder<Potion>)Potions.LONG_REGENERATION, Items.NETHER_WART, Items.GHAST_TEAR, Items.REDSTONE),
    RegenerationStrong((Holder<Potion>)Potions.STRONG_REGENERATION, Items.NETHER_WART, Items.GHAST_TEAR, Items.GLOWSTONE_DUST),
    FireResistance((Holder<Potion>)Potions.FIRE_RESISTANCE, Items.NETHER_WART, Items.MAGMA_CREAM),
    FireResistanceLong((Holder<Potion>)Potions.LONG_FIRE_RESISTANCE, Items.NETHER_WART, Items.MAGMA_CREAM, Items.REDSTONE),
    WaterBreathing((Holder<Potion>)Potions.WATER_BREATHING, Items.NETHER_WART, Items.PUFFERFISH),
    WaterBreathingLong((Holder<Potion>)Potions.LONG_WATER_BREATHING, Items.NETHER_WART, Items.PUFFERFISH, Items.REDSTONE),
    NightVision((Holder<Potion>)Potions.NIGHT_VISION, Items.NETHER_WART, Items.GOLDEN_CARROT),
    NightVisionLong((Holder<Potion>)Potions.LONG_NIGHT_VISION, Items.NETHER_WART, Items.GOLDEN_CARROT, Items.REDSTONE),
    Invisibility((Holder<Potion>)Potions.INVISIBILITY, Items.NETHER_WART, Items.GOLDEN_CARROT, Items.FERMENTED_SPIDER_EYE),
    InvisibilityLong((Holder<Potion>)Potions.LONG_INVISIBILITY, Items.NETHER_WART, Items.GOLDEN_CARROT, Items.FERMENTED_SPIDER_EYE, Items.REDSTONE),
    TurtleMaster((Holder<Potion>)Potions.TURTLE_MASTER, Items.NETHER_WART, Items.TURTLE_HELMET),
    TurtleMasterLong((Holder<Potion>)Potions.LONG_TURTLE_MASTER, Items.NETHER_WART, Items.TURTLE_HELMET, Items.REDSTONE),
    TurtleMasterStrong((Holder<Potion>)Potions.STRONG_TURTLE_MASTER, Items.NETHER_WART, Items.TURTLE_HELMET, Items.GLOWSTONE_DUST),
    SlowFalling((Holder<Potion>)Potions.SLOW_FALLING, Items.NETHER_WART, Items.PHANTOM_MEMBRANE),
    SlowFallingLong((Holder<Potion>)Potions.LONG_SLOW_FALLING, Items.NETHER_WART, Items.PHANTOM_MEMBRANE, Items.REDSTONE),
    Weakness((Holder<Potion>)Potions.WEAKNESS, Items.FERMENTED_SPIDER_EYE),
    WeaknessLong((Holder<Potion>)Potions.LONG_WEAKNESS, Items.FERMENTED_SPIDER_EYE, Items.REDSTONE);

    public final Supplier<ItemStack> potion = () -> {
        ItemStack stack = DisplayItemUtils.toStack(Items.POTION);
        stack.set(DataComponents.POTION_CONTENTS, (Object)new PotionContents(potion));
        return stack;
    };
    public final Item[] ingredients;

    private MyPotion(Holder<Potion> potion, Item ... ingredients) {
        this.ingredients = ingredients;
    }
}
