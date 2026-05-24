package net.wurstclient.hacks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.class_1268;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3965;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.events.LeftClickListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.nukers.NukerMultiIdListSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.util.BlockBreaker;
import net.wurstclient.util.BlockBreakingCache;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.OverlayRenderer;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

public final class VeinMinerHack
extends Hack
implements UpdateListener,
LeftClickListener,
RenderListener {
    private static final class_238 BLOCK_BOX = new class_238(0.0625, 0.0625, 0.0625, 0.9375, 0.9375, 0.9375);
    private final SliderSetting range = new SliderSetting("Range", 5.0, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final CheckboxSetting flat = new CheckboxSetting("Flat mode", "Won't break any blocks below your feet.", false);
    private final NukerMultiIdListSetting multiIdList = new NukerMultiIdListSetting("The types of blocks to mine as veins.");
    private final SwingHandSetting swingHand = new SwingHandSetting(SwingHandSetting.genericMiningDescription(this), SwingHandSetting.SwingHand.SERVER);
    private final BlockBreakingCache cache = new BlockBreakingCache();
    private final OverlayRenderer overlay = new OverlayRenderer();
    private final HashSet<class_2338> currentVein = new HashSet();
    private class_2338 currentBlock;
    private final SliderSetting maxVeinSize = new SliderSetting("Max vein size", "Maximum number of blocks to mine in a single vein.", 64.0, 1.0, 1000.0, 1.0, SliderSetting.ValueDisplay.INTEGER);
    private final CheckboxSetting checkLOS = new CheckboxSetting("Check line of sight", "Makes sure that you don't reach through walls when breaking blocks.", false);

    public VeinMinerHack() {
        super("VeinMiner");
        this.setCategory(Category.BLOCKS);
        this.addSetting(this.range);
        this.addSetting(this.flat);
        this.addSetting(this.multiIdList);
        this.addSetting(this.swingHand);
        this.addSetting(this.maxVeinSize);
        this.addSetting(this.checkLOS);
    }

    @Override
    protected void onEnable() {
        VeinMinerHack.WURST.getHax().autoMineHack.setEnabled(false);
        VeinMinerHack.WURST.getHax().excavatorHack.setEnabled(false);
        VeinMinerHack.WURST.getHax().nukerHack.setEnabled(false);
        VeinMinerHack.WURST.getHax().nukerLegitHack.setEnabled(false);
        VeinMinerHack.WURST.getHax().speedNukerHack.setEnabled(false);
        VeinMinerHack.WURST.getHax().tunnellerHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(LeftClickListener.class, this);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(LeftClickListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        this.currentVein.clear();
        if (this.currentBlock != null) {
            VeinMinerHack.MC.field_1761.field_3717 = true;
            VeinMinerHack.MC.field_1761.method_2925();
            this.currentBlock = null;
        }
        this.cache.reset();
        this.overlay.resetProgress();
    }

    @Override
    public void onUpdate() {
        this.currentBlock = null;
        this.currentVein.removeIf(pos -> BlockUtils.getState(pos).method_45474());
        if (VeinMinerHack.MC.field_1690.field_1886.method_1434()) {
            return;
        }
        class_243 eyesVec = RotationUtils.getEyesPos();
        class_2338 eyesBlock = class_2338.method_49638((class_2374)eyesVec);
        double rangeSq = this.range.getValueSq();
        int blockRange = this.range.getValueCeil();
        Stream<BlockBreaker.BlockBreakingParams> stream = BlockUtils.getAllInBoxStream(eyesBlock, blockRange).filter(this::shouldBreakBlock).map(BlockBreaker::getBlockBreakingParams).filter(Objects::nonNull).filter(params -> params.distanceSq() <= rangeSq);
        if (this.checkLOS.isChecked()) {
            stream = stream.filter(BlockBreaker.BlockBreakingParams::lineOfSight);
        }
        stream = stream.sorted(BlockBreaker.comparingParams());
        if (VeinMinerHack.MC.field_1724.method_31549().field_7477) {
            VeinMinerHack.MC.field_1761.method_2925();
            this.overlay.resetProgress();
            ArrayList<class_2338> blocks = this.cache.filterOutRecentBlocks(stream.map(BlockBreaker.BlockBreakingParams::pos));
            if (blocks.isEmpty()) {
                return;
            }
            this.currentBlock = blocks.get(0);
            BlockBreaker.breakBlocksWithPacketSpam(blocks);
            this.swingHand.swing(class_1268.field_5808);
            return;
        }
        this.currentBlock = stream.filter(this::breakOneBlock).map(BlockBreaker.BlockBreakingParams::pos).findFirst().orElse(null);
        if (this.currentBlock == null) {
            VeinMinerHack.MC.field_1761.method_2925();
            this.overlay.resetProgress();
            return;
        }
        this.overlay.updateProgress();
    }

    private boolean shouldBreakBlock(class_2338 pos) {
        if (this.flat.isChecked() && (double)pos.method_10264() < VeinMinerHack.MC.field_1724.method_23318()) {
            return false;
        }
        return this.currentVein.contains(pos);
    }

    private boolean breakOneBlock(BlockBreaker.BlockBreakingParams params) {
        WURST.getRotationFaker().faceVectorPacket(params.hitVec());
        if (!VeinMinerHack.MC.field_1761.method_2902(params.pos(), params.side())) {
            return false;
        }
        this.swingHand.swing(class_1268.field_5808);
        return true;
    }

    @Override
    public void onLeftClick(LeftClickListener.LeftClickEvent event) {
        class_3965 bHitResult;
        if (!this.currentVein.isEmpty()) {
            return;
        }
        class_239 class_2392 = VeinMinerHack.MC.field_1765;
        if (!(class_2392 instanceof class_3965) || (bHitResult = (class_3965)class_2392).method_17783() != class_239.class_240.field_1332) {
            return;
        }
        if (!this.multiIdList.contains(BlockUtils.getBlock(bHitResult.method_17777()))) {
            return;
        }
        this.buildVein(bHitResult.method_17777());
    }

    private void buildVein(class_2338 pos) {
        ArrayDeque<class_2338> queue = new ArrayDeque<class_2338>();
        class_2248 targetBlock = BlockUtils.getBlock(pos);
        int maxSize = this.maxVeinSize.getValueI();
        queue.offer(pos);
        this.currentVein.add(pos);
        while (!queue.isEmpty() && this.currentVein.size() < maxSize) {
            class_2338 current = (class_2338)queue.poll();
            for (class_2350 direction : class_2350.values()) {
                class_2338 neighbor = current.method_10093(direction);
                if (this.currentVein.contains(neighbor) || BlockUtils.getBlock(neighbor) != targetBlock) continue;
                queue.offer(neighbor);
                this.currentVein.add(neighbor);
            }
        }
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        this.overlay.render(matrixStack, partialTicks, this.currentBlock);
        if (this.currentVein.isEmpty()) {
            return;
        }
        List<class_238> boxes = this.currentVein.stream().map(pos -> BLOCK_BOX.method_996(pos)).toList();
        RenderUtils.drawOutlinedBoxes(matrixStack, boxes, Integer.MIN_VALUE, false);
    }
}
