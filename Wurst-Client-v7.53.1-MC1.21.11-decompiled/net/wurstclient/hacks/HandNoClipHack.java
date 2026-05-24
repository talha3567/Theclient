package net.wurstclient.hacks;

import net.minecraft.class_2338;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.BlockListSetting;
import net.wurstclient.util.BlockUtils;

@SearchTags(value={"hand noclip", "hand no clip"})
public final class HandNoClipHack
extends Hack {
    private final BlockListSetting blocks = new BlockListSetting("Blocks", "The blocks you want to reach through walls.", "minecraft:barrel", "minecraft:black_shulker_box", "minecraft:blue_shulker_box", "minecraft:brown_shulker_box", "minecraft:chest", "minecraft:cyan_shulker_box", "minecraft:dispenser", "minecraft:dropper", "minecraft:ender_chest", "minecraft:gray_shulker_box", "minecraft:green_shulker_box", "minecraft:hopper", "minecraft:light_blue_shulker_box", "minecraft:light_gray_shulker_box", "minecraft:lime_shulker_box", "minecraft:magenta_shulker_box", "minecraft:orange_shulker_box", "minecraft:pink_shulker_box", "minecraft:purple_shulker_box", "minecraft:red_shulker_box", "minecraft:shulker_box", "minecraft:trapped_chest", "minecraft:white_shulker_box", "minecraft:yellow_shulker_box");

    public HandNoClipHack() {
        super("HandNoClip");
        this.setCategory(Category.BLOCKS);
        this.addSetting(this.blocks);
    }

    public boolean isBlockInList(class_2338 pos) {
        return this.blocks.contains(BlockUtils.getName(pos));
    }
}
