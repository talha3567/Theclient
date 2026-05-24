package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.settings.ColorListSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.render.color.RainbowColors;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

public class Settings
implements ISerializable<Settings>,
Iterable<SettingGroup> {
    private SettingGroup defaultGroup;
    private boolean invalidate;
    public final List<SettingGroup> groups = new ArrayList<SettingGroup>(1);

    public void onActivated() {
        for (SettingGroup group : this.groups) {
            for (Setting<?> setting : group) {
                setting.onActivated();
            }
        }
    }

    public Setting<?> get(String name) {
        for (SettingGroup sg : this) {
            for (Setting<?> setting : sg) {
                if (!name.equalsIgnoreCase(setting.name)) continue;
                return setting;
            }
        }
        return null;
    }

    public <T> Setting<T> get(String name, Class<T> tClass) {
        for (SettingGroup sg : this) {
            for (Setting<?> setting : sg) {
                Class<?> sClass = setting.getDefaultValue().getClass();
                if (!name.equalsIgnoreCase(setting.name) || !tClass.equals(sClass)) continue;
                return setting;
            }
        }
        return null;
    }

    public void reset() {
        for (SettingGroup group : this.groups) {
            for (Setting<?> setting : group) {
                setting.reset();
            }
        }
        this.invalidate();
    }

    public void invalidate() {
        this.invalidate = true;
    }

    public SettingGroup getGroup(String name) {
        for (SettingGroup sg : this) {
            if (!sg.name.equals(name)) continue;
            return sg;
        }
        return null;
    }

    public int sizeGroups() {
        return this.groups.size();
    }

    public SettingGroup getDefaultGroup() {
        if (this.defaultGroup == null) {
            this.defaultGroup = this.createGroup("General");
        }
        return this.defaultGroup;
    }

    public SettingGroup createGroup(String name, boolean expanded) {
        SettingGroup group = new SettingGroup(name, expanded);
        this.groups.add(group);
        return group;
    }

    public SettingGroup createGroup(String name) {
        return this.createGroup(name, true);
    }

    public void registerColorSettings(Module module) {
        for (SettingGroup group : this) {
            for (Setting<SettingColor> setting : group) {
                setting.module = module;
                if (setting instanceof ColorSetting) {
                    RainbowColors.addSetting(setting);
                    continue;
                }
                if (!(setting instanceof ColorListSetting)) continue;
                RainbowColors.addSettingList(setting);
            }
        }
    }

    public void unregisterColorSettings() {
        for (SettingGroup group : this) {
            for (Setting<SettingColor> setting : group) {
                if (setting instanceof ColorSetting) {
                    RainbowColors.removeSetting(setting);
                    continue;
                }
                if (!(setting instanceof ColorListSetting)) continue;
                RainbowColors.removeSettingList(setting);
            }
        }
    }

    public void tick(WContainer settings, GuiTheme theme) {
        if (settings == null) {
            return;
        }
        for (SettingGroup group : this.groups) {
            for (Setting<?> setting : group) {
                boolean visible = setting.isVisible();
                if (visible != setting.lastWasVisible) {
                    this.invalidate();
                }
                setting.lastWasVisible = visible;
            }
        }
        if (this.invalidate) {
            settings.clear();
            settings.add(theme.settings(this)).expandX();
            this.invalidate = false;
        }
    }

    @Override
    @NotNull
    public Iterator<SettingGroup> iterator() {
        return this.groups.iterator();
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag groupsTag = new ListTag();
        for (SettingGroup group : this.groups) {
            if (!group.wasChanged()) continue;
            groupsTag.add((Object)group.toTag());
        }
        if (!groupsTag.isEmpty()) {
            tag.put("groups", (Tag)groupsTag);
        }
        return tag;
    }

    @Override
    public Settings fromTag(CompoundTag tag) {
        this.reset();
        ListTag groupsTag = tag.getListOrEmpty("groups");
        for (Tag t : groupsTag) {
            CompoundTag groupTag = (CompoundTag)t;
            SettingGroup sg = this.getGroup(groupTag.getStringOr("name", ""));
            if (sg == null) continue;
            sg.fromTag(groupTag);
        }
        return this;
    }
}
