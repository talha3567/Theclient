package meteordevelopment.meteorclient.systems.modules.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VeinMiner
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Set<Vec3i> blockNeighbours;
    private final Setting<List<Block>> selectedBlocks;
    private final Setting<ListMode> mode;
    private final Setting<Integer> depth;
    private final Setting<Integer> delay;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> swingHand;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Pool<MyBlock> blockPool;
    private final List<MyBlock> blocks;
    private final List<BlockPos> foundBlockPositions;
    private int tick;

    public VeinMiner() {
        super(Categories.World, "vein-miner", "Mines all nearby blocks with this type");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.blockNeighbours = Set.of(new Vec3i(1, -1, 1), new Vec3i(0, -1, 1), new Vec3i(-1, -1, 1), new Vec3i(1, -1, 0), new Vec3i(0, -1, 0), new Vec3i(-1, -1, 0), new Vec3i(1, -1, -1), new Vec3i(0, -1, -1), new Vec3i(-1, -1, -1), new Vec3i(1, 0, 1), new Vec3i(0, 0, 1), new Vec3i(-1, 0, 1), new Vec3i(1, 0, 0), new Vec3i(-1, 0, 0), new Vec3i(1, 0, -1), new Vec3i(0, 0, -1), new Vec3i(-1, 0, -1), new Vec3i(1, 1, 1), new Vec3i(0, 1, 1), new Vec3i(-1, 1, 1), new Vec3i(1, 1, 0), new Vec3i(0, 1, 0), new Vec3i(-1, 1, 0), new Vec3i(1, 1, -1), new Vec3i(0, 1, -1), new Vec3i(-1, 1, -1));
        this.selectedBlocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blocks")).description("Which blocks to select.")).defaultValue(Blocks.STONE, Blocks.DIRT, Blocks.GRASS_BLOCK).build());
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Selection mode.")).defaultValue(ListMode.Blacklist)).build());
        this.depth = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("depth")).description("Amount of iterations used to scan for similar blocks.")).defaultValue(3)).min(1).sliderRange(1, 15).build());
        this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("Delay between mining blocks.")).defaultValue(0)).min(0).sliderRange(0, 20).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Sends rotation packets to the server when mining.")).defaultValue(true)).build());
        this.swingHand = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swing-hand")).description("Swing hand client-side.")).defaultValue(true)).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Whether or not to render the block being mined.")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The color of the sides of the blocks being rendered.")).defaultValue(new SettingColor(204, 0, 0, 10)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The color of the lines of the blocks being rendered.")).defaultValue(new SettingColor(204, 0, 0, 255)).build());
        this.blockPool = new Pool<MyBlock>(() -> new MyBlock(this));
        this.blocks = new ArrayList<MyBlock>();
        this.foundBlockPositions = new ArrayList<BlockPos>();
        this.tick = 0;
    }

    @Override
    public void onDeactivate() {
        this.blockPool.freeAll(this.blocks);
        this.blocks.clear();
        this.foundBlockPositions.clear();
    }

    private boolean isMiningBlock(BlockPos pos) {
        for (MyBlock block : this.blocks) {
            if (!block.blockPos.equals((Object)pos)) continue;
            return true;
        }
        return false;
    }

    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        BlockState state = this.mc.level.getBlockState(event.blockPos);
        if (state.getDestroySpeed((BlockGetter)this.mc.level, event.blockPos) < 0.0f) {
            return;
        }
        if (this.mode.get() == ListMode.Whitelist && !this.selectedBlocks.get().contains(state.getBlock())) {
            return;
        }
        if (this.mode.get() == ListMode.Blacklist && this.selectedBlocks.get().contains(state.getBlock())) {
            return;
        }
        this.foundBlockPositions.clear();
        if (!this.isMiningBlock(event.blockPos)) {
            MyBlock block = this.blockPool.get();
            block.set(event);
            this.blocks.add(block);
            this.mineNearbyBlocks(block.originalBlock.asItem(), event.blockPos, event.direction, this.depth.get());
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        this.blocks.removeIf(MyBlock::shouldRemove);
        if (!this.blocks.isEmpty()) {
            if (this.tick < this.delay.get() && !this.blocks.getFirst().mining) {
                ++this.tick;
                return;
            }
            this.tick = 0;
            this.blocks.getFirst().mine();
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.render.get().booleanValue()) {
            for (MyBlock block : this.blocks) {
                block.render(event);
            }
        }
    }

    private void mineNearbyBlocks(Item item, BlockPos pos, Direction dir, int depth) {
        if (depth <= 0) {
            return;
        }
        if (this.foundBlockPositions.contains(pos)) {
            return;
        }
        this.foundBlockPositions.add(pos);
        if (Utils.distance(this.mc.player.getX() - 0.5, this.mc.player.getY() + (double)this.mc.player.getEyeHeight(this.mc.player.getPose()), this.mc.player.getZ() - 0.5, pos.getX(), pos.getY(), pos.getZ()) > this.mc.player.blockInteractionRange()) {
            return;
        }
        for (Vec3i neighbourOffset : this.blockNeighbours) {
            BlockPos neighbour = pos.offset(neighbourOffset);
            if (this.mc.level.getBlockState(neighbour).getBlock().asItem() != item) continue;
            MyBlock block = this.blockPool.get();
            block.set(neighbour, dir);
            this.blocks.add(block);
            this.mineNearbyBlocks(item, neighbour, dir, depth - 1);
        }
    }

    @Override
    public String getInfoString() {
        return this.mode.get().toString() + " (" + this.selectedBlocks.get().size() + ")";
    }

    public static enum ListMode {
        Whitelist,
        Blacklist;

    }

    private class MyBlock {
        public BlockPos blockPos;
        public Direction direction;
        public Block originalBlock;
        public boolean mining;
        final /* synthetic */ VeinMiner this$0;

        private MyBlock(VeinMiner veinMiner) {
            VeinMiner veinMiner2 = veinMiner;
            Objects.requireNonNull(veinMiner2);
            this.this$0 = veinMiner2;
        }

        public void set(StartBreakingBlockEvent event) {
            this.blockPos = event.blockPos;
            this.direction = event.direction;
            this.originalBlock = ((VeinMiner)this.this$0).mc.level.getBlockState(this.blockPos).getBlock();
            this.mining = false;
        }

        public void set(BlockPos pos, Direction dir) {
            this.blockPos = pos;
            this.direction = dir;
            this.originalBlock = ((VeinMiner)this.this$0).mc.level.getBlockState(pos).getBlock();
            this.mining = false;
        }

        public boolean shouldRemove() {
            return ((VeinMiner)this.this$0).mc.level.getBlockState(this.blockPos).getBlock() != this.originalBlock || Utils.distance(((VeinMiner)this.this$0).mc.player.getX() - 0.5, ((VeinMiner)this.this$0).mc.player.getY() + (double)((VeinMiner)this.this$0).mc.player.getEyeHeight(((VeinMiner)this.this$0).mc.player.getPose()), ((VeinMiner)this.this$0).mc.player.getZ() - 0.5, this.blockPos.getX() + this.direction.getStepX(), this.blockPos.getY() + this.direction.getStepY(), this.blockPos.getZ() + this.direction.getStepZ()) > ((VeinMiner)this.this$0).mc.player.blockInteractionRange();
        }

        public void mine() {
            if (!this.mining) {
                ((VeinMiner)this.this$0).mc.player.swing(InteractionHand.MAIN_HAND);
                this.mining = true;
            }
            if (this.this$0.rotate.get().booleanValue()) {
                Rotations.rotate(Rotations.getYaw(this.blockPos), Rotations.getPitch(this.blockPos), 50, this::updateBlockBreakingProgress);
            } else {
                this.updateBlockBreakingProgress();
            }
        }

        private void updateBlockBreakingProgress() {
            BlockUtils.breakBlock(this.blockPos, this.this$0.swingHand.get());
        }

        public void render(Render3DEvent event) {
            VoxelShape shape = ((VeinMiner)this.this$0).mc.level.getBlockState(this.blockPos).getShape((BlockGetter)((VeinMiner)this.this$0).mc.level, this.blockPos);
            double x1 = this.blockPos.getX();
            double y1 = this.blockPos.getY();
            double z1 = this.blockPos.getZ();
            double x2 = this.blockPos.getX() + 1;
            double y2 = this.blockPos.getY() + 1;
            double z2 = this.blockPos.getZ() + 1;
            if (!shape.isEmpty()) {
                x1 = (double)this.blockPos.getX() + shape.min(Direction.Axis.X);
                y1 = (double)this.blockPos.getY() + shape.min(Direction.Axis.Y);
                z1 = (double)this.blockPos.getZ() + shape.min(Direction.Axis.Z);
                x2 = (double)this.blockPos.getX() + shape.max(Direction.Axis.X);
                y2 = (double)this.blockPos.getY() + shape.max(Direction.Axis.Y);
                z2 = (double)this.blockPos.getZ() + shape.max(Direction.Axis.Z);
            }
            event.renderer.box(x1, y1, z1, x2, y2, z2, this.this$0.sideColor.get(), this.this$0.lineColor.get(), this.this$0.shapeMode.get(), 0);
        }
    }
}
