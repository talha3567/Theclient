package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_3988;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterVillagersSetting
extends EntityFilterCheckbox {
    public FilterVillagersSetting(String description, boolean checked) {
        super("Filter villagers", description, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        return !(e instanceof class_3988);
    }

    public static FilterVillagersSetting genericCombat(boolean checked) {
        return new FilterVillagersSetting("description.wurst.setting.generic.filter_villagers_combat", checked);
    }

    public static FilterVillagersSetting genericVision(boolean checked) {
        return new FilterVillagersSetting("description.wurst.setting.generic.filter_villagers_vision", checked);
    }
}
