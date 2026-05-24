package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
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
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.util.EntityUtils;

@SearchTags(value={"fight bot"})
@DontSaveState
public final class FightBotHack
extends Hack
implements UpdateListener,
RenderListener {
    private final SliderSetting range = new SliderSetting("Range", "Attack range (like Killaura)", 4.25, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final AttackSpeedSliderSetting speed = new AttackSpeedSliderSetting();
    private final SwingHandSetting swingHand = new SwingHandSetting(SwingHandSetting.genericCombatDescription(this), SwingHandSetting.SwingHand.CLIENT);
    private final SliderSetting distance = new SliderSetting("Distance", "How closely to follow the target.\nThis should be set to a lower value than Range.", 3.0, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final CheckboxSetting useAi = new CheckboxSetting("Use AI (experimental)", false);
    private final PauseAttackOnContainersSetting pauseOnContainers = new PauseAttackOnContainersSetting(true);
    private final EntityFilterList entityFilters = EntityFilterList.genericCombat();
    private EntityPathFinder pathFinder;
    private PathProcessor processor;
    private int ticksProcessing;

    public FightBotHack() {
        super("FightBot");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.range);
        this.addSetting(this.speed);
        this.addSetting(this.swingHand);
        this.addSetting(this.distance);
        this.addSetting(this.useAi);
        this.addSetting(this.pauseOnContainers);
        this.entityFilters.forEach(x$0 -> this.addSetting((Setting)x$0));
    }

    @Override
    protected void onEnable() {
        FightBotHack.WURST.getHax().aimAssistHack.setEnabled(false);
        FightBotHack.WURST.getHax().clickAuraHack.setEnabled(false);
        FightBotHack.WURST.getHax().crystalAuraHack.setEnabled(false);
        FightBotHack.WURST.getHax().killauraLegitHack.setEnabled(false);
        FightBotHack.WURST.getHax().killauraHack.setEnabled(false);
        FightBotHack.WURST.getHax().multiAuraHack.setEnabled(false);
        FightBotHack.WURST.getHax().protectHack.setEnabled(false);
        FightBotHack.WURST.getHax().triggerBotHack.setEnabled(false);
        FightBotHack.WURST.getHax().tpAuraHack.setEnabled(false);
        FightBotHack.WURST.getHax().tunnellerHack.setEnabled(false);
        this.pathFinder = new EntityPathFinder((class_1297)FightBotHack.MC.field_1724);
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
    }

    @Override
    public void onUpdate() {
        this.speed.updateTimer();
        if (this.pauseOnContainers.shouldPause()) {
            return;
        }
        Stream<class_1297> stream = EntityUtils.getAttackableEntities();
        class_1297 entity = (stream = this.entityFilters.applyTo(stream)).min(Comparator.comparingDouble(EntityUtils::distanceToHitboxSq)).orElse(null);
        if (entity == null) {
            return;
        }
        FightBotHack.WURST.getHax().autoSwordHack.setSlot(entity);
        if (this.useAi.isChecked()) {
            if ((this.processor == null || this.processor.isDone() || this.ticksProcessing >= 10 || !this.pathFinder.isPathStillValid(this.processor.getIndex())) && (this.pathFinder.isDone() || this.pathFinder.isFailed())) {
                this.pathFinder = new EntityPathFinder(entity);
                this.processor = null;
                this.ticksProcessing = 0;
            }
            if (!this.pathFinder.isDone() && !this.pathFinder.isFailed()) {
                PathProcessor.lockControls();
                WURST.getRotationFaker().faceVectorClient(entity.method_5829().method_1005());
                this.pathFinder.think();
                this.pathFinder.formatPath();
                this.processor = this.pathFinder.getProcessor();
            }
            if (!this.processor.isDone()) {
                this.processor.process();
                ++this.ticksProcessing;
            }
        } else {
            if (FightBotHack.MC.field_1724.field_5976 && FightBotHack.MC.field_1724.method_24828()) {
                FightBotHack.MC.field_1724.method_6043();
            }
            if (FightBotHack.MC.field_1724.method_5799() && FightBotHack.MC.field_1724.method_23318() < entity.method_23318()) {
                FightBotHack.MC.field_1724.method_5762(0.0, 0.04, 0.0);
            }
            if (!FightBotHack.MC.field_1724.method_24828() && (FightBotHack.MC.field_1724.method_31549().field_7479 || FightBotHack.WURST.getHax().flightHack.isEnabled()) && FightBotHack.MC.field_1724.method_5649(entity.method_23317(), FightBotHack.MC.field_1724.method_23318(), entity.method_23321()) <= FightBotHack.MC.field_1724.method_5649(FightBotHack.MC.field_1724.method_23317(), entity.method_23318(), FightBotHack.MC.field_1724.method_23321())) {
                if (FightBotHack.MC.field_1724.method_23318() > entity.method_23318() + 1.0) {
                    FightBotHack.MC.field_1690.field_1832.method_23481(true);
                } else if (FightBotHack.MC.field_1724.method_23318() < entity.method_23318() - 1.0) {
                    FightBotHack.MC.field_1690.field_1903.method_23481(true);
                }
            } else {
                FightBotHack.MC.field_1690.field_1832.method_23481(false);
                FightBotHack.MC.field_1690.field_1903.method_23481(false);
            }
            FightBotHack.MC.field_1690.field_1894.method_23481(FightBotHack.MC.field_1724.method_5739(entity) > this.distance.getValueF());
            WURST.getRotationFaker().faceVectorClient(entity.method_5829().method_1005());
        }
        if (!this.speed.isTimeToAttack()) {
            return;
        }
        if (EntityUtils.distanceToHitboxSq(entity) > this.range.getValueSq()) {
            return;
        }
        FightBotHack.MC.field_1761.method_2918((class_1657)FightBotHack.MC.field_1724, entity);
        this.swingHand.swing(class_1268.field_5808);
        this.speed.resetTimer();
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        PathCmd pathCmd = FightBotHack.WURST.getCmds().pathCmd;
        this.pathFinder.renderPath(matrixStack, pathCmd.isDebugMode(), pathCmd.isDepthTest());
    }

    private class EntityPathFinder
    extends PathFinder {
        private final class_1297 entity;

        public EntityPathFinder(class_1297 entity) {
            super(class_2338.method_49638((class_2374)entity.method_73189()));
            this.entity = entity;
            this.setThinkTime(1);
        }

        @Override
        protected boolean checkDone() {
            this.done = this.entity.method_5707(class_243.method_24953((class_2382)this.current)) <= Math.pow(FightBotHack.this.distance.getValue(), 2.0);
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
