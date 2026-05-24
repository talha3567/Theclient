package net.wurstclient.hacks;

import java.util.Comparator;
import java.util.stream.Stream;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_465;
import net.wurstclient.Category;
import net.wurstclient.events.MouseUpdateListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.AimAtSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.settings.filters.AttackDetectingEntityFilter;
import net.wurstclient.settings.filters.FilterArmorStandsSetting;
import net.wurstclient.settings.filters.FilterBabiesSetting;
import net.wurstclient.settings.filters.FilterBatsSetting;
import net.wurstclient.settings.filters.FilterCrystalsSetting;
import net.wurstclient.settings.filters.FilterEndermenSetting;
import net.wurstclient.settings.filters.FilterFlyingSetting;
import net.wurstclient.settings.filters.FilterGolemsSetting;
import net.wurstclient.settings.filters.FilterHostileSetting;
import net.wurstclient.settings.filters.FilterInvisibleSetting;
import net.wurstclient.settings.filters.FilterNamedSetting;
import net.wurstclient.settings.filters.FilterNeutralSetting;
import net.wurstclient.settings.filters.FilterPassiveSetting;
import net.wurstclient.settings.filters.FilterPassiveWaterSetting;
import net.wurstclient.settings.filters.FilterPetsSetting;
import net.wurstclient.settings.filters.FilterPiglinsSetting;
import net.wurstclient.settings.filters.FilterPlayersSetting;
import net.wurstclient.settings.filters.FilterShulkerBulletSetting;
import net.wurstclient.settings.filters.FilterShulkersSetting;
import net.wurstclient.settings.filters.FilterSleepingSetting;
import net.wurstclient.settings.filters.FilterSlimesSetting;
import net.wurstclient.settings.filters.FilterVillagersSetting;
import net.wurstclient.settings.filters.FilterZombiePiglinsSetting;
import net.wurstclient.settings.filters.FilterZombieVillagersSetting;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.Rotation;
import net.wurstclient.util.RotationUtils;

public final class AimAssistHack
extends Hack
implements UpdateListener,
MouseUpdateListener {
    private final SliderSetting range = new SliderSetting("Range", 4.5, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final SliderSetting rotationSpeed = new SliderSetting("Rotation Speed", 600.0, 10.0, 3600.0, 10.0, SliderSetting.ValueDisplay.DEGREES.withSuffix("/s"));
    private final SliderSetting fov = new SliderSetting("FOV", "description.wurst.setting.aimassist.fov", 120.0, 30.0, 360.0, 10.0, SliderSetting.ValueDisplay.DEGREES);
    private final AimAtSetting aimAt = new AimAtSetting("What point in the target's hitbox AimAssist should aim at.");
    private final SliderSetting ignoreMouseInput = new SliderSetting("Ignore mouse input", "description.wurst.setting.aimassist.ignore_mouse_input", 0.0, 0.0, 1.0, 0.01, SliderSetting.ValueDisplay.PERCENTAGE);
    private final CheckboxSetting checkLOS = new CheckboxSetting("Check line of sight", "description.wurst.setting.aimassist.check_line_of_sight", true);
    private final CheckboxSetting aimWhileBlocking = new CheckboxSetting("Aim while blocking", "description.wurst.setting.aimassist.aim_while_blocking", false);
    private final EntityFilterList entityFilters = new EntityFilterList(FilterPlayersSetting.genericCombat(false), FilterSleepingSetting.genericCombat(false), FilterFlyingSetting.genericCombat(0.0), FilterHostileSetting.genericCombat(false), FilterNeutralSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF), FilterPassiveSetting.genericCombat(true), FilterPassiveWaterSetting.genericCombat(true), FilterBabiesSetting.genericCombat(true), FilterBatsSetting.genericCombat(true), FilterSlimesSetting.genericCombat(true), FilterPetsSetting.genericCombat(true), FilterVillagersSetting.genericCombat(true), FilterZombieVillagersSetting.genericCombat(true), FilterGolemsSetting.genericCombat(false), FilterPiglinsSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF), FilterZombiePiglinsSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF), FilterEndermenSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF), FilterShulkersSetting.genericCombat(false), FilterInvisibleSetting.genericCombat(true), FilterNamedSetting.genericCombat(false), FilterShulkerBulletSetting.genericCombat(false), FilterArmorStandsSetting.genericCombat(true), FilterCrystalsSetting.genericCombat(true));
    private class_1297 target;
    private float nextYaw;
    private float nextPitch;

    public AimAssistHack() {
        super("AimAssist");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.range);
        this.addSetting(this.rotationSpeed);
        this.addSetting(this.fov);
        this.addSetting(this.aimAt);
        this.addSetting(this.ignoreMouseInput);
        this.addSetting(this.checkLOS);
        this.addSetting(this.aimWhileBlocking);
        this.entityFilters.forEach(x$0 -> this.addSetting((Setting)x$0));
    }

    @Override
    protected void onEnable() {
        AimAssistHack.WURST.getHax().autoFishHack.setEnabled(false);
        AimAssistHack.WURST.getHax().clickAuraHack.setEnabled(false);
        AimAssistHack.WURST.getHax().crystalAuraHack.setEnabled(false);
        AimAssistHack.WURST.getHax().fightBotHack.setEnabled(false);
        AimAssistHack.WURST.getHax().killauraHack.setEnabled(false);
        AimAssistHack.WURST.getHax().killauraLegitHack.setEnabled(false);
        AimAssistHack.WURST.getHax().multiAuraHack.setEnabled(false);
        AimAssistHack.WURST.getHax().protectHack.setEnabled(false);
        AimAssistHack.WURST.getHax().tpAuraHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(MouseUpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(MouseUpdateListener.class, this);
        this.target = null;
    }

    @Override
    public void onUpdate() {
        this.target = null;
        if (AimAssistHack.MC.field_1755 instanceof class_465) {
            return;
        }
        if (!this.aimWhileBlocking.isChecked() && AimAssistHack.MC.field_1724.method_6115()) {
            return;
        }
        this.chooseTarget();
        if (this.target == null) {
            return;
        }
        class_243 hitVec = this.aimAt.getAimPoint(this.target);
        if (this.checkLOS.isChecked() && !BlockUtils.hasLineOfSight(hitVec)) {
            this.target = null;
            return;
        }
        AimAssistHack.WURST.getHax().autoSwordHack.setSlot(this.target);
        Rotation needed = RotationUtils.getNeededRotations(hitVec);
        Rotation next = RotationUtils.slowlyTurnTowards(needed, (float)this.rotationSpeed.getValueI() / 20.0f);
        this.nextYaw = next.yaw();
        this.nextPitch = next.pitch();
    }

    private void chooseTarget() {
        Stream<class_1297> stream = EntityUtils.getAttackableEntities();
        double rangeSq = this.range.getValueSq();
        stream = stream.filter(e -> EntityUtils.distanceToHitboxSq(e) <= rangeSq);
        if (this.fov.getValue() < 360.0) {
            stream = stream.filter(e -> RotationUtils.getAngleToLookVec(this.aimAt.getAimPoint((class_1297)e)) <= this.fov.getValue() / 2.0);
        }
        stream = this.entityFilters.applyTo(stream);
        this.target = stream.min(Comparator.comparingDouble(e -> RotationUtils.getAngleToLookVec(this.aimAt.getAimPoint((class_1297)e)))).orElse(null);
    }

    @Override
    public void onMouseUpdate(MouseUpdateListener.MouseUpdateEvent event) {
        if (this.target == null || AimAssistHack.MC.field_1724 == null) {
            return;
        }
        float curYaw = AimAssistHack.MC.field_1724.method_36454();
        float curPitch = AimAssistHack.MC.field_1724.method_36455();
        int diffYaw = (int)(this.nextYaw - curYaw);
        int diffPitch = (int)(this.nextPitch - curPitch);
        if (diffYaw == 0 && diffPitch == 0 && !RotationUtils.isFacingBox(this.target.method_5829(), this.range.getValue())) {
            diffYaw = this.nextYaw < curYaw ? -1 : 1;
            diffPitch = this.nextPitch < curPitch ? -1 : 1;
        }
        double inputFactor = 1.0 - this.ignoreMouseInput.getValue();
        int mouseInputX = (int)(event.getDefaultDeltaX() * inputFactor);
        int mouseInputY = (int)(event.getDefaultDeltaY() * inputFactor);
        event.setDeltaX(mouseInputX + diffYaw);
        event.setDeltaY(mouseInputY + diffPitch);
    }
}
