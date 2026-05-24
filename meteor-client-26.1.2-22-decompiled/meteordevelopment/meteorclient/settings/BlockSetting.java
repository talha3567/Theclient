package meteordevelopment.meteorclient.settings;

import java.util.function.Consumer;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public class BlockSetting
extends Setting<Block> {
    public final Predicate<Block> filter;

    public BlockSetting(String name, String description, Block defaultValue, Consumer<Block> onChanged, Consumer<Setting<Block>> onModuleActivated, IVisible visible, Predicate<Block> filter) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.filter = filter;
    }

    @Override
    protected Block parseImpl(String str) {
        return (Block)BlockSetting.parseId(BuiltInRegistries.BLOCK, str);
    }

    @Override
    protected boolean isValueValid(Block value) {
        return this.filter == null || this.filter.test(value);
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return BuiltInRegistries.BLOCK.keySet();
    }

    @Override
    protected CompoundTag save(CompoundTag tag) {
        tag.putString("value", BuiltInRegistries.BLOCK.getKey((Object)((Block)this.get())).toString());
        return tag;
    }

    @Override
    protected Block load(CompoundTag tag) {
        this.value = BuiltInRegistries.BLOCK.getValue(Identifier.parse((String)tag.getStringOr("value", "")));
        if (this.filter != null && !this.filter.test((Block)this.value)) {
            for (Block block : BuiltInRegistries.BLOCK) {
                if (!this.filter.test(block)) continue;
                this.value = block;
                break;
            }
        }
        return (Block)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, Block, BlockSetting> {
        private Predicate<Block> filter;

        public Builder() {
            super(null);
        }

        public Builder filter(Predicate<Block> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public BlockSetting build() {
            return new BlockSetting(this.name, this.description, (Block)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.filter);
        }
    }
}
