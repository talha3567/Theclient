package net.wurstclient.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2266;
import net.minecraft.class_2338;
import net.minecraft.class_2349;
import net.minecraft.class_2350;
import net.minecraft.class_2354;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_2399;
import net.minecraft.class_2404;
import net.minecraft.class_243;
import net.minecraft.class_2440;
import net.minecraft.class_2478;
import net.minecraft.class_2490;
import net.minecraft.class_2492;
import net.minecraft.class_2538;
import net.minecraft.class_2541;
import net.minecraft.class_2544;
import net.minecraft.class_2560;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3611;
import net.minecraft.class_3616;
import net.minecraft.class_3621;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4770;
import net.wurstclient.WurstClient;
import net.wurstclient.WurstRenderLayers;
import net.wurstclient.ai.FlyPathProcessor;
import net.wurstclient.ai.PathPos;
import net.wurstclient.ai.PathProcessor;
import net.wurstclient.ai.PathQueue;
import net.wurstclient.ai.PlayerAbilities;
import net.wurstclient.ai.WalkPathProcessor;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RegionPos;
import net.wurstclient.util.RenderUtils;

public class PathFinder {
    private static final class_310 MC = WurstClient.MC;
    private final PlayerAbilities abilities = PlayerAbilities.get();
    protected boolean fallingAllowed = true;
    protected boolean divingAllowed = true;
    private final PathPos start;
    protected PathPos current;
    private final class_2338 goal;
    private final HashMap<PathPos, Float> costMap = new HashMap();
    protected final HashMap<PathPos, PathPos> prevPosMap = new HashMap();
    private final PathQueue queue = new PathQueue();
    protected int thinkSpeed = 1024;
    protected int thinkTime = 200;
    private int iterations;
    protected boolean done;
    protected boolean failed;
    private final ArrayList<PathPos> path = new ArrayList();

    public PathFinder(class_2338 goal) {
        this.start = PathFinder.MC.field_1724.method_24828() ? new PathPos(class_2338.method_49637((double)PathFinder.MC.field_1724.method_23317(), (double)(PathFinder.MC.field_1724.method_23318() + 0.5), (double)PathFinder.MC.field_1724.method_23321())) : new PathPos(class_2338.method_49638((class_2374)PathFinder.MC.field_1724.method_73189()));
        this.goal = goal;
        this.costMap.put(this.start, Float.valueOf(0.0f));
        this.queue.add(this.start, this.getHeuristic(this.start));
    }

    public PathFinder(PathFinder pathFinder) {
        this(pathFinder.goal);
        this.thinkSpeed = pathFinder.thinkSpeed;
        this.thinkTime = pathFinder.thinkTime;
    }

    public void think() {
        int i;
        if (this.done) {
            throw new IllegalStateException("Path was already found!");
        }
        for (i = 0; i < this.thinkSpeed && !this.checkFailed(); ++i) {
            this.current = this.queue.poll();
            if (this.checkDone()) {
                return;
            }
            for (PathPos next : this.getNeighbors(this.current)) {
                float newCost = this.costMap.get((Object)this.current).floatValue() + this.getCost(this.current, next);
                if (this.costMap.containsKey((Object)next) && this.costMap.get((Object)next).floatValue() <= newCost) continue;
                this.costMap.put(next, Float.valueOf(newCost));
                this.prevPosMap.put(next, this.current);
                this.queue.add(next, newCost + this.getHeuristic(next));
            }
        }
        this.iterations += i;
    }

    protected boolean checkDone() {
        this.done = this.goal.equals((Object)this.current);
        return this.done;
    }

    private boolean checkFailed() {
        this.failed = this.queue.isEmpty() || this.iterations >= this.thinkSpeed * this.thinkTime;
        return this.failed;
    }

    private ArrayList<PathPos> getNeighbors(PathPos pos) {
        ArrayList<PathPos> neighbors = new ArrayList<PathPos>();
        if (Math.abs(this.start.method_10263() - pos.method_10263()) > 256 || Math.abs(this.start.method_10260() - pos.method_10260()) > 256) {
            return neighbors;
        }
        class_2338 north = pos.method_10095();
        class_2338 east = pos.method_10078();
        class_2338 south = pos.method_10072();
        class_2338 west = pos.method_10067();
        class_2338 northEast = north.method_10078();
        class_2338 southEast = south.method_10078();
        class_2338 southWest = south.method_10067();
        class_2338 northWest = north.method_10067();
        class_2338 up = pos.method_10084();
        class_2338 down = pos.method_10074();
        boolean flying = this.canFlyAt(pos);
        boolean onGround = this.canBeSolid(down);
        if (flying || onGround || pos.isJumping() || this.canMoveSidewaysInMidairAt(pos) || this.canClimbUpAt(pos.method_10074())) {
            if (this.checkHorizontalMovement(pos, north)) {
                neighbors.add(new PathPos(north));
            }
            if (this.checkHorizontalMovement(pos, east)) {
                neighbors.add(new PathPos(east));
            }
            if (this.checkHorizontalMovement(pos, south)) {
                neighbors.add(new PathPos(south));
            }
            if (this.checkHorizontalMovement(pos, west)) {
                neighbors.add(new PathPos(west));
            }
            if (this.checkDiagonalMovement(pos, class_2350.field_11043, class_2350.field_11034)) {
                neighbors.add(new PathPos(northEast));
            }
            if (this.checkDiagonalMovement(pos, class_2350.field_11035, class_2350.field_11034)) {
                neighbors.add(new PathPos(southEast));
            }
            if (this.checkDiagonalMovement(pos, class_2350.field_11035, class_2350.field_11039)) {
                neighbors.add(new PathPos(southWest));
            }
            if (this.checkDiagonalMovement(pos, class_2350.field_11043, class_2350.field_11039)) {
                neighbors.add(new PathPos(northWest));
            }
        }
        if (pos.method_10264() < PathFinder.MC.field_1687.method_31600() && this.canGoThrough(up.method_10084()) && (flying || onGround || this.canClimbUpAt(pos)) && (flying || this.canClimbUpAt(pos) || this.goal.equals((Object)up) || this.canSafelyStandOn(north) || this.canSafelyStandOn(east) || this.canSafelyStandOn(south) || this.canSafelyStandOn(west)) && (this.divingAllowed || BlockUtils.getBlock(up.method_10084()) != class_2246.field_10382)) {
            neighbors.add(new PathPos(up, onGround));
        }
        if (pos.method_10264() > PathFinder.MC.field_1687.method_31607() && this.canGoThrough(down) && this.canGoAbove(down.method_10074()) && (flying || this.canFallBelow(pos)) && (this.divingAllowed || BlockUtils.getBlock(pos) != class_2246.field_10382)) {
            neighbors.add(new PathPos(down));
        }
        return neighbors;
    }

    private boolean checkHorizontalMovement(class_2338 current, class_2338 next) {
        return this.isPassable(next) && (this.canFlyAt(current) || this.canGoThrough(next.method_10074()) || this.canSafelyStandOn(next.method_10074()));
    }

    private boolean checkDiagonalMovement(class_2338 current, class_2350 direction1, class_2350 direction2) {
        class_2338 horizontal1 = current.method_10093(direction1);
        class_2338 horizontal2 = current.method_10093(direction2);
        class_2338 next = horizontal1.method_10093(direction2);
        return this.isPassableWithoutMining(horizontal1) && this.isPassableWithoutMining(horizontal2) && this.checkHorizontalMovement(current, next);
    }

    protected boolean isPassable(class_2338 pos) {
        if (!this.canGoThrough(pos) && !this.isMineable(pos)) {
            return false;
        }
        class_2338 up = pos.method_10084();
        if (!this.canGoThrough(up) && !this.isMineable(up)) {
            return false;
        }
        if (!this.canGoAbove(pos.method_10074())) {
            return false;
        }
        return this.divingAllowed || BlockUtils.getBlock(up) != class_2246.field_10382;
    }

    protected boolean isPassableWithoutMining(class_2338 pos) {
        if (!this.canGoThrough(pos)) {
            return false;
        }
        class_2338 up = pos.method_10084();
        if (!this.canGoThrough(up)) {
            return false;
        }
        if (!this.canGoAbove(pos.method_10074())) {
            return false;
        }
        return this.divingAllowed || BlockUtils.getBlock(up) != class_2246.field_10382;
    }

    protected boolean isMineable(class_2338 pos) {
        return false;
    }

    protected boolean canBeSolid(class_2338 pos) {
        class_2680 state = BlockUtils.getState(pos);
        class_2248 block = state.method_26204();
        return state.method_51366() && !(block instanceof class_2478) || block instanceof class_2399 || this.abilities.jesus() && (block == class_2246.field_10382 || block == class_2246.field_10164);
    }

    private boolean canGoThrough(class_2338 pos) {
        if (!PathFinder.MC.field_1687.method_22340(pos)) {
            return false;
        }
        class_2680 state = BlockUtils.getState(pos);
        class_2248 block = state.method_26204();
        if (state.method_51366() && !(block instanceof class_2478)) {
            return false;
        }
        if (block instanceof class_2538 || block instanceof class_2440) {
            return false;
        }
        return this.abilities.invulnerable() || block != class_2246.field_10164 && !(block instanceof class_4770);
    }

    private boolean canGoAbove(class_2338 pos) {
        class_2248 block = BlockUtils.getBlock(pos);
        return !(block instanceof class_2354) && !(block instanceof class_2544) && !(block instanceof class_2349);
    }

    private boolean canSafelyStandOn(class_2338 pos) {
        if (!this.canBeSolid(pos)) {
            return false;
        }
        class_2680 state = BlockUtils.getState(pos);
        class_3611 fluid = state.method_26227().method_15772();
        return this.abilities.invulnerable() || !(state.method_26204() instanceof class_2266) && !(fluid instanceof class_3616);
    }

    private boolean canFallBelow(PathPos pos) {
        class_2338 down2 = pos.method_10087(2);
        if (this.fallingAllowed && this.canGoThrough(down2)) {
            return true;
        }
        if (!this.canSafelyStandOn(down2)) {
            return false;
        }
        if (this.abilities.immuneToFallDamage() && this.fallingAllowed) {
            return true;
        }
        if (BlockUtils.getBlock(down2) instanceof class_2490 && this.fallingAllowed) {
            return true;
        }
        PathPos prevPos = pos;
        for (int i = 0; i <= (this.fallingAllowed ? 3 : 1); ++i) {
            if (prevPos == null) {
                return true;
            }
            if (!pos.method_10086(i).equals((Object)prevPos)) {
                return true;
            }
            class_2248 prevBlock = BlockUtils.getBlock(prevPos);
            class_2680 prevState = BlockUtils.getState(prevPos);
            if (prevState.method_26227().method_15772() instanceof class_3621 || prevBlock instanceof class_2399 || prevBlock instanceof class_2541 || prevBlock instanceof class_2560) {
                return true;
            }
            prevPos = this.prevPosMap.get((Object)prevPos);
        }
        return false;
    }

    private boolean canFlyAt(class_2338 pos) {
        return this.abilities.flying() || !this.abilities.noWaterSlowdown() && BlockUtils.getBlock(pos) == class_2246.field_10382;
    }

    private boolean canClimbUpAt(class_2338 pos) {
        class_2248 block = BlockUtils.getBlock(pos);
        if (!(this.abilities.spider() || block instanceof class_2399 || block instanceof class_2541)) {
            return false;
        }
        class_2338 up = pos.method_10084();
        return this.canBeSolid(pos.method_10095()) || this.canBeSolid(pos.method_10078()) || this.canBeSolid(pos.method_10072()) || this.canBeSolid(pos.method_10067()) || this.canBeSolid(up.method_10095()) || this.canBeSolid(up.method_10078()) || this.canBeSolid(up.method_10072()) || this.canBeSolid(up.method_10067());
    }

    private boolean canMoveSidewaysInMidairAt(class_2338 pos) {
        class_2248 blockFeet = BlockUtils.getBlock(pos);
        if (BlockUtils.getBlock(pos) instanceof class_2404 || blockFeet instanceof class_2399 || blockFeet instanceof class_2541 || blockFeet instanceof class_2560) {
            return true;
        }
        class_2248 blockHead = BlockUtils.getBlock(pos.method_10084());
        return BlockUtils.getBlock(pos.method_10084()) instanceof class_2404 || blockHead instanceof class_2560;
    }

    private float getCost(class_2338 current, class_2338 next) {
        float[] costs = new float[]{0.5f, 0.5f};
        class_2338[] positions = new class_2338[]{current, next};
        for (int i = 0; i < positions.length; ++i) {
            class_2338 pos = positions[i];
            class_2248 block = BlockUtils.getBlock(pos);
            if (block == class_2246.field_10382 && !this.abilities.noWaterSlowdown()) {
                int n = i;
                costs[n] = costs[n] * 1.3164438f;
            } else if (block == class_2246.field_10164) {
                int n = i;
                costs[n] = costs[n] * 4.5395155f;
            }
            if (!this.canFlyAt(pos) && BlockUtils.getBlock(pos.method_10074()) instanceof class_2492) {
                int n = i;
                costs[n] = costs[n] * 2.5f;
            }
            if (this.isMineable(pos)) {
                int n = i;
                costs[n] = costs[n] * 2.0f;
            }
            if (!this.isMineable(pos.method_10084())) continue;
            int n = i;
            costs[n] = costs[n] * 2.0f;
        }
        float cost = costs[0] + costs[1];
        if (current.method_10263() != next.method_10263() && current.method_10260() != next.method_10260()) {
            cost *= 1.4142135f;
        }
        return cost;
    }

    private float getHeuristic(class_2338 pos) {
        float dx = Math.abs(pos.method_10263() - this.goal.method_10263());
        float dy = Math.abs(pos.method_10264() - this.goal.method_10264());
        float dz = Math.abs(pos.method_10260() - this.goal.method_10260());
        return 1.001f * (dx + dy + dz - 0.58578646f * Math.min(dx, dz));
    }

    public PathPos getCurrentPos() {
        return this.current;
    }

    public class_2338 getGoal() {
        return this.goal;
    }

    public int countProcessedBlocks() {
        return this.prevPosMap.size();
    }

    public int getQueueSize() {
        return this.queue.size();
    }

    public float getCost(class_2338 pos) {
        return this.costMap.get(pos).floatValue();
    }

    public boolean isDone() {
        return this.done;
    }

    public boolean isFailed() {
        return this.failed;
    }

    public ArrayList<PathPos> formatPath() {
        PathPos pos;
        if (!this.done && !this.failed) {
            throw new IllegalStateException("No path found!");
        }
        if (!this.path.isEmpty()) {
            throw new IllegalStateException("Path was already formatted!");
        }
        if (!this.failed) {
            pos = this.current;
        } else {
            pos = this.start;
            for (PathPos next : this.prevPosMap.keySet()) {
                if (!(this.getHeuristic(next) < this.getHeuristic(pos)) || !this.canFlyAt(next) && !this.canBeSolid(next.method_10074())) continue;
                pos = next;
            }
        }
        while (pos != null) {
            this.path.add(pos);
            pos = this.prevPosMap.get((Object)pos);
        }
        Collections.reverse(this.path);
        return this.path;
    }

    public void renderPath(class_4587 matrixStack, boolean debugMode, boolean depthTest) {
        class_4597.class_4598 vcp = MC.method_22940().method_23000();
        class_4588 buffer = vcp.method_73477(WurstRenderLayers.getLines(depthTest));
        matrixStack.method_22903();
        RegionPos region = RenderUtils.getCameraRegion();
        class_243 regionOffset = region.negate().toVec3d();
        RenderUtils.applyRegionalRenderOffset(matrixStack, region);
        if (debugMode) {
            int thingsRendered = 0;
            for (PathPos element : this.queue.toArray()) {
                if (thingsRendered >= 5000) break;
                class_238 box = new class_238((class_2338)element).method_997(regionOffset).method_1011(0.4);
                RenderUtils.drawNode(matrixStack, buffer, box, -1056964864);
                ++thingsRendered;
            }
            for (Map.Entry entry : this.prevPosMap.entrySet()) {
                if (thingsRendered >= 5000) break;
                int color = ((PathPos)((Object)entry.getKey())).isJumping() ? -1057029889 : -1057030144;
                RenderUtils.drawArrow(matrixStack, buffer, (class_2338)entry.getValue(), (class_2338)entry.getKey(), region, color);
                ++thingsRendered;
            }
        }
        int pathColor = debugMode ? -1073741569 : -1073676544;
        for (int i = 0; i < this.path.size() - 1; ++i) {
            RenderUtils.drawArrow(matrixStack, buffer, this.path.get(i), this.path.get(i + 1), region, pathColor);
        }
        matrixStack.method_22909();
        vcp.method_37104();
    }

    public boolean isPathStillValid(int index) {
        PathPos pos;
        if (this.path.isEmpty()) {
            throw new IllegalStateException("Path is not formatted!");
        }
        if (!this.abilities.equals(PlayerAbilities.get())) {
            return false;
        }
        if (!(index != 0 || this.isPassable(pos = this.path.get(0)) && (this.canFlyAt(pos) || this.canGoThrough(pos.method_10074()) || this.canSafelyStandOn(pos.method_10074())))) {
            return false;
        }
        for (int i = Math.max(1, index); i < this.path.size(); ++i) {
            if (this.getNeighbors(this.path.get(i - 1)).contains((Object)this.path.get(i))) continue;
            return false;
        }
        return true;
    }

    public PathProcessor getProcessor() {
        if (this.abilities.flying()) {
            return new FlyPathProcessor(this.path, this.abilities.creativeFlying());
        }
        return new WalkPathProcessor(this.path);
    }

    public void setThinkSpeed(int thinkSpeed) {
        this.thinkSpeed = thinkSpeed;
    }

    public void setThinkTime(int thinkTime) {
        this.thinkTime = thinkTime;
    }

    public void setFallingAllowed(boolean fallingAllowed) {
        this.fallingAllowed = fallingAllowed;
    }

    public void setDivingAllowed(boolean divingAllowed) {
        this.divingAllowed = divingAllowed;
    }

    public List<PathPos> getPath() {
        return Collections.unmodifiableList(this.path);
    }
}
