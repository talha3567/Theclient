package net.wurstclient.hacks.autofish;

import net.minecraft.class_1536;
import net.wurstclient.WurstClient;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.ChatUtils;

public class ShallowWaterWarningCheckbox
extends CheckboxSetting {
    private boolean hasAlreadyWarned;

    public ShallowWaterWarningCheckbox() {
        super("Shallow water warning", "Displays a warning message in chat when you are fishing in shallow water.", true);
    }

    public void reset() {
        this.hasAlreadyWarned = false;
    }

    public void checkWaterType() {
        class_1536 bobber = WurstClient.MC.field_1724.field_7513;
        if (bobber.method_26086(bobber.method_24515())) {
            this.hasAlreadyWarned = false;
            return;
        }
        if (this.isChecked() && !this.hasAlreadyWarned) {
            ChatUtils.warning("You are currently fishing in shallow water.");
            ChatUtils.message("You can't get any treasure items while fishing like this.");
            if (!WurstClient.INSTANCE.getHax().openWaterEspHack.isEnabled()) {
                ChatUtils.message("Use OpenWaterESP to find open water.");
            }
            this.hasAlreadyWarned = true;
        }
    }
}
