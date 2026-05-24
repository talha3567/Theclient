package net.wurstclient.hacks;

import java.util.Comparator;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.HandleInputListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.AttackSpeedSliderSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.PauseAttackOnContainersSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags(value={"kill aura", "ForceField", "force field", "CrystalAura", "crystal aura", "AutoCrystal", "auto crystal"})
public final class KillauraHack
extends Hack
implements UpdateListener,
HandleInputListener,
RenderListener {
    private final SliderSetting range = new SliderSetting("Range", "Determines how far Killaura will reach to attack entities.\nAnything that is further away than the specified value will not be attacked.", 5.0, 1.0, 10.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final AttackSpeedSliderSetting speed = new AttackSpeedSliderSetting();
    private final SliderSetting speedRandMS = new SliderSetting("Speed randomization", "Helps you bypass anti-cheat plugins by varying the delay between attacks.\n\n\u00b1100ms is recommended for Vulcan.\n\n0 (off) is fine for NoCheat+, AAC, Grim, Verus, Spartan, and vanilla servers.", 100.0, 0.0, 1000.0, 50.0, SliderSetting.ValueDisplay.INTEGER.withPrefix("\u00b1").withSuffix("ms").withLabel(0.0, "off"));
    private final EnumSetting<Priority> priority = new EnumSetting("Priority", "Determines which entity will be attacked first.\n\u00a7lDistance\u00a7r - Attacks the closest entity.\n\u00a7lAngle\u00a7r - Attacks the entity that requires the least head movement.\n\u00a7lHealth\u00a7r - Attacks the weakest entity.", (Enum[])Priority.values(), (Enum)Priority.ANGLE);
    private final SliderSetting fov = new SliderSetting("FOV", 360.0, 30.0, 360.0, 10.0, SliderSetting.ValueDisplay.DEGREES);
    private final SwingHandSetting swingHand = new SwingHandSetting(SwingHandSetting.genericCombatDescription(this), SwingHandSetting.SwingHand.CLIENT);
    private final CheckboxSetting damageIndicator = new CheckboxSetting("Damage indicator", "Renders a colored box within the target, inversely proportional to its remaining health.", true);
    private final PauseAttackOnContainersSetting pauseOnContainers = new PauseAttackOnContainersSetting(true);
    private final CheckboxSetting checkLOS = new CheckboxSetting("Check line of sight", "Ensures that you don't reach through blocks when attacking.\n\nSlower but can help with anti-cheat plugins.", false);
    private final EntityFilterList entityFilters = EntityFilterList.genericCombat();
    private class_1297 target;
    private class_1297 renderTarget;

    public KillauraHack() {
        super("Killaura");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.range);
        this.addSetting(this.speed);
        this.addSetting(this.speedRandMS);
        this.addSetting(this.priority);
        this.addSetting(this.fov);
        this.addSetting(this.swingHand);
        this.addSetting(this.damageIndicator);
        this.addSetting(this.pauseOnContainers);
        this.addSetting(this.checkLOS);
        this.entityFilters.forEach(x$0 -> this.addSetting((Setting)x$0));
    }

    @Override
    protected void onEnable() {
        KillauraHack.WURST.getHax().aimAssistHack.setEnabled(false);
        KillauraHack.WURST.getHax().clickAuraHack.setEnabled(false);
        KillauraHack.WURST.getHax().crystalAuraHack.setEnabled(false);
        KillauraHack.WURST.getHax().fightBotHack.setEnabled(false);
        KillauraHack.WURST.getHax().killauraLegitHack.setEnabled(false);
        KillauraHack.WURST.getHax().multiAuraHack.setEnabled(false);
        KillauraHack.WURST.getHax().protectHack.setEnabled(false);
        KillauraHack.WURST.getHax().triggerBotHack.setEnabled(false);
        KillauraHack.WURST.getHax().tpAuraHack.setEnabled(false);
        this.speed.resetTimer(this.speedRandMS.getValue());
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(HandleInputListener.class, this);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(HandleInputListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        this.target = null;
        this.renderTarget = null;
    }

    @Override
    public void onUpdate() {
        this.speed.updateTimer();
        if (!this.speed.isTimeToAttack()) {
            return;
        }
        if (this.pauseOnContainers.shouldPause()) {
            return;
        }
        Stream<class_1297> stream = EntityUtils.getAttackableEntities();
        double rangeSq = this.range.getValueSq();
        stream = stream.filter(e -> EntityUtils.distanceToHitboxSq(e) <= rangeSq);
        if (this.fov.getValue() < 360.0) {
            stream = stream.filter(e -> RotationUtils.getAngleToLookVec(e.method_5829().method_1005()) <= this.fov.getValue() / 2.0);
        }
        stream = this.entityFilters.applyTo(stream);
        this.renderTarget = this.target = (class_1297)stream.min(this.priority.getSelected().comparator).orElse(null);
        if (this.target == null) {
            return;
        }
        KillauraHack.WURST.getHax().autoSwordHack.setSlot(this.target);
        class_243 hitVec = this.target.method_5829().method_1005();
        if (this.checkLOS.isChecked() && !BlockUtils.hasLineOfSight(hitVec)) {
            this.target = null;
            return;
        }
        WURST.getRotationFaker().faceVectorPacket(hitVec);
    }

    @Override
    public void onHandleInput() {
        if (this.target == null) {
            return;
        }
        KillauraHack.MC.field_1761.method_2918((class_1657)KillauraHack.MC.field_1724, this.target);
        this.swingHand.swing(class_1268.field_5808);
        this.target = null;
        this.speed.resetTimer(this.speedRandMS.getValue());
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        class_1309 le;
        if (this.renderTarget == null || !this.damageIndicator.isChecked()) {
            return;
        }
        float p = 1.0f;
        class_1297 class_12972 = this.renderTarget;
        if (class_12972 instanceof class_1309 && (double)(le = (class_1309)class_12972).method_6063() > 1.0E-5) {
            p = 1.0f - le.method_6032() / le.method_6063();
        }
        float red = p * 2.0f;
        float green = 2.0f - red;
        float[] rgb = new float[]{red, green, 0.0f};
        int quadColor = RenderUtils.toIntColor(rgb, 0.25f);
        int lineColor = RenderUtils.toIntColor(rgb, 0.5f);
        class_238 box = EntityUtils.getLerpedBox(this.renderTarget, partialTicks);
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
