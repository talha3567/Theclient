package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1688;
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
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.settings.filterlists.FollowFilterList;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.FakePlayerEntity;

@DontSaveState
public final class FollowHack
extends Hack
implements UpdateListener,
RenderListener {
    private class_1297 entity;
    private EntityPathFinder pathFinder;
    private PathProcessor processor;
    private int ticksProcessing;
    private final SliderSetting distance = new SliderSetting("Distance", "How closely to follow the target.", 1.0, 1.0, 12.0, 0.5, SliderSetting.ValueDisplay.DECIMAL);
    private final CheckboxSetting useAi = new CheckboxSetting("Use AI (experimental)", false);
    private final EntityFilterList entityFilters = FollowFilterList.create();

    public FollowHack() {
        super("Follow");
        this.setCategory(Category.MOVEMENT);
        this.addSetting(this.distance);
        this.addSetting(this.useAi);
        this.entityFilters.forEach(x$0 -> this.addSetting((Setting)x$0));
    }

    @Override
    public String getRenderName() {
        if (this.entity != null) {
            return "Following " + this.entity.method_5477().getString();
        }
        return "Follow";
    }

    @Override
    protected void onEnable() {
        FollowHack.WURST.getHax().fightBotHack.setEnabled(false);
        FollowHack.WURST.getHax().protectHack.setEnabled(false);
        FollowHack.WURST.getHax().tunnellerHack.setEnabled(false);
        if (this.entity == null) {
            Stream<class_1297> stream = StreamSupport.stream(FollowHack.MC.field_1687.method_18112().spliterator(), true).filter(e -> !e.method_31481()).filter(e -> e instanceof class_1309 && ((class_1309)e).method_6032() > 0.0f || e instanceof class_1688).filter(e -> e != FollowHack.MC.field_1724).filter(e -> !(e instanceof FakePlayerEntity));
            stream = this.entityFilters.applyTo(stream);
            this.entity = stream.min(Comparator.comparingDouble(EntityUtils::distanceToHitboxSq)).orElse(null);
            if (this.entity == null) {
                ChatUtils.error("Could not find a valid entity.");
                this.setEnabled(false);
                return;
            }
        }
        this.pathFinder = new EntityPathFinder();
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(RenderListener.class, this);
        ChatUtils.message("Now following " + this.entity.method_5477().getString());
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        this.pathFinder = null;
        this.processor = null;
        this.ticksProcessing = 0;
        PathProcessor.releaseControls();
        if (this.entity != null) {
            ChatUtils.message("No longer following " + this.entity.method_5477().getString());
        }
        this.entity = null;
    }

    @Override
    public void onUpdate() {
        block20: {
            block19: {
                if (FollowHack.MC.field_1724.method_6032() <= 0.0f) {
                    if (this.entity == null) {
                        ChatUtils.message("No longer following entity");
                    }
                    this.setEnabled(false);
                    return;
                }
                if (this.entity.method_31481()) break block19;
                if (!(this.entity instanceof class_1309) || !(((class_1309)this.entity).method_6032() <= 0.0f)) break block20;
            }
            this.entity = StreamSupport.stream(FollowHack.MC.field_1687.method_18112().spliterator(), true).filter(class_1309.class::isInstance).filter(e -> !e.method_31481() && ((class_1309)e).method_6032() > 0.0f).filter(e -> e != FollowHack.MC.field_1724).filter(e -> !(e instanceof FakePlayerEntity)).filter(e -> this.entity.method_5477().getString().equalsIgnoreCase(e.method_5477().getString())).min(Comparator.comparingDouble(EntityUtils::distanceToHitboxSq)).orElse(null);
            if (this.entity == null) {
                ChatUtils.message("No longer following entity");
                this.setEnabled(false);
                return;
            }
            this.pathFinder = new EntityPathFinder();
            this.processor = null;
            this.ticksProcessing = 0;
        }
        if (this.useAi.isChecked()) {
            if ((this.processor == null || this.processor.isDone() || this.ticksProcessing >= 10 || !this.pathFinder.isPathStillValid(this.processor.getIndex())) && (this.pathFinder.isDone() || this.pathFinder.isFailed())) {
                this.pathFinder = new EntityPathFinder();
                this.processor = null;
                this.ticksProcessing = 0;
            }
            if (!this.pathFinder.isDone() && !this.pathFinder.isFailed()) {
                PathProcessor.lockControls();
                WURST.getRotationFaker().faceVectorClient(this.entity.method_5829().method_1005());
                this.pathFinder.think();
                this.pathFinder.formatPath();
                this.processor = this.pathFinder.getProcessor();
            }
            if (!this.processor.isDone()) {
                this.processor.process();
                ++this.ticksProcessing;
            }
        } else {
            if (FollowHack.MC.field_1724.field_5976 && FollowHack.MC.field_1724.method_24828()) {
                FollowHack.MC.field_1724.method_6043();
            }
            if (FollowHack.MC.field_1724.method_5799() && FollowHack.MC.field_1724.method_23318() < this.entity.method_23318()) {
                FollowHack.MC.field_1724.method_18799(FollowHack.MC.field_1724.method_18798().method_1031(0.0, 0.04, 0.0));
            }
            if (!FollowHack.MC.field_1724.method_24828() && (FollowHack.MC.field_1724.method_31549().field_7479 || FollowHack.WURST.getHax().flightHack.isEnabled()) && FollowHack.MC.field_1724.method_5649(this.entity.method_23317(), FollowHack.MC.field_1724.method_23318(), this.entity.method_23321()) <= FollowHack.MC.field_1724.method_5649(FollowHack.MC.field_1724.method_23317(), this.entity.method_23318(), FollowHack.MC.field_1724.method_23321())) {
                if (FollowHack.MC.field_1724.method_23318() > this.entity.method_23318() + 1.0) {
                    FollowHack.MC.field_1690.field_1832.method_23481(true);
                } else if (FollowHack.MC.field_1724.method_23318() < this.entity.method_23318() - 1.0) {
                    FollowHack.MC.field_1690.field_1903.method_23481(true);
                }
            } else {
                FollowHack.MC.field_1690.field_1832.method_23481(false);
                FollowHack.MC.field_1690.field_1903.method_23481(false);
            }
            WURST.getRotationFaker().faceVectorClient(this.entity.method_5829().method_1005());
            double distanceSq = this.distance.getValueSq();
            FollowHack.MC.field_1690.field_1894.method_23481(FollowHack.MC.field_1724.method_5649(this.entity.method_23317(), FollowHack.MC.field_1724.method_23318(), this.entity.method_23321()) > distanceSq);
        }
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        PathCmd pathCmd = FollowHack.WURST.getCmds().pathCmd;
        this.pathFinder.renderPath(matrixStack, pathCmd.isDebugMode(), pathCmd.isDepthTest());
    }

    public void setEntity(class_1297 entity) {
        this.entity = entity;
    }

    private class EntityPathFinder
    extends PathFinder {
        public EntityPathFinder() {
            super(class_2338.method_49638((class_2374)FollowHack.this.entity.method_73189()));
            this.setThinkTime(1);
        }

        @Override
        protected boolean checkDone() {
            class_243 center = class_243.method_24953((class_2382)this.current);
            double distanceSq = Math.pow(FollowHack.this.distance.getValue(), 2.0);
            this.done = FollowHack.this.entity.method_5707(center) <= distanceSq;
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
