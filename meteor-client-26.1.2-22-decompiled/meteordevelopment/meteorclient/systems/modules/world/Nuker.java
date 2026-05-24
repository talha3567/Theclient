package meteordevelopment.meteorclient.systems.modules.world;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.events.entity.player.BlockBreakingCooldownEvent;
import meteordevelopment.meteorclient.events.meteor.KeyInputEvent;
import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Nuker
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgWhitelist;
    private final SettingGroup sgRender;
    private final Setting<Shape> shape;
    private final Setting<Mode> mode;
    private final Setting<Double> range;
    private final Setting<Integer> range_up;
    private final Setting<Integer> range_down;
    private final Setting<Integer> range_left;
    private final Setting<Integer> range_right;
    private final Setting<Integer> range_forward;
    private final Setting<Integer> range_back;
    private final Setting<Double> wallsRange;
    private final Setting<Integer> delay;
    private final Setting<Integer> maxBlocksPerTick;
    private final Setting<SortMode> sortMode;
    private final Setting<Boolean> packetMine;
    private final Setting<Boolean> suitableTools;
    private final Setting<Boolean> interact;
    private final Setting<Boolean> rotate;
    private final Setting<ListMode> listMode;
    private final Setting<List<Block>> blacklist;
    private final Setting<List<Block>> whitelist;
    private final Setting<Keybind> selectBlockBind;
    private final Setting<Boolean> swing;
    private final Setting<Boolean> enableRenderBounding;
    private final Setting<ShapeMode> shapeModeBox;
    private final Setting<SettingColor> sideColorBox;
    private final Setting<SettingColor> lineColorBox;
    private final Setting<Boolean> enableRenderBreaking;
    private final Setting<ShapeMode> shapeModeBreak;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final List<BlockPos> blocks;
    private final Set<BlockPos> interacted;
    private boolean firstBlock;
    private final BlockPos.MutableBlockPos lastBlockPos;
    private int timer;
    private int noBlockTimer;
    private final BlockPos.MutableBlockPos pos1;
    private final BlockPos.MutableBlockPos pos2;
    int maxh;
    int maxv;

    public Nuker() {
        super(Categories.World, "nuker", "Breaks blocks around you.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgWhitelist = this.settings.createGroup("Whitelist");
        this.sgRender = this.settings.createGroup("Render");
        this.shape = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape")).description("The shape of nuking algorithm.")).defaultValue(Shape.Sphere)).build());
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("The way the blocks are broken.")).defaultValue(Mode.Flatten)).build());
        this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("The break range.")).defaultValue(4.0).min(0.0).visible(() -> this.shape.get() != Shape.Cube)).build());
        this.range_up = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("up")).description("The break range.")).defaultValue(1)).min(0).visible(() -> this.shape.get() == Shape.Cube)).build());
        this.range_down = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("down")).description("The break range.")).defaultValue(1)).min(0).visible(() -> this.shape.get() == Shape.Cube)).build());
        this.range_left = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("left")).description("The break range.")).defaultValue(1)).min(0).visible(() -> this.shape.get() == Shape.Cube)).build());
        this.range_right = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("right")).description("The break range.")).defaultValue(1)).min(0).visible(() -> this.shape.get() == Shape.Cube)).build());
        this.range_forward = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("forward")).description("The break range.")).defaultValue(1)).min(0).visible(() -> this.shape.get() == Shape.Cube)).build());
        this.range_back = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("back")).description("The break range.")).defaultValue(1)).min(0).visible(() -> this.shape.get() == Shape.Cube)).build());
        this.wallsRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("walls-range")).description("Range in which to break when behind blocks.")).defaultValue(4.0).min(0.0).sliderMax(6.0).build());
        this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("Delay in ticks between breaking blocks.")).defaultValue(0)).build());
        this.maxBlocksPerTick = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("max-blocks-per-tick")).description("Maximum blocks to try to break per tick. Useful when insta mining.")).defaultValue(1)).min(1).build());
        this.sortMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("sort-mode")).description("The blocks you want to mine first.")).defaultValue(SortMode.Closest)).build());
        this.packetMine = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("packet-mine")).description("Attempt to instamine everything at once.")).defaultValue(false)).build());
        this.suitableTools = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-suitable-tools")).description("Only mines when using an appropriate for the block.")).defaultValue(false)).build());
        this.interact = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("interact")).description("Interacts with the block instead of mining.")).defaultValue(false)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Rotates server-side to the block being mined.")).defaultValue(true)).build());
        this.listMode = this.sgWhitelist.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("list-mode")).description("Selection mode.")).defaultValue(ListMode.Blacklist)).build());
        this.blacklist = this.sgWhitelist.add(((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blacklist")).description("The blocks you don't want to mine.")).visible(() -> this.listMode.get() == ListMode.Blacklist)).build());
        this.whitelist = this.sgWhitelist.add(((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("whitelist")).description("The blocks you want to mine.")).visible(() -> this.listMode.get() == ListMode.Whitelist)).build());
        this.selectBlockBind = this.sgWhitelist.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("select-block-bind")).description("Adds targeted block to list when this button is pressed.")).defaultValue(Keybind.none())).build());
        this.swing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swing")).description("Whether to swing hand client-side.")).defaultValue(true)).build());
        this.enableRenderBounding = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("bounding-box")).description("Enable rendering bounding box for Cube and Uniform Cube.")).defaultValue(true)).build());
        this.shapeModeBox = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("nuke-box-mode")).description("How the shape for the bounding box is rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColorBox = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color of the bounding box.")).defaultValue(new SettingColor(16, 106, 144, 100)).build());
        this.lineColorBox = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color of the bounding box.")).defaultValue(new SettingColor(16, 106, 144, 255)).build());
        this.enableRenderBreaking = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("broken-blocks")).description("Enable rendering bounding box for Cube and Uniform Cube.")).defaultValue(true)).build());
        this.shapeModeBreak = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("nuke-block-mode")).description("How the shapes for broken blocks are rendered.")).defaultValue(ShapeMode.Both)).visible(this.enableRenderBreaking::get)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color of the target block rendering.")).defaultValue(new SettingColor(255, 0, 0, 80)).visible(this.enableRenderBreaking::get)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color of the target block rendering.")).defaultValue(new SettingColor(255, 0, 0, 255)).visible(this.enableRenderBreaking::get)).build());
        this.blocks = new ArrayList<BlockPos>();
        this.interacted = new ObjectOpenHashSet();
        this.lastBlockPos = new BlockPos.MutableBlockPos();
        this.pos1 = new BlockPos.MutableBlockPos();
        this.pos2 = new BlockPos.MutableBlockPos();
        this.maxh = 0;
        this.maxv = 0;
    }

    @Override
    public void onActivate() {
        this.firstBlock = true;
        this.timer = 0;
        this.noBlockTimer = 0;
        this.interacted.clear();
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.enableRenderBounding.get().booleanValue() && this.shape.get() != Shape.Sphere && this.mode.get() != Mode.Smash) {
            int minX = Math.min(this.pos1.getX(), this.pos2.getX());
            int minY = Math.min(this.pos1.getY(), this.pos2.getY());
            int minZ = Math.min(this.pos1.getZ(), this.pos2.getZ());
            int maxX = Math.max(this.pos1.getX(), this.pos2.getX());
            int maxY = Math.max(this.pos1.getY(), this.pos2.getY());
            int maxZ = Math.max(this.pos1.getZ(), this.pos2.getZ());
            event.renderer.box(minX, minY, minZ, maxX, maxY, maxZ, this.sideColorBox.get(), this.lineColorBox.get(), this.shapeModeBox.get(), 0);
        }
    }

    @EventHandler
    private void onMouseClick(MouseClickEvent event) {
        if (event.action == KeyAction.Press) {
            this.addTargetedBlockToList();
        }
    }

    @EventHandler
    private void onKey(KeyInputEvent event) {
        if (event.action == KeyAction.Press) {
            this.addTargetedBlockToList();
        }
    }

    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        if (this.timer > 0) {
            --this.timer;
            return;
        }
        double pX = this.mc.player.getX();
        double pY = this.mc.player.getY();
        double pZ = this.mc.player.getZ();
        double rangeSq = Math.pow(this.range.get(), 2.0);
        BlockPos playerBlockPos = this.mc.player.blockPosition();
        if (this.shape.get() == Shape.UniformCube) {
            this.range.set(Double.valueOf(Math.round(this.range.get())));
        }
        double pX_ = pX;
        double pZ_ = pZ;
        int r = (int)Math.round(this.range.get());
        if (this.shape.get() == Shape.UniformCube) {
            this.pos1.set((pX_ += 1.0) - (double)r, pY - (double)r + 1.0, pZ - (double)r + 1.0);
            this.pos2.set(pX_ + (double)r - 1.0, pY + (double)r, pZ + (double)r);
            this.maxh = 0;
            this.maxv = 0;
        } else {
            Direction direction = this.mc.player.getDirection();
            switch (direction) {
                case SOUTH: {
                    this.pos1.set((pX_ += 1.0) - (double)(this.range_right.get() + 1), Math.ceil(pY) - (double)this.range_down.get().intValue(), (pZ_ += 1.0) - (double)(this.range_back.get() + 1));
                    this.pos2.set(pX_ + (double)this.range_left.get().intValue(), Math.ceil(pY + (double)this.range_up.get().intValue() + 1.0), pZ_ + (double)this.range_forward.get().intValue());
                    break;
                }
                case WEST: {
                    this.pos1.set(pX_ - (double)this.range_forward.get().intValue(), Math.ceil(pY) - (double)this.range_down.get().intValue(), pZ_ - (double)this.range_right.get().intValue());
                    this.pos2.set(pX_ + (double)this.range_back.get().intValue() + 1.0, Math.ceil(pY + (double)this.range_up.get().intValue() + 1.0), pZ_ + (double)this.range_left.get().intValue() + 1.0);
                    break;
                }
                case NORTH: {
                    this.pos1.set((pX_ += 1.0) - (double)(this.range_left.get() + 1), Math.ceil(pY) - (double)this.range_down.get().intValue(), (pZ_ += 1.0) - (double)(this.range_forward.get() + 1));
                    this.pos2.set(pX_ + (double)this.range_right.get().intValue(), Math.ceil(pY + (double)this.range_up.get().intValue() + 1.0), pZ_ + (double)this.range_back.get().intValue());
                    break;
                }
                case EAST: {
                    this.pos1.set((pX_ += 1.0) - (double)(this.range_back.get() + 1), Math.ceil(pY) - (double)this.range_down.get().intValue(), pZ_ - (double)this.range_left.get().intValue());
                    this.pos2.set(pX_ + (double)this.range_forward.get().intValue(), Math.ceil(pY + (double)this.range_up.get().intValue() + 1.0), pZ_ + (double)this.range_right.get().intValue() + 1.0);
                }
            }
            this.maxh = 1 + Math.max(Math.max(Math.max(this.range_back.get(), this.range_right.get()), this.range_forward.get()), this.range_left.get());
            this.maxv = 1 + Math.max(this.range_up.get(), this.range_down.get());
        }
        if (this.mode.get() == Mode.Flatten) {
            this.pos1.setY((int)Math.floor(pY + 0.5));
        }
        AABB box = new AABB(this.pos1.getCenter(), this.pos2.getCenter());
        BlockIterator.register(Math.max((int)Math.ceil(this.range.get() + 1.0), this.maxh), Math.max((int)Math.ceil(this.range.get()), this.maxv), (blockPos, blockState) -> {
            Vec3 center = blockPos.getCenter();
            switch (this.shape.get().ordinal()) {
                case 2: {
                    if (!(Utils.squaredDistance(pX, pY, pZ, center.x(), center.y(), center.z()) > rangeSq)) break;
                    return;
                }
                case 1: {
                    if (!((double)Nuker.chebyshevDist(playerBlockPos.getX(), playerBlockPos.getY(), playerBlockPos.getZ(), blockPos.getX(), blockPos.getY(), blockPos.getZ()) >= this.range.get())) break;
                    return;
                }
                case 0: {
                    if (box.contains(center)) break;
                    return;
                }
            }
            if (this.mode.get() == Mode.Flatten && (double)blockPos.getY() + 0.5 < pY) {
                return;
            }
            if (this.mode.get() == Mode.Smash && blockState.getDestroySpeed((BlockGetter)this.mc.level, blockPos) != 0.0f) {
                return;
            }
            if (this.suitableTools.get().booleanValue() && !this.interact.get().booleanValue() && !this.mc.player.getMainHandItem().isCorrectToolForDrops(blockState)) {
                return;
            }
            if (!BlockUtils.canBreak(blockPos, blockState) && !this.interact.get().booleanValue()) {
                return;
            }
            if (this.isOutOfRange((BlockPos)blockPos)) {
                return;
            }
            if (this.listMode.get() == ListMode.Whitelist && !this.whitelist.get().contains(blockState.getBlock())) {
                return;
            }
            if (this.listMode.get() == ListMode.Blacklist && this.blacklist.get().contains(blockState.getBlock())) {
                return;
            }
            if (this.interact.get().booleanValue() && this.interacted.contains(blockPos)) {
                return;
            }
            this.blocks.add(blockPos.immutable());
        });
        BlockIterator.after(() -> {
            if (this.sortMode.get() == SortMode.TopDown) {
                this.blocks.sort(Comparator.comparingDouble(value -> -value.getY()));
            } else if (this.sortMode.get() != SortMode.None) {
                this.blocks.sort(Comparator.comparingDouble(value -> Utils.squaredDistance(pX, pY, pZ, (double)value.getX() + 0.5, (double)value.getY() + 0.5, (double)value.getZ() + 0.5) * (double)(this.sortMode.get() == SortMode.Closest ? 1 : -1)));
            }
            if (this.blocks.isEmpty()) {
                this.interacted.clear();
                if (this.noBlockTimer++ >= this.delay.get()) {
                    this.firstBlock = true;
                }
                return;
            }
            this.noBlockTimer = 0;
            if (!this.firstBlock && !this.lastBlockPos.equals((Object)this.blocks.getFirst())) {
                this.timer = this.delay.get();
                this.firstBlock = false;
                this.lastBlockPos.set((Vec3i)this.blocks.getFirst());
                if (this.timer > 0) {
                    return;
                }
            }
            int count = 0;
            for (BlockPos block : this.blocks) {
                if (count >= this.maxBlocksPerTick.get()) break;
                boolean canInstaMine = BlockUtils.canInstaBreak(block);
                if (this.rotate.get().booleanValue()) {
                    Rotations.rotate(Rotations.getYaw(block), Rotations.getPitch(block), () -> this.breakBlock(block));
                } else {
                    this.breakBlock(block);
                }
                if (this.enableRenderBreaking.get().booleanValue()) {
                    RenderUtils.renderTickingBlock(block, this.sideColor.get(), this.lineColor.get(), this.shapeModeBreak.get(), 0, 8, true, false);
                }
                this.lastBlockPos.set((Vec3i)block);
                ++count;
                if (canInstaMine || this.packetMine.get().booleanValue()) continue;
                break;
            }
            this.firstBlock = false;
            this.blocks.clear();
        });
    }

    private void breakBlock(BlockPos blockPos) {
        if (this.interact.get().booleanValue()) {
            BlockUtils.interact(new BlockHitResult(blockPos.getCenter(), BlockUtils.getDirection(blockPos), blockPos, true), InteractionHand.MAIN_HAND, this.swing.get());
            this.interacted.add(blockPos);
        } else if (this.packetMine.get().booleanValue()) {
            this.mc.gameMode.startPrediction(this.mc.level, sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, BlockUtils.getDirection(blockPos), sequence));
            if (this.swing.get().booleanValue()) {
                this.mc.player.swing(InteractionHand.MAIN_HAND);
            } else {
                this.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            }
            this.mc.gameMode.startPrediction(this.mc.level, sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockPos, BlockUtils.getDirection(blockPos), sequence));
        } else {
            BlockUtils.breakBlock(blockPos, this.swing.get());
        }
    }

    private boolean isOutOfRange(BlockPos blockPos) {
        Vec3 pos = blockPos.getCenter();
        ClipContext clipContext = new ClipContext(this.mc.player.getEyePosition(), pos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this.mc.player);
        BlockHitResult result = this.mc.level.clip(clipContext);
        if (result == null || !result.getBlockPos().equals((Object)blockPos)) {
            return !PlayerUtils.isWithin(pos, (double)this.wallsRange.get());
        }
        return false;
    }

    private void addTargetedBlockToList() {
        if (!this.selectBlockBind.get().isPressed() || this.mc.screen != null) {
            return;
        }
        HitResult hitResult = this.mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos pos = ((BlockHitResult)hitResult).getBlockPos();
        Block targetBlock = this.mc.level.getBlockState(pos).getBlock();
        List<Block> list = this.listMode.get() == ListMode.Whitelist ? this.whitelist.get() : this.blacklist.get();
        String modeName = this.listMode.get().name();
        if (list.contains(targetBlock)) {
            list.remove(targetBlock);
            this.info("Removed " + Names.get(targetBlock) + " from " + modeName, new Object[0]);
        } else {
            list.add(targetBlock);
            this.info("Added " + Names.get(targetBlock) + " to " + modeName, new Object[0]);
        }
    }

    @EventHandler(priority=200)
    private void onBlockBreakingCooldown(BlockBreakingCooldownEvent event) {
        event.cooldown = 0;
    }

    public static int chebyshevDist(int x1, int y1, int z1, int x2, int y2, int z2) {
        int dX = Math.abs(x2 - x1);
        int dY = Math.abs(y2 - y1);
        int dZ = Math.abs(z2 - z1);
        return Math.max(Math.max(dX, dY), dZ);
    }

    public static enum Shape {
        Cube,
        UniformCube,
        Sphere;

    }

    public static enum Mode {
        All,
        Flatten,
        Smash;

    }

    public static enum SortMode {
        None,
        Closest,
        Furthest,
        TopDown;

    }

    public static enum ListMode {
        Whitelist,
        Blacklist;

    }
}
