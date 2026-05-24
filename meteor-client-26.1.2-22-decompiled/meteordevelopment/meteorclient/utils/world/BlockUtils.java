package meteordevelopment.meteorclient.utils.world;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.InstantRebreak;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CartographyTableBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.LoomBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockUtils {
    public static boolean breaking;
    private static boolean breakingThisTick;
    private static final ThreadLocal<BlockPos.MutableBlockPos> EXPOSED_POS;

    private BlockUtils() {
    }

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(BlockUtils.class);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, int rotationPriority) {
        return BlockUtils.place(blockPos, findItemResult, rotationPriority, true);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority) {
        return BlockUtils.place(blockPos, findItemResult, rotate, rotationPriority, true);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean checkEntities) {
        return BlockUtils.place(blockPos, findItemResult, rotate, rotationPriority, true, checkEntities);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, int rotationPriority, boolean checkEntities) {
        return BlockUtils.place(blockPos, findItemResult, true, rotationPriority, true, checkEntities);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean swingHand, boolean checkEntities) {
        return BlockUtils.place(blockPos, findItemResult, rotate, rotationPriority, swingHand, checkEntities, true);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean swingHand, boolean checkEntities, boolean swapBack) {
        if (findItemResult.isOffhand()) {
            return BlockUtils.place(blockPos, InteractionHand.OFF_HAND, MeteorClient.mc.player.getInventory().getSelectedSlot(), rotate, rotationPriority, swingHand, checkEntities, swapBack);
        }
        if (findItemResult.isHotbar()) {
            return BlockUtils.place(blockPos, InteractionHand.MAIN_HAND, findItemResult.slot(), rotate, rotationPriority, swingHand, checkEntities, swapBack);
        }
        return false;
    }

    public static boolean place(BlockPos blockPos, InteractionHand hand, int slot, boolean rotate, int rotationPriority, boolean swingHand, boolean checkEntities, boolean swapBack) {
        BlockPos neighbour;
        if (slot < 0 || slot > 8) {
            return false;
        }
        Block toPlace = Blocks.OBSIDIAN;
        ItemStack i = hand == InteractionHand.MAIN_HAND ? MeteorClient.mc.player.getInventory().getItem(slot) : MeteorClient.mc.player.getInventory().getItem(40);
        Item item = i.getItem();
        if (item instanceof BlockItem) {
            BlockItem blockItem = (BlockItem)item;
            toPlace = blockItem.getBlock();
        }
        if (!BlockUtils.canPlaceBlock(blockPos, checkEntities, toPlace)) {
            return false;
        }
        Vec3 hitPos = Vec3.atCenterOf((Vec3i)blockPos);
        Direction side = BlockUtils.getPlaceSide(blockPos);
        if (side == null) {
            side = Direction.UP;
            neighbour = blockPos;
        } else {
            neighbour = blockPos.relative(side);
            hitPos = hitPos.add((double)side.getStepX() * 0.5, (double)side.getStepY() * 0.5, (double)side.getStepZ() * 0.5);
        }
        BlockHitResult bhr = new BlockHitResult(hitPos, side.getOpposite(), neighbour, false);
        if (rotate) {
            Rotations.rotate(Rotations.getYaw(hitPos), Rotations.getPitch(hitPos), rotationPriority, () -> {
                InvUtils.swap(slot, swapBack);
                BlockUtils.interact(bhr, hand, swingHand);
                if (swapBack) {
                    InvUtils.swapBack();
                }
            });
        } else {
            InvUtils.swap(slot, swapBack);
            BlockUtils.interact(bhr, hand, swingHand);
            if (swapBack) {
                InvUtils.swapBack();
            }
        }
        return true;
    }

    public static void interact(BlockHitResult blockHitResult, InteractionHand hand, boolean swing) {
        boolean wasSneaking = MeteorClient.mc.player.isShiftKeyDown();
        MeteorClient.mc.player.setShiftKeyDown(false);
        InteractionResult result = MeteorClient.mc.gameMode.useItemOn(MeteorClient.mc.player, hand, blockHitResult);
        if (result.consumesAction()) {
            if (swing) {
                MeteorClient.mc.player.swing(hand);
            } else {
                MeteorClient.mc.getConnection().send((Packet)new ServerboundSwingPacket(hand));
            }
        }
        MeteorClient.mc.player.setShiftKeyDown(wasSneaking);
    }

    public static boolean canPlaceBlock(BlockPos blockPos, boolean checkEntities, Block block) {
        if (blockPos == null) {
            return false;
        }
        if (!Level.isInSpawnableBounds((BlockPos)blockPos)) {
            return false;
        }
        if (!MeteorClient.mc.level.getBlockState(blockPos).canBeReplaced()) {
            return false;
        }
        return !checkEntities || MeteorClient.mc.level.isUnobstructed(block.defaultBlockState(), blockPos, CollisionContext.empty());
    }

    public static boolean canPlace(BlockPos blockPos, boolean checkEntities) {
        return BlockUtils.canPlaceBlock(blockPos, checkEntities, Blocks.OBSIDIAN);
    }

    public static boolean canPlace(BlockPos blockPos) {
        return BlockUtils.canPlace(blockPos, true);
    }

    public static Direction getPlaceSide(BlockPos blockPos) {
        Vec3 lookVec = blockPos.getCenter().subtract(MeteorClient.mc.player.getEyePosition());
        double bestRelevancy = -1.7976931348623157E308;
        Direction bestSide = null;
        for (Direction side : Direction.values()) {
            double relevancy;
            BlockPos neighbor = blockPos.relative(side);
            BlockState state = MeteorClient.mc.level.getBlockState(neighbor);
            if (state.isAir() || BlockUtils.isClickable(state.getBlock()) || !state.getFluidState().isEmpty() || !((relevancy = side.getAxis().choose(lookVec.x(), lookVec.y(), lookVec.z()) * (double)side.getAxisDirection().getStep()) > bestRelevancy)) continue;
            bestRelevancy = relevancy;
            bestSide = side;
        }
        return bestSide;
    }

    public static Direction getClosestPlaceSide(BlockPos blockPos) {
        return BlockUtils.getClosestPlaceSide(blockPos, MeteorClient.mc.player.getEyePosition());
    }

    public static Direction getClosestPlaceSide(BlockPos blockPos, Vec3 pos) {
        Direction closestSide = null;
        double closestDistance = Double.MAX_VALUE;
        for (Direction side : Direction.values()) {
            double distance;
            BlockPos neighbor = blockPos.relative(side);
            BlockState state = MeteorClient.mc.level.getBlockState(neighbor);
            if (state.isAir() || BlockUtils.isClickable(state.getBlock()) || !state.getFluidState().isEmpty() || !((distance = pos.distanceToSqr((double)neighbor.getX(), (double)neighbor.getY(), (double)neighbor.getZ())) < closestDistance)) continue;
            closestDistance = distance;
            closestSide = side;
        }
        return closestSide;
    }

    @EventHandler(priority=300)
    private static void onTickPre(TickEvent.Pre event) {
        breakingThisTick = false;
    }

    @EventHandler(priority=-300)
    private static void onTickPost(TickEvent.Post event) {
        if (!breakingThisTick && breaking) {
            breaking = false;
            if (MeteorClient.mc.gameMode != null) {
                MeteorClient.mc.gameMode.stopDestroyBlock();
            }
        }
    }

    public static boolean breakBlock(BlockPos blockPos, boolean swing) {
        if (!BlockUtils.canBreak(blockPos, MeteorClient.mc.level.getBlockState(blockPos))) {
            return false;
        }
        BlockPos pos = blockPos instanceof BlockPos.MutableBlockPos ? new BlockPos((Vec3i)blockPos) : blockPos;
        InstantRebreak ir = Modules.get().get(InstantRebreak.class);
        if (ir != null && ir.isActive() && ir.blockPos.equals((Object)pos) && ir.shouldMine()) {
            ir.sendPacket();
            return true;
        }
        if (MeteorClient.mc.gameMode.isDestroying()) {
            MeteorClient.mc.gameMode.continueDestroyBlock(pos, BlockUtils.getDirection(blockPos));
        } else {
            MeteorClient.mc.gameMode.startDestroyBlock(pos, BlockUtils.getDirection(blockPos));
        }
        if (swing) {
            MeteorClient.mc.player.swing(InteractionHand.MAIN_HAND);
        } else {
            MeteorClient.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        breaking = true;
        breakingThisTick = true;
        return true;
    }

    public static boolean canBreak(BlockPos blockPos, BlockState state) {
        if (!MeteorClient.mc.player.isCreative() && state.getDestroySpeed((BlockGetter)MeteorClient.mc.level, blockPos) < 0.0f) {
            return false;
        }
        return state.getShape((BlockGetter)MeteorClient.mc.level, blockPos) != Shapes.empty();
    }

    public static boolean canBreak(BlockPos blockPos) {
        return BlockUtils.canBreak(blockPos, MeteorClient.mc.level.getBlockState(blockPos));
    }

    public static boolean canInstaBreak(BlockPos blockPos, float breakSpeed) {
        return MeteorClient.mc.player.isCreative() || BlockUtils.calcBlockBreakingDelta2(blockPos, breakSpeed) >= 1.0f;
    }

    public static boolean canInstaBreak(BlockPos blockPos) {
        BlockState state = MeteorClient.mc.level.getBlockState(blockPos);
        return BlockUtils.canInstaBreak(blockPos, MeteorClient.mc.player.getDestroySpeed(state));
    }

    public static float calcBlockBreakingDelta2(BlockPos blockPos, float breakSpeed) {
        BlockState state = MeteorClient.mc.level.getBlockState(blockPos);
        float f = state.getDestroySpeed((BlockGetter)MeteorClient.mc.level, blockPos);
        if (f == -1.0f) {
            return 0.0f;
        }
        int i = MeteorClient.mc.player.hasCorrectToolForDrops(state) ? 30 : 100;
        return breakSpeed / f / (float)i;
    }

    public static boolean isClickable(Block block) {
        return block instanceof CraftingTableBlock || block instanceof AnvilBlock || block instanceof LoomBlock || block instanceof CartographyTableBlock || block instanceof GrindstoneBlock || block instanceof StonecutterBlock || block instanceof ButtonBlock || block instanceof BasePressurePlateBlock || block instanceof BaseEntityBlock || block instanceof BedBlock || block instanceof FenceGateBlock || block instanceof DoorBlock || block instanceof NoteBlock || block instanceof TrapDoorBlock;
    }

    public static MobSpawn isValidMobSpawn(BlockPos blockPos, BlockState blockState, int spawnLightLimit) {
        boolean snow;
        boolean bl = snow = blockState.getBlock() instanceof SnowLayerBlock && (Integer)blockState.getValue((Property)SnowLayerBlock.LAYERS) == 1;
        if (!blockState.isAir() && !snow) {
            return MobSpawn.Never;
        }
        if (!BlockUtils.isValidSpawnBlock(MeteorClient.mc.level.getBlockState(blockPos.below()))) {
            return MobSpawn.Never;
        }
        if (MeteorClient.mc.level.getBrightness(LightLayer.BLOCK, blockPos) > spawnLightLimit) {
            return MobSpawn.Never;
        }
        if (MeteorClient.mc.level.getBrightness(LightLayer.SKY, blockPos) > spawnLightLimit) {
            return MobSpawn.Potential;
        }
        return MobSpawn.Always;
    }

    public static boolean isValidSpawnBlock(BlockState blockState) {
        Block block = blockState.getBlock();
        if (block == Blocks.BEDROCK || block == Blocks.BARRIER || block instanceof TransparentBlock || block instanceof ScaffoldingBlock) {
            return false;
        }
        if (block == Blocks.SOUL_SAND || block == Blocks.MUD) {
            return true;
        }
        if (block instanceof SlabBlock && blockState.getValue((Property)SlabBlock.TYPE) == SlabType.TOP) {
            return true;
        }
        if (block instanceof StairBlock && blockState.getValue((Property)StairBlock.HALF) == Half.TOP) {
            return true;
        }
        return blockState.isSolidRender();
    }

    public static Direction getDirection(BlockPos pos) {
        double eyePos = MeteorClient.mc.player.getY() + (double)MeteorClient.mc.player.getEyeHeight(MeteorClient.mc.player.getPose());
        VoxelShape outline = MeteorClient.mc.level.getBlockState(pos).getCollisionShape((BlockGetter)MeteorClient.mc.level, pos);
        if (eyePos > (double)pos.getY() + outline.max(Direction.Axis.Y) && MeteorClient.mc.level.getBlockState(pos.above()).canBeReplaced()) {
            return Direction.UP;
        }
        if (eyePos < (double)pos.getY() + outline.min(Direction.Axis.Y) && MeteorClient.mc.level.getBlockState(pos.below()).canBeReplaced()) {
            return Direction.DOWN;
        }
        BlockPos difference = pos.subtract((Vec3i)MeteorClient.mc.player.blockPosition());
        if (Math.abs(difference.getX()) > Math.abs(difference.getZ())) {
            return difference.getX() > 0 ? Direction.WEST : Direction.EAST;
        }
        return difference.getZ() > 0 ? Direction.NORTH : Direction.SOUTH;
    }

    public static boolean isExposed(BlockPos blockPos) {
        for (Direction direction : Direction.values()) {
            if (MeteorClient.mc.level.getBlockState((BlockPos)EXPOSED_POS.get().setWithOffset((Vec3i)blockPos, direction)).isSolidRender()) continue;
            return true;
        }
        return false;
    }

    public static double getBreakDelta(int slot, BlockState state) {
        float hardness = state.getDestroySpeed(null, null);
        if (hardness == -1.0f) {
            return 0.0;
        }
        return BlockUtils.getDestroySpeed(slot, state) / (double)hardness / (double)(!state.requiresCorrectToolForDrops() || ((ItemStack)MeteorClient.mc.player.getInventory().getNonEquipmentItems().get(slot)).isCorrectToolForDrops(state) ? 30 : 100);
    }

    private static double getDestroySpeed(int slot, BlockState block) {
        ItemStack tool;
        int efficiency;
        double speed = ((ItemStack)MeteorClient.mc.player.getInventory().getNonEquipmentItems().get(slot)).getDestroySpeed(block);
        if (speed > 1.0 && (efficiency = Utils.getEnchantmentLevel(tool = MeteorClient.mc.player.getInventory().getItem(slot), (ResourceKey<Enchantment>)Enchantments.EFFICIENCY)) > 0 && !tool.isEmpty()) {
            speed += (double)(efficiency * efficiency + 1);
        }
        if (MobEffectUtil.hasDigSpeed((LivingEntity)MeteorClient.mc.player)) {
            speed *= (double)(1.0f + (float)(MobEffectUtil.getDigSpeedAmplification((LivingEntity)MeteorClient.mc.player) + 1) * 0.2f);
        }
        if (MeteorClient.mc.player.hasEffect(MobEffects.MINING_FATIGUE)) {
            float k = switch (MeteorClient.mc.player.getEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1E-4f;
            };
            speed *= (double)k;
        }
        if (MeteorClient.mc.player.isEyeInFluid(FluidTags.WATER)) {
            speed *= MeteorClient.mc.player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED);
        }
        if (!MeteorClient.mc.player.onGround()) {
            speed /= 5.0;
        }
        return speed;
    }

    public static BlockPos.MutableBlockPos mutateAround(BlockPos.MutableBlockPos mutable, BlockPos origin, int xOffset, int yOffset, int zOffset) {
        return mutable.set(origin.getX() + xOffset, origin.getY() + yOffset, origin.getZ() + zOffset);
    }

    static {
        EXPOSED_POS = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
    }

    public static enum MobSpawn {
        Never,
        Potential,
        Always;

    }
}
