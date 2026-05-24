package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.wurstclient.WurstClient;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.filterlists.EntityFilterList;

public final class FilterFlyingSetting
extends SliderSetting
implements EntityFilterList.EntityFilter {
    public FilterFlyingSetting(String description, double value) {
        super("Filter flying", description, value, 0.0, 2.0, 0.05, SliderSetting.ValueDisplay.DECIMAL.withLabel(0.0, "off"));
    }

    @Override
    public boolean test(class_1297 e) {
        if (!(e instanceof class_1657)) {
            return true;
        }
        class_238 box = e.method_5829();
        return !WurstClient.MC.field_1687.method_18026(box = box.method_991(box.method_989(0.0, -this.getValue(), 0.0)));
    }

    @Override
    public boolean isFilterEnabled() {
        return this.getValue() > 0.0;
    }

    @Override
    public Setting getSetting() {
        return this;
    }

    public static FilterFlyingSetting genericCombat(double value) {
        return new FilterFlyingSetting("description.wurst.setting.generic.filter_flying_combat", value);
    }
}
