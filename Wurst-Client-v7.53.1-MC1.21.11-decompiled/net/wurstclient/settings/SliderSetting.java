package net.wurstclient.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.text.DecimalFormat;
import java.util.LinkedHashSet;
import net.minecraft.class_3532;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.components.SliderComponent;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderLock;
import net.wurstclient.util.MathUtils;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.text.WText;

public class SliderSetting
extends Setting
implements SliderLock {
    private double value;
    private final double defaultValue;
    private final double minimum;
    private final double maximum;
    private final double increment;
    private final ValueDisplay display;
    private SliderLock lock;
    private boolean disabled;
    private double usableMin;
    private double usableMax;

    public SliderSetting(String name, WText description, double value, double minimum, double maximum, double increment, ValueDisplay display) {
        super(name, description);
        this.value = value;
        this.defaultValue = value;
        this.minimum = minimum;
        this.maximum = maximum;
        this.usableMin = minimum;
        this.usableMax = maximum;
        this.increment = increment;
        this.display = display;
    }

    public SliderSetting(String name, String descriptionKey, double value, double minimum, double maximum, double increment, ValueDisplay display) {
        this(name, WText.translated(descriptionKey, new Object[0]), value, minimum, maximum, increment, display);
    }

    public SliderSetting(String name, double value, double minimum, double maximum, double increment, ValueDisplay display) {
        this(name, WText.empty(), value, minimum, maximum, increment, display);
    }

    @Override
    public final double getValue() {
        double value = this.isLocked() ? this.lock.getValue() : this.value;
        return MathUtils.clamp(value, this.usableMin, this.usableMax);
    }

    public final double getValueSq() {
        return class_3532.method_33723((double)this.getValue());
    }

    public final float getValueF() {
        return (float)this.getValue();
    }

    public final int getValueI() {
        return (int)this.getValue();
    }

    public final int getValueCeil() {
        return class_3532.method_15384((double)this.getValue());
    }

    public final int getValueLog() {
        return (int)Math.pow(10.0, this.getValueI());
    }

    public final double getDefaultValue() {
        return this.defaultValue;
    }

    public final String getValueString() {
        return this.display.getValueString(this.getValue());
    }

    public final void setValue(double value) {
        if (this.disabled || this.isLocked()) {
            return;
        }
        this.setValueIgnoreLock(value);
    }

    private void setValueIgnoreLock(double value) {
        value = (double)((int)Math.round(value / this.increment)) * this.increment;
        this.value = value = MathUtils.clamp(value, this.usableMin, this.usableMax);
        this.update();
        WurstClient.INSTANCE.saveSettings();
    }

    public final void increaseValue() {
        this.setValue(this.getValue() + this.increment);
    }

    public final void decreaseValue() {
        this.setValue(this.getValue() - this.increment);
    }

    public final double getMinimum() {
        return this.minimum;
    }

    public final double getMaximum() {
        return this.maximum;
    }

    public final double getRange() {
        return this.maximum - this.minimum;
    }

    public final double getIncrement() {
        return this.increment;
    }

    public final double getPercentage() {
        return (this.getValue() - this.minimum) / this.getRange();
    }

    public float[] getKnobColor() {
        float f = (float)(2.0 * this.getPercentage());
        float red = MathUtils.clamp(f, 0.0f, 1.0f);
        float green = MathUtils.clamp(2.0f - f, 0.0f, 1.0f);
        float blue = 0.0f;
        return new float[]{red, green, blue};
    }

    public final boolean isLocked() {
        return this.lock != null;
    }

    public final void lock(SliderLock lock) {
        this.lock = lock;
        this.update();
    }

    public final void unlock() {
        this.lock = null;
        this.update();
    }

    public final boolean isDisabled() {
        return this.disabled;
    }

    public final void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public final boolean isLimited() {
        return this.usableMax != this.maximum || this.usableMin != this.minimum;
    }

    public final double getUsableMin() {
        return this.usableMin;
    }

    public final void setUsableMin(double usableMin) {
        if (usableMin < this.minimum) {
            throw new IllegalArgumentException("usableMin < minimum");
        }
        this.usableMin = usableMin;
        this.update();
    }

    public final void resetUsableMin() {
        this.usableMin = this.minimum;
        this.update();
    }

    public final double getUsableMax() {
        return this.usableMax;
    }

    public final void setUsableMax(double usableMax) {
        if (usableMax > this.maximum) {
            throw new IllegalArgumentException("usableMax > maximum");
        }
        this.usableMax = usableMax;
        this.update();
    }

    public final void resetUsableMax() {
        this.usableMax = this.maximum;
        this.update();
    }

    @Override
    public final Component getComponent() {
        return new SliderComponent(this);
    }

    @Override
    public final void fromJson(JsonElement json) {
        if (!JsonUtils.isNumber(json)) {
            return;
        }
        double value = json.getAsDouble();
        if (value > this.maximum || value < this.minimum) {
            return;
        }
        this.setValueIgnoreLock(value);
    }

    @Override
    public final JsonElement toJson() {
        return new JsonPrimitive((double)Math.round(this.value * 1000000.0) / 1000000.0);
    }

    @Override
    public JsonObject exportWikiData() {
        JsonObject json = new JsonObject();
        json.addProperty("name", this.getName());
        json.addProperty("description", this.getDescription());
        json.addProperty("type", "Slider");
        json.addProperty("defaultValue", this.defaultValue);
        json.addProperty("minimum", this.minimum);
        json.addProperty("maximum", this.maximum);
        json.addProperty("increment", this.increment);
        return json;
    }

    public final LinkedHashSet<PossibleKeybind> getPossibleKeybinds(String featureName) {
        String fullName = featureName + " " + this.getName();
        String command = ".setslider " + featureName.toLowerCase() + " ";
        command = command + this.getName().toLowerCase().replace(" ", "_") + " ";
        LinkedHashSet<PossibleKeybind> pkb = new LinkedHashSet<PossibleKeybind>();
        pkb.add(new PossibleKeybind(command + "more", "Increase " + fullName));
        pkb.add(new PossibleKeybind(command + "less", "Decrease " + fullName));
        return pkb;
    }

    public static interface ValueDisplay {
        public static final ValueDisplay INTEGER = v -> "" + (int)v;
        public static final ValueDisplay DECIMAL = v -> {
            String s = "" + (double)Math.round(v * 1000000.0) / 1000000.0;
            return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
        };
        public static final ValueDisplay PERCENTAGE = v -> (int)((double)Math.round(v * 1.0E8) / 1000000.0) + "%";
        public static final ValueDisplay LOGARITHMIC = new ValueDisplay(){
            private static final DecimalFormat FORMAT = new DecimalFormat("#,###");

            @Override
            public String getValueString(double v) {
                return FORMAT.format(Math.pow(10.0, v));
            }
        };
        public static final ValueDisplay DEGREES = INTEGER.withSuffix("\u00b0");
        public static final ValueDisplay ROUNDING_PRECISION = v -> (int)v == 0 ? "1" : "0." + "0".repeat((int)v - 1) + "1";
        public static final ValueDisplay AREA_FROM_RADIUS = v -> {
            int d = 2 * (int)v + 1;
            return d + "x" + d;
        };
        public static final ValueDisplay NONE = v -> "";

        public String getValueString(double var1);

        default public ValueDisplay withLabel(double value, String label) {
            return v -> v == value ? label : this.getValueString(v);
        }

        default public ValueDisplay withPrefix(String prefix) {
            return v -> prefix + this.getValueString(v);
        }

        default public ValueDisplay withSuffix(String suffix) {
            return v -> this.getValueString(v) + suffix;
        }
    }
}
