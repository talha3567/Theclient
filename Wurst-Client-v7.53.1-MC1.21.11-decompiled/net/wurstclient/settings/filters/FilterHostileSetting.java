package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1569;
import net.minecraft.class_4836;
import net.minecraft.class_5354;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterHostileSetting
extends EntityFilterCheckbox {
    private static final String EXCEPTIONS_TEXT = "\n\nThis filter does not affect endermen, non-brute piglins, and zombified piglins.";

    public FilterHostileSetting(String description, boolean checked) {
        super("Filter hostile mobs", description + EXCEPTIONS_TEXT, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        if (e instanceof class_5354 || e instanceof class_4836) {
            return true;
        }
        return !(e instanceof class_1569);
    }

    public static FilterHostileSetting genericCombat(boolean checked) {
        return new FilterHostileSetting("Won't attack hostile mobs like zombies and creepers.", checked);
    }

    public static FilterHostileSetting genericVision(boolean checked) {
        return new FilterHostileSetting("Won't show hostile mobs like zombies and creepers.", checked);
    }
}
