package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_4836;
import net.wurstclient.settings.filters.AttackDetectingEntityFilter;

public final class FilterPiglinsSetting
extends AttackDetectingEntityFilter {
    private static final String EXCEPTIONS_TEXT = "\n\nThis filter does not affect piglin brutes.";

    private FilterPiglinsSetting(String description, AttackDetectingEntityFilter.Mode selected, boolean checked) {
        super("Filter piglins", description + EXCEPTIONS_TEXT, selected, checked);
    }

    public FilterPiglinsSetting(String description, AttackDetectingEntityFilter.Mode selected) {
        this(description, selected, false);
    }

    @Override
    public boolean onTest(class_1297 e) {
        return !(e instanceof class_4836);
    }

    @Override
    public boolean ifCalmTest(class_1297 e) {
        class_4836 pe;
        return !(e instanceof class_4836) || (pe = (class_4836)e).method_6510();
    }

    public static FilterPiglinsSetting genericCombat(AttackDetectingEntityFilter.Mode selected) {
        return new FilterPiglinsSetting("When set to \u00a7lOn\u00a7r, piglins won't be attacked at all.\n\nWhen set to \u00a7lIf calm\u00a7r, piglins won't be attacked until they attack first. Be warned that this filter cannot detect if the piglins are attacking you or someone else.\n\nWhen set to \u00a7lOff\u00a7r, this filter does nothing and piglins can be attacked.", selected);
    }

    public static FilterPiglinsSetting genericVision(AttackDetectingEntityFilter.Mode selected) {
        return new FilterPiglinsSetting("When set to \u00a7lOn\u00a7r, piglins won't be shown at all.\n\nWhen set to \u00a7lIf calm\u00a7r, piglins won't be shown until they attack something.\n\nWhen set to \u00a7lOff\u00a7r, this filter does nothing and piglins can be shown.", selected);
    }

    public static FilterPiglinsSetting onOffOnly(String description, boolean onByDefault) {
        return new FilterPiglinsSetting(description, null, onByDefault);
    }
}
