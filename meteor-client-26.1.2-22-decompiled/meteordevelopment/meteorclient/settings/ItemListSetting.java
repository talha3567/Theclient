package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class ItemListSetting
extends Setting<List<Item>> {
    public final Predicate<Item> filter;
    private final boolean bypassFilterWhenSavingAndLoading;

    public ItemListSetting(String name, String description, List<Item> defaultValue, Consumer<List<Item>> onChanged, Consumer<Setting<List<Item>>> onModuleActivated, IVisible visible, Predicate<Item> filter, boolean bypassFilterWhenSavingAndLoading) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.filter = filter;
        this.bypassFilterWhenSavingAndLoading = bypassFilterWhenSavingAndLoading;
    }

    @Override
    protected List<Item> parseImpl(String str) {
        String[] values = str.split(",");
        ArrayList<Item> items = new ArrayList<Item>(values.length);
        try {
            for (String value : values) {
                Item item = (Item)ItemListSetting.parseId(BuiltInRegistries.ITEM, value);
                if (item == null || this.filter != null && !this.filter.test(item)) continue;
                items.add(item);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return items;
    }

    @Override
    public void resetImpl() {
        this.value = new ArrayList((Collection)this.defaultValue);
    }

    @Override
    protected boolean isValueValid(List<Item> value) {
        return true;
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return BuiltInRegistries.ITEM.keySet();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag valueTag = new ListTag();
        for (Item item : (List)this.get()) {
            if (!this.bypassFilterWhenSavingAndLoading && this.filter != null && !this.filter.test(item)) continue;
            valueTag.add((Object)StringTag.valueOf((String)BuiltInRegistries.ITEM.getKey((Object)item).toString()));
        }
        tag.put("value", (Tag)valueTag);
        return tag;
    }

    @Override
    public List<Item> load(CompoundTag tag) {
        ((List)this.get()).clear();
        ListTag valueTag = tag.getListOrEmpty("value");
        for (Tag tagI : valueTag) {
            Item item = (Item)BuiltInRegistries.ITEM.getValue(Identifier.parse((String)tagI.asString().orElse("")));
            if (!this.bypassFilterWhenSavingAndLoading && this.filter != null && !this.filter.test(item)) continue;
            ((List)this.get()).add(item);
        }
        return (List)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, List<Item>, ItemListSetting> {
        private Predicate<Item> filter;
        private boolean bypassFilterWhenSavingAndLoading;

        public Builder() {
            super(new ArrayList(0));
        }

        @Override
        public Builder defaultValue(Item ... defaults) {
            return (Builder)this.defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList());
        }

        public Builder filter(Predicate<Item> filter) {
            this.filter = filter;
            return this;
        }

        public Builder bypassFilterWhenSavingAndLoading() {
            this.bypassFilterWhenSavingAndLoading = true;
            return this;
        }

        @Override
        public ItemListSetting build() {
            return new ItemListSetting(this.name, this.description, (List)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.filter, this.bypassFilterWhenSavingAndLoading);
        }
    }
}
