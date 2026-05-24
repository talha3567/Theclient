package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.DirectionAccessor;
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
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.utils.world.Dir;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class HoleFiller
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgSmart;
    private final SettingGroup sgRender;
    private final Setting<List<Block>> blocks;
    private final Setting<Integer> searchRadius;
    private final Setting<Double> placeRange;
    private final Setting<Double> placeWallsRange;
    private final Setting<Boolean> doubles;
    private final Setting<Boolean> rotate;
    private final Setting<Integer> placeDelay;
    private final Setting<Integer> blocksPerTick;
    private final Setting<Boolean> smart;
    public final Setting<Keybind> forceFill;
    private final Setting<Boolean> predictMovement;
    private final Setting<Double> ticksToPredict;
    private final Setting<Boolean> ignoreSafe;
    private final Setting<Boolean> onlyMoving;
    private final Setting<Double> targetRange;
    private final Setting<Double> feetRange;
    private final Setting<Boolean> swing;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<SettingColor> nextSideColor;
    private final Setting<SettingColor> nextLineColor;
    private final List<Player> targets;
    private final List<Hole> holes;
    private int timer;

    public HoleFiller() {
        super(Categories.Combat, "hole-filler", "Fills holes with specified blocks.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgSmart = this.settings.createGroup("Smart");
        this.sgRender = this.settings.createGroup("Render");
        this.blocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blocks")).description("Which blocks can be used to fill holes.")).defaultValue(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK, Blocks.RESPAWN_ANCHOR, Blocks.COBWEB).build());
        this.searchRadius = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("search-radius")).description("Horizontal radius in which to search for holes.")).defaultValue(5)).min(0).sliderMax(6).build());
        this.placeRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("place-range")).description("How far away from the player you can place a block.")).defaultValue(4.5).min(0.0).sliderMax(6.0).build());
        this.placeWallsRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("walls-range")).description("How far away from the player you can place a block behind walls.")).defaultValue(4.5).min(0.0).sliderMax(6.0).build());
        this.doubles = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("doubles")).description("Fills double holes.")).defaultValue(true)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Automatically rotates towards the holes being filled.")).defaultValue(false)).build());
        this.placeDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("place-delay")).description("The ticks delay between placement.")).defaultValue(1)).min(0).build());
        this.blocksPerTick = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("blocks-per-tick")).description("How many blocks to place in one tick.")).defaultValue(3)).min(1).build());
        this.smart = this.sgSmart.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("smart")).description("Take more factors into account before filling a hole.")).defaultValue(true)).build());
        this.forceFill = this.sgSmart.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("force-fill")).description("Fills all holes around you regardless of target checks.")).defaultValue(Keybind.none())).visible(this.smart::get)).build());
        this.predictMovement = this.sgSmart.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("predict-movement")).description("Predict target movement to account for ping.")).defaultValue(true)).visible(this.smart::get)).build());
        this.ticksToPredict = this.sgSmart.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("ticks-to-predict")).description("How many ticks ahead we should predict for.")).defaultValue(10.0).min(1.0).sliderMax(30.0).visible(() -> this.smart.get() != false && this.predictMovement.get() != false)).build());
        this.ignoreSafe = this.sgSmart.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-safe")).description("Ignore players in safe holes.")).defaultValue(true)).visible(this.smart::get)).build());
        this.onlyMoving = this.sgSmart.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-moving")).description("Ignore players if they're standing still.")).defaultValue(true)).visible(this.smart::get)).build());
        this.targetRange = this.sgSmart.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("target-range")).description("How far away to target players.")).defaultValue(7.0).min(0.0).sliderMin(1.0).sliderMax(10.0).visible(this.smart::get)).build());
        this.feetRange = this.sgSmart.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("feet-range")).description("How far from a hole a player's feet must be to fill it.")).defaultValue(1.5).min(0.0).sliderMax(4.0).visible(this.smart::get)).build());
        this.swing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swing")).description("Swing the player's hand when placing.")).defaultValue(true)).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Renders an overlay where blocks will be placed.")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).visible(this.render::get)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color of the target block rendering.")).defaultValue(new SettingColor(197, 137, 232, 10)).visible(() -> this.render.get() != false && this.shapeMode.get().sides())).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color of the target block rendering.")).defaultValue(new SettingColor(197, 137, 232)).visible(() -> this.render.get() != false && this.shapeMode.get().lines())).build());
        this.nextSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("next-side-color")).description("The side color of the next block to be placed.")).defaultValue(new SettingColor(227, 196, 245, 10)).visible(() -> this.render.get() != false && this.shapeMode.get().sides())).build());
        this.nextLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("next-line-color")).description("The line color of the next block to be placed.")).defaultValue(new SettingColor(5, 139, 221)).visible(() -> this.render.get() != false && this.shapeMode.get().lines())).build());
        this.targets = new ArrayList<Player>();
        this.holes = new ArrayList<Hole>();
    }

    @Override
    public void onActivate() {
        this.timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.smart.get().booleanValue()) {
            this.setTargets();
        }
        this.holes.clear();
        FindItemResult block = InvUtils.findInHotbar(itemStack -> this.blocks.get().contains(Block.byItem((Item)itemStack.getItem())));
        if (!block.found()) {
            return;
        }
        BlockIterator.register(this.searchRadius.get(), this.searchRadius.get(), (blockPos, blockState) -> {
            if (!this.validHole((BlockPos)blockPos)) {
                return;
            }
            int surroundBlocks = 0;
            Direction air = null;
            for (Direction direction : Direction.values()) {
                if (direction == Direction.UP) continue;
                BlockState state = this.mc.level.getBlockState(blockPos.relative(direction));
                if (state.getBlock().getExplosionResistance() >= 600.0f) {
                    ++surroundBlocks;
                } else {
                    if (direction == Direction.DOWN) {
                        return;
                    }
                    if (this.validHole(blockPos.relative(direction)) && air == null) {
                        for (Direction dir : Direction.values()) {
                            if (dir == direction.getOpposite() || dir == Direction.UP) continue;
                            BlockState state1 = this.mc.level.getBlockState(blockPos.relative(direction).relative(dir));
                            if (state1.getBlock().getExplosionResistance() >= 600.0f) {
                                ++surroundBlocks;
                                continue;
                            }
                            return;
                        }
                        air = direction;
                    }
                }
                if (surroundBlocks == 5 && air == null) {
                    this.holes.add(new Hole((BlockPos)blockPos, 0));
                    continue;
                }
                if (surroundBlocks != 8 || !this.doubles.get().booleanValue() || air == null) continue;
                this.holes.add(new Hole((BlockPos)blockPos, Dir.get(air)));
            }
        });
        BlockIterator.after(() -> {
            if (this.timer > 0 || this.holes.isEmpty()) {
                return;
            }
            int placedCount = 0;
            for (Hole hole : this.holes) {
                if (placedCount >= this.blocksPerTick.get() || !BlockUtils.place((BlockPos)hole.blockPos, block, this.rotate.get(), 10, this.swing.get(), true)) continue;
                ++placedCount;
            }
            this.timer = this.placeDelay.get();
        });
        --this.timer;
    }

    @EventHandler(priority=100)
    private void onRender(Render3DEvent event) {
        if (!this.render.get().booleanValue() || this.holes.isEmpty()) {
            return;
        }
        for (int i = 0; i < this.holes.size(); ++i) {
            boolean isNext = i < this.blocksPerTick.get();
            Color side = isNext ? (Color)this.nextSideColor.get() : (Color)this.sideColor.get();
            Color line = isNext ? (Color)this.nextLineColor.get() : (Color)this.lineColor.get();
            Hole hole = this.holes.get(i);
            event.renderer.box((BlockPos)hole.blockPos, side, line, this.shapeMode.get(), (int)hole.exclude);
        }
    }

    private boolean validHole(BlockPos blockPos) {
        if (!BlockUtils.canPlace(blockPos)) {
            return false;
        }
        if (!this.mc.level.getBlockState(blockPos.above()).canBeReplaced()) {
            return false;
        }
        if (this.isOutOfRange(blockPos)) {
            return false;
        }
        if (!this.smart.get().booleanValue() || this.forceFill.get().isPressed()) {
            return true;
        }
        return this.targets.stream().anyMatch(target -> target.getY() > (double)blockPos.getY() && this.isCloseToHolePos((Player)target, blockPos));
    }

    private void setTargets() {
        this.targets.clear();
        for (Player player : this.mc.level.players()) {
            if (player.distanceToSqr((Entity)this.mc.player) > Math.pow(this.targetRange.get(), 2.0) || player.isCreative() || player == this.mc.player || player.isDeadOrDying() || !Friends.get().shouldAttack(player) || this.ignoreSafe.get().booleanValue() && this.isSurrounded(player) || this.onlyMoving.get().booleanValue() && (player.getX() - player.xo != 0.0 || player.getY() - player.yo != 0.0 || player.getZ() - player.zo != 0.0)) continue;
            this.targets.add(player);
        }
    }

    private boolean isSurrounded(Player target) {
        for (Direction dir : DirectionAccessor.meteor$getHorizontal()) {
            BlockPos blockPos = target.blockPosition().relative(dir);
            Block block = this.mc.level.getBlockState(blockPos).getBlock();
            if (!(block.getExplosionResistance() < 600.0f)) continue;
            return false;
        }
        return true;
    }

    private boolean isOutOfRange(BlockPos blockPos) {
        Vec3 pos = blockPos.getCenter().add(0.0, 0.499, 0.0);
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

    private boolean isCloseToHolePos(Player target, BlockPos blockPos) {
        double k;
        double j;
        double i;
        double distance;
        Vec3 pos = target.position();
        if (this.predictMovement.get().booleanValue()) {
            double dx = target.getX() - target.xo;
            double dy = target.getY() - target.yo;
            double dz = target.getZ() - target.zo;
            pos = pos.add(dx * this.ticksToPredict.get(), dy * this.ticksToPredict.get(), dz * this.ticksToPredict.get());
        }
        return (distance = Math.sqrt((i = pos.x - ((double)blockPos.getX() + 0.5)) * i + (j = pos.y - ((double)blockPos.getY() + 1.0)) * j + (k = pos.z - ((double)blockPos.getZ() + 0.5)) * k)) < this.feetRange.get();
    }

    private static class Hole {
        private final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        private final byte exclude;

        public Hole(BlockPos blockPos, byte exclude) {
            this.blockPos.set((Vec3i)blockPos);
            this.exclude = exclude;
        }
    }
}
