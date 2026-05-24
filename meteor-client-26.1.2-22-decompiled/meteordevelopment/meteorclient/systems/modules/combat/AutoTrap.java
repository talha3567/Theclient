package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AutoTrap
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<List<Block>> blocks;
    private final Setting<Double> placeRange;
    private final Setting<Double> placeWallsRange;
    private final Setting<SortPriority> priority;
    private final Setting<Double> targetRange;
    private final Setting<Integer> delay;
    private final Setting<Integer> blocksPerTick;
    private final Setting<TopMode> topPlacement;
    private final Setting<BottomMode> bottomPlacement;
    private final Setting<Boolean> selfToggle;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<SettingColor> nextSideColor;
    private final Setting<SettingColor> nextLineColor;
    private final List<BlockPos> placePositions;
    private Player target;
    private boolean placed;
    private int timer;

    public AutoTrap() {
        super(Categories.Combat, "auto-trap", "Traps people in a box to prevent them from moving.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.blocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("whitelist")).description("Which blocks to use.")).defaultValue(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN).build());
        this.placeRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("place-range")).description("The range at which blocks can be placed.")).defaultValue(4.0).min(0.0).sliderMax(6.0).build());
        this.placeWallsRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("walls-range")).description("Range in which to place when behind blocks.")).defaultValue(4.0).min(0.0).sliderMax(6.0).build());
        this.priority = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("target-priority")).description("How to select the player to target.")).defaultValue(SortPriority.LowestHealth)).build());
        this.targetRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("target-range")).description("The maximum distance to target players.")).defaultValue(3.0).min(0.0).sliderMax(10.0).build());
        this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("place-delay")).description("How many ticks between block placements.")).defaultValue(1)).build());
        this.blocksPerTick = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("blocks-per-tick")).description("How many blocks to place in one tick.")).defaultValue(1)).min(1).build());
        this.topPlacement = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("top-blocks")).description("Which blocks to place on the top half of the target.")).defaultValue(TopMode.Full)).build());
        this.bottomPlacement = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("bottom-blocks")).description("Which blocks to place on the bottom half of the target.")).defaultValue(BottomMode.Platform)).build());
        this.selfToggle = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("self-toggle")).description("Turns off after placing all blocks.")).defaultValue(true)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Rotates towards blocks when placing.")).defaultValue(true)).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Renders an overlay where blocks will be placed.")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).visible(this.render::get)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color of the target block rendering.")).defaultValue(new SettingColor(197, 137, 232, 10)).visible(() -> this.render.get() != false && this.shapeMode.get().sides())).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color of the target block rendering.")).defaultValue(new SettingColor(197, 137, 232)).visible(() -> this.render.get() != false && this.shapeMode.get().lines())).build());
        this.nextSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("next-side-color")).description("The side color of the next block to be placed.")).defaultValue(new SettingColor(227, 196, 245, 10)).visible(() -> this.render.get() != false && this.shapeMode.get().sides())).build());
        this.nextLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("next-line-color")).description("The line color of the next block to be placed.")).defaultValue(new SettingColor(5, 139, 221)).visible(() -> this.render.get() != false && this.shapeMode.get().lines())).build());
        this.placePositions = new ArrayList<BlockPos>();
    }

    @Override
    public void onActivate() {
        this.target = null;
        this.placePositions.clear();
        this.timer = 0;
        this.placed = false;
    }

    @Override
    public void onDeactivate() {
        this.placePositions.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.selfToggle.get().booleanValue() && this.placed && this.placePositions.isEmpty()) {
            this.placed = false;
            this.toggle();
            return;
        }
        FindItemResult block = InvUtils.findInHotbar(itemStack -> this.blocks.get().contains(Block.byItem((Item)itemStack.getItem())));
        if (!block.found()) {
            return;
        }
        if (TargetUtils.isBadTarget(this.target, this.targetRange.get())) {
            this.target = TargetUtils.getPlayerTarget(this.targetRange.get(), this.priority.get());
            if (TargetUtils.isBadTarget(this.target, this.targetRange.get())) {
                return;
            }
        }
        this.fillPlaceArray(this.target);
        if (this.timer >= this.delay.get() && !this.placePositions.isEmpty()) {
            int placedCount = 0;
            for (BlockPos placePosition : this.placePositions) {
                if (placedCount >= this.blocksPerTick.get() || !BlockUtils.place(placePosition, block, this.rotate.get(), 50, true)) continue;
                this.placed = true;
                ++placedCount;
            }
            this.timer = 0;
        } else {
            ++this.timer;
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!this.render.get().booleanValue() || this.placePositions.isEmpty()) {
            return;
        }
        for (int i = 0; i < this.placePositions.size(); ++i) {
            boolean isNext = i < this.blocksPerTick.get();
            Color side = isNext ? (Color)this.nextSideColor.get() : (Color)this.sideColor.get();
            Color line = isNext ? (Color)this.nextLineColor.get() : (Color)this.lineColor.get();
            event.renderer.box(this.placePositions.get(i), side, line, this.shapeMode.get(), 0);
        }
    }

    private void fillPlaceArray(Player target) {
        this.placePositions.clear();
        double epsilon = 1.0E-5;
        AABB box = target.getBoundingBox();
        ArrayList<BlockPos> corners = new ArrayList<BlockPos>();
        corners.add(BlockPos.containing((double)box.minX, (double)box.minY, (double)box.minZ));
        corners.add(BlockPos.containing((double)box.minX, (double)box.minY, (double)(box.maxZ - epsilon)));
        corners.add(BlockPos.containing((double)(box.maxX - epsilon), (double)box.minY, (double)box.minZ));
        corners.add(BlockPos.containing((double)(box.maxX - epsilon), (double)box.minY, (double)(box.maxZ - epsilon)));
        LinkedHashSet overlappedPositions = new LinkedHashSet(corners);
        for (BlockPos targetPos : overlappedPositions) {
            switch (this.topPlacement.get().ordinal()) {
                case 0: {
                    this.add(targetPos.offset(0, 2, 0));
                    this.add(targetPos.offset(1, 1, 0));
                    this.add(targetPos.offset(-1, 1, 0));
                    this.add(targetPos.offset(0, 1, 1));
                    this.add(targetPos.offset(0, 1, -1));
                    break;
                }
                case 2: {
                    this.add(targetPos.offset(1, 1, 0));
                    this.add(targetPos.offset(-1, 1, 0));
                    this.add(targetPos.offset(0, 1, 1));
                    this.add(targetPos.offset(0, 1, -1));
                    break;
                }
                case 1: {
                    this.add(targetPos.offset(0, 2, 0));
                }
            }
            switch (this.bottomPlacement.get().ordinal()) {
                case 1: {
                    this.add(targetPos.offset(0, -1, 0));
                    this.add(targetPos.offset(1, -1, 0));
                    this.add(targetPos.offset(-1, -1, 0));
                    this.add(targetPos.offset(0, -1, 1));
                    this.add(targetPos.offset(0, -1, -1));
                    break;
                }
                case 2: {
                    this.add(targetPos.offset(0, -1, 0));
                    this.add(targetPos.offset(1, 0, 0));
                    this.add(targetPos.offset(-1, 0, 0));
                    this.add(targetPos.offset(0, 0, -1));
                    this.add(targetPos.offset(0, 0, 1));
                    break;
                }
                case 0: {
                    this.add(targetPos.offset(0, -1, 0));
                }
            }
        }
        double pX = this.mc.player.getX();
        double pY = this.mc.player.getY();
        double pZ = this.mc.player.getZ();
        this.placePositions.sort(Comparator.comparingDouble(value -> Utils.squaredDistance(pX, pY, pZ, (double)value.getX() + 0.5, (double)value.getY() + 0.5, (double)value.getZ() + 0.5) * -1.0));
    }

    private void add(BlockPos blockPos) {
        if (this.placePositions.contains(blockPos)) {
            return;
        }
        if (!BlockUtils.canPlace(blockPos)) {
            return;
        }
        if (this.isOutOfRange(blockPos)) {
            return;
        }
        this.placePositions.add(blockPos);
    }

    private boolean isOutOfRange(BlockPos blockPos) {
        Vec3 pos = blockPos.getCenter();
        if (!PlayerUtils.isWithin(pos, (double)this.placeRange.get())) {
            return true;
        }
        ClipContext clipContext = new ClipContext(this.mc.player.getEyePosition(), pos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this.mc.player);
        BlockHitResult result = this.mc.level.clip(clipContext);
        if (result == null || !result.getBlockPos().equals((Object)blockPos)) {
            return !PlayerUtils.isWithin(pos, (double)this.placeWallsRange.get());
        }
        return false;
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName((Entity)this.target);
    }

    public static enum TopMode {
        Full,
        Top,
        Face,
        None;

    }

    public static enum BottomMode {
        Single,
        Platform,
        Full,
        None;

    }
}
