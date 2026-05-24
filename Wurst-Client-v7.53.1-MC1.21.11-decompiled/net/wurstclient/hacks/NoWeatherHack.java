package net.wurstclient.hacks;

import net.minecraft.class_12131;
import net.wurstclient.Category;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;

public final class NoWeatherHack
extends Hack {
    private final CheckboxSetting disableRain = new CheckboxSetting("Disable Rain", true);
    private final CheckboxSetting changeTime = new CheckboxSetting("Change World Time", false);
    private final SliderSetting time = new SliderSetting("Time", 6000.0, 0.0, 23900.0, 100.0, SliderSetting.ValueDisplay.INTEGER);
    private final CheckboxSetting changeMoonPhase = new CheckboxSetting("Change Moon Phase", false);
    private final SliderSetting moonPhase = new SliderSetting("Moon Phase", 0.0, 0.0, 7.0, 1.0, SliderSetting.ValueDisplay.INTEGER);

    public NoWeatherHack() {
        super("NoWeather");
        this.setCategory(Category.RENDER);
        this.addSetting(this.disableRain);
        this.addSetting(this.changeTime);
        this.addSetting(this.time);
        this.addSetting(this.changeMoonPhase);
        this.addSetting(this.moonPhase);
    }

    public boolean isRainDisabled() {
        return this.isEnabled() && this.disableRain.isChecked();
    }

    public boolean isTimeChanged() {
        return this.isEnabled() && this.changeTime.isChecked();
    }

    public long getChangedTime() {
        return this.time.getValueI();
    }

    public boolean isMoonPhaseChanged() {
        return this.isEnabled() && this.changeMoonPhase.isChecked();
    }

    public class_12131 getChangedMoonPhase() {
        return class_12131.values()[this.moonPhase.getValueI()];
    }
}
