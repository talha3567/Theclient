package meteordevelopment.meteorclient.systems.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import meteordevelopment.meteorclient.events.render.ArmRenderEvent;
import meteordevelopment.meteorclient.events.render.HeldItemRendererEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Vector3dSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.InteractionHand;
import org.joml.Quaternionfc;
import org.joml.Vector3d;

public class HandView
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgMainHand;
    private final SettingGroup sgOffHand;
    private final SettingGroup sgArm;
    private final Setting<Boolean> followRotations;
    public final Setting<Boolean> oldAnimations;
    public final Setting<Boolean> skipSwapping;
    private final Setting<Boolean> disableFoodAnimation;
    public final Setting<Boolean> swordSlash;
    public final Setting<SwingMode> swingMode;
    public final Setting<Integer> swingSpeed;
    public final Setting<Double> mainSwing;
    public final Setting<Double> offSwing;
    private final Setting<Vector3d> scaleMain;
    private final Setting<Vector3d> posMain;
    private final Setting<Vector3d> rotMain;
    private final Setting<Vector3d> scaleOff;
    private final Setting<Vector3d> posOff;
    private final Setting<Vector3d> rotOff;
    private final Setting<Vector3d> scaleArm;
    private final Setting<Vector3d> posArm;
    private final Setting<Vector3d> rotArm;

    public HandView() {
        super(Categories.Render, "hand-view", "Alters the way items are rendered in your hands.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgMainHand = this.settings.createGroup("Main Hand");
        this.sgOffHand = this.settings.createGroup("Off Hand");
        this.sgArm = this.settings.createGroup("Arm");
        this.followRotations = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("server-rotations")).description("Makes your hands follow your serverside rotations.")).defaultValue(false)).build());
        this.oldAnimations = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("old-animations")).description("Changes hit animations to those like 1.8")).defaultValue(false)).build());
        this.skipSwapping = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("skip-swapping-animation")).description("Whether or not to skip the item swapping animation")).defaultValue(false)).build());
        this.disableFoodAnimation = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("disable-eating-animation")).description("Disables the eating animation. Potentially desirable if it goes offscreen.")).defaultValue(false)).build());
        this.swordSlash = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sword-slash")).description("Replaces the sword swing animation with the offhand idle animation.")).defaultValue(false)).build());
        this.swingMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("swing-mode")).description("Modifies your client & server hand swinging.")).defaultValue(SwingMode.None)).build());
        this.swingSpeed = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("swing-speed")).description("The swing speed of your hands.")).defaultValue(6)).range(0, 20).sliderMax(20).build());
        this.mainSwing = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("main-hand-progress")).description("The swing progress of your main hand.")).defaultValue(0.0).range(0.0, 1.0).sliderMax(1.0).build());
        this.offSwing = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("off-hand-progress")).description("The swing progress of your off hand.")).defaultValue(0.0).range(0.0, 1.0).sliderMax(1.0).build());
        this.scaleMain = this.sgMainHand.add(((Vector3dSetting.Builder)((Vector3dSetting.Builder)new Vector3dSetting.Builder().name("scale")).description("The scale of your main hand.")).defaultValue(1.0, 1.0, 1.0).sliderMax(5.0).decimalPlaces(1).build());
        this.posMain = this.sgMainHand.add(((Vector3dSetting.Builder)((Vector3dSetting.Builder)new Vector3dSetting.Builder().name("position")).description("The position of your main hand.")).defaultValue(0.0, 0.0, 0.0).sliderRange(-3.0, 3.0).decimalPlaces(1).build());
        this.rotMain = this.sgMainHand.add(((Vector3dSetting.Builder)((Vector3dSetting.Builder)new Vector3dSetting.Builder().name("rotation")).description("The rotation of your main hand.")).defaultValue(0.0, 0.0, 0.0).sliderRange(-180.0, 180.0).decimalPlaces(0).build());
        this.scaleOff = this.sgOffHand.add(((Vector3dSetting.Builder)((Vector3dSetting.Builder)new Vector3dSetting.Builder().name("scale")).description("The scale of your off hand.")).defaultValue(1.0, 1.0, 1.0).sliderMax(5.0).decimalPlaces(1).build());
        this.posOff = this.sgOffHand.add(((Vector3dSetting.Builder)((Vector3dSetting.Builder)new Vector3dSetting.Builder().name("position")).description("The position of your off hand.")).defaultValue(0.0, 0.0, 0.0).sliderRange(-3.0, 3.0).decimalPlaces(1).build());
        this.rotOff = this.sgOffHand.add(((Vector3dSetting.Builder)((Vector3dSetting.Builder)new Vector3dSetting.Builder().name("rotation")).description("The rotation of your off hand.")).defaultValue(0.0, 0.0, 0.0).sliderRange(-180.0, 180.0).decimalPlaces(0).build());
        this.scaleArm = this.sgArm.add(((Vector3dSetting.Builder)new Vector3dSetting.Builder().name("scale")).defaultValue(1.0, 1.0, 1.0).sliderMax(5.0).decimalPlaces(1).build());
        this.posArm = this.sgArm.add(((Vector3dSetting.Builder)new Vector3dSetting.Builder().name("position")).defaultValue(0.0, 0.0, 0.0).sliderRange(-3.0, 3.0).decimalPlaces(1).build());
        this.rotArm = this.sgArm.add(((Vector3dSetting.Builder)new Vector3dSetting.Builder().name("rotation")).defaultValue(0.0, 0.0, 0.0).sliderRange(-180.0, 180.0).decimalPlaces(0).build());
    }

    @EventHandler
    private void onHeldItemRender(HeldItemRendererEvent event) {
        if (Rotations.rotating && this.followRotations.get().booleanValue()) {
            this.applyServerRotations(event.matrix);
        }
        if (event.hand == InteractionHand.MAIN_HAND) {
            this.rotate(event.matrix, this.rotMain.get());
            this.scale(event.matrix, this.scaleMain.get());
            this.translate(event.matrix, this.posMain.get());
        } else {
            this.rotate(event.matrix, this.rotOff.get());
            this.scale(event.matrix, this.scaleOff.get());
            this.translate(event.matrix, this.posOff.get());
        }
    }

    @EventHandler
    private void onRenderArm(ArmRenderEvent event) {
        this.rotate(event.matrix, this.rotArm.get());
        this.scale(event.matrix, this.scaleArm.get());
        this.translate(event.matrix, this.posArm.get());
    }

    private void rotate(PoseStack matrix, Vector3d rotation) {
        matrix.mulPose((Quaternionfc)Axis.XP.rotationDegrees((float)rotation.x));
        matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)rotation.y));
        matrix.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)rotation.z));
    }

    private void scale(PoseStack matrix, Vector3d scale) {
        matrix.scale((float)scale.x, (float)scale.y, (float)scale.z);
    }

    private void translate(PoseStack matrix, Vector3d translation) {
        matrix.translate((float)translation.x, (float)translation.y, (float)translation.z);
    }

    private void applyServerRotations(PoseStack matrix) {
        matrix.mulPose((Quaternionfc)Axis.XP.rotationDegrees(this.mc.player.getXRot() - Rotations.serverPitch));
        matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees(this.mc.player.getYRot() - Rotations.serverYaw));
    }

    public boolean oldAnimations() {
        return this.isActive() && this.oldAnimations.get() != false;
    }

    public boolean skipSwapping() {
        return this.isActive() && this.skipSwapping.get() != false;
    }

    public boolean disableFoodAnimation() {
        return this.isActive() && this.disableFoodAnimation.get() != false;
    }

    public boolean swordSlash() {
        return this.isActive() && this.swordSlash.get() != false;
    }

    public static enum SwingMode {
        Offhand,
        Mainhand,
        None;

    }
}
