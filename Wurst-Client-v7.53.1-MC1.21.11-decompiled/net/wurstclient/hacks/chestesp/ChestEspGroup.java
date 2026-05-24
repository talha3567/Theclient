package net.wurstclient.hacks.chestesp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.class_238;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.Setting;

public abstract class ChestEspGroup {
    protected final ArrayList<class_238> boxes = new ArrayList();
    private final ColorSetting color;
    private final CheckboxSetting enabled = this.createIncludeSetting();

    public ChestEspGroup() {
        this.color = this.createColorSetting();
    }

    protected abstract CheckboxSetting createIncludeSetting();

    protected abstract ColorSetting createColorSetting();

    public void clear() {
        this.boxes.clear();
    }

    public final boolean isEnabled() {
        return this.enabled == null || this.enabled.isChecked();
    }

    public final Stream<Setting> getSettings() {
        return Stream.of(this.enabled, this.color).filter(Objects::nonNull);
    }

    public final int getColorI(int alpha) {
        return this.color.getColorI(alpha);
    }

    public final List<class_238> getBoxes() {
        return Collections.unmodifiableList(this.boxes);
    }
}
