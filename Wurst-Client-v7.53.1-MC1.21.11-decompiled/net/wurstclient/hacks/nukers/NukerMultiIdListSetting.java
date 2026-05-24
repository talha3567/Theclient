package net.wurstclient.hacks.nukers;

import net.wurstclient.settings.BlockListSetting;

public final class NukerMultiIdListSetting
extends BlockListSetting {
    public NukerMultiIdListSetting(String descriptionKey) {
        super("MultiID List", descriptionKey, "minecraft:ancient_debris", "minecraft:bone_block", "minecraft:coal_ore", "minecraft:copper_ore", "minecraft:deepslate_coal_ore", "minecraft:deepslate_copper_ore", "minecraft:deepslate_diamond_ore", "minecraft:deepslate_emerald_ore", "minecraft:deepslate_gold_ore", "minecraft:deepslate_iron_ore", "minecraft:deepslate_lapis_ore", "minecraft:deepslate_redstone_ore", "minecraft:diamond_ore", "minecraft:emerald_ore", "minecraft:glowstone", "minecraft:gold_ore", "minecraft:iron_ore", "minecraft:lapis_ore", "minecraft:nether_gold_ore", "minecraft:nether_quartz_ore", "minecraft:raw_copper_block", "minecraft:raw_gold_block", "minecraft:raw_iron_block", "minecraft:redstone_ore");
    }

    public NukerMultiIdListSetting() {
        this("The types of blocks to break in MultiID mode.");
    }
}
