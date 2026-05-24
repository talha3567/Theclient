package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.class_1268;
import net.minecraft.class_1794;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_243;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.HandleInputListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.util.BlockBreaker;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.InteractionSimulator;
import net.wurstclient.util.RotationUtils;

@SearchTags(value={"till aura", "HoeAura", "hoe aura", "FarmlandAura", "farmland aura", "farm land aura", "AutoTill", "auto till", "AutoHoe", "auto hoe"})
public final class TillauraHack
extends Hack
implements HandleInputListener {
    private final SliderSetting range = new SliderSetting("Range", "How far Tillaura will reach to till blocks.", 5.0, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final CheckboxSetting multiTill = new CheckboxSetting("MultiTill", "Tills multiple blocks at once.\nFaster, but can't bypass NoCheat+.", false);
    private final CheckboxSetting checkLOS = new CheckboxSetting("Check line of sight", "Prevents Tillaura from reaching through blocks.\nGood for NoCheat+ servers, but unnecessary in vanilla.", true);
    private final List<class_2248> tillableBlocks = List.of(class_2246.field_10219, class_2246.field_10194, class_2246.field_10566, class_2246.field_10253);

    public TillauraHack() {
        super("Tillaura");
        this.setCategory(Category.BLOCKS);
        this.addSetting(this.range);
        this.addSetting(this.multiTill);
        this.addSetting(this.checkLOS);
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
        block6: {
            ArrayList<class_2338> validBlocks;
            block5: {
                if (TillauraHack.MC.field_1752 > 0) {
                    return;
                }
                if (TillauraHack.MC.field_1761.method_2923() || TillauraHack.MC.field_1724.method_3144()) {
                    return;
                }
                if (!TillauraHack.MC.field_1724.method_24520(stack -> stack.method_7909() instanceof class_1794)) {
                    return;
                }
                validBlocks = this.getValidBlocks();
                if (!this.multiTill.isChecked()) break block5;
                boolean shouldSwing = false;
                for (class_2338 pos : validBlocks) {
                    if (!this.rightClickBlockSimple(pos)) continue;
                    shouldSwing = true;
                }
                if (!shouldSwing) break block6;
                TillauraHack.MC.field_1724.method_6104(class_1268.field_5808);
                break block6;
            }
            for (class_2338 pos : validBlocks) {
                if (this.rightClickBlockLegit(pos)) break;
            }
        }
    }

    private ArrayList<class_2338> getValidBlocks() {
        class_243 eyesVec = RotationUtils.getEyesPos();
        class_2338 eyesBlock = class_2338.method_49638((class_2374)eyesVec);
        double rangeSq = this.range.getValueSq();
        int blockRange = this.range.getValueCeil();
        return BlockUtils.getAllInBoxStream(eyesBlock, blockRange).filter(pos -> pos.method_19770((class_2374)eyesVec) <= rangeSq).filter(this::isCorrectBlock).sorted(Comparator.comparingDouble(pos -> pos.method_19770((class_2374)eyesVec))).collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean isCorrectBlock(class_2338 pos) {
        if (!this.tillableBlocks.contains(BlockUtils.getBlock(pos))) {
            return false;
        }
        return BlockUtils.getState(pos.method_10084()).method_26215();
    }

    private boolean rightClickBlockLegit(class_2338 pos) {
        BlockBreaker.BlockBreakingParams params = BlockBreaker.getBlockBreakingParams(pos);
        if (params == null || params.distanceSq() > this.range.getValueSq()) {
            return false;
        }
        if (this.checkLOS.isChecked() && !params.lineOfSight()) {
            return false;
        }
        TillauraHack.MC.field_1752 = 4;
        WURST.getRotationFaker().faceVectorPacket(params.hitVec());
        InteractionSimulator.rightClickBlock(params.toHitResult());
        return true;
    }

    private boolean rightClickBlockSimple(class_2338 pos) {
        BlockBreaker.BlockBreakingParams params = BlockBreaker.getBlockBreakingParams(pos);
        if (params == null || params.distanceSq() > this.range.getValueSq()) {
            return false;
        }
        if (this.checkLOS.isChecked() && !params.lineOfSight()) {
            return false;
        }
        InteractionSimulator.rightClickBlock(params.toHitResult(), SwingHandSetting.SwingHand.OFF);
        return true;
    }
}
