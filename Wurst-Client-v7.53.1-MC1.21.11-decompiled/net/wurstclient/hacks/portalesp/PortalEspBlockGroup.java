package net.wurstclient.hacks.portalesp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.BlockUtils;

public final class PortalEspBlockGroup {
    protected final ArrayList<class_238> boxes = new ArrayList();
    private final class_2248 block;
    private final ColorSetting color;
    private final CheckboxSetting enabled;

    public PortalEspBlockGroup(class_2248 block, ColorSetting color, CheckboxSetting enabled) {
        this.block = block;
        this.color = Objects.requireNonNull(color);
        this.enabled = enabled;
    }

    public void add(class_2338 pos) {
        class_238 box = this.getBox(pos);
        if (box == null) {
            return;
        }
        this.boxes.add(box);
    }

    private class_238 getBox(class_2338 pos) {
        if (!BlockUtils.canBeClicked(pos)) {
            return null;
        }
        return BlockUtils.getBoundingBox(pos);
    }

    public void clear() {
        this.boxes.clear();
    }

    public boolean isEnabled() {
        return this.enabled == null || this.enabled.isChecked();
    }

    public Stream<Setting> getSettings() {
        return Stream.of(this.enabled, this.color).filter(Objects::nonNull);
    }

    public class_2248 getBlock() {
        return this.block;
    }

    public int getColorI(int alpha) {
        return this.color.getColorI(alpha);
    }

    public List<class_238> getBoxes() {
        return Collections.unmodifiableList(this.boxes);
    }
}
