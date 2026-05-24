package meteordevelopment.meteorclient.systems.modules.render;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.mixin.LevelRendererAccessor;
import meteordevelopment.meteorclient.mixin.MultiPlayerGameModeAccessor;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.HighwayBuilder;
import meteordevelopment.meteorclient.systems.modules.world.PacketMine;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BreakIndicators
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<ShapeMode> shapeMode;
    public final Setting<Boolean> packetMine;
    private final Setting<SettingColor> startSideColor;
    private final Setting<SettingColor> startLineColor;
    private final Setting<SettingColor> endSideColor;
    private final Setting<SettingColor> endLineColor;
    private final Color cSides;
    private final Color cLines;

    public BreakIndicators() {
        super(Categories.Render, "break-indicators", "Renders the progress of a block being broken.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.shapeMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.packetMine = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("packet-mine")).description("Whether or not to render blocks being packet mined.")).defaultValue(true)).build());
        this.startSideColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("start-side-color")).description("The side color for the non-broken block.")).defaultValue(new SettingColor(25, 252, 25, 150)).visible(() -> this.shapeMode.get().sides())).build());
        this.startLineColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("start-line-color")).description("The line color for the non-broken block.")).defaultValue(new SettingColor(25, 252, 25, 150)).visible(() -> this.shapeMode.get().lines())).build());
        this.endSideColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("end-side-color")).description("The side color for the fully-broken block.")).defaultValue(new SettingColor(255, 25, 25, 150)).visible(() -> this.shapeMode.get().sides())).build());
        this.endLineColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("end-line-color")).description("The line color for the fully-broken block.")).defaultValue(new SettingColor(255, 25, 25, 150)).visible(() -> this.shapeMode.get().lines())).build());
        this.cSides = new Color();
        this.cLines = new Color();
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        double normalised;
        VoxelShape voxelShape;
        HighwayBuilder b;
        this.renderNormal(event);
        if (this.packetMine.get().booleanValue() && !Modules.get().get(PacketMine.class).blocks.isEmpty()) {
            this.renderPacket(event, Modules.get().get(PacketMine.class).blocks);
        }
        if (!(b = Modules.get().get(HighwayBuilder.class)).isActive()) {
            return;
        }
        if (b.normalMining != null) {
            voxelShape = b.normalMining.blockState.getShape((BlockGetter)this.mc.level, b.normalMining.blockPos);
            if (voxelShape.isEmpty()) {
                return;
            }
            normalised = Math.min(1.0, b.normalMining.progress());
            this.renderBlock(event, voxelShape.bounds(), b.normalMining.blockPos, 1.0 - normalised, normalised);
        }
        if (b.packetMining != null) {
            voxelShape = b.packetMining.blockState.getShape((BlockGetter)this.mc.level, b.packetMining.blockPos);
            if (voxelShape.isEmpty()) {
                return;
            }
            normalised = Math.min(1.0, b.packetMining.progress());
            this.renderBlock(event, voxelShape.bounds(), b.packetMining.blockPos, 1.0 - normalised, normalised);
        }
    }

    private void renderNormal(Render3DEvent event) {
        Int2ObjectMap<BlockDestructionProgress> blocks = ((LevelRendererAccessor)this.mc.levelRenderer).meteor$getDestroyingBlocks();
        float ownBreakingStage = ((MultiPlayerGameModeAccessor)this.mc.gameMode).meteor$getBreakingProgress();
        BlockPos ownBreakingPos = ((MultiPlayerGameModeAccessor)this.mc.gameMode).meteor$getCurrentBreakingBlockPos();
        if (ownBreakingPos != null && ownBreakingStage > 0.0f) {
            BlockState state = this.mc.level.getBlockState(ownBreakingPos);
            VoxelShape shape = state.getShape((BlockGetter)this.mc.level, ownBreakingPos);
            if (shape == null || shape.isEmpty()) {
                return;
            }
            AABB orig = shape.bounds();
            double shrinkFactor = 1.0 - (double)ownBreakingStage;
            this.renderBlock(event, orig, ownBreakingPos, shrinkFactor, ownBreakingStage);
        }
        blocks.values().forEach(info -> {
            BlockPos pos = info.getPos();
            int stage = info.getProgress();
            if (pos.equals((Object)ownBreakingPos)) {
                return;
            }
            BlockState state = this.mc.level.getBlockState(pos);
            VoxelShape shape = state.getShape((BlockGetter)this.mc.level, pos);
            if (shape == null || shape.isEmpty()) {
                return;
            }
            AABB orig = shape.bounds();
            double shrinkFactor = (double)(9 - (stage + 1)) / 9.0;
            double progress = 1.0 - shrinkFactor;
            this.renderBlock(event, orig, pos, shrinkFactor, progress);
        });
    }

    private void renderPacket(Render3DEvent event, List<PacketMine.MyBlock> blocks) {
        for (PacketMine.MyBlock block : blocks) {
            if (!block.mining || block.progress() == Double.POSITIVE_INFINITY) continue;
            VoxelShape shape = block.blockState.getShape((BlockGetter)this.mc.level, block.blockPos);
            if (shape == null || shape.isEmpty()) {
                return;
            }
            AABB orig = shape.bounds();
            double progressNormalised = Math.min(1.0, block.progress());
            double shrinkFactor = 1.0 - progressNormalised;
            BlockPos pos = block.blockPos;
            this.renderBlock(event, orig, pos, shrinkFactor, progressNormalised);
        }
    }

    private void renderBlock(Render3DEvent event, AABB orig, BlockPos pos, double shrinkFactor, double progress) {
        AABB box = orig.contract(orig.getXsize() * shrinkFactor, orig.getYsize() * shrinkFactor, orig.getZsize() * shrinkFactor);
        double xShrink = orig.getXsize() * shrinkFactor / 2.0;
        double yShrink = orig.getYsize() * shrinkFactor / 2.0;
        double zShrink = orig.getZsize() * shrinkFactor / 2.0;
        double x1 = (double)pos.getX() + box.minX + xShrink;
        double y1 = (double)pos.getY() + box.minY + yShrink;
        double z1 = (double)pos.getZ() + box.minZ + zShrink;
        double x2 = (double)pos.getX() + box.maxX + xShrink;
        double y2 = (double)pos.getY() + box.maxY + yShrink;
        double z2 = (double)pos.getZ() + box.maxZ + zShrink;
        Color c1Sides = this.startSideColor.get().copy().a(this.startSideColor.get().a / 2);
        Color c2Sides = this.endSideColor.get().copy().a(this.endSideColor.get().a / 2);
        this.cSides.set((int)Math.round((double)c1Sides.r + (double)(c2Sides.r - c1Sides.r) * progress), (int)Math.round((double)c1Sides.g + (double)(c2Sides.g - c1Sides.g) * progress), (int)Math.round((double)c1Sides.b + (double)(c2Sides.b - c1Sides.b) * progress), (int)Math.round((double)c1Sides.a + (double)(c2Sides.a - c1Sides.a) * progress));
        Color c1Lines = this.startLineColor.get();
        Color c2Lines = this.endLineColor.get();
        this.cLines.set((int)Math.round((double)c1Lines.r + (double)(c2Lines.r - c1Lines.r) * progress), (int)Math.round((double)c1Lines.g + (double)(c2Lines.g - c1Lines.g) * progress), (int)Math.round((double)c1Lines.b + (double)(c2Lines.b - c1Lines.b) * progress), (int)Math.round((double)c1Lines.a + (double)(c2Lines.a - c1Lines.a) * progress));
        event.renderer.box(x1, y1, z1, x2, y2, z2, this.cSides, this.cLines, this.shapeMode.get(), 0);
    }
}
