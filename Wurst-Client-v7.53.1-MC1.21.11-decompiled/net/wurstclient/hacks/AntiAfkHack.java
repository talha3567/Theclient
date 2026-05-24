package net.wurstclient.hacks;

import java.util.ArrayList;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_5819;
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
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;

@SearchTags(value={"anti afk", "AFKBot", "afk bot"})
@DontSaveState
public final class AntiAfkHack
extends Hack
implements UpdateListener,
RenderListener {
    private final CheckboxSetting useAi = new CheckboxSetting("Use AI", "description.wurst.setting.antiafk.use_ai", true);
    private final SliderSetting aiRange = new SliderSetting("AI range", "description.wurst.setting.antiafk.ai_range", 16.0, 1.0, 64.0, 1.0, SliderSetting.ValueDisplay.AREA_FROM_RADIUS);
    private final SliderSetting nonAiRange = new SliderSetting("Non-AI range", "description.wurst.setting.antiafk.non-ai_range", 1.0, 1.0, 64.0, 1.0, SliderSetting.ValueDisplay.AREA_FROM_RADIUS);
    private final SliderSetting waitTime = new SliderSetting("Wait time", "description.wurst.setting.antiafk.wait_time", 2.5, 0.0, 60.0, 0.05, SliderSetting.ValueDisplay.DECIMAL.withSuffix("s"));
    private final SliderSetting waitTimeRand = new SliderSetting("Wait time randomization", "description.wurst.setting.antiafk.wait_time_randomization", 0.5, 0.0, 60.0, 0.05, SliderSetting.ValueDisplay.DECIMAL.withPrefix("\u00b1").withSuffix("s"));
    private final CheckboxSetting showWaitTime = new CheckboxSetting("Show wait time", "description.wurst.setting.antiafk.show_wait_time", true);
    private int timer;
    private class_5819 random = class_5819.method_43053();
    private class_2338 start;
    private class_2338 nextBlock;
    private RandomPathFinder pathFinder;
    private PathProcessor processor;
    private boolean creativeFlying;

    public AntiAfkHack() {
        super("AntiAFK");
        this.setCategory(Category.OTHER);
        this.addSetting(this.useAi);
        this.addSetting(this.aiRange);
        this.addSetting(this.nonAiRange);
        this.addSetting(this.waitTime);
        this.addSetting(this.waitTimeRand);
        this.addSetting(this.showWaitTime);
    }

    @Override
    public String getRenderName() {
        if (this.showWaitTime.isChecked() && this.timer > 0) {
            return this.getName() + " [" + this.timer * 50 + "ms]";
        }
        return this.getName();
    }

    @Override
    protected void onEnable() {
        this.start = class_2338.method_49638((class_2374)AntiAfkHack.MC.field_1724.method_73189());
        this.nextBlock = null;
        this.pathFinder = new RandomPathFinder(this, this.randomize(this.start, this.aiRange.getValueI(), true));
        this.creativeFlying = AntiAfkHack.MC.field_1724.method_31549().field_7479;
        AntiAfkHack.WURST.getHax().autoFishHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        PathProcessor.releaseControls();
        this.pathFinder = null;
        this.processor = null;
    }

    @Override
    public void onUpdate() {
        if (AntiAfkHack.MC.field_1724.method_6032() <= 0.0f) {
            this.setEnabled(false);
            return;
        }
        AntiAfkHack.MC.field_1724.method_31549().field_7479 = this.creativeFlying;
        if (this.useAi.isChecked()) {
            if (AntiAfkHack.MC.field_1724.method_5869() && !AntiAfkHack.WURST.getHax().jesusHack.isEnabled()) {
                AntiAfkHack.MC.field_1690.field_1903.method_23481(true);
                return;
            }
            if (this.timer > 0) {
                --this.timer;
                return;
            }
            if (!this.pathFinder.isDone() && !this.pathFinder.isFailed()) {
                PathProcessor.lockControls();
                this.pathFinder.think();
                if (!this.pathFinder.isDone() && !this.pathFinder.isFailed()) {
                    return;
                }
                this.pathFinder.formatPath();
                this.processor = this.pathFinder.getProcessor();
            }
            if (this.processor != null && !this.pathFinder.isPathStillValid(this.processor.getIndex()) || this.processor.getTicksOffPath() > 20) {
                this.pathFinder = new RandomPathFinder(this, this.pathFinder);
                return;
            }
            if (!this.processor.isDone()) {
                this.processor.process();
            } else {
                PathProcessor.releaseControls();
                this.pathFinder = new RandomPathFinder(this, this.randomize(this.start, this.aiRange.getValueI(), true));
                this.setTimer();
            }
        } else {
            if (this.timer <= 0 || this.nextBlock == null) {
                this.nextBlock = this.randomize(this.start, this.nonAiRange.getValueI(), false);
                this.setTimer();
            }
            WURST.getRotationFaker().faceVectorClientIgnorePitch(class_243.method_24953((class_2382)this.nextBlock));
            if (AntiAfkHack.MC.field_1724.method_5707(class_243.method_24953((class_2382)this.nextBlock)) > 0.5) {
                AntiAfkHack.MC.field_1690.field_1894.method_23481(true);
            } else {
                AntiAfkHack.MC.field_1690.field_1894.method_23481(false);
            }
            AntiAfkHack.MC.field_1690.field_1903.method_23481(AntiAfkHack.MC.field_1724.method_5799());
            if (this.timer > 0) {
                --this.timer;
            }
        }
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        if (!this.useAi.isChecked()) {
            return;
        }
        PathCmd pathCmd = AntiAfkHack.WURST.getCmds().pathCmd;
        this.pathFinder.renderPath(matrixStack, pathCmd.isDebugMode(), pathCmd.isDepthTest());
    }

    private void setTimer() {
        int baseTime = (int)(this.waitTime.getValue() * 20.0);
        double randTime = this.waitTimeRand.getValue() * 20.0;
        int randOffset = (int)(this.random.method_43059() * randTime);
        randOffset = Math.max(randOffset, -baseTime);
        this.timer = baseTime + randOffset;
    }

    private class_2338 randomize(class_2338 pos, int range, boolean includeY) {
        int x = this.random.method_43048(2 * range + 1) - range;
        int y = includeY ? this.random.method_43048(2 * range + 1) - range : 0;
        int z = this.random.method_43048(2 * range + 1) - range;
        return pos.method_10069(x, y, z);
    }

    private class RandomPathFinder
    extends PathFinder {
        public RandomPathFinder(AntiAfkHack antiAfkHack, class_2338 goal) {
            super(goal);
            this.setThinkTime(10);
            this.setFallingAllowed(false);
            this.setDivingAllowed(false);
        }

        public RandomPathFinder(AntiAfkHack antiAfkHack, PathFinder pathFinder) {
            super(pathFinder);
            this.setFallingAllowed(false);
            this.setDivingAllowed(false);
        }

        @Override
        public ArrayList<PathPos> formatPath() {
            this.failed = true;
            return super.formatPath();
        }
    }
}
