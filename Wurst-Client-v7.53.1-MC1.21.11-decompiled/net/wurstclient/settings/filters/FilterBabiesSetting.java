package net.wurstclient.settings.filters;

import net.minecraft.class_1296;
import net.minecraft.class_1297;
import net.minecraft.class_1569;
import net.minecraft.class_7110;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterBabiesSetting
extends EntityFilterCheckbox {
    private static final String EXCEPTIONS_TEXT = "\n\nThis filter does not affect baby zombies and other hostile baby mobs.";

    public FilterBabiesSetting(String description, boolean checked) {
        super("Filter babies", description + EXCEPTIONS_TEXT, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        class_1296 pe;
        if (e instanceof class_1569) {
            return true;
        }
        if (e instanceof class_1296 && (pe = (class_1296)e).method_6109()) {
            return false;
        }
        return !(e instanceof class_7110);
    }

    public static FilterBabiesSetting genericCombat(boolean checked) {
        return new FilterBabiesSetting("Won't attack baby pigs, baby villagers, etc.", checked);
    }
}
