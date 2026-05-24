package net.wurstclient.hacks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstClient;
import net.wurstclient.ai.PathFinder;
import net.wurstclient.ai.PathPos;
import net.wurstclient.ai.PathProcessor;
import net.wurstclient.commands.PathCmd;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.treebot.Tree;
import net.wurstclient.hacks.treebot.TreeBotUtils;
import net.wurstclient.settings.FaceTargetSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.util.BlockBreaker;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.OverlayRenderer;

@SearchTags(value={"tree bot"})
@DontSaveState
public final class TreeBotHack
extends Hack
implements UpdateListener,
RenderListener {
    private final SliderSetting range = new SliderSetting("Range", "How far TreeBot will reach to break blocks.", 4.5, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final FaceTargetSetting faceTarget = FaceTargetSetting.withoutPacketSpam(this, FaceTargetSetting.FaceTarget.SERVER);
    private final SwingHandSetting swingHand = new SwingHandSetting(this, SwingHandSetting.SwingHand.SERVER);
    private TreeFinder treeFinder;
    private AngleFinder angleFinder;
    private TreeBotPathProcessor processor;
    private Tree tree;
    private class_2338 currentBlock;
    private final OverlayRenderer overlay = new OverlayRenderer();

    public TreeBotHack() {
        super("TreeBot");
        this.setCategory(Category.BLOCKS);
        this.addSetting(this.range);
        this.addSetting(this.faceTarget);
        this.addSetting(this.swingHand);
    }

    @Override
    public String getRenderName() {
        if (this.treeFinder != null && !this.treeFinder.isDone() && !this.treeFinder.isFailed()) {
            return this.getName() + " [Searching]";
        }
        if (this.processor != null && !this.processor.isDone()) {
            return this.getName() + " [Going]";
        }
        if (this.tree != null && !this.tree.getLogs().isEmpty()) {
            return this.getName() + " [Chopping]";
        }
        return this.getName();
    }

    @Override
    protected void onEnable() {
        this.treeFinder = new TreeFinder();
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        PathProcessor.releaseControls();
        this.treeFinder = null;
        this.angleFinder = null;
        this.processor = null;
        this.tree = null;
        if (this.currentBlock != null) {
            TreeBotHack.MC.field_1761.field_3717 = true;
            TreeBotHack.MC.field_1761.method_2925();
            this.currentBlock = null;
        }
        this.overlay.resetProgress();
    }

    @Override
    public void onUpdate() {
        if (this.treeFinder != null) {
            this.goToTree();
            return;
        }
        if (this.tree == null) {
            this.treeFinder = new TreeFinder();
            return;
        }
        this.tree.getLogs().removeIf(Predicate.not(TreeBotUtils::isLog));
        if (this.tree.getLogs().isEmpty()) {
            this.tree = null;
            return;
        }
        if (this.angleFinder != null) {
            this.goToAngle();
            return;
        }
        if (this.breakBlocks(this.tree.getLogs())) {
            return;
        }
        if (this.angleFinder == null) {
            this.angleFinder = new AngleFinder();
        }
    }

    private void goToTree() {
        if (!this.treeFinder.isDoneOrFailed()) {
            PathProcessor.lockControls();
            this.treeFinder.findPath();
            return;
        }
        if (this.processor != null && !this.processor.isDone()) {
            this.processor.goToGoal();
            return;
        }
        PathProcessor.releaseControls();
        this.treeFinder = null;
    }

    private void goToAngle() {
        if (!this.angleFinder.isDone() && !this.angleFinder.isFailed()) {
            PathProcessor.lockControls();
            this.angleFinder.findPath();
            return;
        }
        if (this.processor != null && !this.processor.isDone()) {
            this.processor.goToGoal();
            return;
        }
        PathProcessor.releaseControls();
        this.angleFinder = null;
    }

    private boolean breakBlocks(ArrayList<class_2338> blocks) {
        for (class_2338 pos : blocks) {
            if (!this.breakBlock(pos)) continue;
            this.currentBlock = pos;
            return true;
        }
        return false;
    }

    private boolean breakBlock(class_2338 pos) {
        BlockBreaker.BlockBreakingParams params = BlockBreaker.getBlockBreakingParams(pos);
        if (params == null || !params.lineOfSight() || params.distanceSq() > this.range.getValueSq()) {
            return false;
        }
        TreeBotHack.WURST.getHax().autoToolHack.equipBestTool(pos, false, true, 0);
        this.faceTarget.face(params.hitVec());
        if (TreeBotHack.MC.field_1761.method_2902(pos, params.side())) {
            this.swingHand.swing(class_1268.field_5808);
        }
        this.overlay.updateProgress();
        return true;
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        PathCmd pathCmd = TreeBotHack.WURST.getCmds().pathCmd;
        if (this.treeFinder != null) {
            this.treeFinder.renderPath(matrixStack, pathCmd.isDebugMode(), pathCmd.isDepthTest());
        }
        if (this.angleFinder != null) {
            this.angleFinder.renderPath(matrixStack, pathCmd.isDebugMode(), pathCmd.isDepthTest());
        }
        if (this.tree != null) {
            this.tree.draw(matrixStack);
        }
        this.overlay.render(matrixStack, partialTicks, this.currentBlock);
    }

    private ArrayList<class_2338> getNeighbors(class_2338 pos) {
        return BlockUtils.getAllInBoxStream(pos.method_10069(-1, -1, -1), pos.method_10069(1, 1, 1)).filter(TreeBotUtils::isLog).collect(Collectors.toCollection(ArrayList::new));
    }

    private class TreeFinder
    extends TreeBotPathFinder {
        public TreeFinder() {
            super(class_2338.method_49638((class_2374)WurstClient.MC.field_1724.method_73189()));
        }

        public TreeFinder(TreeBotPathFinder pathFinder) {
            super(pathFinder);
        }

        @Override
        protected boolean isMineable(class_2338 pos) {
            return TreeBotUtils.isLeaves(pos);
        }

        @Override
        protected boolean checkDone() {
            this.done = this.isNextToTreeStump(this.current);
            return this.done;
        }

        private boolean isNextToTreeStump(PathPos pos) {
            return this.isTreeStump(pos.method_10095()) || this.isTreeStump(pos.method_10078()) || this.isTreeStump(pos.method_10072()) || this.isTreeStump(pos.method_10067());
        }

        private boolean isTreeStump(class_2338 pos) {
            if (!TreeBotUtils.isLog(pos)) {
                return false;
            }
            if (TreeBotUtils.isLog(pos.method_10074())) {
                return false;
            }
            this.analyzeTree(pos);
            return TreeBotHack.this.tree.getLogs().size() <= 6;
        }

        private void analyzeTree(class_2338 stump) {
            ArrayList<class_2338> logs = new ArrayList<class_2338>(Arrays.asList(stump));
            ArrayDeque<class_2338> queue = new ArrayDeque<class_2338>(Arrays.asList(stump));
            for (int i = 0; i < 1024 && !queue.isEmpty(); ++i) {
                class_2338 current = queue.pollFirst();
                for (class_2338 next : TreeBotHack.this.getNeighbors(current)) {
                    if (logs.contains(next)) continue;
                    logs.add(next);
                    queue.add(next);
                }
            }
            TreeBotHack.this.tree = new Tree(stump, logs);
        }

        @Override
        public void reset() {
            TreeBotHack.this.treeFinder = new TreeFinder(TreeBotHack.this.treeFinder);
        }
    }

    private class TreeBotPathProcessor {
        private final TreeBotPathFinder pathFinder;
        private final PathProcessor processor;

        public TreeBotPathProcessor(TreeBotPathFinder pathFinder) {
            this.pathFinder = pathFinder;
            this.processor = pathFinder.getProcessor();
        }

        public void goToGoal() {
            if (!this.pathFinder.isPathStillValid(this.processor.getIndex()) || this.processor.getTicksOffPath() > 20) {
                this.pathFinder.reset();
                return;
            }
            if (this.processor.canBreakBlocks() && TreeBotHack.this.breakBlocks(this.getLeavesOnPath())) {
                return;
            }
            this.processor.process();
        }

        private ArrayList<class_2338> getLeavesOnPath() {
            List<PathPos> path = this.pathFinder.getPath();
            path = path.subList(this.processor.getIndex(), path.size());
            return path.stream().flatMap(pos -> Stream.of(pos, pos.method_10084())).distinct().filter(TreeBotUtils::isLeaves).collect(Collectors.toCollection(ArrayList::new));
        }

        public final boolean isDone() {
            return this.processor.isDone();
        }
    }

    private class AngleFinder
    extends TreeBotPathFinder {
        public AngleFinder() {
            super(class_2338.method_49638((class_2374)WurstClient.MC.field_1724.method_73189()));
            this.setThinkSpeed(512);
            this.setThinkTime(1);
        }

        public AngleFinder(TreeBotPathFinder pathFinder) {
            super(pathFinder);
        }

        @Override
        protected boolean isMineable(class_2338 pos) {
            return TreeBotUtils.isLeaves(pos);
        }

        @Override
        protected boolean checkDone() {
            this.done = this.hasAngle(this.current);
            return this.done;
        }

        private boolean hasAngle(PathPos pos) {
            double rangeSq = TreeBotHack.this.range.getValueSq();
            class_746 player = WurstClient.MC.field_1724;
            class_243 eyes = class_243.method_24955((class_2382)pos).method_1031(0.0, (double)player.method_18381(player.method_18376()), 0.0);
            for (class_2338 log : TreeBotHack.this.tree.getLogs()) {
                BlockBreaker.BlockBreakingParams params = BlockBreaker.getBlockBreakingParams(eyes, log);
                if (params == null || !params.lineOfSight() || !(params.distanceSq() <= rangeSq)) continue;
                return true;
            }
            return false;
        }

        @Override
        public void reset() {
            TreeBotHack.this.angleFinder = new AngleFinder(TreeBotHack.this.angleFinder);
        }
    }

    private abstract class TreeBotPathFinder
    extends PathFinder {
        public TreeBotPathFinder(class_2338 goal) {
            super(goal);
        }

        public TreeBotPathFinder(TreeBotPathFinder pathFinder) {
            super(pathFinder);
        }

        public void findPath() {
            this.think();
            if (this.isDoneOrFailed()) {
                this.formatPath();
                TreeBotHack.this.processor = new TreeBotPathProcessor(this);
            }
        }

        public boolean isDoneOrFailed() {
            return this.isDone() || this.isFailed();
        }

        public abstract void reset();
    }
}
