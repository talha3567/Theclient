package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterPlayersSetting
extends EntityFilterCheckbox {
    public FilterPlayersSetting(String description, boolean checked) {
        super("Filter players", description, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        return !(e instanceof class_1657);
    }

    public static FilterPlayersSetting genericCombat(boolean checked) {
        return new FilterPlayersSetting("description.wurst.setting.generic.filter_players_combat", checked);
    }

    public static FilterPlayersSetting genericVision(boolean checked) {
        return new FilterPlayersSetting("description.wurst.setting.generic.filter_players_vision", checked);
    }
}
