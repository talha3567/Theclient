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
import net.minecraft.world.level.block.Block;

public class BlockListSetting
extends Setting<List<Block>> {
    public final Predicate<Block> filter;

    public BlockListSetting(String name, String description, List<Block> defaultValue, Consumer<List<Block>> onChanged, Consumer<Setting<List<Block>>> onModuleActivated, Predicate<Block> filter, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.filter = filter;
    }

    @Override
    public void resetImpl() {
        this.value = new ArrayList((Collection)this.defaultValue);
    }

    @Override
    protected List<Block> parseImpl(String str) {
        String[] values = str.split(",");
        ArrayList<Block> blocks = new ArrayList<Block>(values.length);
        try {
            for (String value : values) {
                Block block = (Block)BlockListSetting.parseId(BuiltInRegistries.BLOCK, value);
                if (block == null || this.filter != null && !this.filter.test(block)) continue;
                blocks.add(block);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return blocks;
    }

    @Override
    protected boolean isValueValid(List<Block> value) {
        return true;
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return BuiltInRegistries.BLOCK.keySet();
    }

    @Override
    protected CompoundTag save(CompoundTag tag) {
        ListTag valueTag = new ListTag();
        for (Block block : (List)this.get()) {
            valueTag.add((Object)StringTag.valueOf((String)BuiltInRegistries.BLOCK.getKey((Object)block).toString()));
        }
        tag.put("value", (Tag)valueTag);
        return tag;
    }

    @Override
    protected List<Block> load(CompoundTag tag) {
        ((List)this.get()).clear();
        ListTag valueTag = tag.getListOrEmpty("value");
        for (Tag tagI : valueTag) {
            Block block = (Block)BuiltInRegistries.BLOCK.getValue(Identifier.parse((String)tagI.asString().orElse("")));
            if (this.filter != null && !this.filter.test(block)) continue;
            ((List)this.get()).add(block);
        }
        return (List)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, List<Block>, BlockListSetting> {
        private Predicate<Block> filter;

        public Builder() {
            super(new ArrayList(0));
        }

        @Override
        public Builder defaultValue(Block ... defaults) {
            return (Builder)this.defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList());
        }

        public Builder filter(Predicate<Block> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public BlockListSetting build() {
            return new BlockListSetting(this.name, this.description, (List)this.defaultValue, this.onChanged, this.onModuleActivated, this.filter, this.visible);
        }
    }
}
