package net.wurstclient.other_features;

import net.minecraft.class_1041;
import net.minecraft.class_2561;
import net.minecraft.class_3675;
import net.minecraft.class_7172;
import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.events.MouseScrollListener;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.TextFieldSetting;
import net.wurstclient.util.MathUtils;

@SearchTags(value={"telescope", "optifine"})
@DontBlock
public final class ZoomOtf
extends OtherFeature
implements MouseScrollListener {
    private final SliderSetting level = new SliderSetting("Zoom level", 3.0, 1.0, 50.0, 0.1, SliderSetting.ValueDisplay.DECIMAL.withSuffix("x"));
    private final CheckboxSetting scroll = new CheckboxSetting("Use mouse wheel", "If enabled, you can use the mouse wheel while zooming to zoom in even further.", true);
    private final CheckboxSetting zoomInScreens = new CheckboxSetting("Zoom in screens", "If enabled, you can also zoom while a screen (chat, inventory, etc.) is open.", false);
    private final TextFieldSetting keybind = new TextFieldSetting("Keybind", "Determines the zoom keybind.\n\nInstead of editing this value manually, you should go to Wurst Options -> Zoom and set it there.", "key.keyboard.v", this::isValidKeybind);
    private Double currentLevel;
    private Double defaultMouseSensitivity;

    public ZoomOtf() {
        super("Zoom", "Allows you to zoom in.\nBy default, the zoom is activated by pressing the \u00a7lV\u00a7r key.\nGo to Wurst Options -> Zoom to change this keybind.");
        this.addSetting(this.level);
        this.addSetting(this.scroll);
        this.addSetting(this.zoomInScreens);
        this.addSetting(this.keybind);
        EVENTS.add(MouseScrollListener.class, this);
    }

    public float changeFovBasedOnZoom(float fov) {
        class_7172 mouseSensitivitySetting = ZoomOtf.MC.field_1690.method_42495();
        if (this.currentLevel == null) {
            this.currentLevel = this.level.getValue();
        }
        if (!this.isZoomKeyPressed()) {
            this.currentLevel = this.level.getValue();
            if (this.defaultMouseSensitivity != null) {
                mouseSensitivitySetting.method_41748((Object)this.defaultMouseSensitivity);
                this.defaultMouseSensitivity = null;
            }
            return fov;
        }
        if (this.defaultMouseSensitivity == null) {
            this.defaultMouseSensitivity = (Double)mouseSensitivitySetting.method_41753();
        }
        mouseSensitivitySetting.method_41748((Object)(this.defaultMouseSensitivity * (1.0 / this.currentLevel)));
        return (float)((double)fov / this.currentLevel);
    }

    @Override
    public void onMouseScroll(double amount) {
        if (!this.isZoomKeyPressed() || !this.scroll.isChecked()) {
            return;
        }
        if (this.currentLevel == null) {
            this.currentLevel = this.level.getValue();
        }
        if (amount > 0.0) {
            this.currentLevel = this.currentLevel * 1.1;
        } else if (amount < 0.0) {
            this.currentLevel = this.currentLevel * 0.9;
        }
        this.currentLevel = MathUtils.clamp(this.currentLevel, this.level.getMinimum(), this.level.getMaximum());
    }

    public boolean isControllingScrollEvents() {
        return this.isZoomKeyPressed() && this.scroll.isChecked();
    }

    public class_2561 getTranslatedKeybindName() {
        return class_3675.method_15981((String)this.keybind.getValue()).method_27445();
    }

    public void setBoundKey(String translationKey) {
        this.keybind.setValue(translationKey);
    }

    private boolean isZoomKeyPressed() {
        if (ZoomOtf.MC.field_1755 != null && !this.zoomInScreens.isChecked()) {
            return false;
        }
        return class_3675.method_15987((class_1041)MC.method_22683(), (int)class_3675.method_15981((String)this.keybind.getValue()).method_1444());
    }

    private boolean isValidKeybind(String keybind) {
        try {
            return class_3675.method_15981((String)keybind) != null;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }

    public SliderSetting getLevelSetting() {
        return this.level;
    }

    public CheckboxSetting getScrollSetting() {
        return this.scroll;
    }
}
