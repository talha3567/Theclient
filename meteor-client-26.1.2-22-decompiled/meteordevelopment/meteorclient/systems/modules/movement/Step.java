package meteordevelopment.meteorclient.systems.modules.movement;

import com.google.common.collect.Streams;
import java.util.OptionalDouble;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class Step
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Double> height;
    private final Setting<ActiveWhen> activeWhen;
    private final Setting<Boolean> safeStep;
    private final Setting<Integer> stepHealth;
    private float prevStepHeight;
    private boolean prevPathManagerStep;

    public Step() {
        super(Categories.Movement, "step", "Allows you to walk up full blocks instantly.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.height = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("height")).description("Step height.")).defaultValue(1.25).min(0.0).build());
        this.activeWhen = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("active-when")).description("Step is active when you meet these requirements.")).defaultValue(ActiveWhen.Always)).build());
        this.safeStep = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("safe-step")).description("Doesn't let you step out of a hole if you are low on health or there is a crystal nearby.")).defaultValue(false)).build());
        this.stepHealth = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("step-health")).description("The health you stop being able to step at.")).defaultValue(5)).range(1, 36).sliderRange(1, 36).visible(this.safeStep::get)).build());
    }

    @Override
    public void onActivate() {
        this.prevStepHeight = this.mc.player.maxUpStep();
        this.prevPathManagerStep = PathManagers.get().getSettings().getStep().get();
        PathManagers.get().getSettings().getStep().set(true);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean work = this.activeWhen.get() == ActiveWhen.Always || this.activeWhen.get() == ActiveWhen.Sneaking && this.mc.player.isShiftKeyDown() || this.activeWhen.get() == ActiveWhen.NotSneaking && !this.mc.player.isShiftKeyDown();
        double height = this.getMaxSafeHeight();
        if (work && height > 0.0) {
            this.mc.player.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(height);
        } else {
            this.mc.player.getAttribute(Attributes.STEP_HEIGHT).setBaseValue((double)this.prevStepHeight);
        }
    }

    @Override
    public void onDeactivate() {
        this.mc.player.getAttribute(Attributes.STEP_HEIGHT).setBaseValue((double)this.prevStepHeight);
        PathManagers.get().getSettings().getStep().set(this.prevPathManagerStep);
    }

    private float getHealth() {
        return this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount();
    }

    private double getExplosionDamage() {
        OptionalDouble crystalDamage = Streams.stream(this.mc.level.entitiesForRendering()).filter(EndCrystal.class::isInstance).filter(Entity::isAlive).mapToDouble(entity -> DamageUtils.crystalDamage((LivingEntity)this.mc.player, entity.position())).max();
        return crystalDamage.orElse(0.0);
    }

    private boolean isSafe() {
        return this.getHealth() > (float)this.stepHealth.get().intValue() && (double)this.getHealth() - this.getExplosionDamage() > (double)this.stepHealth.get().intValue();
    }

    private boolean isSaferThanWith(double damage) {
        return this.isSafe() || this.getExplosionDamage() - damage <= 0.0;
    }

    private double getMaxSafeHeight() {
        if (!this.safeStep.get().booleanValue()) {
            return this.height.get();
        }
        double max = this.height.get();
        double h = 0.0;
        double currentDamage = this.getExplosionDamage();
        AABB initial = this.mc.player.getBoundingBox();
        Vec3 inputOffset = this.mc.player.getLookAngle();
        Vec2 input = this.mc.player.input.getMoveVector();
        ((IVec3)inputOffset).meteor$setY(0.0);
        inputOffset = inputOffset.normalize().scale(1.2);
        double zdot = inputOffset.z;
        double xdot = inputOffset.x;
        inputOffset = new Vec3((double)input.y * xdot + (double)input.x * zdot, 0.0, (double)input.x * xdot + (double)input.y * zdot);
        int i = 1;
        while ((double)i < max) {
            this.mc.player.setBoundingBox(initial.move(0.0, (double)i, 0.0));
            if (!this.isSaferThanWith(currentDamage)) {
                this.mc.player.setBoundingBox(initial);
                return h;
            }
            this.mc.player.setBoundingBox(this.mc.player.getBoundingBox().move(inputOffset));
            if (!this.isSaferThanWith(currentDamage)) {
                this.mc.player.setBoundingBox(initial);
                return h;
            }
            h += 1.0;
            ++i;
        }
        this.mc.player.setBoundingBox(initial.move(0.0, max, 0.0));
        if (this.isSaferThanWith(currentDamage)) {
            h = max;
        }
        this.mc.player.setBoundingBox(initial);
        return h;
    }

    public static enum ActiveWhen {
        Always,
        Sneaking,
        NotSneaking;

    }
}
