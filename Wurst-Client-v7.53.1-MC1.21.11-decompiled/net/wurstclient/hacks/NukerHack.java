package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.events.LeftClickListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.nukers.CommonNukerSettings;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.util.BlockBreaker;
import net.wurstclient.util.BlockBreakingCache;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.OverlayRenderer;
import net.wurstclient.util.RotationUtils;

public final class NukerHack
extends Hack
implements UpdateListener,
RenderListener {
    private final SliderSetting range = new SliderSetting("Range", 5.0, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final CommonNukerSettings commonSettings = new CommonNukerSettings();
    private final SwingHandSetting swingHand = new SwingHandSetting(SwingHandSetting.genericMiningDescription(this), SwingHandSetting.SwingHand.SERVER);
    private final BlockBreakingCache cache = new BlockBreakingCache();
    private final OverlayRenderer overlay = new OverlayRenderer();
    private class_2338 currentBlock;

    public NukerHack() {
        super("Nuker");
        this.setCategory(Category.BLOCKS);
        this.addSetting(this.range);
        this.commonSettings.getSettings().forEach(x$0 -> this.addSetting((Setting)x$0));
        this.addSetting(this.swingHand);
    }

    @Override
    public String getRenderName() {
        return this.getName() + this.commonSettings.getRenderNameSuffix();
    }

    @Override
    protected void onEnable() {
        NukerHack.WURST.getHax().autoMineHack.setEnabled(false);
        NukerHack.WURST.getHax().excavatorHack.setEnabled(false);
        NukerHack.WURST.getHax().nukerLegitHack.setEnabled(false);
        NukerHack.WURST.getHax().speedNukerHack.setEnabled(false);
        NukerHack.WURST.getHax().tunnellerHack.setEnabled(false);
        NukerHack.WURST.getHax().veinMinerHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(LeftClickListener.class, this.commonSettings);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(LeftClickListener.class, this.commonSettings);
        EVENTS.remove(RenderListener.class, this);
        if (this.currentBlock != null) {
            NukerHack.MC.field_1761.field_3717 = true;
            NukerHack.MC.field_1761.method_2925();
            this.currentBlock = null;
        }
        this.cache.reset();
        this.overlay.resetProgress();
        this.commonSettings.reset();
    }

    @Override
    public void onUpdate() {
        this.currentBlock = null;
        if (NukerHack.MC.field_1690.field_1886.method_1434() || this.commonSettings.isIdModeWithAir()) {
            return;
        }
        class_243 eyesVec = RotationUtils.getEyesPos();
        class_2338 eyesBlock = class_2338.method_49638((class_2374)eyesVec);
        double rangeSq = this.range.getValueSq();
        int blockRange = this.range.getValueCeil();
        Stream<BlockBreaker.BlockBreakingParams> stream = BlockUtils.getAllInBoxStream(eyesBlock, blockRange).filter(this.commonSettings::shouldBreakBlock).map(BlockBreaker::getBlockBreakingParams).filter(Objects::nonNull);
        if (this.commonSettings.isSphereShape()) {
            stream = stream.filter(params -> params.distanceSq() <= rangeSq);
        }
        stream = stream.sorted(BlockBreaker.comparingParams());
        if (NukerHack.MC.field_1724.method_31549().field_7477) {
            NukerHack.MC.field_1761.method_2925();
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
            NukerHack.MC.field_1761.method_2925();
            this.overlay.resetProgress();
            return;
        }
        this.overlay.updateProgress();
    }

    private boolean breakOneBlock(BlockBreaker.BlockBreakingParams params) {
        WURST.getRotationFaker().faceVectorPacket(params.hitVec());
        if (!NukerHack.MC.field_1761.method_2902(params.pos(), params.side())) {
            return false;
        }
        this.swingHand.swing(class_1268.field_5808);
        return true;
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        this.overlay.render(matrixStack, partialTicks, this.currentBlock);
    }
}
