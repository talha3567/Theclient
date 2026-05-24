package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;

public class ScreenHandlerListSetting
extends Setting<List<MenuType<?>>> {
    public ScreenHandlerListSetting(String name, String description, List<MenuType<?>> defaultValue, Consumer<List<MenuType<?>>> onChanged, Consumer<Setting<List<MenuType<?>>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    public void resetImpl() {
        this.value = new ArrayList((Collection)this.defaultValue);
    }

    @Override
    protected List<MenuType<?>> parseImpl(String str) {
        String[] values = str.split(",");
        ArrayList handlers = new ArrayList(values.length);
        try {
            for (String value : values) {
                MenuType handler = (MenuType)ScreenHandlerListSetting.parseId(BuiltInRegistries.MENU, value);
                if (handler == null) continue;
                handlers.add(handler);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return handlers;
    }

    @Override
    protected boolean isValueValid(List<MenuType<?>> value) {
        return true;
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return BuiltInRegistries.MENU.keySet();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag valueTag = new ListTag();
        for (MenuType type : (List)this.get()) {
            Identifier id = BuiltInRegistries.MENU.getKey((Object)type);
            if (id == null) continue;
            valueTag.add((Object)StringTag.valueOf((String)id.toString()));
        }
        tag.put("value", (Tag)valueTag);
        return tag;
    }

    @Override
    public List<MenuType<?>> load(CompoundTag tag) {
        ((List)this.get()).clear();
        ListTag valueTag = tag.getListOrEmpty("value");
        for (Tag tagI : valueTag) {
            MenuType type = (MenuType)BuiltInRegistries.MENU.getValue(Identifier.parse((String)tagI.asString().orElse("")));
            if (type == null) continue;
            ((List)this.get()).add(type);
        }
        return (List)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, List<MenuType<?>>, ScreenHandlerListSetting> {
        public Builder() {
            super(new ArrayList(0));
        }

        @Override
        public Builder defaultValue(MenuType<?> ... defaults) {
            return (Builder)this.defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList());
        }

        @Override
        public ScreenHandlerListSetting build() {
            return new ScreenHandlerListSetting(this.name, this.description, (List)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
