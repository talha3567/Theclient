package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1454;
import net.minecraft.class_1480;
import net.minecraft.class_5762;
import net.minecraft.class_9866;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterPassiveWaterSetting
extends EntityFilterCheckbox {
    private static final String EXCEPTIONS_TEXT = "\n\nThis filter does not affect guardians, drowned, and pufferfish.";

    public FilterPassiveWaterSetting(String description, boolean checked) {
        super("Filter passive water mobs", description + EXCEPTIONS_TEXT, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        if (e instanceof class_1454) {
            return true;
        }
        return !(e instanceof class_1480) && !(e instanceof class_9866) && !(e instanceof class_5762);
    }

    public static FilterPassiveWaterSetting genericCombat(boolean checked) {
        return new FilterPassiveWaterSetting("Won't attack passive water mobs like fish, squid, dolphins and axolotls.", checked);
    }

    public static FilterPassiveWaterSetting genericVision(boolean checked) {
        return new FilterPassiveWaterSetting("Won't show passive water mobs like fish, squid, dolphins and axolotls.", checked);
    }
}
