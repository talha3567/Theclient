package net.wurstclient.hacks;

import java.util.Comparator;
import java.util.Random;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.AttackSpeedSliderSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.PauseAttackOnContainersSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags(value={"TpAura", "tp aura", "EnderAura", "Ender-Aura", "ender aura"})
public final class TpAuraHack
extends Hack
implements UpdateListener {
    private final Random random = new Random();
    private final SliderSetting range = new SliderSetting("Range", 4.25, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final AttackSpeedSliderSetting speed = new AttackSpeedSliderSetting();
    private final EnumSetting<Priority> priority = new EnumSetting("Priority", "Determines which entity will be attacked first.\n\u00a7lDistance\u00a7r - Attacks the closest entity.\n\u00a7lAngle\u00a7r - Attacks the entity that requires the least head movement.\n\u00a7lHealth\u00a7r - Attacks the weakest entity.", (Enum[])Priority.values(), (Enum)Priority.ANGLE);
    private final SwingHandSetting swingHand = new SwingHandSetting(SwingHandSetting.genericCombatDescription(this), SwingHandSetting.SwingHand.CLIENT);
    private final PauseAttackOnContainersSetting pauseOnContainers = new PauseAttackOnContainersSetting(true);
    private final EntityFilterList entityFilters = EntityFilterList.genericCombat();

    public TpAuraHack() {
        super("TP-Aura");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.range);
        this.addSetting(this.speed);
        this.addSetting(this.priority);
        this.addSetting(this.swingHand);
        this.addSetting(this.pauseOnContainers);
        this.entityFilters.forEach(x$0 -> this.addSetting((Setting)x$0));
    }

    @Override
    protected void onEnable() {
        TpAuraHack.WURST.getHax().aimAssistHack.setEnabled(false);
        TpAuraHack.WURST.getHax().clickAuraHack.setEnabled(false);
        TpAuraHack.WURST.getHax().crystalAuraHack.setEnabled(false);
        TpAuraHack.WURST.getHax().fightBotHack.setEnabled(false);
        TpAuraHack.WURST.getHax().killauraLegitHack.setEnabled(false);
        TpAuraHack.WURST.getHax().killauraHack.setEnabled(false);
        TpAuraHack.WURST.getHax().multiAuraHack.setEnabled(false);
        TpAuraHack.WURST.getHax().protectHack.setEnabled(false);
        TpAuraHack.WURST.getHax().triggerBotHack.setEnabled(false);
        this.speed.resetTimer();
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
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
        class_746 player = TpAuraHack.MC.field_1724;
        Stream<class_1297> stream = EntityUtils.getAttackableEntities();
        double rangeSq = Math.pow(this.range.getValue(), 2.0);
        stream = stream.filter(e -> EntityUtils.distanceToHitboxSq(e) <= rangeSq);
        class_1297 entity = (stream = this.entityFilters.applyTo(stream)).min(this.priority.getSelected().comparator).orElse(null);
        if (entity == null) {
            return;
        }
        TpAuraHack.WURST.getHax().autoSwordHack.setSlot(entity);
        player.method_5814(entity.method_23317() + (double)(this.random.nextInt(3) * 2) - 2.0, entity.method_23318(), entity.method_23321() + (double)(this.random.nextInt(3) * 2) - 2.0);
        if (player.method_7261(0.0f) < 1.0f) {
            return;
        }
        RotationUtils.getNeededRotations(entity.method_5829().method_1005()).sendPlayerLookPacket();
        TpAuraHack.MC.field_1761.method_2918((class_1657)player, entity);
        this.swingHand.swing(class_1268.field_5808);
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
