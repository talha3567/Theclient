package net.wurstclient.hacks.chestesp.groups;

import java.awt.Color;
import net.minecraft.class_2586;
import net.minecraft.class_2601;
import net.minecraft.class_2608;
import net.wurstclient.hacks.chestesp.ChestEspBlockGroup;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;

public final class DispensersGroup
extends ChestEspBlockGroup {
    @Override
    protected CheckboxSetting createIncludeSetting() {
        return new CheckboxSetting("Include dispensers", false);
    }

    @Override
    protected ColorSetting createColorSetting() {
        return new ColorSetting("Dispenser color", "Dispensers will be highlighted in this color.", new Color(0xFF8000));
    }

    @Override
    protected boolean matches(class_2586 be) {
        return be instanceof class_2601 && !(be instanceof class_2608);
    }
}
