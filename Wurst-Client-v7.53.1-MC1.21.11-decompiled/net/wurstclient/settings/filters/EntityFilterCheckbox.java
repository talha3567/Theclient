package net.wurstclient.settings.filters;

import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.filterlists.EntityFilterList;

public abstract class EntityFilterCheckbox
extends CheckboxSetting
implements EntityFilterList.EntityFilter {
    public EntityFilterCheckbox(String name, String description, boolean checked) {
        super(name, description, checked);
    }

    @Override
    public final boolean isFilterEnabled() {
        return this.isChecked();
    }

    @Override
    public final Setting getSetting() {
        return this;
    }
}
