package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class PlayerModelHud
extends HudElement {
    public static final HudElementInfo<PlayerModelHud> INFO = new HudElementInfo<PlayerModelHud>(Hud.GROUP, "player-model", "Displays a model of your player.", PlayerModelHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgScale;
    private final SettingGroup sgBackground;
    private final Setting<Boolean> copyYaw;
    private final Setting<Integer> customYaw;
    private final Setting<Boolean> copyPitch;
    private final Setting<Integer> customPitch;
    private final Setting<CenterOrientation> centerOrientation;
    public final Setting<Boolean> customScale;
    public final Setting<Double> scale;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;

    public PlayerModelHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgScale = this.settings.createGroup("Scale");
        this.sgBackground = this.settings.createGroup("Background");
        this.copyYaw = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("copy-yaw")).description("Makes the player model's yaw equal to yours.")).defaultValue(true)).build());
        this.customYaw = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("custom-yaw")).description("Custom yaw for when copy yaw is off.")).defaultValue(0)).range(-180, 180).sliderRange(-180, 180).visible(() -> this.copyYaw.get() == false)).build());
        this.copyPitch = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("copy-pitch")).description("Makes the player model's pitch equal to yours.")).defaultValue(true)).build());
        this.customPitch = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("custom-pitch")).description("Custom pitch for when copy pitch is off.")).defaultValue(0)).range(-90, 90).sliderRange(-90, 90).visible(() -> this.copyPitch.get() == false)).build());
        this.centerOrientation = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("center-orientation")).description("Which direction the player faces when the HUD model faces directly forward.")).defaultValue(CenterOrientation.South)).build());
        this.customScale = this.sgScale.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-scale")).description("Applies a custom scale to this hud element.")).defaultValue(false)).onChanged(bl -> this.calculateSize())).build());
        this.scale = this.sgScale.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("Custom scale.")).visible(this.customScale::get)).defaultValue(2.0).onChanged(d -> this.calculateSize())).min(0.5).sliderRange(0.5, 3.0).build());
        this.background = this.sgBackground.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("background")).description("Displays background.")).defaultValue(false)).build());
        this.backgroundColor = this.sgBackground.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color used for the background.")).visible(this.background::get)).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        this.calculateSize();
    }

    @Override
    public void render(HudRenderer renderer) {
        renderer.post(() -> {
            LocalPlayer player = MeteorClient.mc.player;
            if (player == null) {
                return;
            }
            float offsetYaw = this.centerOrientation.get() == CenterOrientation.North ? 180.0f : 0.0f;
            float yaw = this.copyYaw.get() != false ? Mth.wrapDegrees((float)(player.yRotO + (player.getYRot() - player.yRotO) * MeteorClient.mc.getDeltaTracker().getGameTimeDeltaPartialTick(true) + offsetYaw)) : (float)this.customYaw.get().intValue();
            float pitch = this.copyPitch.get() != false ? player.getXRot() : (float)this.customPitch.get().intValue();
            renderer.entity((LivingEntity)player, this.x, this.y, this.getWidth(), this.getHeight(), -yaw, -pitch);
        });
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, this.getWidth(), this.getHeight(), this.backgroundColor.get());
        } else if (MeteorClient.mc.player == null) {
            renderer.quad(this.x, this.y, this.getWidth(), this.getHeight(), this.backgroundColor.get());
            renderer.line(this.x, this.y, this.x + this.getWidth(), this.y + this.getHeight(), Color.GRAY);
            renderer.line(this.x + this.getWidth(), this.y, this.x, this.y + this.getHeight(), Color.GRAY);
        }
    }

    private void calculateSize() {
        this.setSize(50.0 * this.getScale(), 75.0 * this.getScale());
    }

    private double getScale() {
        return this.customScale.get() != false ? this.scale.get() : this.scale.getDefaultValue();
    }

    private static enum CenterOrientation {
        North,
        South;

    }
}
