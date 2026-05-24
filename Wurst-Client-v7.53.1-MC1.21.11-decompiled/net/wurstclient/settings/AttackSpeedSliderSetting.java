package net.wurstclient.settings;

import net.minecraft.class_5819;
import net.wurstclient.WurstClient;
import net.wurstclient.settings.SliderSetting;

public final class AttackSpeedSliderSetting
extends SliderSetting {
    private final class_5819 random = class_5819.method_43053();
    private int tickTimer;

    public AttackSpeedSliderSetting() {
        this("Speed", "description.wurst.setting.generic.attack_speed");
    }

    public AttackSpeedSliderSetting(String name, String description) {
        super(name, description, 0.0, 0.0, 20.0, 0.1, SliderSetting.ValueDisplay.DECIMAL.withLabel(0.0, "auto"));
    }

    @Override
    public float[] getKnobColor() {
        if (this.getValue() == 0.0) {
            return new float[]{0.0f, 0.5f, 1.0f};
        }
        return super.getKnobColor();
    }

    public void resetTimer() {
        double value = this.getValue();
        this.tickTimer = value <= 0.0 ? -1 : (int)(1000.0 / value);
    }

    public void resetTimer(double maxRandMS) {
        if (maxRandMS <= 0.0) {
            this.resetTimer();
            return;
        }
        double value = this.getValue();
        double rand = this.random.method_43059();
        int randOffset = (int)(rand * maxRandMS);
        this.tickTimer = value <= 0.0 ? randOffset : (int)(1000.0 / value) + randOffset;
    }

    public void updateTimer() {
        if (this.tickTimer >= 0) {
            this.tickTimer -= 50;
        }
    }

    public boolean isTimeToAttack() {
        double value = this.getValue();
        if (value <= 0.0 && WurstClient.MC.field_1724.method_7261(0.0f) < 1.0f) {
            return false;
        }
        return this.tickTimer <= 0;
    }
}
