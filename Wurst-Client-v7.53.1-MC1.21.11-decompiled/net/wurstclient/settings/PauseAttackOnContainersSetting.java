package net.wurstclient.settings;

import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_481;
import net.minecraft.class_490;
import net.wurstclient.WurstClient;
import net.wurstclient.settings.CheckboxSetting;

public final class PauseAttackOnContainersSetting
extends CheckboxSetting {
    public PauseAttackOnContainersSetting(boolean checked) {
        super("Pause on containers", "description.wurst.setting.generic.pause_attack_on_containers", checked);
    }

    public PauseAttackOnContainersSetting(String name, String description, boolean checked) {
        super(name, description, checked);
    }

    public boolean shouldPause() {
        if (!this.isChecked()) {
            return false;
        }
        class_437 screen = WurstClient.MC.field_1755;
        return screen instanceof class_465 && !(screen instanceof class_490) && !(screen instanceof class_481);
    }
}
