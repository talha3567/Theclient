package net.wurstclient.hacks.chestesp.groups;

import java.awt.Color;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2646;
import net.wurstclient.hacks.chestesp.ChestEspBlockGroup;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.util.LootrModCompat;

public final class NormalChestsGroup
extends ChestEspBlockGroup {
    @Override
    protected CheckboxSetting createIncludeSetting() {
        return new CheckboxSetting("Include normal chests", true);
    }

    @Override
    protected ColorSetting createColorSetting() {
        return new ColorSetting("Chest color", "Normal chests will be highlighted in this color.", Color.GREEN);
    }

    @Override
    protected boolean matches(class_2586 be) {
        return be instanceof class_2595 && !(be instanceof class_2646) && !LootrModCompat.isLootrTrappedChest(be);
    }
}
