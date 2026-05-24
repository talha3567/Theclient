package net.wurstclient.hacks;

import java.util.Comparator;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.LeftClickListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.AttackSpeedSliderSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags(value={"click aura", "ClickAimbot", "click aimbot"})
public final class ClickAuraHack
extends Hack
implements UpdateListener,
LeftClickListener {
    private final SliderSetting range = new SliderSetting("Range", 5.0, 1.0, 10.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final AttackSpeedSliderSetting speed = new AttackSpeedSliderSetting();
    private final EnumSetting<Priority> priority = new EnumSetting("Priority", "Determines which entity will be attacked first.\n\u00a7lDistance\u00a7r - Attacks the closest entity.\n\u00a7lAngle\u00a7r - Attacks the entity that requires the least head movement.\n\u00a7lHealth\u00a7r - Attacks the weakest entity.", (Enum[])Priority.values(), (Enum)Priority.ANGLE);
    private final SliderSetting fov = new SliderSetting("FOV", 360.0, 30.0, 360.0, 10.0, SliderSetting.ValueDisplay.DEGREES);
    private final EntityFilterList entityFilters = EntityFilterList.genericCombat();

    public ClickAuraHack() {
        super("ClickAura");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.range);
        this.addSetting(this.speed);
        this.addSetting(this.priority);
        this.addSetting(this.fov);
        this.entityFilters.forEach(x$0 -> this.addSetting((Setting)x$0));
    }

    @Override
    protected void onEnable() {
        ClickAuraHack.WURST.getHax().aimAssistHack.setEnabled(false);
        ClickAuraHack.WURST.getHax().crystalAuraHack.setEnabled(false);
        ClickAuraHack.WURST.getHax().fightBotHack.setEnabled(false);
        ClickAuraHack.WURST.getHax().killauraLegitHack.setEnabled(false);
        ClickAuraHack.WURST.getHax().killauraHack.setEnabled(false);
        ClickAuraHack.WURST.getHax().multiAuraHack.setEnabled(false);
        ClickAuraHack.WURST.getHax().protectHack.setEnabled(false);
        ClickAuraHack.WURST.getHax().triggerBotHack.setEnabled(false);
        ClickAuraHack.WURST.getHax().tpAuraHack.setEnabled(false);
        this.speed.resetTimer();
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(LeftClickListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(LeftClickListener.class, this);
    }

    @Override
    public void onUpdate() {
        if (!ClickAuraHack.MC.field_1690.field_1886.method_1434()) {
            return;
        }
        this.speed.updateTimer();
        if (!this.speed.isTimeToAttack()) {
            return;
        }
        this.attack();
    }

    @Override
    public void onLeftClick(LeftClickListener.LeftClickEvent event) {
        this.attack();
    }

    private void attack() {
        class_1297 target;
        class_746 player = ClickAuraHack.MC.field_1724;
        Stream<class_1297> stream = EntityUtils.getAttackableEntities();
        double rangeSq = Math.pow(this.range.getValue(), 2.0);
        stream = stream.filter(e -> EntityUtils.distanceToHitboxSq(e) <= rangeSq);
        if (this.fov.getValue() < 360.0) {
            stream = stream.filter(e -> RotationUtils.getAngleToLookVec(e.method_5829().method_1005()) <= this.fov.getValue() / 2.0);
        }
        if ((target = (class_1297)(stream = this.entityFilters.applyTo(stream)).min(this.priority.getSelected().comparator).orElse(null)) == null) {
            return;
        }
        ClickAuraHack.WURST.getHax().autoSwordHack.setSlot(target);
        RotationUtils.getNeededRotations(target.method_5829().method_1005()).sendPlayerLookPacket();
        ClickAuraHack.MC.field_1761.method_2918((class_1657)player, target);
        player.method_6104(class_1268.field_5808);
        this.speed.resetTimer();
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
