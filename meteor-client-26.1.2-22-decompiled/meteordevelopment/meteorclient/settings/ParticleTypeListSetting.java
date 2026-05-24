package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

public class ParticleTypeListSetting
extends Setting<List<ParticleType<?>>> {
    public ParticleTypeListSetting(String name, String description, List<ParticleType<?>> defaultValue, Consumer<List<ParticleType<?>>> onChanged, Consumer<Setting<List<ParticleType<?>>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    public void resetImpl() {
        this.value = new ArrayList((Collection)this.defaultValue);
    }

    @Override
    protected List<ParticleType<?>> parseImpl(String str) {
        String[] values = str.split(",");
        ArrayList particleTypes = new ArrayList(values.length);
        try {
            for (String value : values) {
                ParticleType particleType = (ParticleType)ParticleTypeListSetting.parseId(BuiltInRegistries.PARTICLE_TYPE, value);
                if (particleType == null) continue;
                particleTypes.add(particleType);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return particleTypes;
    }

    @Override
    protected boolean isValueValid(List<ParticleType<?>> value) {
        return true;
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return BuiltInRegistries.PARTICLE_TYPE.keySet();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag valueTag = new ListTag();
        for (ParticleType particleType : (List)this.get()) {
            Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey((Object)particleType);
            if (id == null) continue;
            valueTag.add((Object)StringTag.valueOf((String)id.toString()));
        }
        tag.put("value", (Tag)valueTag);
        return tag;
    }

    @Override
    public List<ParticleType<?>> load(CompoundTag tag) {
        ((List)this.get()).clear();
        ListTag valueTag = tag.getListOrEmpty("value");
        for (Tag tagI : valueTag) {
            ParticleType particleType = (ParticleType)BuiltInRegistries.PARTICLE_TYPE.getValue(Identifier.parse((String)tagI.asString().orElse("")));
            if (particleType == null) continue;
            ((List)this.get()).add(particleType);
        }
        return (List)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, List<ParticleType<?>>, ParticleTypeListSetting> {
        public Builder() {
            super(new ArrayList(0));
        }

        @Override
        public Builder defaultValue(ParticleType<?> ... defaults) {
            return (Builder)this.defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList());
        }

        @Override
        public ParticleTypeListSetting build() {
            return new ParticleTypeListSetting(this.name, this.description, (List)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
