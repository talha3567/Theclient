package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class ModuleListSetting
extends Setting<List<Module>> {
    private static List<String> suggestions;

    public ModuleListSetting(String name, String description, List<Module> defaultValue, Consumer<List<Module>> onChanged, Consumer<Setting<List<Module>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    public void resetImpl() {
        this.value = new ArrayList((Collection)this.defaultValue);
    }

    @Override
    protected List<Module> parseImpl(String str) {
        String[] values = str.split(",");
        ArrayList<Module> modules = new ArrayList<Module>(values.length);
        try {
            for (String value : values) {
                Module module = Modules.get().get(value.trim());
                if (module == null) continue;
                modules.add(module);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return modules;
    }

    @Override
    protected boolean isValueValid(List<Module> value) {
        return true;
    }

    @Override
    public List<String> getSuggestions() {
        if (suggestions == null) {
            suggestions = new ArrayList<String>(Modules.get().getAll().size());
            for (Module module : Modules.get().getAll()) {
                suggestions.add(module.name);
            }
        }
        return suggestions;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag modulesTag = new ListTag();
        for (Module module : (List)this.get()) {
            modulesTag.add((Object)StringTag.valueOf((String)module.name));
        }
        tag.put("modules", (Tag)modulesTag);
        return tag;
    }

    @Override
    public List<Module> load(CompoundTag tag) {
        ((List)this.get()).clear();
        ListTag valueTag = tag.getListOrEmpty("modules");
        for (Tag tagI : valueTag) {
            Module module = Modules.get().get(tagI.asString().orElse(""));
            if (module == null) continue;
            ((List)this.get()).add(module);
        }
        return (List)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, List<Module>, ModuleListSetting> {
        public Builder() {
            super(new ArrayList(0));
        }

        @Override
        @SafeVarargs
        public final Builder defaultValue(Class<? extends Module> ... defaults) {
            ArrayList<Module> modules = new ArrayList<Module>();
            for (Class<? extends Module> klass : defaults) {
                if (Modules.get().get(klass) == null) continue;
                modules.add(Modules.get().get(klass));
            }
            return (Builder)this.defaultValue(modules);
        }

        @Override
        public ModuleListSetting build() {
            return new ModuleListSetting(this.name, this.description, (List)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
