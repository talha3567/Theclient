package meteordevelopment.meteorclient.utils.player;

import java.util.ArrayList;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PathFinder {
    private static final int PATH_AHEAD = 3;
    private static final int QUAD_1 = 1;
    private static final int QUAD_2 = 2;
    private static final int SOUTH = 0;
    private static final int NORTH = 180;
    private final ArrayList<PathBlock> path = new ArrayList(3);
    private Entity target;
    private PathBlock currentPathBlock;

    public PathBlock getNextPathBlock() {
        PathBlock nextBlock = new PathBlock(this, BlockPos.containing((Position)this.getNextStraightPos()));
        if (this.isSolidFloor(nextBlock.blockPos) && this.isAirAbove(nextBlock.blockPos)) {
            return nextBlock;
        }
        if (!this.isSolidFloor(nextBlock.blockPos) && this.isAirAbove(nextBlock.blockPos)) {
            int drop = this.getDrop(nextBlock.blockPos);
            if (this.getDrop(nextBlock.blockPos) < 3) {
                nextBlock = new PathBlock(this, new BlockPos(nextBlock.blockPos.getX(), nextBlock.blockPos.getY() - drop, nextBlock.blockPos.getZ()));
            }
        }
        return nextBlock;
    }

    public int getDrop(BlockPos pos) {
        int drop;
        for (drop = 0; !this.isSolidFloor(pos) && drop < 3; ++drop) {
            pos = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
        }
        return drop;
    }

    public boolean isAirAbove(BlockPos blockPos) {
        if (!this.getBlockStateAtPos(blockPos.getX(), blockPos.getY(), blockPos.getZ()).isAir()) {
            return false;
        }
        return this.getBlockStateAtPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ()).isAir();
    }

    public Vec3 getNextStraightPos() {
        Vec3 nextPos = new Vec3(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY(), MeteorClient.mc.player.getZ());
        double multiplier = 1.0;
        while (nextPos == MeteorClient.mc.player.position()) {
            nextPos = new Vec3((double)((int)(MeteorClient.mc.player.getX() + multiplier * Math.cos(Math.toRadians(MeteorClient.mc.player.getYRot())))), (double)((int)MeteorClient.mc.player.getY()), (double)((int)(MeteorClient.mc.player.getZ() + multiplier * Math.sin(Math.toRadians(MeteorClient.mc.player.getYRot())))));
            multiplier += 0.1;
        }
        return nextPos;
    }

    public int getYawToTarget() {
        int yaw;
        if (this.target == null || MeteorClient.mc.player == null) {
            return Integer.MAX_VALUE;
        }
        Vec3 tPos = this.target.position();
        Vec3 pPos = MeteorClient.mc.player.position();
        int direction = this.getDirection();
        double tan = (tPos.z - pPos.z) / (tPos.x - pPos.x);
        switch (direction) {
            case 1: {
                yaw = (int)(1.5707963267948966 - Math.atan(tan));
                break;
            }
            case 2: {
                yaw = (int)(-1.5707963267948966 - Math.atan(tan));
                break;
            }
            default: {
                return direction;
            }
        }
        return yaw;
    }

    public int getDirection() {
        if (this.target == null || MeteorClient.mc.player == null) {
            return 0;
        }
        Vec3 targetPos = this.target.position();
        Vec3 playerPos = MeteorClient.mc.player.position();
        if (targetPos.x == playerPos.x && targetPos.z > playerPos.z) {
            return 0;
        }
        if (targetPos.x == playerPos.x && targetPos.z < playerPos.z) {
            return 180;
        }
        if (targetPos.x < playerPos.x) {
            return 1;
        }
        if (targetPos.x > playerPos.x) {
            return 2;
        }
        return 0;
    }

    public BlockState getBlockStateAtPos(BlockPos pos) {
        if (MeteorClient.mc.level != null) {
            return MeteorClient.mc.level.getBlockState(pos);
        }
        return null;
    }

    public BlockState getBlockStateAtPos(int x, int y, int z) {
        if (MeteorClient.mc.level != null) {
            return MeteorClient.mc.level.getBlockState(new BlockPos(x, y, z));
        }
        return null;
    }

    public Block getBlockAtPos(BlockPos pos) {
        if (MeteorClient.mc.level != null) {
            return MeteorClient.mc.level.getBlockState(pos).getBlock();
        }
        return null;
    }

    public boolean isSolidFloor(BlockPos blockPos) {
        return this.isAir(this.getBlockAtPos(blockPos));
    }

    public boolean isAir(Block block) {
        return block == Blocks.AIR;
    }

    public boolean isWater(Block block) {
        return block == Blocks.WATER;
    }

    public void lookAtDestination(PathBlock pathBlock) {
        if (MeteorClient.mc.player != null) {
            MeteorClient.mc.player.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3((double)pathBlock.blockPos.getX(), (double)((float)pathBlock.blockPos.getY() + MeteorClient.mc.player.getEyeHeight()), (double)pathBlock.blockPos.getZ()));
        }
    }

    @EventHandler
    private void moveEventListener(PlayerMoveEvent event) {
        if (this.target != null && MeteorClient.mc.player != null) {
            if (!PlayerUtils.isWithin(this.target, 3.0)) {
                if (this.currentPathBlock == null) {
                    this.currentPathBlock = this.getNextPathBlock();
                }
                Vec3 vec3 = new Vec3((double)this.currentPathBlock.blockPos.getX(), (double)this.currentPathBlock.blockPos.getY(), (double)this.currentPathBlock.blockPos.getZ());
                if (MeteorClient.mc.player.position().distanceToSqr(vec3) < 0.01) {
                    this.currentPathBlock = this.getNextPathBlock();
                }
                this.lookAtDestination(this.currentPathBlock);
                if (!MeteorClient.mc.options.keyUp.isDown()) {
                    MeteorClient.mc.options.keyUp.setDown(true);
                }
            } else {
                if (MeteorClient.mc.options.keyUp.isDown()) {
                    MeteorClient.mc.options.keyUp.setDown(false);
                }
                this.path.clear();
                this.currentPathBlock = null;
            }
        }
    }

    public void initiate(Entity entity) {
        this.target = entity;
        if (this.target != null) {
            this.currentPathBlock = this.getNextPathBlock();
        }
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public void disable() {
        this.target = null;
        this.path.clear();
        if (MeteorClient.mc.options.keyUp.isDown()) {
            MeteorClient.mc.options.keyUp.setDown(false);
        }
        MeteorClient.EVENT_BUS.unsubscribe(this);
    }

    public class PathBlock {
        public final Block block;
        public final BlockPos blockPos;
        public final BlockState blockState;
        public double yaw;

        public PathBlock(PathFinder this$0, Block b, BlockPos pos, BlockState state) {
            Objects.requireNonNull(this$0);
            this.block = b;
            this.blockPos = pos;
            this.blockState = state;
        }

        public PathBlock(PathFinder this$0, Block b, BlockPos pos) {
            Objects.requireNonNull(this$0);
            this.block = b;
            this.blockPos = pos;
            this.blockState = this$0.getBlockStateAtPos(this.blockPos);
        }

        public PathBlock(PathFinder this$0, BlockPos pos) {
            Objects.requireNonNull(this$0);
            this.blockPos = pos;
            this.block = this$0.getBlockAtPos(pos);
            this.blockState = this$0.getBlockStateAtPos(this.blockPos);
        }
    }
}
