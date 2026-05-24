package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.AttackSpeedSliderSetting;
import net.wurstclient.settings.PauseAttackOnContainersSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags(value={"multi aura", "ForceField", "force field"})
public final class MultiAuraHack
extends Hack
implements UpdateListener {
    private final SliderSetting range = new SliderSetting("Range", 5.0, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final AttackSpeedSliderSetting speed = new AttackSpeedSliderSetting();
    private final SliderSetting fov = new SliderSetting("FOV", 360.0, 30.0, 360.0, 10.0, SliderSetting.ValueDisplay.DEGREES);
    private final SwingHandSetting swingHand = new SwingHandSetting(SwingHandSetting.genericCombatDescription(this), SwingHandSetting.SwingHand.CLIENT);
    private final PauseAttackOnContainersSetting pauseOnContainers = new PauseAttackOnContainersSetting(false);
    private final EntityFilterList entityFilters = EntityFilterList.genericCombat();

    public MultiAuraHack() {
        super("MultiAura");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.range);
        this.addSetting(this.speed);
        this.addSetting(this.fov);
        this.addSetting(this.swingHand);
        this.addSetting(this.pauseOnContainers);
        this.entityFilters.forEach(x$0 -> this.addSetting((Setting)x$0));
    }

    @Override
    protected void onEnable() {
        MultiAuraHack.WURST.getHax().aimAssistHack.setEnabled(false);
        MultiAuraHack.WURST.getHax().clickAuraHack.setEnabled(false);
        MultiAuraHack.WURST.getHax().crystalAuraHack.setEnabled(false);
        MultiAuraHack.WURST.getHax().fightBotHack.setEnabled(false);
        MultiAuraHack.WURST.getHax().killauraLegitHack.setEnabled(false);
        MultiAuraHack.WURST.getHax().killauraHack.setEnabled(false);
        MultiAuraHack.WURST.getHax().protectHack.setEnabled(false);
        MultiAuraHack.WURST.getHax().tpAuraHack.setEnabled(false);
        MultiAuraHack.WURST.getHax().triggerBotHack.setEnabled(false);
        this.speed.resetTimer();
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        ArrayList entities;
        this.speed.updateTimer();
        if (!this.speed.isTimeToAttack()) {
            return;
        }
        if (this.pauseOnContainers.shouldPause()) {
            return;
        }
        Stream<class_1297> stream = EntityUtils.getAttackableEntities();
        double rangeSq = Math.pow(this.range.getValue(), 2.0);
        stream = stream.filter(e -> EntityUtils.distanceToHitboxSq(e) <= rangeSq);
        if (this.fov.getValue() < 360.0) {
            stream = stream.filter(e -> RotationUtils.getAngleToLookVec(e.method_5829().method_1005()) <= this.fov.getValue() / 2.0);
        }
        if ((entities = (stream = this.entityFilters.applyTo(stream)).collect(Collectors.toCollection(ArrayList::new))).isEmpty()) {
            return;
        }
        MultiAuraHack.WURST.getHax().autoSwordHack.setSlot((class_1297)entities.get(0));
        for (class_1297 entity : entities) {
            RotationUtils.getNeededRotations(entity.method_5829().method_1005()).sendPlayerLookPacket();
            MultiAuraHack.MC.field_1761.method_2918((class_1657)MultiAuraHack.MC.field_1724, entity);
        }
        this.swingHand.swing(class_1268.field_5808);
        this.speed.resetTimer();
    }
}
