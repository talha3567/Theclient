package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1688;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterMinecartsSetting
extends EntityFilterCheckbox {
    public FilterMinecartsSetting(String description, boolean checked) {
        super("Filter minecarts", description, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        return !(e instanceof class_1688);
    }
}
