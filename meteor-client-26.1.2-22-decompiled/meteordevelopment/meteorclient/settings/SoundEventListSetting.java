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
import net.minecraft.sounds.SoundEvent;

public class SoundEventListSetting
extends Setting<List<SoundEvent>> {
    public SoundEventListSetting(String name, String description, List<SoundEvent> defaultValue, Consumer<List<SoundEvent>> onChanged, Consumer<Setting<List<SoundEvent>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    public void resetImpl() {
        this.value = new ArrayList((Collection)this.defaultValue);
    }

    @Override
    protected List<SoundEvent> parseImpl(String str) {
        String[] values = str.split(",");
        ArrayList<SoundEvent> sounds = new ArrayList<SoundEvent>(values.length);
        try {
            for (String value : values) {
                SoundEvent sound = (SoundEvent)SoundEventListSetting.parseId(BuiltInRegistries.SOUND_EVENT, value);
                if (sound == null) continue;
                sounds.add(sound);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return sounds;
    }

    @Override
    protected boolean isValueValid(List<SoundEvent> value) {
        return true;
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return BuiltInRegistries.SOUND_EVENT.keySet();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag valueTag = new ListTag();
        for (SoundEvent sound : (List)this.get()) {
            Identifier id = BuiltInRegistries.SOUND_EVENT.getKey((Object)sound);
            if (id == null) continue;
            valueTag.add((Object)StringTag.valueOf((String)id.toString()));
        }
        tag.put("value", (Tag)valueTag);
        return tag;
    }

    @Override
    public List<SoundEvent> load(CompoundTag tag) {
        ((List)this.get()).clear();
        for (Tag tagI : tag.getListOrEmpty("value")) {
            SoundEvent soundEvent = (SoundEvent)BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse((String)tagI.asString().orElse("")));
            if (soundEvent == null) continue;
            ((List)this.get()).add(soundEvent);
        }
        return (List)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, List<SoundEvent>, SoundEventListSetting> {
        public Builder() {
            super(new ArrayList(0));
        }

        @Override
        public Builder defaultValue(SoundEvent ... defaults) {
            return (Builder)this.defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList());
        }

        @Override
        public SoundEventListSetting build() {
            return new SoundEventListSetting(this.name, this.description, (List)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
