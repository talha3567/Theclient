package net.wurstclient.hacks;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.StreamSupport;
import net.minecraft.class_1268;
import net.minecraft.class_1540;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1922;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2346;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2527;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2682;
import net.minecraft.class_290;
import net.minecraft.class_304;
import net.minecraft.class_315;
import net.minecraft.class_4538;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.WurstRenderLayers;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hack.Hack;
import net.wurstclient.hack.HackList;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.EasyVertexBuffer;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.OverlayRenderer;
import net.wurstclient.util.RegionPos;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

@DontSaveState
public final class TunnellerHack
extends Hack
implements UpdateListener,
RenderListener {
    private final EnumSetting<TunnelSize> size = new EnumSetting("Tunnel size", (Enum[])TunnelSize.values(), (Enum)TunnelSize.SIZE_3X3);
    private final SliderSetting limit = new SliderSetting("Limit", "Automatically stops once the tunnel has reached the given length.\n\n0 = no limit", 0.0, 0.0, 1000.0, 1.0, SliderSetting.ValueDisplay.INTEGER.withSuffix(" blocks").withLabel(1.0, "1 block").withLabel(0.0, "disabled"));
    private final CheckboxSetting torches = new CheckboxSetting("Place torches", "Places just enough torches to prevent mobs from spawning inside the tunnel.", false);
    private final OverlayRenderer overlay = new OverlayRenderer();
    private class_2338 start;
    private class_2350 direction;
    private int length;
    private Task[] tasks;
    private EasyVertexBuffer[] vertexBuffers = new EasyVertexBuffer[5];
    private class_2338 currentBlock;
    private class_2338 lastTorch;
    private class_2338 nextTorch;

    public TunnellerHack() {
        super("Tunneller");
        this.setCategory(Category.BLOCKS);
        this.addSetting(this.size);
        this.addSetting(this.limit);
        this.addSetting(this.torches);
    }

    @Override
    public String getRenderName() {
        if (this.limit.getValueI() == 0) {
            return this.getName();
        }
        return this.getName() + " [" + this.length + "/" + this.limit.getValueI() + "]";
    }

    @Override
    protected void onEnable() {
        TunnellerHack.WURST.getHax().autoMineHack.setEnabled(false);
        TunnellerHack.WURST.getHax().excavatorHack.setEnabled(false);
        TunnellerHack.WURST.getHax().fightBotHack.setEnabled(false);
        TunnellerHack.WURST.getHax().followHack.setEnabled(false);
        TunnellerHack.WURST.getHax().instantBunkerHack.setEnabled(false);
        TunnellerHack.WURST.getHax().nukerHack.setEnabled(false);
        TunnellerHack.WURST.getHax().nukerLegitHack.setEnabled(false);
        TunnellerHack.WURST.getHax().protectHack.setEnabled(false);
        TunnellerHack.WURST.getHax().speedNukerHack.setEnabled(false);
        TunnellerHack.WURST.getHax().veinMinerHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(RenderListener.class, this);
        class_746 player = TunnellerHack.MC.field_1724;
        this.start = class_2338.method_49638((class_2374)player.method_73189());
        this.direction = player.method_5735();
        this.length = 0;
        this.lastTorch = null;
        this.nextTorch = this.start;
        this.tasks = new Task[]{new DodgeLiquidTask(), new FillInFloorTask(), new PlaceTorchTask(), new WaitForFallingBlocksTask(), new DigTunnelTask(), new WalkForwardTask()};
        this.updateCyanBuffer();
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        this.overlay.resetProgress();
        if (this.currentBlock != null) {
            TunnellerHack.MC.field_1761.field_3717 = true;
            TunnellerHack.MC.field_1761.method_2925();
            this.currentBlock = null;
        }
        for (int i = 0; i < this.vertexBuffers.length; ++i) {
            if (this.vertexBuffers[i] == null) continue;
            this.vertexBuffers[i].close();
            this.vertexBuffers[i] = null;
        }
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public void onUpdate() {
        class_304[] bindings;
        void var5_7;
        Hack[] incompatibleHax;
        HackList hax = WURST.getHax();
        Hack[] hackArray = incompatibleHax = new Hack[]{hax.autoSwitchHack, hax.autoToolHack, hax.autoWalkHack, hax.blinkHack, hax.flightHack, hax.scaffoldWalkHack, hax.sneakHack};
        int n = hackArray.length;
        boolean bl = false;
        while (var5_7 < n) {
            Hack hack = hackArray[var5_7];
            hack.setEnabled(false);
            ++var5_7;
        }
        if (hax.freecamHack.isMovingCamera() || hax.remoteViewHack.isEnabled()) {
            return;
        }
        class_315 gs = TunnellerHack.MC.field_1690;
        for (class_304 class_3042 : bindings = new class_304[]{gs.field_1894, gs.field_1881, gs.field_1913, gs.field_1849, gs.field_1903, gs.field_1832}) {
            class_3042.method_23481(false);
        }
        for (Task task : this.tasks) {
            if (!task.canRun()) continue;
            task.run();
            break;
        }
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        matrixStack.method_22903();
        RenderUtils.applyRegionalRenderOffset(matrixStack);
        for (EasyVertexBuffer buffer : this.vertexBuffers) {
            if (buffer == null) continue;
            buffer.draw(matrixStack, WurstRenderLayers.ESP_LINES);
        }
        matrixStack.method_22909();
        this.overlay.render(matrixStack, partialTicks, this.currentBlock);
    }

    private void updateCyanBuffer() {
        if (this.vertexBuffers[0] != null) {
            this.vertexBuffers[0].close();
        }
        RegionPos region = RenderUtils.getCameraRegion();
        class_243 offset = class_243.method_24953((class_2382)this.start).method_1020(region.toVec3d());
        int cyan = -2147418113;
        class_238 nodeBox = new class_238(-0.25, -0.25, -0.25, 0.25, 0.25, 0.25).method_997(offset);
        class_243 dirVec = class_243.method_24954((class_2382)this.direction.method_62675());
        class_243 arrowStart = dirVec.method_1021(0.25).method_1019(offset);
        class_243 arrowEnd = dirVec.method_1021(Math.max(0.5, (double)this.length)).method_1019(offset);
        this.vertexBuffers[0] = EasyVertexBuffer.createAndUpload(VertexFormat.class_5596.field_27377, class_290.field_63455, buffer -> {
            RenderUtils.drawNode(buffer, nodeBox, cyan);
            RenderUtils.drawArrow(buffer, arrowStart, arrowEnd, cyan, 0.1f);
        });
    }

    private class_2338 offset(class_2338 pos, class_2382 vec) {
        return pos.method_10079(this.direction.method_10160(), vec.method_10263()).method_10086(vec.method_10264());
    }

    private int getDistance(class_2338 pos1, class_2338 pos2) {
        return Math.abs(pos1.method_10263() - pos2.method_10263()) + Math.abs(pos1.method_10264() - pos2.method_10264()) + Math.abs(pos1.method_10260() - pos2.method_10260());
    }

    public ArrayList<class_2338> getAllInBox(class_2338 from, class_2338 to) {
        ArrayList<class_2338> blocks = new ArrayList<class_2338>();
        class_2350 front = this.direction;
        class_2350 left = front.method_10160();
        int fromFront = from.method_10263() * front.method_10148() + from.method_10260() * front.method_10165();
        int toFront = to.method_10263() * front.method_10148() + to.method_10260() * front.method_10165();
        int fromLeft = from.method_10263() * left.method_10148() + from.method_10260() * left.method_10165();
        int toLeft = to.method_10263() * left.method_10148() + to.method_10260() * left.method_10165();
        int minFront = Math.min(fromFront, toFront);
        int maxFront = Math.max(fromFront, toFront);
        int minY = Math.min(from.method_10264(), to.method_10264());
        int maxY = Math.max(from.method_10264(), to.method_10264());
        int minLeft = Math.min(fromLeft, toLeft);
        int maxLeft = Math.max(fromLeft, toLeft);
        for (int f = minFront; f <= maxFront; ++f) {
            for (int y = maxY; y >= minY; --y) {
                for (int l = maxLeft; l >= minLeft; --l) {
                    int x = f * front.method_10148() + l * left.method_10148();
                    int z = f * front.method_10165() + l * left.method_10165();
                    blocks.add(new class_2338(x, y, z));
                }
            }
        }
        return blocks;
    }

    private void placeBlockSimple(class_2338 pos) {
        int i;
        class_2350 side = null;
        class_2350[] sides = class_2350.values();
        class_243 eyesPos = RotationUtils.getEyesPos();
        class_243 posVec = class_243.method_24953((class_2382)pos);
        double distanceSqPosVec = eyesPos.method_1025(posVec);
        class_243[] hitVecs = new class_243[sides.length];
        for (i = 0; i < sides.length; ++i) {
            hitVecs[i] = posVec.method_1019(class_243.method_24954((class_2382)sides[i].method_62675()).method_1021(0.5));
        }
        for (i = 0; i < sides.length; ++i) {
            class_2680 neighborState;
            class_265 neighborShape;
            class_2338 neighbor = pos.method_10093(sides[i]);
            if (!BlockUtils.canBeClicked(neighbor) || TunnellerHack.MC.field_1687.method_17745(eyesPos, hitVecs[i], neighbor, neighborShape = (neighborState = BlockUtils.getState(neighbor)).method_26218((class_1922)TunnellerHack.MC.field_1687, neighbor), neighborState) != null) continue;
            side = sides[i];
            break;
        }
        if (side == null) {
            for (i = 0; i < sides.length; ++i) {
                if (!BlockUtils.canBeClicked(pos.method_10093(sides[i])) || distanceSqPosVec > eyesPos.method_1025(hitVecs[i])) continue;
                side = sides[i];
                break;
            }
        }
        if (side == null) {
            return;
        }
        class_243 hitVec = hitVecs[side.ordinal()];
        WURST.getRotationFaker().faceVectorPacket(hitVec);
        if (RotationUtils.getAngleToLastReportedLookVec(hitVec) > 1.0) {
            return;
        }
        if (TunnellerHack.MC.field_1752 > 0) {
            return;
        }
        IMC.getInteractionManager().rightClickBlock(pos.method_10093(side), side.method_10153(), hitVec);
        SwingHandSetting.SwingHand.SERVER.swing(class_1268.field_5808);
        TunnellerHack.MC.field_1752 = 4;
    }

    private boolean breakBlock(class_2338 pos) {
        class_2350[] sides = class_2350.values();
        class_243 eyesPos = RotationUtils.getEyesPos();
        class_243 relCenter = BlockUtils.getBoundingBox(pos).method_989((double)(-pos.method_10263()), (double)(-pos.method_10264()), (double)(-pos.method_10260())).method_1005();
        class_243 center = class_243.method_24954((class_2382)pos).method_1019(relCenter);
        class_243[] hitVecs = new class_243[sides.length];
        for (int i = 0; i < sides.length; ++i) {
            class_2382 dirVec = sides[i].method_62675();
            class_243 relHitVec = new class_243(relCenter.field_1352 * (double)dirVec.method_10263(), relCenter.field_1351 * (double)dirVec.method_10264(), relCenter.field_1350 * (double)dirVec.method_10260());
            hitVecs[i] = center.method_1019(relHitVec);
        }
        double[] distancesSq = new double[sides.length];
        boolean[] linesOfSight = new boolean[sides.length];
        double distanceSqToCenter = eyesPos.method_1025(center);
        for (int i = 0; i < sides.length; ++i) {
            distancesSq[i] = eyesPos.method_1025(hitVecs[i]);
            if (distancesSq[i] >= distanceSqToCenter) continue;
            linesOfSight[i] = BlockUtils.hasLineOfSight(eyesPos, hitVecs[i]);
        }
        class_2350 side = sides[0];
        for (int i = 1; i < sides.length; ++i) {
            int bestSide = side.ordinal();
            if (!linesOfSight[bestSide] && linesOfSight[i]) {
                side = sides[i];
                continue;
            }
            if (!(distancesSq[i] < distancesSq[bestSide])) continue;
            side = sides[i];
        }
        WURST.getRotationFaker().faceVectorPacket(hitVecs[side.ordinal()]);
        if (!TunnellerHack.MC.field_1761.method_2902(pos, side)) {
            return false;
        }
        SwingHandSetting.SwingHand.SERVER.swing(class_1268.field_5808);
        return true;
    }

    private static enum TunnelSize {
        SIZE_1X2("1x2", new class_2382(0, 1, 0), new class_2382(0, 0, 0), 4, 13),
        SIZE_1X3("1x3", new class_2382(0, 2, 0), new class_2382(0, 0, 0), 4, 13),
        SIZE_1X4("1x4", new class_2382(0, 3, 0), new class_2382(0, 0, 0), 4, 13),
        SIZE_1X5("1x5", new class_2382(0, 4, 0), new class_2382(0, 0, 0), 3, 13),
        SIZE_2X2("2x2", new class_2382(1, 1, 0), new class_2382(0, 0, 0), 4, 11),
        SIZE_2X3("2x3", new class_2382(1, 2, 0), new class_2382(0, 0, 0), 4, 11),
        SIZE_2X4("2x4", new class_2382(1, 3, 0), new class_2382(0, 0, 0), 4, 11),
        SIZE_2X5("2x5", new class_2382(1, 4, 0), new class_2382(0, 0, 0), 3, 11),
        SIZE_3X2("3x2", new class_2382(1, 1, 0), new class_2382(-1, 0, 0), 4, 11),
        SIZE_3X3("3x3", new class_2382(1, 2, 0), new class_2382(-1, 0, 0), 4, 11),
        SIZE_3X4("3x4", new class_2382(1, 3, 0), new class_2382(-1, 0, 0), 4, 11),
        SIZE_3X5("3x5", new class_2382(1, 4, 0), new class_2382(-1, 0, 0), 3, 11),
        SIZE_4X2("4x2", new class_2382(2, 1, 0), new class_2382(-1, 0, 0), 4, 9),
        SIZE_4X3("4x3", new class_2382(2, 2, 0), new class_2382(-1, 0, 0), 4, 9),
        SIZE_4X4("4x4", new class_2382(2, 3, 0), new class_2382(-1, 0, 0), 4, 9),
        SIZE_4X5("4x5", new class_2382(2, 4, 0), new class_2382(-1, 0, 0), 3, 9),
        SIZE_5X2("5x2", new class_2382(2, 1, 0), new class_2382(-2, 0, 0), 4, 9),
        SIZE_5X3("5x3", new class_2382(2, 2, 0), new class_2382(-2, 0, 0), 4, 9),
        SIZE_5X4("5x4", new class_2382(2, 3, 0), new class_2382(-2, 0, 0), 4, 9),
        SIZE_5X5("5x5", new class_2382(2, 4, 0), new class_2382(-2, 0, 0), 3, 9);

        private final String name;
        private final class_2382 from;
        private final class_2382 to;
        private final int maxRange;
        private final int torchDistance;

        private TunnelSize(String name, class_2382 from, class_2382 to, int maxRange, int torchDistance) {
            this.name = name;
            this.from = from;
            this.to = to;
            this.maxRange = maxRange;
            this.torchDistance = torchDistance;
        }

        public String toString() {
            return this.name;
        }
    }

    private static abstract class Task {
        private Task() {
        }

        public abstract boolean canRun();

        public abstract void run();
    }

    private class DodgeLiquidTask
    extends Task {
        private final HashSet<class_2338> liquids = new HashSet();
        private int disableTimer = 60;

        private DodgeLiquidTask() {
        }

        @Override
        public boolean canRun() {
            if (!this.liquids.isEmpty()) {
                return true;
            }
            class_2338 base = TunnellerHack.this.start.method_10079(TunnellerHack.this.direction, TunnellerHack.this.length);
            class_2338 from = TunnellerHack.this.offset(base, TunnellerHack.this.size.getSelected().from);
            class_2338 to = TunnellerHack.this.offset(base, TunnellerHack.this.size.getSelected().to);
            int maxY = Math.max(from.method_10264(), to.method_10264());
            for (class_2338 pos : BlockUtils.getAllInBox(from, to)) {
                class_2338 pos4;
                int maxOffset = Math.min(TunnellerHack.this.size.getSelected().maxRange, TunnellerHack.this.length);
                for (int i = 0; i <= maxOffset; ++i) {
                    class_2338 pos2 = pos.method_10079(TunnellerHack.this.direction.method_10153(), i);
                    if (BlockUtils.getState(pos2).method_26227().method_15769()) continue;
                    this.liquids.add(pos2);
                }
                if (BlockUtils.getState(pos).method_26234((class_1922)MC.field_1687, pos)) continue;
                class_2338 pos3 = pos.method_10093(TunnellerHack.this.direction);
                if (!BlockUtils.getState(pos3).method_26227().method_15769()) {
                    this.liquids.add(pos3);
                }
                if (pos.method_10264() != maxY || BlockUtils.getState(pos4 = pos.method_10084()).method_26227().method_15769()) continue;
                this.liquids.add(pos4);
            }
            if (this.liquids.isEmpty()) {
                return false;
            }
            ChatUtils.error("The tunnel is flooded, cannot continue.");
            if (TunnellerHack.this.vertexBuffers[3] != null) {
                TunnellerHack.this.vertexBuffers[3].close();
                TunnellerHack.this.vertexBuffers[3] = null;
            }
            if (!this.liquids.isEmpty()) {
                RegionPos region = RenderUtils.getCameraRegion();
                class_238 box = new class_238(class_2338.field_10980).method_1011(0.1).method_997(region.negate().toVec3d());
                int red = -2130771968;
                TunnellerHack.this.vertexBuffers[3] = EasyVertexBuffer.createAndUpload(VertexFormat.class_5596.field_27377, class_290.field_63455, buffer -> {
                    for (class_2338 pos : this.liquids) {
                        RenderUtils.drawOutlinedBox(buffer, box.method_996(pos), red);
                    }
                });
            }
            return true;
        }

        @Override
        public void run() {
            class_243 dirVec;
            class_2338 player = class_2338.method_49638((class_2374)MC.field_1724.method_73189());
            class_304 forward = MC.field_1690.field_1894;
            class_243 diffVec = class_243.method_24954((class_2382)player.method_10059((class_2382)TunnellerHack.this.start));
            double dotProduct = diffVec.method_1026(dirVec = class_243.method_24954((class_2382)TunnellerHack.this.direction.method_62675()));
            class_2338 pos1 = TunnellerHack.this.start.method_10079(TunnellerHack.this.direction, (int)dotProduct);
            if (!player.equals((Object)pos1)) {
                WURST.getRotationFaker().faceVectorClientIgnorePitch(this.toVec3d(pos1));
                forward.method_23481(true);
                return;
            }
            class_2338 pos2 = TunnellerHack.this.start.method_10079(TunnellerHack.this.direction, Math.max(0, TunnellerHack.this.length - 10));
            if (!player.equals((Object)pos2)) {
                WURST.getRotationFaker().faceVectorClientIgnorePitch(this.toVec3d(pos2));
                forward.method_23481(true);
                MC.field_1724.method_5728(true);
                return;
            }
            class_2338 pos3 = TunnellerHack.this.start.method_10079(TunnellerHack.this.direction, TunnellerHack.this.length + 1);
            WURST.getRotationFaker().faceVectorClientIgnorePitch(this.toVec3d(pos3));
            forward.method_23481(false);
            MC.field_1724.method_5728(false);
            if (this.disableTimer > 0) {
                --this.disableTimer;
                return;
            }
            TunnellerHack.this.setEnabled(false);
        }

        private class_243 toVec3d(class_2338 pos) {
            return class_243.method_24953((class_2382)pos);
        }
    }

    private class FillInFloorTask
    extends Task {
        private final ArrayList<class_2338> blocks = new ArrayList();

        private FillInFloorTask() {
        }

        @Override
        public boolean canRun() {
            class_2338 player = class_2338.method_49638((class_2374)MC.field_1724.method_73189());
            class_2338 from = this.offsetFloor(player, TunnellerHack.this.size.getSelected().from);
            class_2338 to = this.offsetFloor(player, TunnellerHack.this.size.getSelected().to);
            this.blocks.clear();
            for (class_2338 pos : BlockUtils.getAllInBox(from, to)) {
                if (BlockUtils.getState(pos).method_26234((class_1922)MC.field_1687, pos)) continue;
                this.blocks.add(pos);
            }
            if (TunnellerHack.this.vertexBuffers[2] != null) {
                TunnellerHack.this.vertexBuffers[2].close();
                TunnellerHack.this.vertexBuffers[2] = null;
            }
            if (!this.blocks.isEmpty()) {
                RegionPos region = RenderUtils.getCameraRegion();
                class_238 box = new class_238(class_2338.field_10980).method_1011(0.1).method_997(region.negate().toVec3d());
                int yellow = -2130706688;
                TunnellerHack.this.vertexBuffers[2] = EasyVertexBuffer.createAndUpload(VertexFormat.class_5596.field_27377, class_290.field_63455, buffer -> {
                    for (class_2338 pos : this.blocks) {
                        RenderUtils.drawOutlinedBox(buffer, box.method_996(pos), yellow);
                    }
                });
                return true;
            }
            return false;
        }

        private class_2338 offsetFloor(class_2338 pos, class_2382 vec) {
            return pos.method_10079(TunnellerHack.this.direction.method_10160(), vec.method_10263()).method_10074();
        }

        @Override
        public void run() {
            MC.field_1690.field_1832.method_23481(true);
            class_243 velocity = MC.field_1724.method_18798();
            MC.field_1724.method_18800(0.0, velocity.field_1351, 0.0);
            class_243 eyes = RotationUtils.getEyesPos().method_1031(-0.5, -0.5, -0.5);
            Comparator<class_2338> comparator = Comparator.comparingDouble(p -> eyes.method_1025(class_243.method_24954((class_2382)p)));
            class_2338 pos = this.blocks.stream().max(comparator).get();
            if (!this.equipSolidBlock(pos)) {
                ChatUtils.error("Found a hole in the tunnel's floor but don't have any blocks to fill it with.");
                TunnellerHack.this.setEnabled(false);
                return;
            }
            if (BlockUtils.getState(pos).method_45474()) {
                TunnellerHack.this.placeBlockSimple(pos);
            } else {
                WURST.getHax().autoToolHack.equipBestTool(pos, false, true, 0);
                TunnellerHack.this.breakBlock(pos);
            }
        }

        private boolean equipSolidBlock(class_2338 pos) {
            for (int slot = 0; slot < 9; ++slot) {
                class_2248 block;
                class_2680 state;
                class_1799 stack = MC.field_1724.method_31548().method_5438(slot);
                if (stack.method_7960() || !(stack.method_7909() instanceof class_1747) || !(state = (block = class_2248.method_9503((class_1792)stack.method_7909())).method_9564()).method_26234((class_1922)class_2682.field_12294, class_2338.field_10980) || block instanceof class_2346 && class_2346.method_10128((class_2680)BlockUtils.getState(pos.method_10074()))) continue;
                MC.field_1724.method_31548().method_61496(slot);
                return true;
            }
            return false;
        }
    }

    private class PlaceTorchTask
    extends Task {
        private PlaceTorchTask() {
        }

        @Override
        public boolean canRun() {
            if (TunnellerHack.this.vertexBuffers[4] != null) {
                TunnellerHack.this.vertexBuffers[4].close();
                TunnellerHack.this.vertexBuffers[4] = null;
            }
            if (!TunnellerHack.this.torches.isChecked()) {
                TunnellerHack.this.lastTorch = null;
                TunnellerHack.this.nextTorch = class_2338.method_49638((class_2374)MC.field_1724.method_73189());
                return false;
            }
            if (BlockUtils.getBlock(TunnellerHack.this.nextTorch) instanceof class_2527) {
                TunnellerHack.this.lastTorch = TunnellerHack.this.nextTorch;
            }
            if (TunnellerHack.this.lastTorch != null) {
                TunnellerHack.this.nextTorch = TunnellerHack.this.lastTorch.method_10079(TunnellerHack.this.direction, TunnellerHack.this.size.getSelected().torchDistance);
            }
            RegionPos region = RenderUtils.getCameraRegion();
            class_243 torchVec = class_243.method_24955((class_2382)TunnellerHack.this.nextTorch).method_1020(region.toVec3d());
            int yellow = -2130706688;
            TunnellerHack.this.vertexBuffers[4] = EasyVertexBuffer.createAndUpload(VertexFormat.class_5596.field_27377, class_290.field_63455, buffer -> RenderUtils.drawArrow(buffer, torchVec, torchVec.method_1031(0.0, 0.5, 0.0), yellow, 0.1f));
            class_2338 player = class_2338.method_49638((class_2374)MC.field_1724.method_73189());
            if (TunnellerHack.this.getDistance(player, TunnellerHack.this.nextTorch) > 4) {
                return false;
            }
            class_2680 state = BlockUtils.getState(TunnellerHack.this.nextTorch);
            if (!state.method_45474()) {
                return false;
            }
            return class_2246.field_10336.method_9564().method_26184((class_4538)MC.field_1687, TunnellerHack.this.nextTorch);
        }

        @Override
        public void run() {
            if (!this.equipTorch()) {
                ChatUtils.error("Out of torches.");
                TunnellerHack.this.setEnabled(false);
                return;
            }
            MC.field_1690.field_1832.method_23481(true);
            TunnellerHack.this.placeBlockSimple(TunnellerHack.this.nextTorch);
        }

        private boolean equipTorch() {
            for (int slot = 0; slot < 9; ++slot) {
                class_2248 block;
                class_1799 stack = MC.field_1724.method_31548().method_5438(slot);
                if (stack.method_7960() || !(stack.method_7909() instanceof class_1747) || !((block = class_2248.method_9503((class_1792)stack.method_7909())) instanceof class_2527)) continue;
                MC.field_1724.method_31548().method_61496(slot);
                return true;
            }
            return false;
        }
    }

    private static class WaitForFallingBlocksTask
    extends Task {
        private WaitForFallingBlocksTask() {
        }

        @Override
        public boolean canRun() {
            return StreamSupport.stream(MC.field_1687.method_18112().spliterator(), false).filter(class_1540.class::isInstance).anyMatch(e -> EntityUtils.distanceToHitboxSq(e) < 36.0);
        }

        @Override
        public void run() {
        }
    }

    private class DigTunnelTask
    extends Task {
        private int maxDistance;

        private DigTunnelTask() {
        }

        @Override
        public boolean canRun() {
            class_2338 base;
            class_2338 player = class_2338.method_49638((class_2374)MC.field_1724.method_73189());
            int distance = TunnellerHack.this.getDistance(player, base = TunnellerHack.this.start.method_10079(TunnellerHack.this.direction, TunnellerHack.this.length));
            if (distance <= 1) {
                this.maxDistance = TunnellerHack.this.size.getSelected().maxRange;
            } else if (distance > TunnellerHack.this.size.getSelected().maxRange) {
                this.maxDistance = 1;
            }
            return distance <= this.maxDistance;
        }

        @Override
        public void run() {
            class_2338 player = class_2338.method_49638((class_2374)MC.field_1724.method_73189());
            class_2338 base = TunnellerHack.this.start.method_10079(TunnellerHack.this.direction, TunnellerHack.this.length);
            class_2338 from = TunnellerHack.this.offset(player, TunnellerHack.this.size.getSelected().from);
            class_2338 to = TunnellerHack.this.offset(base, TunnellerHack.this.size.getSelected().to);
            ArrayList blocks = new ArrayList();
            TunnellerHack.this.getAllInBox(from, to).forEach(blocks::add);
            RegionPos region = RenderUtils.getCameraRegion();
            class_238 blockBox = new class_238(class_2338.field_10980).method_1011(0.1).method_997(region.negate().toVec3d());
            TunnellerHack.this.currentBlock = null;
            ArrayList<class_238> boxes = new ArrayList<class_238>();
            for (class_2338 pos : blocks) {
                if (!BlockUtils.canBeClicked(pos) || (pos.equals((Object)TunnellerHack.this.nextTorch) || pos.equals((Object)TunnellerHack.this.lastTorch)) && BlockUtils.getBlock(pos) instanceof class_2527) continue;
                if (TunnellerHack.this.currentBlock == null) {
                    TunnellerHack.this.currentBlock = pos;
                }
                boxes.add(blockBox.method_996(pos));
            }
            if (TunnellerHack.this.vertexBuffers[1] != null) {
                TunnellerHack.this.vertexBuffers[1].close();
                TunnellerHack.this.vertexBuffers[1] = null;
            }
            int green = -2147418368;
            if (!boxes.isEmpty()) {
                TunnellerHack.this.vertexBuffers[1] = EasyVertexBuffer.createAndUpload(VertexFormat.class_5596.field_27377, class_290.field_63455, buffer -> {
                    for (class_238 box : boxes) {
                        RenderUtils.drawOutlinedBox(buffer, box, green);
                    }
                });
            }
            if (TunnellerHack.this.currentBlock == null) {
                MC.field_1761.method_2925();
                TunnellerHack.this.overlay.resetProgress();
                ++TunnellerHack.this.length;
                if (TunnellerHack.this.limit.getValueI() == 0 || TunnellerHack.this.length < TunnellerHack.this.limit.getValueI()) {
                    TunnellerHack.this.updateCyanBuffer();
                } else {
                    ChatUtils.message("Tunnel completed.");
                    TunnellerHack.this.setEnabled(false);
                }
                return;
            }
            WURST.getHax().autoToolHack.equipBestTool(TunnellerHack.this.currentBlock, false, true, 0);
            TunnellerHack.this.breakBlock(TunnellerHack.this.currentBlock);
            if (MC.field_1724.method_31549().field_7477 || BlockUtils.getHardness(TunnellerHack.this.currentBlock) >= 1.0f) {
                TunnellerHack.this.overlay.resetProgress();
                return;
            }
            TunnellerHack.this.overlay.updateProgress();
        }
    }

    private class WalkForwardTask
    extends Task {
        private WalkForwardTask() {
        }

        @Override
        public boolean canRun() {
            class_2338 base;
            class_2338 player = class_2338.method_49638((class_2374)MC.field_1724.method_73189());
            return TunnellerHack.this.getDistance(player, base = TunnellerHack.this.start.method_10079(TunnellerHack.this.direction, TunnellerHack.this.length)) > 1;
        }

        @Override
        public void run() {
            class_2338 base = TunnellerHack.this.start.method_10079(TunnellerHack.this.direction, TunnellerHack.this.length);
            class_243 vec = class_243.method_24953((class_2382)base);
            WURST.getRotationFaker().faceVectorClientIgnorePitch(vec);
            MC.field_1690.field_1894.method_23481(true);
        }
    }
}
