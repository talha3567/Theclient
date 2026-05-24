package net.wurstclient.hacks.chestesp.groups;

import java.awt.Color;
import net.minecraft.class_2586;
import net.minecraft.class_8172;
import net.wurstclient.hacks.chestesp.ChestEspBlockGroup;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;

public final class PotsGroup
extends ChestEspBlockGroup {
    @Override
    protected CheckboxSetting createIncludeSetting() {
        return new CheckboxSetting("Include pots", false);
    }

    @Override
    protected ColorSetting createColorSetting() {
        return new ColorSetting("Pots color", "Decorated pots will be highlighted in this color.", Color.GREEN);
    }

    @Override
    protected boolean matches(class_2586 be) {
        return be instanceof class_8172;
    }
}
