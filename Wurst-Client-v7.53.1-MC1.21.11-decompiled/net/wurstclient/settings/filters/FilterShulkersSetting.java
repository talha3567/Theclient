package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1606;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterShulkersSetting
extends EntityFilterCheckbox {
    public FilterShulkersSetting(String description, boolean checked) {
        super("Filter shulkers", description, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        return !(e instanceof class_1606);
    }

    public static FilterShulkersSetting genericCombat(boolean checked) {
        return new FilterShulkersSetting("description.wurst.setting.generic.filter_shulkers_combat", checked);
    }

    public static FilterShulkersSetting genericVision(boolean checked) {
        return new FilterShulkersSetting("description.wurst.setting.generic.filter_shulkers_vision", checked);
    }
}
