package meteordevelopment.meteorclient.pathing;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalGetToBlock;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.api.utils.SettingsUtil;
import java.util.Objects;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.BaritoneSettings;
import meteordevelopment.meteorclient.pathing.IPathManager;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class BaritonePathManager
implements IPathManager {
    private final BaritoneSettings settings;
    private GoalDirection directionGoal;
    private boolean pathingPaused;

    public BaritonePathManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
        this.settings = new BaritoneSettings();
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager().registerProcess((IBaritoneProcess)new BaritoneProcess(this));
    }

    @Override
    public String getName() {
        return "Baritone";
    }

    @Override
    public boolean isPathing() {
        return BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing();
    }

    @Override
    public void pause() {
        this.pathingPaused = true;
    }

    @Override
    public void resume() {
        this.pathingPaused = false;
    }

    @Override
    public void stop() {
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
    }

    @Override
    public void moveTo(BlockPos pos, boolean ignoreY) {
        if (ignoreY) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath((Goal)new GoalXZ(pos.getX(), pos.getZ()));
            return;
        }
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath((Goal)new GoalGetToBlock(pos));
    }

    @Override
    public void moveInDirection(float yaw) {
        this.directionGoal = new GoalDirection(yaw);
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath((Goal)this.directionGoal);
    }

    @Override
    public void mine(Block ... blocks) {
        BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().mine(blocks);
    }

    @Override
    public void follow(Predicate<Entity> entity) {
        BaritoneAPI.getProvider().getPrimaryBaritone().getFollowProcess().follow(entity);
    }

    @Override
    public float getTargetYaw() {
        return BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext().playerRotations().getYaw();
    }

    @Override
    public float getTargetPitch() {
        return BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext().playerRotations().getPitch();
    }

    @Override
    public IPathManager.ISettings getSettings() {
        return this.settings;
    }

    @EventHandler(priority=200)
    private void onTick(TickEvent.Pre event) {
        if (this.directionGoal == null) {
            return;
        }
        if (this.directionGoal != BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().getGoal()) {
            this.directionGoal = null;
            return;
        }
        this.directionGoal.tick();
    }

    private class BaritoneProcess
    implements IBaritoneProcess {
        final /* synthetic */ BaritonePathManager this$0;

        private BaritoneProcess(BaritonePathManager baritonePathManager) {
            BaritonePathManager baritonePathManager2 = baritonePathManager;
            Objects.requireNonNull(baritonePathManager2);
            this.this$0 = baritonePathManager2;
        }

        public boolean isActive() {
            return this.this$0.pathingPaused;
        }

        public PathingCommand onTick(boolean b, boolean b1) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getInputOverrideHandler().clearAllKeys();
            return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
        }

        public boolean isTemporary() {
            return true;
        }

        public void onLostControl() {
        }

        public double priority() {
            return 0.0;
        }

        public String displayName0() {
            return "Meteor Client";
        }
    }

    private static class GoalDirection
    implements Goal {
        private static final double SQRT_2 = Math.sqrt(2.0);
        private final float yaw;
        private int x;
        private int z;
        private int timer;

        public GoalDirection(float yaw) {
            this.yaw = yaw;
            this.tick();
        }

        public static double calculate(double xDiff, double zDiff) {
            double straight;
            double z;
            double x = Math.abs(xDiff);
            if (x < (z = Math.abs(zDiff))) {
                straight = z - x;
                diagonal = x;
            } else {
                straight = x - z;
                diagonal = z;
            }
            return ((diagonal *= SQRT_2) + straight) * (Double)BaritoneAPI.getSettings().costHeuristic.value;
        }

        public void tick() {
            if (this.timer <= 0) {
                this.timer = 20;
                Vec3 pos = MeteorClient.mc.player.position();
                float theta = (float)Math.toRadians(this.yaw);
                this.x = (int)Math.floor(pos.x - (double)Mth.sin((double)theta) * 100.0);
                this.z = (int)Math.floor(pos.z + (double)Mth.cos((double)theta) * 100.0);
            }
            --this.timer;
        }

        public boolean isInGoal(int x, int y, int z) {
            return x == this.x && z == this.z;
        }

        public double heuristic(int x, int y, int z) {
            int xDiff = x - this.x;
            int zDiff = z - this.z;
            return GoalDirection.calculate(xDiff, zDiff);
        }

        public String toString() {
            return String.format("GoalXZ{x=%s,z=%s}", SettingsUtil.maybeCensor((int)this.x), SettingsUtil.maybeCensor((int)this.z));
        }

        public int getX() {
            return this.x;
        }

        public int getZ() {
            return this.z;
        }
    }
}
