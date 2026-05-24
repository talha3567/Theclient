package meteordevelopment.meteorclient.systems.modules.render;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.BlockBehaviourAccessor;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.Dir;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;

public class HoleESP
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Integer> horizontalRadius;
    private final Setting<Integer> verticalRadius;
    private final Setting<Integer> holeHeight;
    private final Setting<Boolean> doubles;
    private final Setting<Boolean> ignoreOwn;
    private final Setting<Boolean> webs;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<Double> height;
    private final Setting<Boolean> topQuad;
    private final Setting<Boolean> bottomQuad;
    private final Setting<SettingColor> bedrockColorTop;
    private final Setting<SettingColor> bedrockColorBottom;
    private final Setting<SettingColor> obsidianColorTop;
    private final Setting<SettingColor> obsidianColorBottom;
    private final Setting<SettingColor> mixedColorTop;
    private final Setting<SettingColor> mixedColorBottom;
    private final Pool<Hole> holePool;
    private final List<Hole> holes;
    private final byte NULL = 0;

    public HoleESP() {
        super(Categories.Render, "hole-esp", "Displays holes that you will take less damage in.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.horizontalRadius = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("horizontal-radius")).description("Horizontal radius in which to search for holes.")).defaultValue(10)).min(0).sliderMax(32).build());
        this.verticalRadius = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("vertical-radius")).description("Vertical radius in which to search for holes.")).defaultValue(5)).min(0).sliderMax(32).build());
        this.holeHeight = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("min-height")).description("Minimum hole height required to be rendered.")).defaultValue(3)).min(1).sliderMin(1).build());
        this.doubles = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("doubles")).description("Highlights double holes that can be stood across.")).defaultValue(true)).build());
        this.ignoreOwn = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-own")).description("Ignores rendering the hole you are currently standing in.")).defaultValue(false)).build());
        this.webs = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("webs")).description("Whether to show holes that have webs inside of them.")).defaultValue(false)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.height = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("height")).description("The height of rendering.")).defaultValue(0.2).min(0.0).build());
        this.topQuad = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("top-quad")).description("Whether to render a quad at the top of the hole.")).defaultValue(true)).build());
        this.bottomQuad = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("bottom-quad")).description("Whether to render a quad at the bottom of the hole.")).defaultValue(false)).build());
        this.bedrockColorTop = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("bedrock-top")).description("The top color for holes that are completely bedrock.")).defaultValue(new SettingColor(100, 255, 0, 200)).build());
        this.bedrockColorBottom = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("bedrock-bottom")).description("The bottom color for holes that are completely bedrock.")).defaultValue(new SettingColor(100, 255, 0, 0)).build());
        this.obsidianColorTop = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("obsidian-top")).description("The top color for holes that are completely obsidian.")).defaultValue(new SettingColor(255, 0, 0, 200)).build());
        this.obsidianColorBottom = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("obsidian-bottom")).description("The bottom color for holes that are completely obsidian.")).defaultValue(new SettingColor(255, 0, 0, 0)).build());
        this.mixedColorTop = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("mixed-top")).description("The top color for holes that have mixed bedrock and obsidian.")).defaultValue(new SettingColor(255, 127, 0, 200)).build());
        this.mixedColorBottom = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("mixed-bottom")).description("The bottom color for holes that have mixed bedrock and obsidian.")).defaultValue(new SettingColor(255, 127, 0, 0)).build());
        this.holePool = new Pool<Hole>(Hole::new);
        this.holes = new ArrayList<Hole>();
        this.NULL = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        this.holePool.freeAll(this.holes);
        this.holes.clear();
        BlockIterator.register(this.horizontalRadius.get(), this.verticalRadius.get(), (blockPos, blockState) -> {
            if (!this.validHole((BlockPos)blockPos)) {
                return;
            }
            int bedrock = 0;
            int obsidian = 0;
            Direction air = null;
            for (Direction direction : Direction.values()) {
                boolean breakable;
                if (direction == Direction.UP) continue;
                BlockPos offsetPos = blockPos.relative(direction);
                Block block = this.mc.level.getBlockState(offsetPos).getBlock();
                boolean bl = breakable = block.defaultDestroyTime() >= 0.0f;
                if (((BlockBehaviourAccessor)block).meteor$isHasCollision() && !breakable) {
                    ++bedrock;
                    continue;
                }
                if (block.getExplosionResistance() >= 600.0f && breakable) {
                    ++obsidian;
                    continue;
                }
                if (direction == Direction.DOWN) {
                    return;
                }
                if (!this.doubles.get().booleanValue() || air != null || !this.validHole(offsetPos)) continue;
                for (Direction dir : Direction.values()) {
                    if (dir == direction.getOpposite() || dir == Direction.UP) continue;
                    block = this.mc.level.getBlockState(offsetPos.relative(dir)).getBlock();
                    boolean bl2 = breakable = block.defaultDestroyTime() >= 0.0f;
                    if (((BlockBehaviourAccessor)block).meteor$isHasCollision() && !breakable) {
                        ++bedrock;
                        continue;
                    }
                    if (block.getExplosionResistance() >= 600.0f && breakable) {
                        ++obsidian;
                        continue;
                    }
                    return;
                }
                air = direction;
            }
            if (obsidian + bedrock == 5 && air == null) {
                this.holes.add(this.holePool.get().set((BlockPos)blockPos, obsidian == 5 ? Hole.Type.Obsidian : (bedrock == 5 ? Hole.Type.Bedrock : Hole.Type.Mixed), (byte)0));
            } else if (obsidian + bedrock == 8 && this.doubles.get().booleanValue() && air != null) {
                this.holes.add(this.holePool.get().set((BlockPos)blockPos, obsidian == 8 ? Hole.Type.Obsidian : (bedrock == 8 ? Hole.Type.Bedrock : Hole.Type.Mixed), Dir.get(air)));
            }
        });
    }

    private boolean validHole(BlockPos pos) {
        if (this.ignoreOwn.get().booleanValue() && this.mc.player.blockPosition().equals((Object)pos)) {
            return false;
        }
        LevelChunk chunk = this.mc.level.getChunk(SectionPos.posToSectionCoord((double)pos.getX()), SectionPos.posToSectionCoord((double)pos.getZ()));
        Block block = chunk.getBlockState(pos).getBlock();
        if (!this.webs.get().booleanValue() && block == Blocks.COBWEB) {
            return false;
        }
        if (((BlockBehaviourAccessor)block).meteor$isHasCollision()) {
            return false;
        }
        for (int i = 0; i < this.holeHeight.get(); ++i) {
            if (!((BlockBehaviourAccessor)chunk.getBlockState(pos.above(i)).getBlock()).meteor$isHasCollision()) continue;
            return false;
        }
        return true;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        for (Hole hole : this.holes) {
            hole.render(event.renderer, this.shapeMode.get(), this.height.get(), this.topQuad.get(), this.bottomQuad.get());
        }
    }

    private static class Hole {
        public BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        public byte exclude;
        public Type type;

        private Hole() {
        }

        public Hole set(BlockPos blockPos, Type type, byte exclude) {
            this.blockPos.set((Vec3i)blockPos);
            this.exclude = exclude;
            this.type = type;
            return this;
        }

        public Color getTopColor() {
            return switch (this.type.ordinal()) {
                case 1 -> Modules.get().get(HoleESP.class).obsidianColorTop.get();
                case 0 -> Modules.get().get(HoleESP.class).bedrockColorTop.get();
                default -> Modules.get().get(HoleESP.class).mixedColorTop.get();
            };
        }

        public Color getBottomColor() {
            return switch (this.type.ordinal()) {
                case 1 -> Modules.get().get(HoleESP.class).obsidianColorBottom.get();
                case 0 -> Modules.get().get(HoleESP.class).bedrockColorBottom.get();
                default -> Modules.get().get(HoleESP.class).mixedColorBottom.get();
            };
        }

        public void render(Renderer3D renderer, ShapeMode mode, double height, boolean topQuad, boolean bottomQuad) {
            int x = this.blockPos.getX();
            int y = this.blockPos.getY();
            int z = this.blockPos.getZ();
            Color top = this.getTopColor();
            Color bottom = this.getBottomColor();
            int originalTopA = top.a;
            int originalBottompA = bottom.a;
            if (mode.lines()) {
                if (Dir.isNot(this.exclude, (byte)32) && Dir.isNot(this.exclude, (byte)8)) {
                    renderer.line(x, y, z, x, (double)y + height, z, bottom, top);
                }
                if (Dir.isNot(this.exclude, (byte)32) && Dir.isNot(this.exclude, (byte)16)) {
                    renderer.line(x, y, z + 1, x, (double)y + height, z + 1, bottom, top);
                }
                if (Dir.isNot(this.exclude, (byte)64) && Dir.isNot(this.exclude, (byte)8)) {
                    renderer.line(x + 1, y, z, x + 1, (double)y + height, z, bottom, top);
                }
                if (Dir.isNot(this.exclude, (byte)64) && Dir.isNot(this.exclude, (byte)16)) {
                    renderer.line(x + 1, y, z + 1, x + 1, (double)y + height, z + 1, bottom, top);
                }
                if (Dir.isNot(this.exclude, (byte)8)) {
                    renderer.line(x, y, z, x + 1, y, z, bottom);
                }
                if (Dir.isNot(this.exclude, (byte)8)) {
                    renderer.line(x, (double)y + height, z, x + 1, (double)y + height, z, top);
                }
                if (Dir.isNot(this.exclude, (byte)16)) {
                    renderer.line(x, y, z + 1, x + 1, y, z + 1, bottom);
                }
                if (Dir.isNot(this.exclude, (byte)16)) {
                    renderer.line(x, (double)y + height, z + 1, x + 1, (double)y + height, z + 1, top);
                }
                if (Dir.isNot(this.exclude, (byte)32)) {
                    renderer.line(x, y, z, x, y, z + 1, bottom);
                }
                if (Dir.isNot(this.exclude, (byte)32)) {
                    renderer.line(x, (double)y + height, z, x, (double)y + height, z + 1, top);
                }
                if (Dir.isNot(this.exclude, (byte)64)) {
                    renderer.line(x + 1, y, z, x + 1, y, z + 1, bottom);
                }
                if (Dir.isNot(this.exclude, (byte)64)) {
                    renderer.line(x + 1, (double)y + height, z, x + 1, (double)y + height, z + 1, top);
                }
            }
            if (mode.sides()) {
                top.a = originalTopA / 2;
                bottom.a = originalBottompA / 2;
                if (Dir.isNot(this.exclude, (byte)2) && topQuad) {
                    renderer.quad(x, (double)y + height, z, x, (double)y + height, z + 1, x + 1, (double)y + height, z + 1, x + 1, (double)y + height, z, top);
                }
                if (Dir.isNot(this.exclude, (byte)4) && bottomQuad) {
                    renderer.quad(x, y, z, x, y, z + 1, x + 1, y, z + 1, x + 1, y, z, bottom);
                }
                if (Dir.isNot(this.exclude, (byte)8)) {
                    renderer.gradientQuadVertical(x, y, z, x + 1, (double)y + height, z, top, bottom);
                }
                if (Dir.isNot(this.exclude, (byte)16)) {
                    renderer.gradientQuadVertical(x, y, z + 1, x + 1, (double)y + height, z + 1, top, bottom);
                }
                if (Dir.isNot(this.exclude, (byte)32)) {
                    renderer.gradientQuadVertical(x, y, z, x, (double)y + height, z + 1, top, bottom);
                }
                if (Dir.isNot(this.exclude, (byte)64)) {
                    renderer.gradientQuadVertical(x + 1, y, z, x + 1, (double)y + height, z + 1, top, bottom);
                }
                top.a = originalTopA;
                bottom.a = originalBottompA;
            }
        }

        public static enum Type {
            Bedrock,
            Obsidian,
            Mixed;

        }
    }
}
