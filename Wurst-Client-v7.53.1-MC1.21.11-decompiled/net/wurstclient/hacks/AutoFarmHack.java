package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.class_1268;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.autofarm.AutoFarmPlantType;
import net.wurstclient.hacks.autofarm.AutoFarmPlantTypeManager;
import net.wurstclient.hacks.autofarm.AutoFarmRenderer;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.FaceTargetSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.util.BlockBreaker;
import net.wurstclient.util.BlockBreakingCache;
import net.wurstclient.util.BlockPlacer;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.InteractionSimulator;
import net.wurstclient.util.InventoryUtils;
import net.wurstclient.util.OverlayRenderer;
import net.wurstclient.util.RotationUtils;

@SearchTags(value={"auto farm", "AutoHarvest", "auto harvest"})
public final class AutoFarmHack
extends Hack
implements UpdateListener,
RenderListener {
    private final SliderSetting range = new SliderSetting("Range", 5.0, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final CheckboxSetting checkLOS = new CheckboxSetting("Check line of sight", "description.wurst.setting.autofarm.check_line_of_sight", false);
    private final FaceTargetSetting faceTarget = FaceTargetSetting.withoutPacketSpam(this, FaceTargetSetting.FaceTarget.SERVER);
    private final SwingHandSetting swingHand = new SwingHandSetting(this, SwingHandSetting.SwingHand.SERVER);
    private final AutoFarmPlantTypeManager plantTypes = new AutoFarmPlantTypeManager();
    private final HashMap<class_2338, AutoFarmPlantType> replantingSpots = new HashMap();
    private final BlockBreakingCache cache = new BlockBreakingCache();
    private class_2338 currentlyMining;
    private final AutoFarmRenderer renderer = new AutoFarmRenderer();
    private final OverlayRenderer overlay = new OverlayRenderer();
    private boolean busy;

    public AutoFarmHack() {
        super("AutoFarm");
        this.setCategory(Category.BLOCKS);
        this.addSetting(this.range);
        this.addSetting(this.checkLOS);
        this.addSetting(this.faceTarget);
        this.addSetting(this.swingHand);
        this.renderer.getSettings().forEach(x$0 -> this.addSetting((Setting)x$0));
        this.plantTypes.getSettings().forEach(x$0 -> this.addSetting((Setting)x$0));
    }

    @Override
    protected void onEnable() {
        AutoFarmHack.WURST.getHax().autoMineHack.setEnabled(false);
        this.replantingSpots.clear();
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        if (this.currentlyMining != null) {
            AutoFarmHack.MC.field_1761.field_3717 = true;
            AutoFarmHack.MC.field_1761.method_2925();
            this.currentlyMining = null;
        }
        this.cache.reset();
        this.overlay.resetProgress();
        this.busy = false;
    }

    @Override
    public void onUpdate() {
        boolean interacting;
        this.currentlyMining = null;
        class_243 eyesVec = RotationUtils.getEyesPos();
        class_2338 eyesBlock = class_2338.method_49638((class_2374)eyesVec);
        double rangeSq = this.range.getValueSq();
        int blockRange = this.range.getValueCeil();
        List<class_2338> nonEmptyBlocks = BlockUtils.getAllInBoxStream(eyesBlock, blockRange).filter(pos -> pos.method_19770((class_2374)eyesVec) <= rangeSq).filter(BlockUtils::canBeClicked).toList();
        for (class_2338 pos2 : nonEmptyBlocks) {
            AutoFarmPlantType plantType = this.plantTypes.getReplantingSpotType(pos2);
            if (plantType == null) continue;
            this.replantingSpots.put(pos2, plantType);
        }
        List<class_2338> blocksToMine = List.of();
        List<class_2338> blocksToInteract = List.of();
        List<class_2338> blocksToReplant = List.of();
        blocksToMine = nonEmptyBlocks.stream().filter(this.plantTypes::shouldHarvestByMining).sorted(Comparator.comparingDouble(pos -> pos.method_19770((class_2374)eyesVec))).toList();
        blocksToInteract = nonEmptyBlocks.stream().filter(this.plantTypes::shouldHarvestByInteracting).sorted(Comparator.comparingDouble(pos -> pos.method_19770((class_2374)eyesVec))).toList();
        blocksToReplant = BlockUtils.getAllInBoxStream(eyesBlock, blockRange).filter(pos -> pos.method_19770((class_2374)eyesVec) <= rangeSq).filter(pos -> BlockUtils.getState(pos).method_45474()).filter(pos -> {
            AutoFarmPlantType plantType = this.replantingSpots.get(pos);
            return plantType != null && plantType.isReplantingEnabled() && plantType.hasPlantingSurface((class_2338)pos);
        }).sorted(Comparator.comparingDouble(pos -> pos.method_19770((class_2374)eyesVec))).toList();
        boolean replanting = this.replant(blocksToReplant);
        boolean bl = interacting = replanting ? false : this.harvestByInteracting(blocksToInteract);
        if (!interacting && !replanting) {
            this.harvestByMining(blocksToMine);
        }
        this.busy = replanting || interacting || this.currentlyMining != null;
        List<class_2338> blocksToHarvest = Stream.of(blocksToMine, blocksToInteract).flatMap(Collection::stream).toList();
        this.renderer.update(this.replantingSpots.keySet(), blocksToHarvest, blocksToReplant);
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        this.renderer.render(matrixStack);
        if (this.renderer.drawBlocksToHarvest.isChecked()) {
            this.overlay.render(matrixStack, partialTicks, this.currentlyMining);
        }
    }

    public boolean isBusy() {
        return this.busy;
    }

    private boolean replant(List<class_2338> blocksToReplant) {
        if (AutoFarmHack.MC.field_1752 > 0) {
            return false;
        }
        if (AutoFarmHack.MC.field_1761.method_2923() || AutoFarmHack.MC.field_1724.method_3144()) {
            return false;
        }
        Optional<class_1792> heldSeed = blocksToReplant.stream().map(this.replantingSpots::get).filter(Objects::nonNull).map(AutoFarmPlantType::getSeedItem).distinct().filter(arg_0 -> ((class_746)AutoFarmHack.MC.field_1724).method_24518(arg_0)).findFirst();
        if (heldSeed.isPresent()) {
            class_1792 item = heldSeed.get();
            class_1268 hand = AutoFarmHack.MC.field_1724.method_6047().method_31574(item) ? class_1268.field_5808 : class_1268.field_5810;
            for (class_2338 pos : blocksToReplant) {
                BlockPlacer.BlockPlacingParams params;
                AutoFarmPlantType plantType = this.replantingSpots.get(pos);
                if (plantType == null || plantType.getSeedItem() != item || (params = BlockPlacer.getBlockPlacingParams(pos)) == null || params.distanceSq() > this.range.getValueSq() || params.requiresSneaking() || this.checkLOS.isChecked() && !params.lineOfSight()) continue;
                AutoFarmHack.MC.field_1752 = 4;
                this.faceTarget.face(params.hitVec());
                InteractionSimulator.rightClickBlock(params.toHitResult(), hand, (SwingHandSetting.SwingHand)((Object)this.swingHand.getSelected()));
                return true;
            }
        }
        for (class_2338 pos : blocksToReplant) {
            AutoFarmPlantType plantType;
            BlockPlacer.BlockPlacingParams params = BlockPlacer.getBlockPlacingParams(pos);
            if (params == null || params.distanceSq() > this.range.getValueSq() || params.requiresSneaking() || (plantType = this.replantingSpots.get(pos)) == null || !InventoryUtils.selectItem(plantType.getSeedItem()) && !InventoryUtils.giveCreativeItem(plantType.getSeedItem())) continue;
            return true;
        }
        return false;
    }

    private boolean harvestByInteracting(List<class_2338> blocksToInteract) {
        if (AutoFarmHack.MC.field_1752 > 0) {
            return false;
        }
        if (AutoFarmHack.MC.field_1761.method_2923() || AutoFarmHack.MC.field_1724.method_3144()) {
            return false;
        }
        for (class_2338 pos : blocksToInteract) {
            BlockBreaker.BlockBreakingParams params = BlockBreaker.getBlockBreakingParams(pos);
            if (params == null || params.distanceSq() > this.range.getValueSq() || this.checkLOS.isChecked() && !params.lineOfSight()) continue;
            if (AutoFarmHack.MC.field_1724.method_6047().method_31574(class_1802.field_8324)) {
                return InventoryUtils.selectItem(s -> !s.method_31574(class_1802.field_8324));
            }
            AutoFarmHack.MC.field_1752 = 4;
            this.faceTarget.face(params.hitVec());
            InteractionSimulator.rightClickBlock(params.toHitResult(), (SwingHandSetting.SwingHand)((Object)this.swingHand.getSelected()));
            return true;
        }
        return false;
    }

    private void harvestByMining(List<class_2338> blocksToMine) {
        double rangeSq = this.range.getValueSq();
        Stream<BlockBreaker.BlockBreakingParams> stream = blocksToMine.stream().map(BlockBreaker::getBlockBreakingParams).filter(Objects::nonNull).filter(params -> params.distanceSq() <= rangeSq);
        if (this.checkLOS.isChecked()) {
            stream = stream.filter(BlockBreaker.BlockBreakingParams::lineOfSight);
        }
        stream = stream.sorted(BlockBreaker.comparingParams());
        if (AutoFarmHack.MC.field_1724.method_31549().field_7477 && this.faceTarget.getSelected() == FaceTargetSetting.FaceTarget.OFF) {
            AutoFarmHack.MC.field_1761.method_2925();
            this.overlay.resetProgress();
            ArrayList<class_2338> blocks = this.cache.filterOutRecentBlocks(stream.map(BlockBreaker.BlockBreakingParams::pos));
            if (blocks.isEmpty()) {
                return;
            }
            this.currentlyMining = (class_2338)blocks.get(0);
            BlockBreaker.breakBlocksWithPacketSpam(blocks);
            this.swingHand.swing(class_1268.field_5808);
            return;
        }
        this.currentlyMining = stream.filter(this::breakOneBlock).map(BlockBreaker.BlockBreakingParams::pos).findFirst().orElse(null);
        if (this.currentlyMining == null) {
            AutoFarmHack.MC.field_1761.method_2925();
            this.overlay.resetProgress();
            return;
        }
        this.overlay.updateProgress();
    }

    private boolean breakOneBlock(BlockBreaker.BlockBreakingParams params) {
        this.faceTarget.face(params.hitVec());
        if (!AutoFarmHack.MC.field_1761.method_2902(params.pos(), params.side())) {
            return false;
        }
        this.swingHand.swing(class_1268.field_5808);
        return true;
    }
}
