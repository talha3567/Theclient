package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.Mth;

public class CompassHud
extends HudElement {
    public static final HudElementInfo<CompassHud> INFO = new HudElementInfo<CompassHud>(Hud.GROUP, "compass", "Displays a compass.", CompassHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgScale;
    private final SettingGroup sgBackground;
    private final Setting<Mode> mode;
    private final Setting<SettingColor> colorNorth;
    private final Setting<SettingColor> colorOther;
    private final Setting<Boolean> shadow;
    private final Setting<Boolean> customScale;
    private final Setting<Double> textScale;
    private final Setting<Double> compassScale;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;

    public CompassHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgScale = this.settings.createGroup("Scale");
        this.sgBackground = this.settings.createGroup("Background");
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("type")).description("Which type of direction information to show.")).defaultValue(Mode.Axis)).build());
        this.colorNorth = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("color-north")).description("Color of north.")).defaultValue(new SettingColor(225, 45, 45)).build());
        this.colorOther = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("color-other")).description("Color of other directions.")).defaultValue(new SettingColor()).build());
        this.shadow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("shadow")).description("Text shadow.")).defaultValue(false)).build());
        this.customScale = this.sgScale.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-scale")).description("Apply custom scales to this hud element.")).defaultValue(false)).onChanged(bl -> this.calculateSize())).build());
        this.textScale = this.sgScale.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("text-scale")).description("Scale to use for the letters.")).visible(this.customScale::get)).defaultValue(1.0).min(0.5).sliderRange(0.5, 3.0).build());
        this.compassScale = this.sgScale.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("compass-scale")).description("Scale of the whole HUD element.")).visible(this.customScale::get)).defaultValue(1.0).min(0.5).sliderRange(0.5, 3.0).onChanged(d -> this.calculateSize())).build());
        this.background = this.sgBackground.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("background")).description("Displays background.")).defaultValue(false)).build());
        this.backgroundColor = this.sgBackground.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color used for the background.")).visible(this.background::get)).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        this.calculateSize();
    }

    private void calculateSize() {
        this.setSize(100.0 * this.getCompassScale(), 100.0 * this.getCompassScale());
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = (double)this.x + (double)this.getWidth() / 2.0;
        double y = (double)this.y + (double)this.getHeight() / 2.0;
        double pitch = this.isInEditor() ? 120.0 : (double)Mth.clamp((float)(MeteorClient.mc.player.getXRot() + 30.0f), (float)-90.0f, (float)90.0f);
        pitch = Math.toRadians(pitch);
        double yaw = this.isInEditor() ? 180.0 : (double)Mth.wrapDegrees((float)MeteorClient.mc.player.getYRot());
        yaw = Math.toRadians(yaw);
        for (Direction direction : Direction.values()) {
            String axis = this.mode.get() == Mode.Axis ? direction.getAxis() : direction.name();
            renderer.text(axis, x + this.getX(direction, yaw) - renderer.textWidth(axis, this.shadow.get(), this.getTextScale()) / 2.0, y + this.getY(direction, yaw, pitch) - renderer.textHeight(this.shadow.get(), this.getTextScale()) / 2.0, direction == Direction.N ? (Color)this.colorNorth.get() : (Color)this.colorOther.get(), this.shadow.get(), this.getTextScale());
        }
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, this.getWidth(), this.getHeight(), this.backgroundColor.get());
        }
    }

    private double getX(Direction direction, double yaw) {
        return Math.sin(this.getPos(direction, yaw)) * this.getCompassScale() * 40.0;
    }

    private double getY(Direction direction, double yaw, double pitch) {
        return Math.cos(this.getPos(direction, yaw)) * Math.sin(pitch) * this.getCompassScale() * 40.0;
    }

    private double getPos(Direction direction, double yaw) {
        return yaw + (double)direction.ordinal() * Math.PI / 2.0;
    }

    private double getTextScale() {
        return this.customScale.get() != false ? this.textScale.get().doubleValue() : Hud.get().getTextScale();
    }

    private double getCompassScale() {
        return this.customScale.get() != false ? this.compassScale.get().doubleValue() : Hud.get().getTextScale();
    }

    public static enum Mode {
        Direction,
        Axis;

    }

    private static enum Direction {
        N("Z-"),
        W("X-"),
        S("Z+"),
        E("X+");

        private final String axis;

        private Direction(String axis) {
            this.axis = axis;
        }

        public String getAxis() {
            return this.axis;
        }
    }
}
