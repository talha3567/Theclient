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
import net.minecraft.world.effect.MobEffect;

public class StatusEffectListSetting
extends Setting<List<MobEffect>> {
    public StatusEffectListSetting(String name, String description, List<MobEffect> defaultValue, Consumer<List<MobEffect>> onChanged, Consumer<Setting<List<MobEffect>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    public void resetImpl() {
        this.value = new ArrayList((Collection)this.defaultValue);
    }

    @Override
    protected List<MobEffect> parseImpl(String str) {
        String[] values = str.split(",");
        ArrayList<MobEffect> effects = new ArrayList<MobEffect>(values.length);
        try {
            for (String value : values) {
                MobEffect effect = (MobEffect)StatusEffectListSetting.parseId(BuiltInRegistries.MOB_EFFECT, value);
                if (effect == null) continue;
                effects.add(effect);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return effects;
    }

    @Override
    protected boolean isValueValid(List<MobEffect> value) {
        return true;
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return BuiltInRegistries.MOB_EFFECT.keySet();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag valueTag = new ListTag();
        for (MobEffect effect : (List)this.get()) {
            Identifier id = BuiltInRegistries.MOB_EFFECT.getKey((Object)effect);
            if (id == null) continue;
            valueTag.add((Object)StringTag.valueOf((String)id.toString()));
        }
        tag.put("value", (Tag)valueTag);
        return tag;
    }

    @Override
    public List<MobEffect> load(CompoundTag tag) {
        ((List)this.get()).clear();
        for (Tag tagI : tag.getListOrEmpty("value")) {
            MobEffect effect = (MobEffect)BuiltInRegistries.MOB_EFFECT.getValue(Identifier.parse((String)tagI.asString().orElse("")));
            if (effect == null) continue;
            ((List)this.get()).add(effect);
        }
        return (List)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, List<MobEffect>, StatusEffectListSetting> {
        public Builder() {
            super(new ArrayList(0));
        }

        @Override
        public Builder defaultValue(MobEffect ... defaults) {
            return (Builder)this.defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList());
        }

        @Override
        public StatusEffectListSetting build() {
            return new StatusEffectListSetting(this.name, this.description, (List)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
