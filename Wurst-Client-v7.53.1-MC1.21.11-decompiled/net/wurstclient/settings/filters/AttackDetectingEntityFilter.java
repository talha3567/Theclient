package net.wurstclient.settings.filters;

import java.util.function.Supplier;
import net.minecraft.class_1297;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.filterlists.EntityFilterList;

public abstract class AttackDetectingEntityFilter
implements EntityFilterList.EntityFilter {
    private final Setting setting;
    private final Supplier<Mode> mode;

    protected AttackDetectingEntityFilter(String name, String description, Mode selected, boolean checked) {
        if (selected == null) {
            CheckboxSetting cbSetting = new CheckboxSetting(name, description, checked);
            this.setting = cbSetting;
            this.mode = () -> cbSetting.isChecked() ? Mode.ON : Mode.OFF;
        } else {
            EnumSetting enumSetting = new EnumSetting(name, description, (Enum[])Mode.values(), (Enum)selected);
            this.setting = enumSetting;
            this.mode = () -> (Mode)((Object)((Object)enumSetting.getSelected()));
        }
    }

    public abstract boolean onTest(class_1297 var1);

    public abstract boolean ifCalmTest(class_1297 var1);

    @Override
    public final boolean test(class_1297 e) {
        return this.mode.get() == Mode.IF_CALM ? this.ifCalmTest(e) : this.onTest(e);
    }

    @Override
    public final boolean isFilterEnabled() {
        return this.mode.get() != Mode.OFF;
    }

    @Override
    public final Setting getSetting() {
        return this.setting;
    }

    public static enum Mode {
        ON("On"),
        IF_CALM("If calm"),
        OFF("Off");

        private final String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}
