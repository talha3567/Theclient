package net.wurstclient.hacks;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.class_1268;
import net.minecraft.class_1802;
import net.minecraft.class_2248;
import net.minecraft.class_2256;
import net.minecraft.class_2282;
import net.minecraft.class_2302;
import net.minecraft.class_2338;
import net.minecraft.class_2372;
import net.minecraft.class_2374;
import net.minecraft.class_2472;
import net.minecraft.class_2473;
import net.minecraft.class_2513;
import net.minecraft.class_2680;
import net.minecraft.class_4538;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.HandleInputListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.FaceTargetSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.settings.TakeItemsFromSetting;
import net.wurstclient.util.BlockBreaker;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.InteractionSimulator;
import net.wurstclient.util.InventoryUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags(value={"bonemeal aura", "bone meal aura", "AutoBonemeal", "auto bonemeal", "auto bone meal", "fertilizer", "bmaura"})
public final class BonemealAuraHack
extends Hack
implements HandleInputListener {
    private final SliderSetting range = new SliderSetting("Range", 5.0, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final CheckboxSetting multiMeal = new CheckboxSetting("MultiMeal", "description.wurst.setting.bonemealaura.multimeal", false);
    private final CheckboxSetting checkLOS = new CheckboxSetting("Check line of sight", "description.wurst.setting.bonemealaura.check_los", true);
    private final FaceTargetSetting faceTarget = FaceTargetSetting.withPacketSpam(this, FaceTargetSetting.FaceTarget.SERVER);
    private final SwingHandSetting swingHand = new SwingHandSetting(this, SwingHandSetting.SwingHand.CLIENT);
    private final CheckboxSetting fastPlace = new CheckboxSetting("Always FastPlace", "description.wurst.setting.bonemealaura.always_fastplace", true);
    private final CheckboxSetting useWhileBreaking = new CheckboxSetting("Use while breaking", "description.wurst.setting.bonemealaura.use_while_breaking", false);
    private final CheckboxSetting useWhileRiding = new CheckboxSetting("Use while riding", "description.wurst.setting.bonemealaura.use_while_riding", false);
    private final TakeItemsFromSetting takeItemsFrom = TakeItemsFromSetting.withHands(this, TakeItemsFromSetting.TakeItemsFrom.HANDS);
    private final CheckboxSetting saplings = new CheckboxSetting("Saplings", true);
    private final CheckboxSetting crops = new CheckboxSetting("Crops", "Wheat, carrots, potatoes and beetroots.", true);
    private final CheckboxSetting stems = new CheckboxSetting("Stems", "Pumpkins and melons.", true);
    private final CheckboxSetting cocoa = new CheckboxSetting("Cocoa", true);
    private final CheckboxSetting seaPickles = new CheckboxSetting("Sea pickles", true);
    private final CheckboxSetting other = new CheckboxSetting("Other", false);

    public BonemealAuraHack() {
        super("BonemealAura");
        this.setCategory(Category.BLOCKS);
        this.addSetting(this.range);
        this.addSetting(this.multiMeal);
        this.addSetting(this.checkLOS);
        this.addSetting(this.faceTarget);
        this.addSetting(this.swingHand);
        this.addSetting(this.fastPlace);
        this.addSetting(this.useWhileBreaking);
        this.addSetting(this.useWhileRiding);
        this.addSetting(this.takeItemsFrom);
        this.addSetting(this.saplings);
        this.addSetting(this.crops);
        this.addSetting(this.stems);
        this.addSetting(this.cocoa);
        this.addSetting(this.seaPickles);
        this.addSetting(this.other);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(HandleInputListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(HandleInputListener.class, this);
    }

    @Override
    public void onHandleInput() {
        if (!this.fastPlace.isChecked() && BonemealAuraHack.MC.field_1752 > 0) {
            return;
        }
        if (!this.useWhileBreaking.isChecked() && BonemealAuraHack.MC.field_1761.method_2923()) {
            return;
        }
        if (!this.useWhileRiding.isChecked() && BonemealAuraHack.MC.field_1724.method_3144()) {
            return;
        }
        if (BonemealAuraHack.WURST.getHax().autoFarmHack.isBusy()) {
            return;
        }
        boolean holdingBoneMeal = BonemealAuraHack.MC.field_1724.method_24518(class_1802.field_8324);
        int boneMealSlot = InventoryUtils.indexOf(class_1802.field_8324, this.takeItemsFrom.getMaxInvSlot());
        if (!holdingBoneMeal && boneMealSlot < 0) {
            return;
        }
        List<BlockBreaker.BlockBreakingParams> validBlocks = this.getValidBlocks();
        if (validBlocks.isEmpty()) {
            return;
        }
        if (!holdingBoneMeal) {
            InventoryUtils.selectItem(boneMealSlot);
            return;
        }
        if (this.multiMeal.isChecked()) {
            boolean shouldSwing = false;
            for (BlockBreaker.BlockBreakingParams params : validBlocks) {
                this.faceTarget.face(params.hitVec());
                InteractionSimulator.rightClickBlock(params.toHitResult(), SwingHandSetting.SwingHand.OFF);
                shouldSwing = true;
            }
            if (shouldSwing) {
                this.swingHand.swing(class_1268.field_5808);
            }
        } else {
            BlockBreaker.BlockBreakingParams params = validBlocks.getFirst();
            BonemealAuraHack.MC.field_1752 = 4;
            this.faceTarget.face(params.hitVec());
            InteractionSimulator.rightClickBlock(params.toHitResult(), (SwingHandSetting.SwingHand)((Object)this.swingHand.getSelected()));
        }
    }

    private List<BlockBreaker.BlockBreakingParams> getValidBlocks() {
        class_2338 eyesBlock = class_2338.method_49638((class_2374)RotationUtils.getEyesPos());
        double rangeSq = this.range.getValueSq();
        Stream<BlockBreaker.BlockBreakingParams> stream = BlockUtils.getAllInBoxStream(eyesBlock, this.range.getValueCeil()).filter(this::isCorrectBlock).map(BlockBreaker::getBlockBreakingParams).filter(Objects::nonNull).filter(params -> params.distanceSq() <= rangeSq);
        if (this.checkLOS.isChecked()) {
            stream = stream.filter(BlockBreaker.BlockBreakingParams::lineOfSight);
        }
        Comparator<BlockBreaker.BlockBreakingParams> farthestFirst = Comparator.comparingDouble(BlockBreaker.BlockBreakingParams::distanceSq).reversed();
        stream = stream.sorted(farthestFirst);
        return stream.toList();
    }

    private boolean isCorrectBlock(class_2338 pos) {
        class_2256 bmBlock;
        class_2248 block = BlockUtils.getBlock(pos);
        class_2680 state = BlockUtils.getState(pos);
        if (!(block instanceof class_2256) || !(bmBlock = (class_2256)block).method_9651((class_4538)BonemealAuraHack.MC.field_1687, pos, state)) {
            return false;
        }
        if (block instanceof class_2372) {
            return false;
        }
        if (block instanceof class_2473) {
            return this.saplings.isChecked();
        }
        if (block instanceof class_2302) {
            return this.crops.isChecked();
        }
        if (block instanceof class_2513) {
            return this.stems.isChecked();
        }
        if (block instanceof class_2282) {
            return this.cocoa.isChecked();
        }
        if (block instanceof class_2472) {
            return this.seaPickles.isChecked();
        }
        return this.other.isChecked();
    }
}
