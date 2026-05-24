package meteordevelopment.meteorclient.settings;

import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;

public class StatusEffectAmplifierMapSetting
extends Setting<Reference2IntMap<MobEffect>> {
    public static final Reference2IntMap<MobEffect> EMPTY_STATUS_EFFECT_MAP = StatusEffectAmplifierMapSetting.createStatusEffectMap();

    public StatusEffectAmplifierMapSetting(String name, String description, Reference2IntMap<MobEffect> defaultValue, Consumer<Reference2IntMap<MobEffect>> onChanged, Consumer<Setting<Reference2IntMap<MobEffect>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    public void resetImpl() {
        this.value = new Reference2IntOpenHashMap((Reference2IntMap)this.defaultValue);
    }

    @Override
    protected Reference2IntMap<MobEffect> parseImpl(String str) {
        String[] values = str.split(",");
        Reference2IntOpenHashMap effects = new Reference2IntOpenHashMap(EMPTY_STATUS_EFFECT_MAP);
        try {
            for (String value : values) {
                String[] split = value.split(" ");
                MobEffect effect = (MobEffect)StatusEffectAmplifierMapSetting.parseId(BuiltInRegistries.MOB_EFFECT, split[0]);
                int level = Integer.parseInt(split[1]);
                effects.put((Object)effect, level);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return effects;
    }

    @Override
    protected boolean isValueValid(Reference2IntMap<MobEffect> value) {
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag valueTag = new CompoundTag();
        for (MobEffect statusEffect : ((Reference2IntMap)this.get()).keySet()) {
            Identifier id = BuiltInRegistries.MOB_EFFECT.getKey((Object)statusEffect);
            if (id == null) continue;
            valueTag.putInt(id.toString(), ((Reference2IntMap)this.get()).getInt((Object)statusEffect));
        }
        tag.put("value", (Tag)valueTag);
        return tag;
    }

    private static Reference2IntMap<MobEffect> createStatusEffectMap() {
        Reference2IntArrayMap map = new Reference2IntArrayMap(BuiltInRegistries.MOB_EFFECT.keySet().size());
        BuiltInRegistries.MOB_EFFECT.forEach(arg_0 -> StatusEffectAmplifierMapSetting.lambda$createStatusEffectMap$0((Reference2IntMap)map, arg_0));
        return map;
    }

    @Override
    public Reference2IntMap<MobEffect> load(CompoundTag tag) {
        ((Reference2IntMap)this.get()).clear();
        CompoundTag valueTag = tag.getCompoundOrEmpty("value");
        for (String key : valueTag.keySet()) {
            MobEffect statusEffect = (MobEffect)BuiltInRegistries.MOB_EFFECT.getValue(Identifier.parse((String)key));
            if (statusEffect == null) continue;
            ((Reference2IntMap)this.get()).put((Object)statusEffect, valueTag.getIntOr(key, 0));
        }
        return (Reference2IntMap)this.get();
    }

    private static /* synthetic */ void lambda$createStatusEffectMap$0(Reference2IntMap map, MobEffect potion) {
        map.put((Object)potion, 0);
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, Reference2IntMap<MobEffect>, StatusEffectAmplifierMapSetting> {
        public Builder() {
            super(new Reference2IntOpenHashMap(0));
        }

        @Override
        public StatusEffectAmplifierMapSetting build() {
            return new StatusEffectAmplifierMapSetting(this.name, this.description, (Reference2IntMap<MobEffect>)((Reference2IntMap)this.defaultValue), this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
