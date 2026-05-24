package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.class_1041;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_3675;
import net.minecraft.class_3965;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.ai.PathFinder;
import net.wurstclient.ai.PathProcessor;
import net.wurstclient.commands.PathCmd;
import net.wurstclient.events.GUIRenderListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.BlockBreaker;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.OverlayRenderer;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

public final class ExcavatorHack
extends Hack
implements UpdateListener,
RenderListener,
GUIRenderListener {
    private final SliderSetting range = new SliderSetting("Range", 5.0, 2.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final EnumSetting<Mode> mode = new EnumSetting("Mode", (Enum[])Mode.values(), (Enum)Mode.FAST);
    private final OverlayRenderer overlay = new OverlayRenderer();
    private Step step;
    private class_2338 posLookingAt;
    private Area area;
    private class_2338 currentBlock;
    private ExcavatorPathFinder pathFinder;
    private PathProcessor processor;

    public ExcavatorHack() {
        super("Excavator");
        this.setCategory(Category.BLOCKS);
        this.addSetting(this.range);
        this.addSetting(this.mode);
    }

    @Override
    public String getRenderName() {
        Object name = this.getName();
        if (this.step == Step.EXCAVATE && this.area != null) {
            int totalBlocks = this.area.blocksList.size();
            double brokenBlocks = totalBlocks - this.area.remainingBlocks;
            double progress = brokenBlocks / (double)totalBlocks;
            int percentage = (int)(progress * 100.0);
            name = (String)name + " " + percentage + "%";
        }
        return name;
    }

    @Override
    protected void onEnable() {
        ExcavatorHack.WURST.getHax().autoMineHack.setEnabled(false);
        ExcavatorHack.WURST.getHax().bowAimbotHack.setEnabled(false);
        ExcavatorHack.WURST.getHax().nukerHack.setEnabled(false);
        ExcavatorHack.WURST.getHax().nukerLegitHack.setEnabled(false);
        ExcavatorHack.WURST.getHax().speedNukerHack.setEnabled(false);
        ExcavatorHack.WURST.getHax().templateToolHack.setEnabled(false);
        ExcavatorHack.WURST.getHax().tunnellerHack.setEnabled(false);
        ExcavatorHack.WURST.getHax().veinMinerHack.setEnabled(false);
        this.step = Step.START_POS;
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(RenderListener.class, this);
        EVENTS.add(GUIRenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        EVENTS.remove(GUIRenderListener.class, this);
        for (Step step : Step.values()) {
            step.pos = null;
        }
        this.posLookingAt = null;
        this.area = null;
        ExcavatorHack.MC.field_1761.method_2925();
        this.overlay.resetProgress();
        this.currentBlock = null;
        this.pathFinder = null;
        this.processor = null;
        PathProcessor.releaseControls();
    }

    @Override
    public void onUpdate() {
        if (this.step.selectPos) {
            this.handlePositionSelection();
        } else if (this.step == Step.SCAN_AREA) {
            this.scanArea();
        } else if (this.step == Step.EXCAVATE) {
            this.excavate();
        }
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        if (this.pathFinder != null) {
            PathCmd pathCmd = ExcavatorHack.WURST.getCmds().pathCmd;
            this.pathFinder.renderPath(matrixStack, pathCmd.isDebugMode(), pathCmd.isDepthTest());
        }
        int black = Integer.MIN_VALUE;
        int gray = 641744960;
        int green1 = 637599488;
        int green2 = 1291910912;
        if (this.area != null) {
            if (this.step == Step.SCAN_AREA && this.area.progress < 1.0f) {
                ArrayList<class_238> boxes = new ArrayList<class_238>();
                for (int i = Math.max(0, this.area.blocksList.size() - this.area.scanSpeed); i < this.area.blocksList.size(); ++i) {
                    boxes.add(new class_238(this.area.blocksList.get(i)).method_1014(0.005));
                }
                RenderUtils.drawOutlinedBoxes(matrixStack, boxes, black, true);
                RenderUtils.drawSolidBoxes(matrixStack, boxes, green1, true);
            }
            class_238 areaBox = new class_238((double)this.area.minX, (double)this.area.minY, (double)this.area.minZ, (double)(this.area.minX + this.area.sizeX), (double)(this.area.minY + this.area.sizeY), (double)(this.area.minZ + this.area.sizeZ)).method_1011(0.0625);
            RenderUtils.drawOutlinedBox(matrixStack, areaBox, black, true);
            if (this.area.progress < 1.0f) {
                double scannerX = class_3532.method_16436((double)this.area.progress, (double)areaBox.field_1323, (double)areaBox.field_1320);
                class_238 scanner = areaBox.method_35574(scannerX).method_35577(scannerX);
                RenderUtils.drawOutlinedBox(matrixStack, scanner, black, true);
                RenderUtils.drawSolidBox(matrixStack, scanner, green2, true);
            }
        }
        if (this.area == null && this.step == Step.END_POS && this.step.pos != null) {
            class_238 preview = class_238.method_54784((class_2338)Step.START_POS.pos, (class_2338)Step.END_POS.pos).method_1011(0.0625);
            RenderUtils.drawOutlinedBox(matrixStack, preview, black, true);
        }
        ArrayList<class_238> selectedBoxes = new ArrayList<class_238>();
        for (Step step : Step.SELECT_POSITION_STEPS) {
            if (step.pos == null) continue;
            selectedBoxes.add(new class_238(step.pos).method_1011(0.0625));
        }
        RenderUtils.drawOutlinedBoxes(matrixStack, selectedBoxes, black, false);
        RenderUtils.drawSolidBoxes(matrixStack, selectedBoxes, green1, false);
        if (this.posLookingAt != null) {
            class_238 box = new class_238(this.posLookingAt).method_1011(0.0625);
            RenderUtils.drawOutlinedBox(matrixStack, box, black, false);
            RenderUtils.drawSolidBox(matrixStack, box, gray, false);
        }
        this.overlay.render(matrixStack, partialTicks, this.currentBlock);
    }

    @Override
    public void onRenderGUI(class_332 context, float partialTicks) {
        String message = this.step.selectPos && this.step.pos != null ? "Press enter to confirm, or select a different position." : this.step.message;
        class_327 tr = ExcavatorHack.MC.field_1772;
        int msgWidth = tr.method_1727(message);
        int msgX1 = context.method_51421() / 2 - msgWidth / 2;
        int msgX2 = msgX1 + msgWidth + 2;
        int msgY1 = context.method_51443() / 2 + 1;
        int msgY2 = msgY1 + 10;
        context.method_25294(msgX1, msgY1, msgX2, msgY2, Integer.MIN_VALUE);
        context.method_51433(tr, message, msgX1 + 2, msgY1 + 1, -1, false);
    }

    public void enableWithArea(class_2338 pos1, class_2338 pos2) {
        this.setEnabled(true);
        Step.START_POS.pos = pos1;
        Step.END_POS.pos = pos2;
        this.step = Step.SCAN_AREA;
    }

    private void handlePositionSelection() {
        if (this.step.pos != null && class_3675.method_15987((class_1041)MC.method_22683(), (int)257)) {
            this.step = Step.values()[this.step.ordinal() + 1];
            if (!this.step.selectPos) {
                this.posLookingAt = null;
            }
            return;
        }
        if (ExcavatorHack.MC.field_1765 instanceof class_3965) {
            this.posLookingAt = ((class_3965)ExcavatorHack.MC.field_1765).method_17777();
            if (ExcavatorHack.MC.field_1690.field_1832.method_1434()) {
                this.posLookingAt = this.posLookingAt.method_10093(((class_3965)ExcavatorHack.MC.field_1765).method_17780());
            }
        } else {
            this.posLookingAt = null;
        }
        if (this.posLookingAt != null && ExcavatorHack.MC.field_1690.field_1904.method_1434()) {
            this.step.pos = this.posLookingAt;
        }
    }

    private void scanArea() {
        if (this.area == null) {
            this.area = new Area(Step.START_POS.pos, Step.END_POS.pos);
            Step.START_POS.pos = null;
            Step.END_POS.pos = null;
        }
        for (int i = 0; i < this.area.scanSpeed && this.area.iterator.hasNext(); ++i) {
            ++this.area.scannedBlocks;
            class_2338 pos = this.area.iterator.next();
            if (!BlockUtils.canBeClicked(pos)) continue;
            this.area.blocksList.add(pos);
            this.area.blocksSet.add(pos);
        }
        this.area.progress = (float)this.area.scannedBlocks / (float)this.area.totalBlocks;
        if (!this.area.iterator.hasNext()) {
            this.area.remainingBlocks = this.area.blocksList.size();
            this.step = Step.values()[this.step.ordinal() + 1];
        }
    }

    private void excavate() {
        boolean legit;
        if (ExcavatorHack.WURST.getHax().autoEatHack.isEating()) {
            return;
        }
        class_243 eyesVec = RotationUtils.getEyesPos();
        Comparator<class_2338> cNextTargetBlock = Comparator.comparingInt(class_2382::method_10264).reversed().thenComparingDouble(pos -> pos.method_19770((class_2374)eyesVec));
        ArrayList<class_2338> validBlocks = this.getValidBlocks();
        validBlocks.sort(cNextTargetBlock);
        this.currentBlock = null;
        boolean bl = legit = this.mode.getSelected() == Mode.LEGIT;
        if (ExcavatorHack.MC.field_1724.method_31549().field_7477 && !legit) {
            ExcavatorHack.MC.field_1761.method_2925();
            this.overlay.resetProgress();
            Iterator<class_2338> iterator = validBlocks.iterator();
            if (iterator.hasNext()) {
                class_2338 pos2;
                this.currentBlock = pos2 = iterator.next();
            }
            BlockBreaker.breakBlocksWithPacketSpam(validBlocks);
        } else {
            for (class_2338 pos2 : validBlocks) {
                ExcavatorHack.WURST.getHax().autoToolHack.equipIfEnabled(pos2);
                if (!BlockBreaker.breakOneBlock(pos2)) continue;
                this.currentBlock = pos2;
                break;
            }
            if (this.currentBlock == null) {
                ExcavatorHack.MC.field_1761.method_2925();
                this.overlay.resetProgress();
            }
        }
        this.overlay.updateProgress();
        Predicate<class_2338> pBreakable = ExcavatorHack.MC.field_1724.method_31549().field_7477 ? BlockUtils::canBeClicked : pos -> BlockUtils.canBeClicked(pos) && !BlockUtils.isUnbreakable(pos);
        this.area.remainingBlocks = (int)this.area.blocksList.parallelStream().filter(pBreakable).count();
        if (this.area.remainingBlocks == 0) {
            this.setEnabled(false);
            return;
        }
        if (this.pathFinder == null) {
            class_2338 closestBlock = this.area.blocksList.parallelStream().filter(pBreakable).min(cNextTargetBlock).get();
            this.pathFinder = new ExcavatorPathFinder(closestBlock);
        }
        if (!this.pathFinder.isDone() && !this.pathFinder.isFailed()) {
            PathProcessor.lockControls();
            this.pathFinder.think();
            if (!this.pathFinder.isDone() && !this.pathFinder.isFailed()) {
                return;
            }
            this.pathFinder.formatPath();
            this.processor = this.pathFinder.getProcessor();
        }
        if (this.processor != null && !this.pathFinder.isPathStillValid(this.processor.getIndex())) {
            this.pathFinder = new ExcavatorPathFinder(this.pathFinder);
            return;
        }
        this.processor.process();
        if (this.processor.isDone()) {
            this.pathFinder = null;
            this.processor = null;
            PathProcessor.releaseControls();
        }
    }

    private ArrayList<class_2338> getValidBlocks() {
        class_243 eyesVec = RotationUtils.getEyesPos();
        class_2338 eyesBlock = class_2338.method_49638((class_2374)eyesVec);
        double rangeSq = Math.pow(this.range.getValue() + 0.5, 2.0);
        int blockRange = this.range.getValueCeil();
        return BlockUtils.getAllInBoxStream(eyesBlock, blockRange).filter(pos -> pos.method_19770((class_2374)eyesVec) <= rangeSq).filter(this.area.blocksSet::contains).filter(BlockUtils::canBeClicked).filter(pos -> !BlockUtils.isUnbreakable(pos)).sorted(Comparator.comparingDouble(pos -> pos.method_19770((class_2374)eyesVec))).collect(Collectors.toCollection(ArrayList::new));
    }

    private static enum Mode {
        FAST("Fast"),
        LEGIT("Legit");

        private final String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    private static enum Step {
        START_POS("Select start position.", true),
        END_POS("Select end position.", true),
        SCAN_AREA("Scanning area...", false),
        EXCAVATE("Excavating...", false);

        private static final Step[] SELECT_POSITION_STEPS;
        private final String message;
        private boolean selectPos;
        private class_2338 pos;

        private Step(String message, boolean selectPos) {
            this.message = message;
            this.selectPos = selectPos;
        }

        static {
            SELECT_POSITION_STEPS = new Step[]{START_POS, END_POS};
        }
    }

    private static class Area {
        private final int minX;
        private final int minY;
        private final int minZ;
        private final int sizeX;
        private final int sizeY;
        private final int sizeZ;
        private final int totalBlocks;
        private final int scanSpeed;
        private final Iterator<class_2338> iterator;
        private int scannedBlocks;
        private int remainingBlocks;
        private float progress;
        private final ArrayList<class_2338> blocksList = new ArrayList();
        private final HashSet<class_2338> blocksSet = new HashSet();

        private Area(class_2338 start, class_2338 end) {
            int startX = start.method_10263();
            int startY = start.method_10264();
            int startZ = start.method_10260();
            int endX = end.method_10263();
            int endY = end.method_10264();
            int endZ = end.method_10260();
            this.minX = Math.min(startX, endX);
            this.minY = Math.min(startY, endY);
            this.minZ = Math.min(startZ, endZ);
            this.sizeX = Math.abs(startX - endX);
            this.sizeY = Math.abs(startY - endY);
            this.sizeZ = Math.abs(startZ - endZ);
            this.totalBlocks = (this.sizeX + 1) * (this.sizeY + 1) * (this.sizeZ + 1);
            this.scanSpeed = class_3532.method_15340((int)(this.totalBlocks / 30), (int)1, (int)16384);
            this.iterator = BlockUtils.getAllInBox(start, end).iterator();
        }
    }

    private static class ExcavatorPathFinder
    extends PathFinder {
        public ExcavatorPathFinder(class_2338 goal) {
            super(goal);
            this.setThinkTime(10);
        }

        public ExcavatorPathFinder(ExcavatorPathFinder pathFinder) {
            super(pathFinder);
        }

        @Override
        protected boolean checkDone() {
            class_2338 goal = this.getGoal();
            this.done = goal.method_10087(2).equals((Object)this.current) || goal.method_10084().equals((Object)this.current) || goal.method_10095().equals((Object)this.current) || goal.method_10072().equals((Object)this.current) || goal.method_10067().equals((Object)this.current) || goal.method_10078().equals((Object)this.current) || goal.method_10074().method_10095().equals((Object)this.current) || goal.method_10074().method_10072().equals((Object)this.current) || goal.method_10074().method_10067().equals((Object)this.current) || goal.method_10074().method_10078().equals((Object)this.current);
            return this.done;
        }
    }
}
