package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.CameraType;
import net.minecraft.util.Mth;

public class FreeLook
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgArrows;
    public final Setting<Mode> mode;
    public final Setting<Boolean> togglePerspective;
    public final Setting<Double> sensitivity;
    public final Setting<Boolean> arrows;
    private final Setting<Double> arrowSpeed;
    public float cameraYaw;
    public float cameraPitch;
    private CameraType prePers;

    public FreeLook() {
        super(Categories.Render, "free-look", "Allows more rotation options in third person.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgArrows = this.settings.createGroup("Arrows");
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Which entity to rotate.")).defaultValue(Mode.Player)).build());
        this.togglePerspective = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-perspective")).description("Changes your perspective on toggle.")).defaultValue(true)).build());
        this.sensitivity = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("camera-sensitivity")).description("How fast the camera moves in camera mode.")).defaultValue(8.0).min(0.0).sliderMax(10.0).build());
        this.arrows = this.sgArrows.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("arrows-control-opposite")).description("Allows you to control the other entities rotation with the arrow keys.")).defaultValue(true)).build());
        this.arrowSpeed = this.sgArrows.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("arrow-speed")).description("Rotation speed with arrow keys.")).defaultValue(4.0).min(0.0).build());
    }

    @Override
    public void onActivate() {
        this.cameraYaw = this.mc.player.getYRot();
        this.cameraPitch = this.mc.player.getXRot();
        this.prePers = this.mc.options.getCameraType();
        if (this.prePers != CameraType.THIRD_PERSON_BACK && this.togglePerspective.get().booleanValue()) {
            this.mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
    }

    @Override
    public void onDeactivate() {
        if (this.mc.options.getCameraType() != this.prePers && this.togglePerspective.get().booleanValue()) {
            this.mc.options.setCameraType(this.prePers);
        }
    }

    public boolean playerMode() {
        return this.isActive() && this.mc.options.getCameraType() == CameraType.THIRD_PERSON_BACK && this.mode.get() == Mode.Player;
    }

    public boolean cameraMode() {
        return this.isActive() && this.mode.get() == Mode.Camera;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.arrows.get().booleanValue()) {
            int i = 0;
            while ((double)i < this.arrowSpeed.get() * 2.0) {
                switch (this.mode.get().ordinal()) {
                    case 0: {
                        if (Input.isKeyPressed(263)) {
                            this.cameraYaw = (float)((double)this.cameraYaw - 0.5);
                        }
                        if (Input.isKeyPressed(262)) {
                            this.cameraYaw = (float)((double)this.cameraYaw + 0.5);
                        }
                        if (Input.isKeyPressed(265)) {
                            this.cameraPitch = (float)((double)this.cameraPitch - 0.5);
                        }
                        if (!Input.isKeyPressed(264)) break;
                        this.cameraPitch = (float)((double)this.cameraPitch + 0.5);
                        break;
                    }
                    case 1: {
                        float yaw = this.mc.player.getYRot();
                        float pitch = this.mc.player.getXRot();
                        if (Input.isKeyPressed(263)) {
                            yaw = (float)((double)yaw - 0.5);
                        }
                        if (Input.isKeyPressed(262)) {
                            yaw = (float)((double)yaw + 0.5);
                        }
                        if (Input.isKeyPressed(265)) {
                            pitch = (float)((double)pitch - 0.5);
                        }
                        if (Input.isKeyPressed(264)) {
                            pitch = (float)((double)pitch + 0.5);
                        }
                        this.mc.player.setYRot(yaw);
                        this.mc.player.setXRot(pitch);
                    }
                }
                ++i;
            }
        }
        this.mc.player.setXRot(Mth.clamp((float)this.mc.player.getXRot(), (float)-90.0f, (float)90.0f));
        this.cameraPitch = Mth.clamp((float)this.cameraPitch, (float)-90.0f, (float)90.0f);
    }

    public static enum Mode {
        Player,
        Camera;

    }
}
