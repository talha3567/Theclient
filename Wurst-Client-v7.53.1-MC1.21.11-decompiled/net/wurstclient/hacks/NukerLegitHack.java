package net.wurstclient.hacks;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3965;
import net.minecraft.class_4587;
import net.minecraft.class_636;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.HandleBlockBreakingListener;
import net.wurstclient.events.LeftClickListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.nukers.CommonNukerSettings;
import net.wurstclient.mixinterface.IKeyMapping;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.util.BlockBreaker;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.OverlayRenderer;
import net.wurstclient.util.RotationUtils;

@SearchTags(value={"LegitNuker", "nuker legit", "legit nuker"})
public final class NukerLegitHack
extends Hack
implements UpdateListener,
HandleBlockBreakingListener,
RenderListener {
    private final SliderSetting range = new SliderSetting("Range", 4.25, 1.0, 4.5, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final CommonNukerSettings commonSettings = new CommonNukerSettings();
    private final SwingHandSetting swingHand = SwingHandSetting.withoutOffOption(SwingHandSetting.genericMiningDescription(this), SwingHandSetting.SwingHand.CLIENT);
    private final OverlayRenderer overlay = new OverlayRenderer();
    private class_2338 currentBlock;

    public NukerLegitHack() {
        super("NukerLegit");
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
        NukerLegitHack.WURST.getHax().autoMineHack.setEnabled(false);
        NukerLegitHack.WURST.getHax().excavatorHack.setEnabled(false);
        NukerLegitHack.WURST.getHax().nukerHack.setEnabled(false);
        NukerLegitHack.WURST.getHax().speedNukerHack.setEnabled(false);
        NukerLegitHack.WURST.getHax().tunnellerHack.setEnabled(false);
        NukerLegitHack.WURST.getHax().veinMinerHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(LeftClickListener.class, this.commonSettings);
        EVENTS.add(HandleBlockBreakingListener.class, this);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(LeftClickListener.class, this.commonSettings);
        EVENTS.remove(HandleBlockBreakingListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        IKeyMapping.get(NukerLegitHack.MC.field_1690.field_1886).resetPressedState();
        NukerLegitHack.MC.field_1761.method_2925();
        this.overlay.resetProgress();
        this.currentBlock = null;
        this.commonSettings.reset();
    }

    @Override
    public void onUpdate() {
        this.currentBlock = null;
        if (this.commonSettings.isIdModeWithAir()) {
            this.overlay.resetProgress();
            return;
        }
        if (NukerLegitHack.MC.field_1724.method_3144()) {
            this.overlay.resetProgress();
            NukerLegitHack.MC.field_1761.method_2925();
            return;
        }
        class_243 eyesVec = RotationUtils.getEyesPos();
        class_2338 eyesBlock = class_2338.method_49638((class_2374)eyesVec);
        double maxRange = NukerLegitHack.MC.field_1724.method_55754() + 1.0;
        double rangeSq = this.commonSettings.isSphereShape() ? this.range.getValueSq() : maxRange * maxRange;
        int blockRange = this.range.getValueCeil();
        Stream<BlockBreaker.BlockBreakingParams> stream = BlockUtils.getAllInBoxStream(eyesBlock, blockRange).filter(this.commonSettings::shouldBreakBlock).map(BlockBreaker::getBlockBreakingParams).filter(Objects::nonNull).filter(BlockBreaker.BlockBreakingParams::lineOfSight).filter(params -> params.distanceSq() <= rangeSq).sorted(Comparator.comparingDouble(BlockBreaker.BlockBreakingParams::distanceSq));
        this.currentBlock = stream.filter(this::breakBlock).map(BlockBreaker.BlockBreakingParams::pos).findFirst().orElse(null);
        if (this.currentBlock == null) {
            IKeyMapping.get(NukerLegitHack.MC.field_1690.field_1886).resetPressedState();
            this.overlay.resetProgress();
        }
        this.overlay.updateProgress();
    }

    private boolean breakBlock(BlockBreaker.BlockBreakingParams params) {
        class_636 im = NukerLegitHack.MC.field_1761;
        WURST.getRotationFaker().faceVectorClient(params.hitVec());
        class_239 hitResult = NukerLegitHack.MC.field_1765;
        if (hitResult == null || hitResult.method_17783() != class_239.class_240.field_1332 || !(hitResult instanceof class_3965)) {
            im.method_2925();
            return true;
        }
        class_3965 bHitResult = (class_3965)hitResult;
        class_2338 pos = bHitResult.method_17777();
        class_2680 state = NukerLegitHack.MC.field_1687.method_8320(pos);
        class_2350 side = bHitResult.method_17780();
        if (state.method_26215() || !params.pos().equals((Object)pos) || !params.side().equals((Object)side)) {
            im.method_2925();
            return true;
        }
        NukerLegitHack.WURST.getHax().autoToolHack.equipIfEnabled(params.pos());
        if (NukerLegitHack.MC.field_1724.method_6115()) {
            return true;
        }
        if (!im.method_2923()) {
            im.method_2910(pos, side);
        }
        if (im.method_2902(pos, side)) {
            NukerLegitHack.MC.field_1687.method_74254(pos, side);
            this.swingHand.swing(class_1268.field_5808);
            NukerLegitHack.MC.field_1690.field_1886.method_23481(true);
        }
        return true;
    }

    @Override
    public void onHandleBlockBreaking(HandleBlockBreakingListener.HandleBlockBreakingEvent event) {
        if (this.currentBlock != null) {
            event.cancel();
        }
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        this.overlay.render(matrixStack, partialTicks, this.currentBlock);
    }
}
