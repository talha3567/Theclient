package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1511;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.FaceTargetSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.settings.TakeItemsFromSetting;
import net.wurstclient.settings.filterlists.CrystalAuraFilterList;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.FakePlayerEntity;
import net.wurstclient.util.InventoryUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags(value={"crystal aura"})
public final class CrystalAuraHack
extends Hack
implements UpdateListener {
    private final SliderSetting range = new SliderSetting("Range", "Determines how far CrystalAura will reach to place and detonate crystals.", 6.0, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final CheckboxSetting autoPlace = new CheckboxSetting("Auto-place crystals", "When enabled, CrystalAura will automatically place crystals near valid entities.\nWhen disabled, CrystalAura will only detonate manually placed crystals.", true);
    private final CheckboxSetting checkLOS = new CheckboxSetting("Check line of sight", "Ensures that you don't reach through blocks when placing or left-clicking end crystals.\n\nSlower but can help with anti-cheat plugins.", false);
    private final FaceTargetSetting faceTarget = FaceTargetSetting.withPacketSpam(this, FaceTargetSetting.FaceTarget.OFF);
    private final SwingHandSetting swingHand = new SwingHandSetting(this, SwingHandSetting.SwingHand.CLIENT);
    private final TakeItemsFromSetting takeItemsFrom = TakeItemsFromSetting.withoutHands(this, TakeItemsFromSetting.TakeItemsFrom.INVENTORY);
    private final EntityFilterList entityFilters = CrystalAuraFilterList.create();

    public CrystalAuraHack() {
        super("CrystalAura");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.range);
        this.addSetting(this.autoPlace);
        this.addSetting(this.checkLOS);
        this.addSetting(this.faceTarget);
        this.addSetting(this.swingHand);
        this.addSetting(this.takeItemsFrom);
        this.entityFilters.forEach(x$0 -> this.addSetting((Setting)x$0));
    }

    @Override
    protected void onEnable() {
        CrystalAuraHack.WURST.getHax().aimAssistHack.setEnabled(false);
        CrystalAuraHack.WURST.getHax().clickAuraHack.setEnabled(false);
        CrystalAuraHack.WURST.getHax().fightBotHack.setEnabled(false);
        CrystalAuraHack.WURST.getHax().killauraHack.setEnabled(false);
        CrystalAuraHack.WURST.getHax().killauraLegitHack.setEnabled(false);
        CrystalAuraHack.WURST.getHax().multiAuraHack.setEnabled(false);
        CrystalAuraHack.WURST.getHax().protectHack.setEnabled(false);
        CrystalAuraHack.WURST.getHax().triggerBotHack.setEnabled(false);
        CrystalAuraHack.WURST.getHax().tpAuraHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        ArrayList<class_1297> crystals = this.getNearbyCrystals();
        if (!crystals.isEmpty()) {
            this.detonate(crystals);
            return;
        }
        if (!this.autoPlace.isChecked()) {
            return;
        }
        if (InventoryUtils.indexOf(class_1802.field_8301, this.takeItemsFrom.getMaxInvSlot()) == -1) {
            return;
        }
        ArrayList<class_1297> targets = this.getNearbyTargets();
        this.placeCrystalsNear(targets);
    }

    private ArrayList<class_2338> placeCrystalsNear(ArrayList<class_1297> targets) {
        ArrayList<class_2338> newCrystals = new ArrayList<class_2338>();
        boolean shouldSwing = false;
        block0: for (class_1297 target : targets) {
            ArrayList<class_2338> freeBlocks = this.getFreeBlocksNear(target);
            for (class_2338 pos : freeBlocks) {
                if (!this.placeCrystal(pos)) continue;
                shouldSwing = true;
                newCrystals.add(pos);
                continue block0;
            }
        }
        if (shouldSwing) {
            this.swingHand.swing(class_1268.field_5808);
        }
        return newCrystals;
    }

    private void detonate(ArrayList<class_1297> crystals) {
        for (class_1297 e : crystals) {
            this.faceTarget.face(e.method_5829().method_1005());
            CrystalAuraHack.MC.field_1761.method_2918((class_1657)CrystalAuraHack.MC.field_1724, e);
        }
        if (!crystals.isEmpty()) {
            this.swingHand.swing(class_1268.field_5808);
        }
    }

    private boolean placeCrystal(class_2338 pos) {
        class_243 eyesPos = RotationUtils.getEyesPos();
        double rangeSq = Math.pow(this.range.getValue(), 2.0);
        class_243 posVec = class_243.method_24953((class_2382)pos);
        double distanceSqPosVec = eyesPos.method_1025(posVec);
        for (class_2350 side : class_2350.values()) {
            class_243 dirVec;
            class_243 hitVec;
            class_2338 neighbor = pos.method_10093(side);
            if (!this.isClickableNeighbor(neighbor) || eyesPos.method_1025(hitVec = posVec.method_1019((dirVec = class_243.method_24954((class_2382)side.method_62675())).method_1021(0.5))) > rangeSq || distanceSqPosVec > eyesPos.method_1025(posVec.method_1019(dirVec)) || this.checkLOS.isChecked() && !BlockUtils.hasLineOfSight(eyesPos, hitVec)) continue;
            InventoryUtils.selectItem(class_1802.field_8301, this.takeItemsFrom.getMaxInvSlot());
            if (!CrystalAuraHack.MC.field_1724.method_24518(class_1802.field_8301)) {
                return false;
            }
            this.faceTarget.face(hitVec);
            IMC.getInteractionManager().rightClickBlock(neighbor, side.method_10153(), hitVec);
            return true;
        }
        return false;
    }

    private ArrayList<class_1297> getNearbyCrystals() {
        double rangeSq = Math.pow(this.range.getValue(), 2.0);
        Comparator<class_1297> furthestFromPlayer = Comparator.comparingDouble(EntityUtils::distanceToHitboxSq).reversed();
        return StreamSupport.stream(CrystalAuraHack.MC.field_1687.method_18112().spliterator(), true).filter(class_1511.class::isInstance).filter(e -> !e.method_31481()).filter(e -> EntityUtils.distanceToHitboxSq(e) <= rangeSq).sorted(furthestFromPlayer).collect(Collectors.toCollection(ArrayList::new));
    }

    private ArrayList<class_1297> getNearbyTargets() {
        double rangeSq = Math.pow(this.range.getValue(), 2.0);
        Comparator<class_1297> furthestFromPlayer = Comparator.comparingDouble(EntityUtils::distanceToHitboxSq).reversed();
        Stream<class_1297> stream = StreamSupport.stream(CrystalAuraHack.MC.field_1687.method_18112().spliterator(), false).filter(e -> !e.method_31481()).filter(e -> e instanceof class_1309 && ((class_1309)e).method_6032() > 0.0f).filter(e -> e != CrystalAuraHack.MC.field_1724).filter(e -> !(e instanceof FakePlayerEntity)).filter(e -> !WURST.getFriends().contains(e.method_5477().getString())).filter(e -> EntityUtils.distanceToHitboxSq(e) <= rangeSq);
        stream = this.entityFilters.applyTo(stream);
        return stream.sorted(furthestFromPlayer).collect(Collectors.toCollection(ArrayList::new));
    }

    private ArrayList<class_2338> getFreeBlocksNear(class_1297 target) {
        class_243 eyesVec = RotationUtils.getEyesPos().method_1023(0.5, 0.5, 0.5);
        double rangeD = this.range.getValue();
        double rangeSq = Math.pow(rangeD + 0.5, 2.0);
        int rangeI = 2;
        class_2338 center = target.method_24515();
        class_2338 min = center.method_10069(-rangeI, -rangeI, -rangeI);
        class_2338 max = center.method_10069(rangeI, rangeI, rangeI);
        class_238 targetBB = target.method_5829();
        class_243 targetEyesVec = target.method_73189().method_1031(0.0, (double)target.method_18381(target.method_18376()), 0.0);
        Comparator<class_2338> closestToTarget = Comparator.comparingDouble(pos -> targetEyesVec.method_1025(class_243.method_24953((class_2382)pos)));
        return BlockUtils.getAllInBoxStream(min, max).filter(pos -> eyesVec.method_1025(class_243.method_24954((class_2382)pos)) <= rangeSq).filter(this::isReplaceable).filter(this::hasCrystalBase).filter(pos -> !targetBB.method_994(new class_238(pos))).sorted(closestToTarget).collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean isReplaceable(class_2338 pos) {
        return BlockUtils.getState(pos).method_45474();
    }

    private boolean hasCrystalBase(class_2338 pos) {
        class_2248 block = BlockUtils.getBlock(pos.method_10074());
        return block == class_2246.field_9987 || block == class_2246.field_10540;
    }

    private boolean isClickableNeighbor(class_2338 pos) {
        return BlockUtils.canBeClicked(pos) && !BlockUtils.getState(pos).method_45474();
    }
}
