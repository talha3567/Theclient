package meteordevelopment.meteorclient.systems.modules.movement;

import com.google.common.collect.Streams;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Scaffold
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<List<Block>> blocks;
    private final Setting<ListMode> blocksFilter;
    private final Setting<Boolean> fastTower;
    private final Setting<Double> towerSpeed;
    private final Setting<Boolean> whileMoving;
    private final Setting<Boolean> onlyOnClick;
    private final Setting<Boolean> renderSwing;
    private final Setting<Boolean> autoSwitch;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> airPlace;
    private final Setting<Double> aheadDistance;
    private final Setting<Double> placeRange;
    private final Setting<Double> radius;
    private final Setting<Integer> blocksPerTick;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final BlockPos.MutableBlockPos bp;

    public Scaffold() {
        super(Categories.Movement, "scaffold", "Automatically places blocks under you.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.blocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blocks")).description("Selected blocks.")).build());
        this.blocksFilter = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("blocks-filter")).description("How to use the block list setting")).defaultValue(ListMode.Blacklist)).build());
        this.fastTower = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fast-tower")).description("Whether or not to scaffold upwards faster.")).defaultValue(false)).build());
        this.towerSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("tower-speed")).description("The speed at which to tower.")).defaultValue(0.5).min(0.0).sliderMax(1.0).visible(this.fastTower::get)).build());
        this.whileMoving = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("while-moving")).description("Allows you to tower while moving.")).defaultValue(false)).visible(this.fastTower::get)).build());
        this.onlyOnClick = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-on-click")).description("Only places blocks when holding right click.")).defaultValue(false)).build());
        this.renderSwing = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swing")).description("Renders your client-side swing.")).defaultValue(false)).build());
        this.autoSwitch = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-switch")).description("Automatically swaps to a block before placing.")).defaultValue(true)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Rotates towards the blocks being placed.")).defaultValue(true)).build());
        this.airPlace = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("air-place")).description("Allow air place. This also allows you to modify scaffold radius.")).defaultValue(false)).build());
        this.aheadDistance = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("ahead-distance")).description("How far ahead to place blocks.")).defaultValue(0.0).min(0.0).sliderMax(1.0).visible(() -> this.airPlace.get() == false)).build());
        this.placeRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("closest-block-range")).description("How far can scaffold place blocks when you are in air.")).defaultValue(4.0).min(0.0).sliderMax(8.0).visible(() -> this.airPlace.get() == false)).build());
        this.radius = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("radius")).description("Scaffold radius.")).defaultValue(0.0).min(0.0).max(6.0).visible(this.airPlace::get)).build());
        this.blocksPerTick = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("blocks-per-tick")).description("How many blocks to place in one tick.")).defaultValue(3)).min(1).visible(this.airPlace::get)).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Whether to render blocks that have been placed.")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).visible(this.render::get)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color of the target block rendering.")).defaultValue(new SettingColor(197, 137, 232, 10)).visible(this.render::get)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color of the target block rendering.")).defaultValue(new SettingColor(197, 137, 232)).visible(this.render::get)).build());
        this.bp = new BlockPos.MutableBlockPos();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.onlyOnClick.get().booleanValue() && !this.mc.options.keyUse.isDown()) {
            return;
        }
        Vec3 vec = this.mc.player.position().add(this.mc.player.getDeltaMovement()).add(0.0, -0.75, 0.0);
        if (this.airPlace.get().booleanValue()) {
            this.bp.set(vec.x(), vec.y(), vec.z());
        } else {
            Vec3 pos = this.mc.player.position();
            if (this.aheadDistance.get() != 0.0 && !this.towering() && !this.mc.level.getBlockState(this.mc.player.blockPosition().below()).getCollisionShape((BlockGetter)this.mc.level, this.mc.player.blockPosition()).isEmpty()) {
                Vec3 dir = Vec3.directionFromRotation((float)0.0f, (float)this.mc.player.getYRot()).multiply(this.aheadDistance.get().doubleValue(), 0.0, this.aheadDistance.get().doubleValue());
                if (this.mc.options.keyUp.isDown()) {
                    pos = pos.add(dir.x, 0.0, dir.z);
                }
                if (this.mc.options.keyDown.isDown()) {
                    pos = pos.add(-dir.x, 0.0, -dir.z);
                }
                if (this.mc.options.keyLeft.isDown()) {
                    pos = pos.add(dir.z, 0.0, -dir.x);
                }
                if (this.mc.options.keyRight.isDown()) {
                    pos = pos.add(-dir.z, 0.0, dir.x);
                }
            }
            this.bp.set(pos.x, vec.y, pos.z);
        }
        if (this.mc.options.keyShift.isDown() && !this.mc.options.keyJump.isDown() && this.mc.player.getY() + vec.y > -1.0) {
            this.bp.setY(this.bp.getY() - 1);
        }
        if (this.bp.getY() >= this.mc.player.blockPosition().getY()) {
            this.bp.setY(this.mc.player.blockPosition().getY() - 1);
        }
        BlockPos targetBlock = this.bp.immutable();
        if (!this.airPlace.get().booleanValue() && BlockUtils.getPlaceSide((BlockPos)this.bp) == null) {
            Vec3 pos = this.mc.player.position();
            pos = pos.add(0.0, (double)-0.98f, 0.0);
            pos.add(this.mc.player.getDeltaMovement());
            ArrayList<BlockPos> blockPosArray = new ArrayList<BlockPos>();
            int x = (int)(this.mc.player.getX() - this.placeRange.get());
            while ((double)x < this.mc.player.getX() + this.placeRange.get()) {
                int z = (int)(this.mc.player.getZ() - this.placeRange.get());
                while ((double)z < this.mc.player.getZ() + this.placeRange.get()) {
                    int y = (int)Math.max((double)this.mc.level.getMinY(), this.mc.player.getY() - this.placeRange.get());
                    while ((double)y < Math.min((double)this.mc.level.getHeight(), this.mc.player.getY() + this.placeRange.get())) {
                        this.bp.set(x, y, z);
                        if (BlockUtils.getPlaceSide((BlockPos)this.bp) != null && BlockUtils.canPlace((BlockPos)this.bp) && !(this.mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf((Vec3i)this.bp.relative(BlockUtils.getClosestPlaceSide((BlockPos)this.bp)))) > 36.0)) {
                            blockPosArray.add(new BlockPos((Vec3i)this.bp));
                        }
                        ++y;
                    }
                    ++z;
                }
                ++x;
            }
            if (blockPosArray.isEmpty()) {
                return;
            }
            blockPosArray.sort(Comparator.comparingDouble(blockPos -> blockPos.distSqr((Vec3i)targetBlock)));
            this.bp.set((Vec3i)blockPosArray.getFirst());
        }
        if (this.airPlace.get().booleanValue()) {
            ArrayList<BlockPos> blocks = new ArrayList<BlockPos>();
            int x = (int)((double)this.bp.getX() - this.radius.get());
            while ((double)x <= (double)this.bp.getX() + this.radius.get()) {
                int z = (int)((double)this.bp.getZ() - this.radius.get());
                while ((double)z <= (double)this.bp.getZ() + this.radius.get()) {
                    BlockPos blockPos2 = BlockPos.containing((double)x, (double)this.bp.getY(), (double)z);
                    if (this.mc.player.position().distanceTo(Vec3.atCenterOf((Vec3i)blockPos2)) <= this.radius.get() || x == this.bp.getX() && z == this.bp.getZ()) {
                        blocks.add(blockPos2);
                    }
                    ++z;
                }
                ++x;
            }
            if (!blocks.isEmpty()) {
                blocks.sort(Comparator.comparingDouble(PlayerUtils::squaredDistanceTo));
                int counter = 0;
                for (BlockPos block : blocks) {
                    if (this.place(block)) {
                        ++counter;
                    }
                    if (counter < this.blocksPerTick.get()) continue;
                    break;
                }
            }
        } else {
            this.place((BlockPos)this.bp);
        }
        FindItemResult result = InvUtils.findInHotbar(itemStack -> this.validItem((ItemStack)itemStack, (BlockPos)this.bp));
        if (this.fastTower.get().booleanValue() && this.mc.options.keyJump.isDown() && !this.mc.options.keyShift.isDown() && result.found() && (this.autoSwitch.get().booleanValue() || result.getHand() != null)) {
            Vec3 velocity = this.mc.player.getDeltaMovement();
            AABB playerBox = this.mc.player.getBoundingBox();
            if (Streams.stream(this.mc.level.getBlockCollisions((Entity)this.mc.player, playerBox.move(0.0, 1.0, 0.0))).toList().isEmpty()) {
                if (this.whileMoving.get().booleanValue() || !PlayerUtils.isMoving()) {
                    velocity = new Vec3(velocity.x, this.towerSpeed.get().doubleValue(), velocity.z);
                }
                this.mc.player.setDeltaMovement(velocity);
            } else {
                this.mc.player.setDeltaMovement(velocity.x, Math.ceil(this.mc.player.getY()) - this.mc.player.getY(), velocity.z);
                this.mc.player.setOnGround(true);
            }
        }
    }

    public boolean scaffolding() {
        return this.isActive() && (this.onlyOnClick.get() == false || this.onlyOnClick.get() != false && this.mc.options.keyUse.isDown());
    }

    public boolean towering() {
        FindItemResult result = InvUtils.findInHotbar(itemStack -> this.validItem((ItemStack)itemStack, (BlockPos)this.bp));
        return !(!this.scaffolding() || this.fastTower.get() == false || !this.mc.options.keyJump.isDown() || this.mc.options.keyShift.isDown() || this.whileMoving.get() == false && PlayerUtils.isMoving() || !result.found() || this.autoSwitch.get() == false && result.getHand() == null);
    }

    private boolean validItem(ItemStack itemStack, BlockPos pos) {
        if (!(itemStack.getItem() instanceof BlockItem)) {
            return false;
        }
        Block block = ((BlockItem)itemStack.getItem()).getBlock();
        if (this.blocksFilter.get() == ListMode.Blacklist && this.blocks.get().contains(block)) {
            return false;
        }
        if (this.blocksFilter.get() == ListMode.Whitelist && !this.blocks.get().contains(block)) {
            return false;
        }
        if (!Block.isShapeFullBlock((VoxelShape)block.defaultBlockState().getCollisionShape((BlockGetter)this.mc.level, pos))) {
            return false;
        }
        return !(block instanceof FallingBlock) || !FallingBlock.isFree((BlockState)this.mc.level.getBlockState(pos));
    }

    private boolean place(BlockPos bp) {
        FindItemResult item = InvUtils.findInHotbar(itemStack -> this.validItem((ItemStack)itemStack, bp));
        if (!item.found()) {
            return false;
        }
        if (item.getHand() == null && !this.autoSwitch.get().booleanValue()) {
            return false;
        }
        if (BlockUtils.place(bp, item, this.rotate.get(), 50, this.renderSwing.get(), true)) {
            if (this.render.get().booleanValue()) {
                RenderUtils.renderTickingBlock(bp.immutable(), this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0, 8, true, false);
            }
            return true;
        }
        return false;
    }

    public static enum ListMode {
        Whitelist,
        Blacklist;

    }
}
