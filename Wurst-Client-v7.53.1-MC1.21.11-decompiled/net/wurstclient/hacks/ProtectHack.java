package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.ai.PathFinder;
import net.wurstclient.ai.PathPos;
import net.wurstclient.ai.PathProcessor;
import net.wurstclient.commands.PathCmd;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.AttackSpeedSliderSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.PauseAttackOnContainersSetting;
import net.wurstclient.settings.Setting;
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
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.FakePlayerEntity;

@DontSaveState
public final class ProtectHack
extends Hack
implements UpdateListener,
RenderListener {
    private final AttackSpeedSliderSetting speed = new AttackSpeedSliderSetting();
    private final SwingHandSetting swingHand = new SwingHandSetting(SwingHandSetting.genericCombatDescription(this), SwingHandSetting.SwingHand.CLIENT);
    private final CheckboxSetting useAi = new CheckboxSetting("Use AI (experimental)", false);
    private final PauseAttackOnContainersSetting pauseOnContainers = new PauseAttackOnContainersSetting(true);
    private final EntityFilterList entityFilters = new EntityFilterList(FilterPlayersSetting.genericCombat(false), FilterSleepingSetting.genericCombat(false), FilterFlyingSetting.genericCombat(0.0), FilterHostileSetting.genericCombat(false), FilterNeutralSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF), FilterPassiveSetting.genericCombat(false), FilterPassiveWaterSetting.genericCombat(false), FilterBabiesSetting.genericCombat(false), FilterBatsSetting.genericCombat(false), FilterSlimesSetting.genericCombat(false), FilterPetsSetting.genericCombat(false), FilterVillagersSetting.genericCombat(false), FilterZombieVillagersSetting.genericCombat(false), FilterGolemsSetting.genericCombat(false), FilterPiglinsSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF), FilterZombiePiglinsSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF), FilterEndermenSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF), FilterShulkersSetting.genericCombat(false), FilterAllaysSetting.genericCombat(false), FilterInvisibleSetting.genericCombat(false), FilterNamedSetting.genericCombat(false), FilterShulkerBulletSetting.genericCombat(false), FilterArmorStandsSetting.genericCombat(false), FilterCrystalsSetting.genericCombat(true));
    private EntityPathFinder pathFinder;
    private PathProcessor processor;
    private int ticksProcessing;
    private class_1297 friend;
    private class_1297 enemy;
    private double distanceF = 2.0;
    private double distanceE = 3.0;

    public ProtectHack() {
        super("Protect");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.speed);
        this.addSetting(this.swingHand);
        this.addSetting(this.useAi);
        this.addSetting(this.pauseOnContainers);
        this.entityFilters.forEach(x$0 -> this.addSetting((Setting)x$0));
    }

    @Override
    public String getRenderName() {
        if (this.friend != null) {
            return "Protecting " + this.friend.method_5477().getString();
        }
        return "Protect";
    }

    @Override
    protected void onEnable() {
        ProtectHack.WURST.getHax().followHack.setEnabled(false);
        ProtectHack.WURST.getHax().tunnellerHack.setEnabled(false);
        ProtectHack.WURST.getHax().aimAssistHack.setEnabled(false);
        ProtectHack.WURST.getHax().clickAuraHack.setEnabled(false);
        ProtectHack.WURST.getHax().crystalAuraHack.setEnabled(false);
        ProtectHack.WURST.getHax().fightBotHack.setEnabled(false);
        ProtectHack.WURST.getHax().killauraLegitHack.setEnabled(false);
        ProtectHack.WURST.getHax().killauraHack.setEnabled(false);
        ProtectHack.WURST.getHax().multiAuraHack.setEnabled(false);
        ProtectHack.WURST.getHax().triggerBotHack.setEnabled(false);
        ProtectHack.WURST.getHax().tpAuraHack.setEnabled(false);
        if (this.friend == null) {
            Stream<class_1297> stream = StreamSupport.stream(ProtectHack.MC.field_1687.method_18112().spliterator(), true).filter(class_1309.class::isInstance).filter(e -> !e.method_31481() && ((class_1309)e).method_6032() > 0.0f).filter(e -> e != ProtectHack.MC.field_1724).filter(e -> !(e instanceof FakePlayerEntity));
            this.friend = stream.min(Comparator.comparingDouble(EntityUtils::distanceToHitboxSq)).orElse(null);
        }
        this.pathFinder = new EntityPathFinder(this, this.friend, this.distanceF);
        this.speed.resetTimer();
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        this.pathFinder = null;
        this.processor = null;
        this.ticksProcessing = 0;
        PathProcessor.releaseControls();
        this.enemy = null;
        if (this.friend != null) {
            ProtectHack.MC.field_1690.field_1894.method_23481(false);
            this.friend = null;
        }
    }

    @Override
    public void onUpdate() {
        double distance;
        this.speed.updateTimer();
        if (this.pauseOnContainers.shouldPause()) {
            return;
        }
        if (this.friend == null || this.friend.method_31481() || !(this.friend instanceof class_1309) || ((class_1309)this.friend).method_6032() <= 0.0f || ProtectHack.MC.field_1724.method_6032() <= 0.0f) {
            this.friend = null;
            this.enemy = null;
            this.setEnabled(false);
            return;
        }
        Stream<class_1297> stream = EntityUtils.getAttackableEntities().filter(e -> EntityUtils.distanceToHitboxSq(e) <= 36.0).filter(e -> e != this.friend);
        stream = this.entityFilters.applyTo(stream);
        this.enemy = stream.min(Comparator.comparingDouble(EntityUtils::distanceToHitboxSq)).orElse(null);
        class_1297 target = this.enemy == null || EntityUtils.distanceToHitboxSq(this.friend) >= 576.0 ? this.friend : this.enemy;
        double d = distance = target == this.enemy ? this.distanceE : this.distanceF;
        if (this.useAi.isChecked()) {
            if ((this.processor == null || this.processor.isDone() || this.ticksProcessing >= 10 || !this.pathFinder.isPathStillValid(this.processor.getIndex())) && (this.pathFinder.isDone() || this.pathFinder.isFailed())) {
                this.pathFinder = new EntityPathFinder(this, target, distance);
                this.processor = null;
                this.ticksProcessing = 0;
            }
            if (!this.pathFinder.isDone() && !this.pathFinder.isFailed()) {
                PathProcessor.lockControls();
                WURST.getRotationFaker().faceVectorClient(target.method_5829().method_1005());
                this.pathFinder.think();
                this.pathFinder.formatPath();
                this.processor = this.pathFinder.getProcessor();
            }
            if (!this.processor.isDone()) {
                this.processor.process();
                ++this.ticksProcessing;
            }
        } else {
            if (ProtectHack.MC.field_1724.field_5976 && ProtectHack.MC.field_1724.method_24828()) {
                ProtectHack.MC.field_1724.method_6043();
            }
            if (ProtectHack.MC.field_1724.method_5799() && ProtectHack.MC.field_1724.method_23318() < target.method_23318()) {
                ProtectHack.MC.field_1724.method_5762(0.0, 0.04, 0.0);
            }
            if (!ProtectHack.MC.field_1724.method_24828() && (ProtectHack.MC.field_1724.method_31549().field_7479 || ProtectHack.WURST.getHax().flightHack.isEnabled()) && ProtectHack.MC.field_1724.method_5649(target.method_23317(), ProtectHack.MC.field_1724.method_23318(), target.method_23321()) <= ProtectHack.MC.field_1724.method_5649(ProtectHack.MC.field_1724.method_23317(), target.method_23318(), ProtectHack.MC.field_1724.method_23321())) {
                if (ProtectHack.MC.field_1724.method_23318() > target.method_23318() + 1.0) {
                    ProtectHack.MC.field_1690.field_1832.method_23481(true);
                } else if (ProtectHack.MC.field_1724.method_23318() < target.method_23318() - 1.0) {
                    ProtectHack.MC.field_1690.field_1903.method_23481(true);
                }
            } else {
                ProtectHack.MC.field_1690.field_1832.method_23481(false);
                ProtectHack.MC.field_1690.field_1903.method_23481(false);
            }
            WURST.getRotationFaker().faceVectorClient(target.method_5829().method_1005());
            ProtectHack.MC.field_1690.field_1894.method_23481((double)ProtectHack.MC.field_1724.method_5739(target) > (target == this.friend ? this.distanceF : this.distanceE));
        }
        if (target == this.enemy) {
            ProtectHack.WURST.getHax().autoSwordHack.setSlot(this.enemy);
            if (!this.speed.isTimeToAttack()) {
                return;
            }
            ProtectHack.MC.field_1761.method_2918((class_1657)ProtectHack.MC.field_1724, this.enemy);
            this.swingHand.swing(class_1268.field_5808);
            this.speed.resetTimer();
        }
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        if (!this.useAi.isChecked()) {
            return;
        }
        PathCmd pathCmd = ProtectHack.WURST.getCmds().pathCmd;
        this.pathFinder.renderPath(matrixStack, pathCmd.isDebugMode(), pathCmd.isDepthTest());
    }

    public void setFriend(class_1297 friend) {
        this.friend = friend;
    }

    private class EntityPathFinder
    extends PathFinder {
        private final class_1297 entity;
        private double distanceSq;

        public EntityPathFinder(ProtectHack protectHack, class_1297 entity, double distance) {
            super(class_2338.method_49638((class_2374)entity.method_73189()));
            this.entity = entity;
            this.distanceSq = distance * distance;
            this.setThinkTime(1);
        }

        @Override
        protected boolean checkDone() {
            this.done = this.entity.method_5707(class_243.method_24953((class_2382)this.current)) <= this.distanceSq;
            return this.done;
        }

        @Override
        public ArrayList<PathPos> formatPath() {
            if (!this.done) {
                this.failed = true;
            }
            return super.formatPath();
        }
    }
}
