package net.wurstclient.hacks;

import net.minecraft.class_437;
import net.minecraft.class_465;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags(value={"no background", "NoGuiBackground", "no gui background", "NoGradient", "no gradient"})
public final class NoBackgroundHack
extends Hack {
    public final CheckboxSetting allGuis = new CheckboxSetting("All GUIs", "Removes the background for all GUIs, not just inventories.", false);

    public NoBackgroundHack() {
        super("NoBackground");
        this.setCategory(Category.RENDER);
        this.addSetting(this.allGuis);
    }

    public boolean shouldCancelBackground(class_437 screen) {
        if (!this.isEnabled()) {
            return false;
        }
        if (NoBackgroundHack.MC.field_1687 == null) {
            return false;
        }
        return this.allGuis.isChecked() || screen instanceof class_465;
    }
}
