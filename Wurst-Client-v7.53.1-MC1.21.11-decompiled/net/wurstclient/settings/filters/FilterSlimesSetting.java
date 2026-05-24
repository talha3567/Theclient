package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1589;
import net.minecraft.class_1621;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterSlimesSetting
extends EntityFilterCheckbox {
    private static final String EXCEPTIONS_TEXT = "\n\nThis filter does not affect magma cubes.";

    public FilterSlimesSetting(String description, boolean checked) {
        super("Filter slimes", description + EXCEPTIONS_TEXT, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        return !(e instanceof class_1621) || e instanceof class_1589;
    }

    public static FilterSlimesSetting genericCombat(boolean checked) {
        return new FilterSlimesSetting("Won't attack slimes.", checked);
    }

    public static FilterSlimesSetting genericVision(boolean checked) {
        return new FilterSlimesSetting("Won't show slimes.", checked);
    }
}
