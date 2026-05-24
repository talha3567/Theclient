package net.wurstclient.hacks;

import java.util.Comparator;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_465;
import net.wurstclient.Category;
import net.wurstclient.events.HandleInputListener;
import net.wurstclient.events.MouseUpdateListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.AttackSpeedSliderSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.settings.filters.AttackDetectingEntityFilter;
import net.wurstclient.settings.filters.FilterAllaysSetting;
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
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.Rotation;
import net.wurstclient.util.RotationUtils;

public final class KillauraLegitHack
extends Hack
implements UpdateListener,
HandleInputListener,
MouseUpdateListener,
RenderListener {
    private final SliderSetting range = new SliderSetting("Range", 4.25, 1.0, 4.25, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final AttackSpeedSliderSetting speed = new AttackSpeedSliderSetting();
    private final SliderSetting speedRandMS = new SliderSetting("Speed randomization", "Helps you bypass anti-cheat plugins by varying the delay between attacks.\n\n\u00b1100ms is recommended for Vulcan.\n\n0 (off) is fine for NoCheat+, AAC, Grim, Verus, Spartan, and vanilla servers.", 100.0, 0.0, 1000.0, 50.0, SliderSetting.ValueDisplay.INTEGER.withPrefix("\u00b1").withSuffix("ms").withLabel(0.0, "off"));
    private final SliderSetting rotationSpeed = new SliderSetting("Rotation Speed", 600.0, 10.0, 3600.0, 10.0, SliderSetting.ValueDisplay.DEGREES.withSuffix("/s"));
    private final EnumSetting<Priority> priority = new EnumSetting("Priority", "Determines which entity will be attacked first.\n\u00a7lDistance\u00a7r - Attacks the closest entity.\n\u00a7lAngle\u00a7r - Attacks the entity that requires the least head movement.\n\u00a7lHealth\u00a7r - Attacks the weakest entity.", (Enum[])Priority.values(), (Enum)Priority.ANGLE);
    private final SliderSetting fov = new SliderSetting("FOV", "Field Of View - how far away from your crosshair an entity can be before it's ignored.\n360\u00b0 = entities can be attacked all around you.", 360.0, 30.0, 360.0, 10.0, SliderSetting.ValueDisplay.DEGREES);
    private final SwingHandSetting swingHand = SwingHandSetting.withoutOffOption(SwingHandSetting.genericCombatDescription(this), SwingHandSetting.SwingHand.CLIENT);
    private final CheckboxSetting damageIndicator = new CheckboxSetting("Damage indicator", "Renders a colored box within the target, inversely proportional to its remaining health.", true);
    private final EntityFilterList entityFilters = new EntityFilterList(FilterPlayersSetting.genericCombat(false), FilterSleepingSetting.genericCombat(true), FilterFlyingSetting.genericCombat(0.5), FilterHostileSetting.genericCombat(false), FilterNeutralSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF), FilterPassiveSetting.genericCombat(false), FilterPassiveWaterSetting.genericCombat(false), FilterBabiesSetting.genericCombat(false), FilterBatsSetting.genericCombat(false), FilterSlimesSetting.genericCombat(false), FilterPetsSetting.genericCombat(false), FilterVillagersSetting.genericCombat(false), FilterZombieVillagersSetting.genericCombat(false), FilterGolemsSetting.genericCombat(false), FilterPiglinsSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF), FilterZombiePiglinsSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF), FilterEndermenSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF), FilterShulkersSetting.genericCombat(false), FilterAllaysSetting.genericCombat(false), FilterInvisibleSetting.genericCombat(true), FilterNamedSetting.genericCombat(false), FilterShulkerBulletSetting.genericCombat(false), FilterArmorStandsSetting.genericCombat(false), FilterCrystalsSetting.genericCombat(false));
    private class_1297 target;
    private float nextYaw;
    private float nextPitch;

    public KillauraLegitHack() {
        super("KillauraLegit");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.range);
        this.addSetting(this.speed);
        this.addSetting(this.speedRandMS);
        this.addSetting(this.rotationSpeed);
        this.addSetting(this.priority);
        this.addSetting(this.fov);
        this.addSetting(this.swingHand);
        this.addSetting(this.damageIndicator);
        this.entityFilters.forEach(x$0 -> this.addSetting((Setting)x$0));
    }

    @Override
    protected void onEnable() {
        KillauraLegitHack.WURST.getHax().aimAssistHack.setEnabled(false);
        KillauraLegitHack.WURST.getHax().clickAuraHack.setEnabled(false);
        KillauraLegitHack.WURST.getHax().crystalAuraHack.setEnabled(false);
        KillauraLegitHack.WURST.getHax().fightBotHack.setEnabled(false);
        KillauraLegitHack.WURST.getHax().killauraHack.setEnabled(false);
        KillauraLegitHack.WURST.getHax().multiAuraHack.setEnabled(false);
        KillauraLegitHack.WURST.getHax().protectHack.setEnabled(false);
        KillauraLegitHack.WURST.getHax().triggerBotHack.setEnabled(false);
        KillauraLegitHack.WURST.getHax().tpAuraHack.setEnabled(false);
        this.speed.resetTimer(this.speedRandMS.getValue());
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(HandleInputListener.class, this);
        EVENTS.add(MouseUpdateListener.class, this);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(HandleInputListener.class, this);
        EVENTS.remove(MouseUpdateListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        this.target = null;
    }

    @Override
    public void onUpdate() {
        this.target = null;
        if (KillauraLegitHack.MC.field_1755 instanceof class_465) {
            return;
        }
        Stream<class_1297> stream = EntityUtils.getAttackableEntities();
        double rangeSq = this.range.getValueSq();
        stream = stream.filter(e -> EntityUtils.distanceToHitboxSq(e) <= rangeSq);
        if (this.fov.getValue() < 360.0) {
            stream = stream.filter(e -> RotationUtils.getAngleToLookVec(e.method_5829().method_1005()) <= this.fov.getValue() / 2.0);
        }
        stream = this.entityFilters.applyTo(stream);
        this.target = stream.min(this.priority.getSelected().comparator).orElse(null);
        if (this.target == null) {
            return;
        }
        if (!BlockUtils.hasLineOfSight(this.target.method_5829().method_1005())) {
            this.target = null;
            return;
        }
        KillauraLegitHack.WURST.getHax().autoSwordHack.setSlot(this.target);
        this.faceEntityClient(this.target);
    }

    @Override
    public void onHandleInput() {
        if (this.target == null) {
            return;
        }
        this.speed.updateTimer();
        if (!this.speed.isTimeToAttack()) {
            return;
        }
        if (!RotationUtils.isFacingBox(this.target.method_5829(), this.range.getValue())) {
            return;
        }
        KillauraLegitHack.MC.field_1761.method_2918((class_1657)KillauraLegitHack.MC.field_1724, this.target);
        this.swingHand.swing(class_1268.field_5808);
        this.speed.resetTimer(this.speedRandMS.getValue());
    }

    private boolean faceEntityClient(class_1297 entity) {
        class_238 box = entity.method_5829();
        Rotation needed = RotationUtils.getNeededRotations(box.method_1005());
        Rotation next = RotationUtils.slowlyTurnTowards(needed, (float)this.rotationSpeed.getValueI() / 20.0f);
        this.nextYaw = next.yaw();
        this.nextPitch = next.pitch();
        if (RotationUtils.isAlreadyFacing(needed)) {
            return true;
        }
        return RotationUtils.isFacingBox(box, this.range.getValue());
    }

    @Override
    public void onMouseUpdate(MouseUpdateListener.MouseUpdateEvent event) {
        if (this.target == null || KillauraLegitHack.MC.field_1724 == null) {
            return;
        }
        int diffYaw = (int)(this.nextYaw - KillauraLegitHack.MC.field_1724.method_36454());
        int diffPitch = (int)(this.nextPitch - KillauraLegitHack.MC.field_1724.method_36455());
        if (class_3532.method_15382((int)diffYaw) < 1 && class_3532.method_15382((int)diffPitch) < 1) {
            return;
        }
        event.setDeltaX(event.getDefaultDeltaX() + (double)diffYaw);
        event.setDeltaY(event.getDefaultDeltaY() + (double)diffPitch);
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        if (this.target == null || !this.damageIndicator.isChecked()) {
            return;
        }
        float p = 1.0f;
        class_1297 class_12972 = this.target;
        if (class_12972 instanceof class_1309) {
            class_1309 le = (class_1309)class_12972;
            p = (le.method_6063() - le.method_6032()) / le.method_6063();
        }
        float red = p * 2.0f;
        float green = 2.0f - red;
        float[] rgb = new float[]{red, green, 0.0f};
        int quadColor = RenderUtils.toIntColor(rgb, 0.25f);
        int lineColor = RenderUtils.toIntColor(rgb, 0.5f);
        class_238 box = EntityUtils.getLerpedBox(this.target, partialTicks);
        if (p < 1.0f) {
            box = box.method_35580((double)(1.0f - p) * 0.5 * box.method_17939(), (double)(1.0f - p) * 0.5 * box.method_17940(), (double)(1.0f - p) * 0.5 * box.method_17941());
        }
        RenderUtils.drawSolidBox(matrixStack, box, quadColor, false);
        RenderUtils.drawOutlinedBox(matrixStack, box, lineColor, false);
    }

    private static enum Priority {
        DISTANCE("Distance", EntityUtils::distanceToHitboxSq),
        ANGLE("Angle", e -> RotationUtils.getAngleToLookVec(e.method_5829().method_1005())),
        HEALTH("Health", e -> e instanceof class_1309 ? (double)((class_1309)e).method_6032() : 2.147483647E9);

        private final String name;
        private final Comparator<class_1297> comparator;

        private Priority(String name, ToDoubleFunction<class_1297> keyExtractor) {
            this.name = name;
            this.comparator = Comparator.comparingDouble(keyExtractor);
        }

        public String toString() {
            return this.name;
        }
    }
}
