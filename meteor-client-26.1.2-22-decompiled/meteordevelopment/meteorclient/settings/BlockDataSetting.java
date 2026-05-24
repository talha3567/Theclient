package meteordevelopment.meteorclient.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IBlockData;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.IGetter;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public class BlockDataSetting<T extends ICopyable<T> & ISerializable<T> & IBlockData<T>>
extends Setting<Map<Block, T>> {
    public final IGetter<T> defaultData;

    public BlockDataSetting(String name, String description, Map<Block, T> defaultValue, Consumer<Map<Block, T>> onChanged, Consumer<Setting<Map<Block, T>>> onModuleActivated, IGetter<T> defaultData, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.defaultData = defaultData;
    }

    @Override
    public void resetImpl() {
        this.value = new HashMap((Map)this.defaultValue);
    }

    @Override
    protected Map<Block, T> parseImpl(String str) {
        return HashMap.newHashMap(0);
    }

    @Override
    protected boolean isValueValid(Map<Block, T> value) {
        return true;
    }

    @Override
    protected CompoundTag save(CompoundTag tag) {
        CompoundTag valueTag = new CompoundTag();
        for (Block block : ((Map)this.get()).keySet()) {
            valueTag.put(BuiltInRegistries.BLOCK.getKey((Object)block).toString(), (Tag)((ISerializable)((Object)((ICopyable)((Map)this.get()).get(block)))).toTag());
        }
        tag.put("value", (Tag)valueTag);
        return tag;
    }

    @Override
    protected Map<Block, T> load(CompoundTag tag) {
        ((Map)this.get()).clear();
        CompoundTag valueTag = tag.getCompoundOrEmpty("value");
        for (String key : valueTag.keySet()) {
            ((Map)this.get()).put((Block)BuiltInRegistries.BLOCK.getValue(Identifier.parse((String)key)), (ICopyable)((ISerializable)((ICopyable)this.defaultData.get()).copy()).fromTag(valueTag.getCompoundOrEmpty(key)));
        }
        return (Map)this.get();
    }

    public static class Builder<T extends ICopyable<T> & ISerializable<T> & IBlockData<T>>
    extends Setting.SettingBuilder<Builder<T>, Map<Block, T>, BlockDataSetting<T>> {
        private IGetter<T> defaultData;

        public Builder() {
            super(HashMap.newHashMap(0));
        }

        public Builder<T> defaultData(IGetter<T> defaultData) {
            this.defaultData = defaultData;
            return this;
        }

        @Override
        public BlockDataSetting<T> build() {
            return new BlockDataSetting<T>(this.name, this.description, (Map)this.defaultValue, this.onChanged, this.onModuleActivated, this.defaultData, this.visible);
        }
    }
}
