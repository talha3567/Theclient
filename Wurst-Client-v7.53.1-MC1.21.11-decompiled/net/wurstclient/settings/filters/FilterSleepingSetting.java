package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_4050;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterSleepingSetting
extends EntityFilterCheckbox {
    public FilterSleepingSetting(String description, boolean checked) {
        super("Filter sleeping", description, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        if (!(e instanceof class_1657)) {
            return true;
        }
        class_1657 pe = (class_1657)e;
        return !pe.method_6113() && pe.method_18376() != class_4050.field_18078;
    }

    public static FilterSleepingSetting genericCombat(boolean checked) {
        return new FilterSleepingSetting("description.wurst.setting.generic.filter_sleeping_combat", checked);
    }

    public static FilterSleepingSetting genericVision(boolean checked) {
        return new FilterSleepingSetting("description.wurst.setting.generic.filter_sleeping_vision", checked);
    }
}
