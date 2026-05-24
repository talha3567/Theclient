package meteordevelopment.meteorclient.settings;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class EntityTypeListSetting
extends Setting<Set<EntityType<?>>> {
    public final Predicate<EntityType<?>> filter;
    private List<String> suggestions;
    private static final List<String> groups = List.of("animal", "wateranimal", "monster", "ambient", "misc");

    public EntityTypeListSetting(String name, String description, Set<EntityType<?>> defaultValue, Consumer<Set<EntityType<?>>> onChanged, Consumer<Setting<Set<EntityType<?>>>> onModuleActivated, IVisible visible, Predicate<EntityType<?>> filter) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.filter = filter;
    }

    @Override
    public void resetImpl() {
        this.value = new ObjectOpenHashSet((Collection)this.defaultValue);
    }

    @Override
    protected Set<EntityType<?>> parseImpl(String str) {
        String[] values = str.split(",");
        ObjectOpenHashSet entities = new ObjectOpenHashSet(values.length);
        try {
            for (String value : values) {
                EntityType entity = (EntityType)EntityTypeListSetting.parseId(BuiltInRegistries.ENTITY_TYPE, value);
                if (entity != null) {
                    entities.add(entity);
                    continue;
                }
                String lowerValue = value.trim().toLowerCase();
                if (!groups.contains(lowerValue)) continue;
                for (EntityType entityType : BuiltInRegistries.ENTITY_TYPE) {
                    if (this.filter != null && !this.filter.test(entityType)) continue;
                    switch (lowerValue) {
                        case "animal": {
                            if (entityType.getCategory() != MobCategory.CREATURE) break;
                            entities.add(entityType);
                            break;
                        }
                        case "wateranimal": {
                            if (entityType.getCategory() != MobCategory.WATER_AMBIENT && entityType.getCategory() != MobCategory.WATER_CREATURE && entityType.getCategory() != MobCategory.UNDERGROUND_WATER_CREATURE && entityType.getCategory() != MobCategory.AXOLOTLS) break;
                            entities.add(entityType);
                            break;
                        }
                        case "monster": {
                            if (entityType.getCategory() != MobCategory.MONSTER) break;
                            entities.add(entityType);
                            break;
                        }
                        case "ambient": {
                            if (entityType.getCategory() != MobCategory.AMBIENT) break;
                            entities.add(entityType);
                            break;
                        }
                        case "misc": {
                            if (entityType.getCategory() != MobCategory.MISC) break;
                            entities.add(entityType);
                        }
                    }
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return entities;
    }

    @Override
    protected boolean isValueValid(Set<EntityType<?>> value) {
        return true;
    }

    @Override
    public List<String> getSuggestions() {
        if (this.suggestions == null) {
            this.suggestions = new ArrayList<String>(groups);
            for (EntityType entityType : BuiltInRegistries.ENTITY_TYPE) {
                if (this.filter != null && !this.filter.test(entityType)) continue;
                this.suggestions.add(BuiltInRegistries.ENTITY_TYPE.getKey((Object)entityType).toString());
            }
        }
        return this.suggestions;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag valueTag = new ListTag();
        for (EntityType entityType : (Set)this.get()) {
            valueTag.add((Object)StringTag.valueOf((String)BuiltInRegistries.ENTITY_TYPE.getKey((Object)entityType).toString()));
        }
        tag.put("value", (Tag)valueTag);
        return tag;
    }

    @Override
    public Set<EntityType<?>> load(CompoundTag tag) {
        ((Set)this.get()).clear();
        ListTag valueTag = tag.getListOrEmpty("value");
        for (Tag tagI : valueTag) {
            EntityType type = (EntityType)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse((String)tagI.asString().orElse("")));
            if (this.filter != null && !this.filter.test(type)) continue;
            ((Set)this.get()).add(type);
        }
        return (Set)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, Set<EntityType<?>>, EntityTypeListSetting> {
        private Predicate<EntityType<?>> filter;

        public Builder() {
            super(new ObjectOpenHashSet(0));
        }

        @Override
        public Builder defaultValue(EntityType<?> ... defaults) {
            return (Builder)this.defaultValue(defaults != null ? new ObjectOpenHashSet((Object[])defaults) : new ObjectOpenHashSet(0));
        }

        public Builder onlyAttackable() {
            this.filter = EntityUtils::isAttackable;
            return this;
        }

        public Builder filter(Predicate<EntityType<?>> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public EntityTypeListSetting build() {
            return new EntityTypeListSetting(this.name, this.description, (Set)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.filter);
        }
    }
}
