package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;

public final class TimerHack
extends Hack {
    private final SliderSetting speed = new SliderSetting("Speed", 2.0, 0.1, 20.0, 0.1, SliderSetting.ValueDisplay.DECIMAL);

    public TimerHack() {
        super("Timer");
        this.setCategory(Category.OTHER);
        this.addSetting(this.speed);
    }

    @Override
    public String getRenderName() {
        return this.getName() + " [" + this.speed.getValueString() + "]";
    }

    public float getTimerSpeed() {
        return this.isEnabled() ? this.speed.getValueF() : 1.0f;
    }
}
