package meteordevelopment.meteorclient.settings;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.lang.reflect.AccessFlag;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class EnchantmentListSetting
extends Setting<Set<ResourceKey<Enchantment>>> {
    public EnchantmentListSetting(String name, String description, Set<ResourceKey<Enchantment>> defaultValue, Consumer<Set<ResourceKey<Enchantment>>> onChanged, Consumer<Setting<Set<ResourceKey<Enchantment>>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    public void resetImpl() {
        this.value = new ObjectOpenHashSet((Collection)this.defaultValue);
    }

    @Override
    protected Set<ResourceKey<Enchantment>> parseImpl(String str) {
        String[] values = str.split(",");
        ObjectOpenHashSet enchs = new ObjectOpenHashSet(values.length);
        for (String value : values) {
            String name = value.trim();
            Identifier id = name.contains(":") ? Identifier.parse((String)name) : Identifier.withDefaultNamespace((String)name);
            enchs.add(ResourceKey.create((ResourceKey)Registries.ENCHANTMENT, (Identifier)id));
        }
        return enchs;
    }

    @Override
    protected boolean isValueValid(Set<ResourceKey<Enchantment>> value) {
        return true;
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return Optional.ofNullable(Minecraft.getInstance().getConnection()).flatMap(networkHandler -> networkHandler.registryAccess().lookup(Registries.ENCHANTMENT)).map(Registry::keySet).orElse(Set.of());
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag valueTag = new ListTag();
        for (ResourceKey ench : (Set)this.get()) {
            valueTag.add((Object)StringTag.valueOf((String)ench.identifier().toString()));
        }
        tag.put("value", (Tag)valueTag);
        return tag;
    }

    @Override
    public Set<ResourceKey<Enchantment>> load(CompoundTag tag) {
        ((Set)this.get()).clear();
        for (Tag tagI : tag.getListOrEmpty("value")) {
            ((Set)this.get()).add(ResourceKey.create((ResourceKey)Registries.ENCHANTMENT, (Identifier)Identifier.parse((String)tagI.asString().orElse(""))));
        }
        return (Set)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, Set<ResourceKey<Enchantment>>, EnchantmentListSetting> {
        private static final Set<ResourceKey<Enchantment>> VANILLA_DEFAULTS = Arrays.stream(Enchantments.class.getDeclaredFields()).filter(field -> field.accessFlags().containsAll(List.of(AccessFlag.PUBLIC, AccessFlag.STATIC, AccessFlag.FINAL))).filter(field -> field.getType() == ResourceKey.class).map(field -> {
            try {
                return field.get(null);
            }
            catch (IllegalAccessException illegalAccessException) {
                return null;
            }
        }).filter(Objects::nonNull).map(ResourceKey.class::cast).filter(registryKey -> registryKey.registryKey() == Registries.ENCHANTMENT).collect(Collectors.toSet());

        public Builder() {
            super(new ObjectOpenHashSet());
        }

        public Builder vanillaDefaults() {
            return (Builder)this.defaultValue(VANILLA_DEFAULTS);
        }

        @Override
        @SafeVarargs
        public final Builder defaultValue(ResourceKey<Enchantment> ... defaults) {
            return (Builder)this.defaultValue(defaults != null ? new ObjectOpenHashSet((Object[])defaults) : new ObjectOpenHashSet());
        }

        @Override
        public EnchantmentListSetting build() {
            return new EnchantmentListSetting(this.name, this.description, (Set)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
