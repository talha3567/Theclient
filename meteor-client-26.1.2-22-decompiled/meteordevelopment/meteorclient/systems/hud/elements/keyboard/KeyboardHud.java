package meteordevelopment.meteorclient.systems.hud.elements.keyboard;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.KeyInputEvent;
import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.mixin.KeyMappingAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.elements.keyboard.KeyDimensions;
import meteordevelopment.meteorclient.systems.hud.elements.keyboard.LayoutContext;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.misc.input.KeyBinds;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class KeyboardHud
extends HudElement {
    public static final HudElementInfo<KeyboardHud> INFO = new HudElementInfo<KeyboardHud>(Hud.GROUP, "keyboard", "Displays pressed keys.", KeyboardHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgColor;
    private final SettingGroup sgBorder;
    private final SettingGroup sgBackground;
    private final Setting<Preset> preset;
    private final Setting<List<Key>> customKeys;
    private final Setting<Double> scale;
    private final Setting<Double> spacing;
    private final Setting<KeyboardLayout> keyboardLayout;
    private final Setting<Boolean> showCps;
    private final Setting<Alignment> alignment;
    private final Setting<SettingColor> pressedColor;
    private final Setting<SettingColor> unpressedColor;
    private final Setting<Boolean> colorFade;
    private final Setting<Double> fadeTime;
    private final Setting<SettingColor> textColor;
    private final Setting<Boolean> border;
    private final Setting<SettingColor> borderColor;
    private final Setting<Double> borderWidth;
    private final Setting<Double> opacity;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;
    private final List<Key> keys;
    private double minX;
    private double minY;

    private meteordevelopment.meteorclient.utils.render.color.Color getColor(SettingColor color, SettingColor out) {
        out.set(color);
        out.a = (int)((double)out.a * this.opacity.get());
        return out;
    }

    private meteordevelopment.meteorclient.utils.render.color.Color getKeyColor(Key key, SettingColor out) {
        if (this.colorFade.get().booleanValue()) {
            boolean pressed = key.isPressed;
            float target = pressed ? 1.0f : 0.0f;
            float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
            float frameDelta = (float)((double)(tickDelta / 20.0f) / this.fadeTime.get()) * (float)(pressed ? 1 : -1);
            key.delta = Math.clamp(key.delta + frameDelta, 0.0f, 1.0f);
            if (key.delta == target) {
                out.set(target == 1.0f ? (meteordevelopment.meteorclient.utils.render.color.Color)this.pressedColor.get() : (meteordevelopment.meteorclient.utils.render.color.Color)this.unpressedColor.get());
            } else {
                meteordevelopment.meteorclient.utils.render.color.Color c1 = this.pressedColor.get();
                meteordevelopment.meteorclient.utils.render.color.Color c2 = this.unpressedColor.get();
                float[] hsb1 = new float[3];
                float[] hsb2 = new float[3];
                Color.RGBtoHSB(c1.r, c1.g, c1.b, hsb2);
                Color.RGBtoHSB(c2.r, c2.g, c2.b, hsb1);
                int rgb = Color.HSBtoRGB(Mth.lerp((float)key.delta, (float)hsb1[0], (float)hsb2[0]), Mth.lerp((float)key.delta, (float)hsb1[1], (float)hsb2[1]), Mth.lerp((float)key.delta, (float)hsb1[2], (float)hsb2[2]));
                out.r = meteordevelopment.meteorclient.utils.render.color.Color.toRGBAR(rgb);
                out.g = meteordevelopment.meteorclient.utils.render.color.Color.toRGBAG(rgb);
                out.b = meteordevelopment.meteorclient.utils.render.color.Color.toRGBAB(rgb);
                out.a = Mth.lerpInt((float)key.delta, (int)this.pressedColor.get().a, (int)this.unpressedColor.get().a);
            }
        } else {
            out.set(key.isPressed ? (meteordevelopment.meteorclient.utils.render.color.Color)this.pressedColor.get() : (meteordevelopment.meteorclient.utils.render.color.Color)this.unpressedColor.get());
        }
        out.a = (int)((double)out.a * this.opacity.get());
        return out;
    }

    @EventHandler(priority=100)
    private void onKey(KeyInputEvent event) {
        for (Key key : this.keys) {
            if (!key.matches(event.input.key(), event.input.scancode(), true)) continue;
            key.update(event.action);
        }
    }

    @EventHandler(priority=100)
    private void onMouseClick(MouseClickEvent event) {
        for (Key key : this.keys) {
            if (!key.matches(event.input.button(), -1, false)) continue;
            key.update(event.action);
        }
    }

    public KeyboardHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgColor = this.settings.createGroup("Color");
        this.sgBorder = this.settings.createGroup("Border");
        this.sgBackground = this.settings.createGroup("Background");
        this.preset = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("preset")).description("Which keys to display.")).defaultValue(Preset.Movement)).onChanged(this::onPresetChanged)).build());
        this.customKeys = this.sgGeneral.add(new CustomKeyListSetting(this));
        this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("Scale of the keyboard.")).defaultValue(1.5).min(0.5).sliderRange(0.5, 5.0).decimalPlaces(1).onChanged(d -> this.calculateSize())).build());
        this.spacing = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("spacing")).description("Spacing between keys.")).defaultValue(1.0).min(0.0).sliderRange(0.0, 10.0).decimalPlaces(1).visible(() -> this.preset.get() != Preset.Custom)).onChanged(d -> this.onPresetChanged(this.preset.get()))).build());
        this.keyboardLayout = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("keyboard-layout")).description("Physical keyboard layout (ANSI or ISO).")).defaultValue(KeyboardLayout.ANSI)).visible(() -> this.preset.get() == Preset.Keyboard)).onChanged(keyboardLayout -> this.onPresetChanged(this.preset.get()))).build());
        this.showCps = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("Show CPS")).description("Shows clicks per second on keys.")).defaultValue(false)).visible(() -> this.preset.get() != Preset.Custom)).onChanged(bl -> this.onPresetChanged(this.preset.get()))).build());
        this.alignment = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("alignment")).description("Horizontal alignment of the text.")).defaultValue(Alignment.Center)).build());
        this.pressedColor = this.sgColor.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("pressed-color")).description("Color of pressed keys.")).defaultValue(new SettingColor(200, 200, 200, 100)).build());
        this.unpressedColor = this.sgColor.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("unpressed-color")).description("Color of unpressed keys.")).defaultValue(new SettingColor(0, 0, 0, 100)).build());
        this.colorFade = this.sgColor.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("color-fade")).description("Whether to fade the key color when pressing/unpressing.")).defaultValue(false)).build());
        this.fadeTime = this.sgColor.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("fade-time")).description("How long to fade the color for, in seconds.")).visible(this.colorFade::get)).defaultValue(0.1).min(0.01).sliderRange(0.01, 0.5).decimalPlaces(2).build());
        this.textColor = this.sgColor.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("text-color")).description("Color of the key name.")).defaultValue(new SettingColor(255, 255, 255)).build());
        this.border = this.sgBorder.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("border")).description("Draw a border around keys.")).defaultValue(false)).build());
        this.borderColor = this.sgBorder.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("border-color")).description("Color of the key border.")).visible(this.border::get)).defaultValue(new SettingColor(255, 255, 255, 200)).build());
        this.borderWidth = this.sgBorder.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("border-width")).description("Width of the key border.")).visible(this.border::get)).defaultValue(1.0).min(0.5).sliderRange(0.5, 5.0).build());
        this.opacity = this.sgColor.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("opacity")).description("Opacity of the whole element.")).defaultValue(1.0).min(0.0).max(1.0).sliderMax(1.0).build());
        this.background = this.sgBackground.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("background")).description("Displays background.")).defaultValue(false)).build());
        this.backgroundColor = this.sgBackground.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color used for the background.")).visible(this.background::get)).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        this.keys = new ArrayList<Key>();
        if (MeteorClient.mc.options != null) {
            this.onPresetChanged(this.preset.get());
        }
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    private void onPresetChanged(Preset preset) {
        if (MeteorClient.mc.options == null) {
            return;
        }
        this.keys.clear();
        double u = 35.0;
        double g = this.spacing.get() * 2.0;
        LayoutContext l = new LayoutContext(u, g, 15.0);
        switch (preset.ordinal()) {
            case 0: {
                this.keys.add(l.key(MeteorClient.mc.options.keyUp, l.ux(1.0), 0.0).setShowCps(this.showCps.get()));
                this.keys.add(l.key(MeteorClient.mc.options.keyLeft, 0.0, l.y(1.0)).setShowCps(this.showCps.get()));
                this.keys.add(l.key(MeteorClient.mc.options.keyDown, l.ux(1.0), l.y(1.0)).setShowCps(this.showCps.get()));
                this.keys.add(l.key(MeteorClient.mc.options.keyRight, l.ux(2.0), l.y(1.0)).setShowCps(this.showCps.get()));
                this.keys.add(l.key(MeteorClient.mc.options.keyShift, 0.0, l.y(2.0)).setShowCps(this.showCps.get()));
                this.keys.add(l.key(MeteorClient.mc.options.keyJump, l.ux(1.0), l.y(2.0), KeyDimensions.UNIT_2U).setShowCps(this.showCps.get()));
                break;
            }
            case 1: {
                this.keys.add(l.key(MeteorClient.mc.options.keyAttack, "LMB", 0.0, 0.0).setShowCps(this.showCps.get()));
                this.keys.add(l.key(MeteorClient.mc.options.keyUse, "RMB", l.ux(1.0), 0.0).setShowCps(this.showCps.get()));
                break;
            }
            case 2: {
                this.keys.add(l.key(MeteorClient.mc.options.keyDrop, 0.0, 0.0).setShowCps(this.showCps.get()));
                this.keys.add(l.key(MeteorClient.mc.options.keySwapOffhand, l.ux(1.0), 0.0).setShowCps(this.showCps.get()));
                this.keys.add(l.key(MeteorClient.mc.options.keyInventory, l.ux(2.0), 0.0).setShowCps(this.showCps.get()));
                break;
            }
            case 3: {
                for (int i = 0; i < 9; ++i) {
                    this.keys.add(l.key(MeteorClient.mc.options.keyHotbarSlots[i], l.ux(i), 0.0).setShowCps(this.showCps.get()));
                }
                break;
            }
            case 4: {
                if (this.keyboardLayout.get() == KeyboardLayout.ANSI) {
                    this.buildAnsiLayout(l);
                } else {
                    this.buildIsoLayout(l);
                }
                for (Key key : this.keys) {
                    key.setShowCps(this.showCps.get());
                }
                break;
            }
            case 5: {
                this.keys.addAll((Collection<Key>)this.customKeys.get());
            }
        }
        this.calculateSize();
    }

    private void buildAnsiLayout(LayoutContext l) {
        int i;
        double row0 = l.uy(0.0);
        double row1 = l.uy(1.0);
        double row2 = l.uy(2.0);
        double row3 = l.uy(3.0);
        double row4 = l.uy(4.0);
        double row5 = l.uy(5.0);
        this.keys.add(l.key(Keybind.fromKey(256), 0.0, row0));
        for (i = 0; i < 4; ++i) {
            this.keys.add(l.key(Keybind.fromKey(290 + i), l.ux(2.0 + (double)i), row0));
        }
        for (i = 0; i < 4; ++i) {
            this.keys.add(l.key(Keybind.fromKey(294 + i), l.ux(6.5 + (double)i), row0));
        }
        for (i = 0; i < 4; ++i) {
            this.keys.add(l.key(Keybind.fromKey(298 + i), l.ux(11.0 + (double)i), row0));
        }
        this.keys.add(l.key(Keybind.fromKey(283), l.ux(15.5), row0));
        this.keys.add(l.key(Keybind.fromKey(281), l.ux(16.5), row0));
        this.keys.add(l.key(Keybind.fromKey(284), l.ux(17.5), row0));
        int[] row1Keys = new int[]{96, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 45, 61};
        for (int i2 = 0; i2 < row1Keys.length; ++i2) {
            this.keys.add(l.key(Keybind.fromKey(row1Keys[i2]), l.ux(i2), row1));
        }
        this.keys.add(l.key(Keybind.fromKey(259), l.ux(13.0), row1, KeyDimensions.BACKSPACE));
        this.keys.add(l.key(Keybind.fromKey(260), l.ux(15.5), row1));
        this.keys.add(l.key(Keybind.fromKey(268), l.ux(16.5), row1));
        this.keys.add(l.key(Keybind.fromKey(266), l.ux(17.5), row1));
        this.keys.add(l.key(Keybind.fromKey(258), 0.0, row2, KeyDimensions.TAB));
        int[] row2Keys = new int[]{81, 87, 69, 82, 84, 89, 85, 73, 79, 80, 91, 93};
        double tabEnd = l.px(KeyDimensions.TAB) + l.keyGap;
        for (int i3 = 0; i3 < row2Keys.length; ++i3) {
            this.keys.add(l.key(Keybind.fromKey(row2Keys[i3]), tabEnd + l.ux(i3), row2));
        }
        this.keys.add(l.key(Keybind.fromKey(92), tabEnd + l.ux(12.0), row2, KeyDimensions.TAB));
        this.keys.add(l.key(Keybind.fromKey(261), l.ux(15.5), row2));
        this.keys.add(l.key(Keybind.fromKey(269), l.ux(16.5), row2));
        this.keys.add(l.key(Keybind.fromKey(267), l.ux(17.5), row2));
        this.keys.add(l.key(Keybind.fromKey(280), 0.0, row3, KeyDimensions.CAPS_LOCK));
        int[] row3Keys = new int[]{65, 83, 68, 70, 71, 72, 74, 75, 76, 59, 39};
        double capsEnd = l.px(KeyDimensions.CAPS_LOCK) + l.keyGap;
        for (int i4 = 0; i4 < row3Keys.length; ++i4) {
            this.keys.add(l.key(Keybind.fromKey(row3Keys[i4]), capsEnd + l.ux(i4), row3));
        }
        this.keys.add(l.key(Keybind.fromKey(257), capsEnd + l.ux(11.0), row3, KeyDimensions.ENTER_ANSI));
        this.keys.add(l.key(Keybind.fromKey(340), 0.0, row4, KeyDimensions.LEFT_SHIFT_ANSI));
        int[] row4Keys = new int[]{90, 88, 67, 86, 66, 78, 77, 44, 46, 47};
        double lShiftEnd = l.px(KeyDimensions.LEFT_SHIFT_ANSI) + l.keyGap;
        for (int i5 = 0; i5 < row4Keys.length; ++i5) {
            this.keys.add(l.key(Keybind.fromKey(row4Keys[i5]), lShiftEnd + l.ux(i5), row4));
        }
        this.keys.add(l.key(Keybind.fromKey(344), lShiftEnd + l.ux(10.0), row4, KeyDimensions.RIGHT_SHIFT));
        this.keys.add(l.key(Keybind.fromKey(265), l.ux(16.5), row4));
        double xPos = 0.0;
        this.keys.add(l.key(Keybind.fromKey(341), xPos, row5, KeyDimensions.CTRL));
        this.keys.add(l.key(Keybind.fromKey(343), xPos += l.px(KeyDimensions.CTRL) + l.keyGap, row5, KeyDimensions.GUI));
        this.keys.add(l.key(Keybind.fromKey(342), xPos += l.px(KeyDimensions.GUI) + l.keyGap, row5, KeyDimensions.ALT));
        this.keys.add(l.key(Keybind.fromKey(32), xPos += l.px(KeyDimensions.ALT) + l.keyGap, row5, KeyDimensions.SPACEBAR));
        this.keys.add(l.key(Keybind.fromKey(346), xPos += l.px(KeyDimensions.SPACEBAR) + l.keyGap, row5, KeyDimensions.ALT));
        this.keys.add(l.key(Keybind.fromKey(347), xPos += l.px(KeyDimensions.ALT) + l.keyGap, row5, KeyDimensions.GUI));
        this.keys.add(l.key(Keybind.fromKey(348), xPos += l.px(KeyDimensions.GUI) + l.keyGap, row5, KeyDimensions.MENU));
        this.keys.add(l.key(Keybind.fromKey(345), xPos += l.px(KeyDimensions.MENU) + l.keyGap, row5, KeyDimensions.CTRL));
        this.keys.add(l.key(Keybind.fromKey(263), l.ux(15.5), row5));
        this.keys.add(l.key(Keybind.fromKey(264), l.ux(16.5), row5));
        this.keys.add(l.key(Keybind.fromKey(262), l.ux(17.5), row5));
    }

    private void buildIsoLayout(LayoutContext l) {
        int i;
        double row0 = l.uy(0.0);
        double row1 = l.uy(1.0);
        double row2 = l.uy(2.0);
        double row3 = l.uy(3.0);
        double row4 = l.uy(4.0);
        double row5 = l.uy(5.0);
        this.keys.add(l.key(Keybind.fromKey(256), 0.0, row0));
        for (i = 0; i < 4; ++i) {
            this.keys.add(l.key(Keybind.fromKey(290 + i), l.ux(2.0 + (double)i), row0));
        }
        for (i = 0; i < 4; ++i) {
            this.keys.add(l.key(Keybind.fromKey(294 + i), l.ux(6.5 + (double)i), row0));
        }
        for (i = 0; i < 4; ++i) {
            this.keys.add(l.key(Keybind.fromKey(298 + i), l.ux(11.0 + (double)i), row0));
        }
        this.keys.add(l.key(Keybind.fromKey(283), l.ux(15.5), row0));
        this.keys.add(l.key(Keybind.fromKey(281), l.ux(16.5), row0));
        this.keys.add(l.key(Keybind.fromKey(284), l.ux(17.5), row0));
        int[] row1Keys = new int[]{96, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 45, 61};
        for (int i2 = 0; i2 < row1Keys.length; ++i2) {
            this.keys.add(l.key(Keybind.fromKey(row1Keys[i2]), l.ux(i2), row1));
        }
        this.keys.add(l.key(Keybind.fromKey(259), l.ux(13.0), row1, KeyDimensions.BACKSPACE));
        this.keys.add(l.key(Keybind.fromKey(260), l.ux(15.5), row1));
        this.keys.add(l.key(Keybind.fromKey(268), l.ux(16.5), row1));
        this.keys.add(l.key(Keybind.fromKey(266), l.ux(17.5), row1));
        this.keys.add(l.key(Keybind.fromKey(258), 0.0, row2, KeyDimensions.TAB));
        int[] row2Keys = new int[]{81, 87, 69, 82, 84, 89, 85, 73, 79, 80, 91, 93};
        double tabEnd = l.px(KeyDimensions.TAB) + l.keyGap;
        for (int i3 = 0; i3 < row2Keys.length; ++i3) {
            this.keys.add(l.key(Keybind.fromKey(row2Keys[i3]), tabEnd + l.ux(i3), row2));
        }
        this.keys.add(l.key(Keybind.fromKey(261), l.ux(15.5), row2));
        this.keys.add(l.key(Keybind.fromKey(269), l.ux(16.5), row2));
        this.keys.add(l.key(Keybind.fromKey(267), l.ux(17.5), row2));
        this.keys.add(l.key(Keybind.fromKey(280), 0.0, row3, KeyDimensions.CAPS_LOCK));
        int[] row3Keys = new int[]{65, 83, 68, 70, 71, 72, 74, 75, 76, 59, 39};
        double capsEnd = l.px(KeyDimensions.CAPS_LOCK) + l.keyGap;
        for (int i4 = 0; i4 < row3Keys.length; ++i4) {
            this.keys.add(l.key(Keybind.fromKey(row3Keys[i4]), capsEnd + l.ux(i4), row3));
        }
        this.keys.add(l.key(Keybind.fromKey(92), capsEnd + l.ux(11.0), row3));
        double topBarStartX = tabEnd + l.ux(12.0);
        double mainBlockRightEdge = tabEnd + l.ux(12.0) + l.px(KeyDimensions.TAB);
        double enterStemWidth = l.px(KeyDimensions.ENTER_ISO_WIDTH);
        double enterStemHeight = l.px(KeyDimensions.ENTER_ISO_HEIGHT);
        double enterStemX = mainBlockRightEdge - enterStemWidth;
        this.keys.add(new IsoEnterKey(Keybind.fromKey(257), enterStemX, row2, enterStemWidth, enterStemHeight, topBarStartX));
        this.keys.add(l.key(Keybind.fromKey(340), 0.0, row4, KeyDimensions.LEFT_SHIFT_ISO));
        double lShiftEnd = l.px(KeyDimensions.LEFT_SHIFT_ISO) + l.keyGap;
        this.keys.add(l.key(Keybind.fromKey(162), lShiftEnd, row4));
        int[] row4Keys = new int[]{90, 88, 67, 86, 66, 78, 77, 44, 46, 47};
        for (int i5 = 0; i5 < row4Keys.length; ++i5) {
            this.keys.add(l.key(Keybind.fromKey(row4Keys[i5]), lShiftEnd + l.ux(1.0 + (double)i5), row4));
        }
        double rShiftX = lShiftEnd + l.ux(11.0);
        this.keys.add(l.key(Keybind.fromKey(344), rShiftX, row4, KeyDimensions.RIGHT_SHIFT));
        this.keys.add(l.key(Keybind.fromKey(265), l.ux(16.5), row4));
        double xPos = 0.0;
        this.keys.add(l.key(Keybind.fromKey(341), xPos, row5, KeyDimensions.CTRL));
        this.keys.add(l.key(Keybind.fromKey(343), xPos += l.px(KeyDimensions.CTRL) + l.keyGap, row5, KeyDimensions.GUI));
        this.keys.add(l.key(Keybind.fromKey(342), xPos += l.px(KeyDimensions.GUI) + l.keyGap, row5, KeyDimensions.ALT));
        this.keys.add(l.key(Keybind.fromKey(32), xPos += l.px(KeyDimensions.ALT) + l.keyGap, row5, KeyDimensions.SPACEBAR));
        this.keys.add(l.keyNamed(Keybind.fromKey(346), "AltGr", xPos += l.px(KeyDimensions.SPACEBAR) + l.keyGap, row5, KeyDimensions.ALT));
        this.keys.add(l.key(Keybind.fromKey(347), xPos += l.px(KeyDimensions.ALT) + l.keyGap, row5, KeyDimensions.GUI));
        this.keys.add(l.key(Keybind.fromKey(348), xPos += l.px(KeyDimensions.GUI) + l.keyGap, row5, KeyDimensions.MENU));
        this.keys.add(l.key(Keybind.fromKey(345), xPos += l.px(KeyDimensions.MENU) + l.keyGap, row5, KeyDimensions.CTRL));
        this.keys.add(l.key(Keybind.fromKey(263), l.ux(15.5), row5));
        this.keys.add(l.key(Keybind.fromKey(264), l.ux(16.5), row5));
        this.keys.add(l.key(Keybind.fromKey(262), l.ux(17.5), row5));
    }

    private void calculateSize() {
        if (this.keys.isEmpty()) {
            this.setSize(0.0, 0.0);
            return;
        }
        this.minY = 0.0;
        this.minX = 0.0;
        double maxX = 0.0;
        double maxY = 0.0;
        for (Key key : this.keys) {
            this.minX = Math.min(this.minX, key.x);
            this.minY = Math.min(this.minY, key.y);
            maxX = Math.max(maxX, key.x + key.width);
            maxY = Math.max(maxY, key.y + key.height);
        }
        this.setSize((maxX - this.minX) * this.scale.get(), (maxY - this.minY) * this.scale.get());
    }

    private static String getShortName(String name) {
        return switch (name.toUpperCase(Locale.ROOT)) {
            case "LEFT SHIFT", "LSHIFT" -> "LSh";
            case "RIGHT SHIFT", "RSHIFT" -> "RSh";
            case "LEFT CONTROL", "LCTRL" -> "LCtrl";
            case "RIGHT CONTROL", "RCTRL" -> "RCtrl";
            case "LEFT ALT", "LALT" -> "LAlt";
            case "RIGHT ALT", "RALT" -> "RAlt";
            case "LEFT SUPER" -> "LSup";
            case "RIGHT SUPER" -> "RSup";
            case "GRAVE ACCENT" -> "`";
            case "COMMA" -> ",";
            case "DOT", "PERIOD" -> ".";
            case "SLASH" -> "/";
            case "APOSTROPHE" -> "'";
            case "BACKSPACE" -> "BS";
            case "ENTER" -> "Ent";
            case "SCROLL", "SCROLL LOCK" -> "ScrL";
            case "PRINT", "PRTSC", "PRINT SCREEN" -> "PrtS";
            case "PAUSE" -> "Paus";
            case "PAGEUP", "PAGE UP", "PGUP" -> "PgUp";
            case "PAGEDOWN", "PAGE DOWN", "PGDN" -> "PgDn";
            case "INSERT", "INS" -> "Ins";
            case "DELETE", "DEL" -> "Del";
            case "HOME" -> "Home";
            case "END" -> "End";
            case "ARROW UP", "UP" -> "Up";
            case "ARROW DOWN", "DOWN" -> "Dn";
            case "ARROW LEFT", "LEFT" -> "Lt";
            case "ARROW RIGHT", "RIGHT" -> "Rt";
            case "WORLD 1" -> "#";
            case "WORLD 2" -> "\\";
            case "UNKNOWN" -> "?";
            default -> name;
        };
    }

    @Override
    public void render(HudRenderer renderer) {
        if (this.keys.isEmpty()) {
            if (MeteorClient.mc.options != null) {
                this.onPresetChanged(this.preset.get());
            }
            return;
        }
        SettingColor mutableColor = new SettingColor();
        this.pressedColor.get().update();
        this.unpressedColor.get().update();
        this.textColor.get().update();
        this.borderColor.get().update();
        this.backgroundColor.get().update();
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, this.getWidth(), this.getHeight(), this.getColor(this.backgroundColor.get(), mutableColor));
        }
        double s = this.scale.get();
        InputConstants.Key guiKey = ((KeyMappingAccessor)KeyBinds.OPEN_GUI).meteor$getKey();
        for (Key key : this.keys) {
            if (key.matches(guiKey.getValue(), guiKey.getValue(), guiKey.getType() != InputConstants.Type.MOUSE) && key.isPressed != key.isNativelyPressed()) {
                key.update(key.isPressed ? KeyAction.Release : KeyAction.Press);
            }
            if (key instanceof IsoEnterKey) {
                IsoEnterKey isoEnter = (IsoEnterKey)key;
                isoEnter.render(this, renderer, s, mutableColor);
                continue;
            }
            meteordevelopment.meteorclient.utils.render.color.Color color = this.getKeyColor(key, mutableColor);
            double kX = (double)this.x + (key.x - this.minX) * s;
            double kY = (double)this.y + (key.y - this.minY) * s;
            double kW = key.width * s;
            double kH = key.height * s;
            renderer.quad(kX, kY, kW, kH, color);
            if (this.border.get().booleanValue()) {
                meteordevelopment.meteorclient.utils.render.color.Color bColor = this.getColor(this.borderColor.get(), mutableColor);
                double bw = this.borderWidth.get();
                renderer.quad(kX, kY, kW, bw, bColor);
                renderer.quad(kX, kY + kH - bw, kW, bw, bColor);
                renderer.quad(kX, kY, bw, kH, bColor);
                renderer.quad(kX + kW - bw, kY, bw, kH, bColor);
            }
            String text = key.getName();
            meteordevelopment.meteorclient.utils.render.color.Color txtColor = this.getColor(this.textColor.get(), mutableColor);
            double padding = 2.0 * s;
            double availableWidth = kW - padding;
            if (!key.showCps) {
                double textScale = Math.min(1.0, availableWidth / renderer.textWidth(text, 1.0));
                double textWidth = renderer.textWidth(text, textScale);
                double yText = kY + (kH - renderer.textHeight(false, textScale)) / 2.0;
                this.drawTextLine(renderer, text, textWidth, kX, yText, kW, textScale, txtColor);
                continue;
            }
            double topScale = Math.min(1.0, availableWidth / renderer.textWidth(text, 1.0));
            double topWidth = renderer.textWidth(text, topScale);
            double topHeight = renderer.textHeight(false, topScale);
            String cpsText = key.getCps() + " CPS";
            double botScale = Math.min(1.0, availableWidth / renderer.textWidth(cpsText, 1.0));
            double botWidth = renderer.textWidth(cpsText, botScale);
            double botHeight = renderer.textHeight(false, botScale);
            double totalHeight = topHeight + botHeight;
            double startY = kY + (kH - totalHeight) / 2.0;
            this.drawTextLine(renderer, text, topWidth, kX, startY, kW, topScale, txtColor);
            this.drawTextLine(renderer, cpsText, botWidth, kX, startY + topHeight, kW, botScale, txtColor);
        }
    }

    private void drawTextLine(HudRenderer renderer, String text, double textWidth, double x, double y, double w, double textScale, meteordevelopment.meteorclient.utils.render.color.Color color) {
        double s = this.scale.get();
        double padding = 2.0 * s;
        double xText = x + (w - textWidth) / 2.0;
        if (this.alignment.get() == Alignment.Left) {
            xText = x + padding;
        } else if (this.alignment.get() == Alignment.Right) {
            xText = x + w - padding - textWidth;
        }
        renderer.text(text, xText, y, color, false, textScale);
    }

    public static void fillTable(GuiTheme theme, WTable table, CustomKeyListSetting setting) {
        table.clear();
        Iterator it = ((List)setting.get()).iterator();
        while (it.hasNext()) {
            Key key = (Key)it.next();
            table.add(theme.label("Key")).expandWidgetX().widget().color(theme.textSecondaryColor());
            table.add(theme.label(String.format("(%s)", key.keybind))).expandWidgetX();
            WButton edit = table.add(theme.button(GuiRenderer.EDIT)).expandCellX().widget();
            edit.action = () -> {
                WidgetScreen screen = (WidgetScreen)MeteorClient.mc.screen;
                MeteorClient.mc.setScreen((Screen)new CustomKeySettingScreen(theme, setting, key, screen));
            };
            WMinus delete = table.add(theme.minus()).right().widget();
            delete.action = () -> {
                it.remove();
                setting.onChanged();
                KeyboardHud.fillTable(theme, table, setting);
            };
            table.row();
        }
        if (!((List)setting.get()).isEmpty()) {
            table.add(theme.horizontalSeparator()).expandX();
            table.row();
        }
        WButton add = table.add(theme.button("Add")).expandX().widget();
        add.action = () -> {
            Key newKey = new Key();
            if (!((List)setting.get()).isEmpty()) {
                Key lastKey = (Key)((List)setting.get()).getLast();
                newKey.x = lastKey.x + lastKey.width + 10.0;
                newKey.y = lastKey.y;
            }
            ((List)setting.get()).add(newKey);
            setting.onChanged();
            KeyboardHud.fillTable(theme, table, setting);
        };
        WButton reset = table.add(theme.button(GuiRenderer.RESET)).widget();
        reset.action = () -> {
            setting.reset();
            KeyboardHud.fillTable(theme, table, setting);
        };
        reset.tooltip = "Reset";
    }

    public static class Key {
        public String name = "";
        public KeyMapping binding;
        public Keybind keybind;
        public double x;
        public double y;
        public double width;
        public double height;
        public boolean showCps = false;
        private final RollingCps rollingCps = new RollingCps();
        private boolean isPressed;
        private float delta;

        public Key() {
            this.keybind = Keybind.fromKey(32);
            this.width = 60.0;
            this.height = 40.0;
        }

        public Key(CompoundTag compound) {
            this.keybind = Keybind.none().fromTag(compound.getCompoundOrEmpty("key"));
            this.name = compound.getStringOr("name", "");
            this.x = compound.getDoubleOr("x", 0.0);
            this.y = compound.getDoubleOr("y", 0.0);
            this.width = compound.getDoubleOr("width", 60.0);
            this.height = compound.getDoubleOr("height", 60.0);
            this.showCps = compound.getBooleanOr("showCps", false);
        }

        Key(KeyMapping binding, String name, double x, double y, double width, double height) {
            this.binding = binding;
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        Key(Keybind keybind, String name, double x, double y, double width, double height) {
            this.keybind = keybind;
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public Key setShowCps(boolean show) {
            this.showCps = show;
            return this;
        }

        public String getName() {
            if (this.name != null && !this.name.isEmpty()) {
                return this.name;
            }
            if (this.keybind != null) {
                return KeyboardHud.getShortName(this.keybind.toString());
            }
            if (this.binding != null) {
                return KeyboardHud.getShortName(this.binding.getTranslatedKeyMessage().getString());
            }
            return "?";
        }

        public boolean matches(int input, int scancode, boolean key) {
            boolean isKey;
            if (this.keybind != null) {
                return this.keybind.isKey() == key && this.keybind.getValue() == input;
            }
            InputConstants.Key inputKey = ((KeyMappingAccessor)this.binding).meteor$getKey();
            boolean bl = isKey = inputKey.getType() != InputConstants.Type.MOUSE;
            return isKey == key && inputKey.getType() == InputConstants.Type.SCANCODE ? scancode == inputKey.getValue() : input == inputKey.getValue();
        }

        public void update(KeyAction action) {
            if (action != KeyAction.Release) {
                this.isPressed = true;
                if (this.showCps && action == KeyAction.Press) {
                    this.rollingCps.add();
                }
            } else {
                this.isPressed = false;
            }
        }

        public boolean isNativelyPressed() {
            long window = MeteorClient.mc.getWindow().handle();
            if (this.keybind != null) {
                if (!this.keybind.isSet()) {
                    return false;
                }
                return this.keybind.isKey() ? GLFW.glfwGetKey((long)window, (int)this.keybind.getValue()) != 0 : GLFW.glfwGetMouseButton((long)window, (int)this.keybind.getValue()) != 0;
            }
            int key = ((KeyMappingAccessor)this.binding).meteor$getKey().getValue();
            return key >= 0 && key < 8 ? GLFW.glfwGetMouseButton((long)window, (int)key) != 0 : GLFW.glfwGetKey((long)window, (int)key) != 0;
        }

        public int getCps() {
            return this.rollingCps.get();
        }

        public CompoundTag serialize() {
            CompoundTag compound = new CompoundTag();
            compound.put("key", (Tag)this.keybind.toTag());
            compound.putString("name", this.name);
            compound.putDouble("x", this.x);
            compound.putDouble("y", this.y);
            compound.putDouble("width", this.width);
            compound.putDouble("height", this.height);
            compound.putBoolean("showCps", this.showCps);
            return compound;
        }
    }

    public static enum Preset {
        Movement,
        Clicks,
        Actions,
        Hotbar,
        Keyboard,
        Custom;

    }

    public class CustomKeyListSetting
    extends Setting<List<Key>> {
        public CustomKeyListSetting(KeyboardHud this$0) {
            Objects.requireNonNull(this$0);
            super("custom-keys", "Configure the custom keys display.", List.of(), list -> this$0.onPresetChanged(this$0.preset.get()), setting -> {}, () -> this$0.preset.get() == Preset.Custom);
        }

        @Override
        protected void resetImpl() {
            this.value = new ObjectArrayList();
            ((List)this.value).add(new Key());
        }

        @Override
        protected List<Key> parseImpl(String str) {
            return List.of();
        }

        @Override
        protected boolean isValueValid(List<Key> value) {
            return true;
        }

        @Override
        protected CompoundTag save(CompoundTag tag) {
            ListTag valueTag = new ListTag();
            for (Key key : (List)this.get()) {
                valueTag.add((Object)key.serialize());
            }
            tag.put("value", (Tag)valueTag);
            return tag;
        }

        @Override
        protected List<Key> load(CompoundTag tag) {
            ((List)this.get()).clear();
            for (Tag tagI : tag.getListOrEmpty("value")) {
                tagI.asCompound().ifPresent(CompoundTag2 -> ((List)this.get()).add(new Key((CompoundTag)CompoundTag2)));
            }
            return (List)this.get();
        }
    }

    public static enum KeyboardLayout {
        ANSI,
        ISO;

    }

    public static enum Alignment {
        Left,
        Center,
        Right;

    }

    public static class IsoEnterKey
    extends Key {
        private final double topBarStartX;

        public IsoEnterKey(Keybind keybind, double x, double y, double width, double height, double topBarStartX) {
            super(keybind, null, x, y, width, height);
            this.topBarStartX = topBarStartX;
        }

        public void render(KeyboardHud hud, HudRenderer renderer, double s, SettingColor mutableColor) {
            double kX = (double)hud.x + (this.x - hud.minX) * s;
            double kY = (double)hud.y + (this.y - hud.minY) * s;
            double kW = this.width * s;
            double kH = this.height * s;
            double u = 35.0 * s;
            meteordevelopment.meteorclient.utils.render.color.Color color = hud.getKeyColor(this, mutableColor);
            double stemRight = kX + kW;
            double topBarX = (double)hud.x + (this.topBarStartX - hud.minX) * s;
            double topBarLeftWidth = stemRight - topBarX - kW;
            if (topBarLeftWidth > 0.0) {
                renderer.quad(topBarX, kY, topBarLeftWidth, u, color);
            }
            renderer.quad(kX, kY, kW, u, color);
            renderer.quad(kX, kY + u, kW, kH - u, color);
            if (hud.border.get().booleanValue()) {
                meteordevelopment.meteorclient.utils.render.color.Color bColor = hud.getColor(hud.borderColor.get(), mutableColor);
                double bw = hud.borderWidth.get();
                double fullTopBarWidth = topBarLeftWidth + kW;
                renderer.quad(topBarX, kY, fullTopBarWidth, bw, bColor);
                renderer.quad(topBarX, kY, bw, u, bColor);
                renderer.quad(topBarX + fullTopBarWidth - bw, kY, bw, kH, bColor);
                renderer.quad(kX, kY + kH - bw, kW, bw, bColor);
                renderer.quad(kX, kY + u, bw, kH - u, bColor);
                if (topBarLeftWidth > 0.0) {
                    renderer.quad(topBarX, kY + u - bw, topBarLeftWidth, bw, bColor);
                }
            }
            String text = this.getName();
            meteordevelopment.meteorclient.utils.render.color.Color txtColor = hud.getColor(hud.textColor.get(), mutableColor);
            double padding = 2.0 * s;
            double availableWidth = kW - padding * 2.0;
            double availableHeight = kH - padding * 2.0;
            double tH = renderer.textHeight();
            double tW = renderer.textWidth(text);
            double widthScale = tW > availableWidth ? availableWidth / tW : 1.0;
            double heightScale = tH > availableHeight * 0.6 ? availableHeight * 0.6 / tH : 1.0;
            double textScale = Math.min(widthScale, heightScale);
            double yText = kY + (kH - tH * textScale) / 2.0;
            hud.drawTextLine(renderer, text, tW, kX, yText, kW, textScale, txtColor);
        }
    }

    public static class CustomKeySettingScreen
    extends WindowScreen {
        private final CustomKeyListSetting setting;
        private final Key key;
        private final WidgetScreen screen;

        public CustomKeySettingScreen(GuiTheme theme, CustomKeyListSetting setting, Key key, WidgetScreen screen) {
            super(theme, "Select Key");
            this.setting = setting;
            this.key = key;
            this.screen = screen;
        }

        @Override
        public void initWidgets() {
            Settings settings = new Settings();
            SettingGroup sgGeneral = settings.getDefaultGroup();
            sgGeneral.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("custom-key")).description("The key to display.")).defaultValue(Keybind.fromKey(32))).onChanged(k -> {
                this.key.keybind = k;
                this.screen.reload();
            })).onModuleActivated(setting -> setting.set(this.key.keybind))).build());
            sgGeneral.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("custom-label")).description("Replace the Key name with custom text.")).defaultValue("")).onChanged(s -> {
                this.key.name = s;
            })).onModuleActivated(setting -> setting.set(this.key.name))).build());
            sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("key-width")).description("Width of the key.")).defaultValue(60.0).min(20.0).sliderRange(20.0, 200.0).decimalPlaces(1).onChanged(d -> {
                this.key.width = d;
                this.setting.onChanged();
            })).onModuleActivated(setting -> setting.set(this.key.width))).build());
            sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("key-height")).description("Height of the key.")).defaultValue(40.0).min(20.0).sliderRange(20.0, 200.0).decimalPlaces(1).onChanged(d -> {
                this.key.height = d;
                this.setting.onChanged();
            })).onModuleActivated(setting -> setting.set(this.key.height))).build());
            sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("key-x")).description("X position offset of the key.")).defaultValue(0.0).sliderRange(-200.0, 200.0).decimalPlaces(1).onChanged(d -> {
                this.key.x = d;
                this.setting.onChanged();
            })).onModuleActivated(setting -> setting.set(this.key.x))).build());
            sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("key-y")).description("Y position offset of the key.")).defaultValue(0.0).sliderRange(-200.0, 200.0).decimalPlaces(1).onChanged(d -> {
                this.key.y = d;
                this.setting.onChanged();
            })).onModuleActivated(setting -> setting.set(this.key.y))).build());
            sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-cps")).description("Show CPS for this key.")).defaultValue(false)).onChanged(b -> {
                this.key.showCps = b;
            })).onModuleActivated(setting -> setting.set(this.key.showCps))).build());
            settings.onActivated();
            this.add(this.theme.settings(settings)).expandX();
        }
    }

    private static class RollingCps {
        private final LongList clicks = new LongArrayList();

        private RollingCps() {
        }

        public void add() {
            this.clicks.add(System.currentTimeMillis());
        }

        public int get() {
            long time = System.currentTimeMillis();
            this.clicks.removeIf(val -> val + 1000L < time);
            return this.clicks.size();
        }
    }
}
