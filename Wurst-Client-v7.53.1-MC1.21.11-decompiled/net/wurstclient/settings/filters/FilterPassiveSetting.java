package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1421;
import net.minecraft.class_1429;
import net.minecraft.class_1454;
import net.minecraft.class_1480;
import net.minecraft.class_1569;
import net.minecraft.class_5354;
import net.minecraft.class_9866;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterPassiveSetting
extends EntityFilterCheckbox {
    private static final String EXCEPTIONS_TEXT = "\n\nThis filter does not affect wolves, bees, polar bears, pufferfish, and villagers.";

    public FilterPassiveSetting(String description, boolean checked) {
        super("Filter passive mobs", description + EXCEPTIONS_TEXT, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        if (e instanceof class_1569) {
            return true;
        }
        if (e instanceof class_5354 || e instanceof class_1454) {
            return true;
        }
        return !(e instanceof class_1429) && !(e instanceof class_1421) && !(e instanceof class_1480) && !(e instanceof class_9866);
    }

    public static FilterPassiveSetting genericCombat(boolean checked) {
        return new FilterPassiveSetting("Won't attack animals like pigs and cows, ambient mobs like bats, and water mobs like fish, squid and dolphins.", checked);
    }

    public static FilterPassiveSetting genericVision(boolean checked) {
        return new FilterPassiveSetting("Won't show animals like pigs and cows, ambient mobs like bats, and water mobs like fish, squid and dolphins.", checked);
    }
}
