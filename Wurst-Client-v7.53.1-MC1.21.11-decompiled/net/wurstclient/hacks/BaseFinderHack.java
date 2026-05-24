package net.wurstclient.hacks;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import net.minecraft.class_2338;
import net.minecraft.class_290;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstRenderLayers;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.BlockListSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.BlockVertexCompiler;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.EasyVertexBuffer;
import net.wurstclient.util.RegionPos;
import net.wurstclient.util.RenderUtils;

@SearchTags(value={"base finder", "factions"})
public final class BaseFinderHack
extends Hack
implements UpdateListener,
RenderListener {
    private final BlockListSetting naturalBlocks = new BlockListSetting("Natural Blocks", "These blocks will be considered part of natural generation.\n\nThey will NOT be highlighted as player bases.", "minecraft:acacia_leaves", "minecraft:acacia_log", "minecraft:air", "minecraft:allium", "minecraft:amethyst_block", "minecraft:amethyst_cluster", "minecraft:andesite", "minecraft:azure_bluet", "minecraft:bedrock", "minecraft:birch_leaves", "minecraft:birch_log", "minecraft:blue_orchid", "minecraft:brown_mushroom", "minecraft:brown_mushroom_block", "minecraft:bubble_column", "minecraft:budding_amethyst", "minecraft:calcite", "minecraft:cave_air", "minecraft:clay", "minecraft:coal_ore", "minecraft:cobweb", "minecraft:copper_ore", "minecraft:cornflower", "minecraft:dandelion", "minecraft:dark_oak_leaves", "minecraft:dark_oak_log", "minecraft:dead_bush", "minecraft:deepslate", "minecraft:deepslate_coal_ore", "minecraft:deepslate_copper_ore", "minecraft:deepslate_diamond_ore", "minecraft:deepslate_emerald_ore", "minecraft:deepslate_gold_ore", "minecraft:deepslate_iron_ore", "minecraft:deepslate_lapis_ore", "minecraft:deepslate_redstone_ore", "minecraft:diamond_ore", "minecraft:diorite", "minecraft:dirt", "minecraft:dripstone_block", "minecraft:emerald_ore", "minecraft:fern", "minecraft:glow_lichen", "minecraft:gold_ore", "minecraft:granite", "minecraft:grass", "minecraft:grass_block", "minecraft:gravel", "minecraft:ice", "minecraft:infested_stone", "minecraft:iron_ore", "minecraft:jungle_leaves", "minecraft:jungle_log", "minecraft:kelp", "minecraft:kelp_plant", "minecraft:lapis_ore", "minecraft:large_amethyst_bud", "minecraft:large_fern", "minecraft:lava", "minecraft:lilac", "minecraft:lily_of_the_valley", "minecraft:lily_pad", "minecraft:medium_amethyst_bud", "minecraft:mossy_cobblestone", "minecraft:mushroom_stem", "minecraft:nether_quartz_ore", "minecraft:netherrack", "minecraft:oak_leaves", "minecraft:oak_log", "minecraft:obsidian", "minecraft:orange_tulip", "minecraft:oxeye_daisy", "minecraft:peony", "minecraft:pink_tulip", "minecraft:pointed_dripstone", "minecraft:poppy", "minecraft:red_mushroom", "minecraft:red_mushroom_block", "minecraft:red_tulip", "minecraft:redstone_ore", "minecraft:rose_bush", "minecraft:sand", "minecraft:sandstone", "minecraft:seagrass", "minecraft:small_amethyst_bud", "minecraft:smooth_basalt", "minecraft:snow", "minecraft:spawner", "minecraft:spruce_leaves", "minecraft:spruce_log", "minecraft:stone", "minecraft:sunflower", "minecraft:tall_grass", "minecraft:tall_seagrass", "minecraft:tuff", "minecraft:vine", "minecraft:water", "minecraft:white_tulip");
    private final ColorSetting color = new ColorSetting("Color", "Man-made blocks will be highlighted in this color.", Color.RED);
    private ArrayList<String> blockNames;
    private final HashSet<class_2338> matchingBlocks = new HashSet();
    private ArrayList<int[]> vertices = new ArrayList();
    private EasyVertexBuffer vertexBuffer;
    private int messageTimer = 0;
    private int counter;
    private RegionPos lastRegion;

    public BaseFinderHack() {
        super("BaseFinder");
        this.setCategory(Category.RENDER);
        this.addSetting(this.naturalBlocks);
        this.addSetting(this.color);
    }

    @Override
    public String getRenderName() {
        String name = this.getName() + " [";
        name = this.counter >= 10000 ? name + "10000+ blocks" : (this.counter == 1 ? name + "1 block" : (this.counter == 0 ? name + "nothing" : name + this.counter + " blocks"));
        name = name + " found]";
        return name;
    }

    @Override
    protected void onEnable() {
        this.messageTimer = 0;
        this.blockNames = new ArrayList<String>(this.naturalBlocks.getBlockNames());
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        this.matchingBlocks.clear();
        this.vertices.clear();
        if (this.vertexBuffer != null) {
            this.vertexBuffer.close();
        }
        this.vertexBuffer = null;
        this.lastRegion = null;
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        RegionPos region = RenderUtils.getCameraRegion();
        if (!region.equals(this.lastRegion)) {
            this.onUpdate();
        }
        if (this.vertexBuffer == null) {
            return;
        }
        matrixStack.method_22903();
        RenderUtils.applyRegionalRenderOffset(matrixStack, region);
        this.vertexBuffer.draw(matrixStack, WurstRenderLayers.ESP_QUADS, this.color.getColorF(), 0.25f);
        matrixStack.method_22909();
    }

    @Override
    public void onUpdate() {
        int modulo = BaseFinderHack.MC.field_1724.field_6012 % 64;
        RegionPos region = RenderUtils.getCameraRegion();
        if (modulo == 0 || !region.equals(this.lastRegion)) {
            if (this.vertexBuffer != null) {
                this.vertexBuffer.close();
            }
            this.vertexBuffer = EasyVertexBuffer.createAndUpload(VertexFormat.class_5596.field_27382, class_290.field_1576, buffer -> {
                for (int[] vertex : this.vertices) {
                    buffer.method_22912((float)(vertex[0] - region.x()), (float)vertex[1], (float)(vertex[2] - region.z())).method_39415(-1);
                }
            });
            this.lastRegion = region;
        }
        if (modulo == 0) {
            this.matchingBlocks.clear();
        }
        int stepSize = BaseFinderHack.MC.field_1687.method_31605() / 64;
        int startY = BaseFinderHack.MC.field_1687.method_31600() - 1 - modulo * stepSize;
        int endY = Math.max(startY - stepSize, BaseFinderHack.MC.field_1687.method_31607());
        class_2338 playerPos = class_2338.method_49637((double)BaseFinderHack.MC.field_1724.method_23317(), (double)0.0, (double)BaseFinderHack.MC.field_1724.method_23321());
        block0: for (int y = startY; y > endY; --y) {
            for (int x = 64; x > -64; --x) {
                for (int z = 64; z > -64; --z) {
                    if (this.matchingBlocks.size() >= 10000) break block0;
                    class_2338 pos = new class_2338(playerPos.method_10263() + x, y, playerPos.method_10260() + z);
                    if (Collections.binarySearch(this.blockNames, BlockUtils.getName(pos)) >= 0) continue;
                    this.matchingBlocks.add(pos);
                }
            }
        }
        if (modulo != 63) {
            return;
        }
        if (this.matchingBlocks.size() < 10000) {
            --this.messageTimer;
        } else {
            if (this.messageTimer <= 0) {
                ChatUtils.warning("BaseFinder found \u00a7lA LOT\u00a7r of blocks.");
                ChatUtils.message("To prevent lag, it will only show the first 10000 blocks.");
            }
            this.messageTimer = 3;
        }
        this.counter = this.matchingBlocks.size();
        this.vertices = BlockVertexCompiler.compile(this.matchingBlocks);
    }
}
