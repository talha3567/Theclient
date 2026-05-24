package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1427;
import net.minecraft.class_1606;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterGolemsSetting
extends EntityFilterCheckbox {
    public FilterGolemsSetting(String description, boolean checked) {
        super("Filter golems", description, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        return !(e instanceof class_1427) || e instanceof class_1606;
    }

    public static FilterGolemsSetting genericCombat(boolean checked) {
        return new FilterGolemsSetting("description.wurst.setting.generic.filter_golems_combat", checked);
    }

    public static FilterGolemsSetting genericVision(boolean checked) {
        return new FilterGolemsSetting("description.wurst.setting.generic.filter_golems_vision", checked);
    }
}
