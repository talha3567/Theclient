package net.wurstclient.hacks;

import net.minecraft.class_640;
import net.minecraft.class_742;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;

@SearchTags(value={"name protect"})
public final class NameProtectHack
extends Hack {
    public NameProtectHack() {
        super("NameProtect");
        this.setCategory(Category.RENDER);
    }

    public String protect(String string) {
        String name;
        if (!this.isEnabled() || NameProtectHack.MC.field_1724 == null) {
            return string;
        }
        String me = MC.method_1548().method_1676();
        if (string.contains(me)) {
            return string.replace(me, "\u00a7oMe\u00a7r");
        }
        int i = 0;
        for (class_640 info : NameProtectHack.MC.field_1724.field_3944.method_2880()) {
            ++i;
            name = info.method_2966().name().replaceAll("\u00a7(?:\\w|\\d)", "");
            if (!string.contains(name)) continue;
            return string.replace(name, "\u00a7oPlayer" + i + "\u00a7r");
        }
        for (class_742 player : NameProtectHack.MC.field_1687.method_18456()) {
            ++i;
            name = player.method_5477().getString();
            if (!string.contains(name)) continue;
            return string.replace(name, "\u00a7oPlayer" + i + "\u00a7r");
        }
        return string;
    }
}
