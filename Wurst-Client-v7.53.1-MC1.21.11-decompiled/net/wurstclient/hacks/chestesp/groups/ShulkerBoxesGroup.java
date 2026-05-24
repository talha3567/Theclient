package net.wurstclient.hacks.chestesp.groups;

import java.awt.Color;
import net.minecraft.class_2586;
import net.minecraft.class_2627;
import net.wurstclient.hacks.chestesp.ChestEspBlockGroup;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.util.LootrModCompat;

public final class ShulkerBoxesGroup
extends ChestEspBlockGroup {
    @Override
    protected CheckboxSetting createIncludeSetting() {
        return new CheckboxSetting("Include shulkers", true);
    }

    @Override
    protected ColorSetting createColorSetting() {
        return new ColorSetting("Shulker color", "Shulker boxes will be highlighted in this color.", Color.MAGENTA);
    }

    @Override
    protected boolean matches(class_2586 be) {
        return be instanceof class_2627 || LootrModCompat.isLootrShulkerBox(be);
    }
}
