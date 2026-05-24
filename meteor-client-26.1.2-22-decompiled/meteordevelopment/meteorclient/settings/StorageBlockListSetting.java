package meteordevelopment.meteorclient.settings;

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StorageBlockListSetting
extends Setting<List<BlockEntityType<?>>> {
    public static final BlockEntityType<?>[] STORAGE_BLOCKS = new BlockEntityType[]{BlockEntityType.BARREL, BlockEntityType.BLAST_FURNACE, BlockEntityType.BREWING_STAND, BlockEntityType.CAMPFIRE, BlockEntityType.CHEST, BlockEntityType.CHISELED_BOOKSHELF, BlockEntityType.CRAFTER, BlockEntityType.DISPENSER, BlockEntityType.DECORATED_POT, BlockEntityType.DROPPER, BlockEntityType.ENDER_CHEST, BlockEntityType.FURNACE, BlockEntityType.HOPPER, BlockEntityType.SHULKER_BOX, BlockEntityType.SMOKER, BlockEntityType.TRAPPED_CHEST};
    public static final Registry<BlockEntityType<?>> REGISTRY = new SRegistry();

    public StorageBlockListSetting(String name, String description, List<BlockEntityType<?>> defaultValue, Consumer<List<BlockEntityType<?>>> onChanged, Consumer<Setting<List<BlockEntityType<?>>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    public void resetImpl() {
        this.value = new ArrayList((Collection)this.defaultValue);
    }

    @Override
    protected List<BlockEntityType<?>> parseImpl(String str) {
        String[] values = str.split(",");
        ArrayList blocks = new ArrayList(values.length);
        try {
            for (String value : values) {
                BlockEntityType block = (BlockEntityType)StorageBlockListSetting.parseId(BuiltInRegistries.BLOCK_ENTITY_TYPE, value);
                if (block == null) continue;
                blocks.add(block);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return blocks;
    }

    @Override
    protected boolean isValueValid(List<BlockEntityType<?>> value) {
        return true;
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.keySet();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag valueTag = new ListTag();
        for (BlockEntityType type : (List)this.get()) {
            Identifier id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey((Object)type);
            if (id == null) continue;
            valueTag.add((Object)StringTag.valueOf((String)id.toString()));
        }
        tag.put("value", (Tag)valueTag);
        return tag;
    }

    @Override
    public List<BlockEntityType<?>> load(CompoundTag tag) {
        ((List)this.get()).clear();
        ListTag valueTag = tag.getListOrEmpty("value");
        for (Tag tagI : valueTag) {
            BlockEntityType type = (BlockEntityType)BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(Identifier.parse((String)tagI.asString().orElse("")));
            if (type == null) continue;
            ((List)this.get()).add(type);
        }
        return (List)this.get();
    }

    private static class SRegistry
    extends MappedRegistry<BlockEntityType<?>> {
        public SRegistry() {
            super(ResourceKey.createRegistryKey((Identifier)MeteorClient.identifier("storage-blocks")), Lifecycle.stable());
        }

        public int size() {
            return STORAGE_BLOCKS.length;
        }

        @Nullable
        public Identifier getKey(BlockEntityType<?> entry) {
            return null;
        }

        public Optional<ResourceKey<BlockEntityType<?>>> getResourceKey(BlockEntityType<?> entry) {
            return Optional.empty();
        }

        public int getId(@Nullable BlockEntityType<?> entry) {
            return 0;
        }

        @Nullable
        public BlockEntityType<?> getValue(@Nullable ResourceKey<BlockEntityType<?>> key) {
            return null;
        }

        @Nullable
        public BlockEntityType<?> getValue(@Nullable Identifier id) {
            return null;
        }

        public Lifecycle registryLifecycle() {
            return null;
        }

        public Set<Identifier> keySet() {
            return null;
        }

        public BlockEntityType<?> byIdOrThrow(int index) {
            return (BlockEntityType)super.byIdOrThrow(index);
        }

        public boolean containsKey(Identifier id) {
            return false;
        }

        @Nullable
        public BlockEntityType<?> byId(int index) {
            return null;
        }

        @NotNull
        public Iterator<BlockEntityType<?>> iterator() {
            return ObjectIterators.wrap((Object[])STORAGE_BLOCKS);
        }

        public boolean containsKey(ResourceKey<BlockEntityType<?>> key) {
            return false;
        }

        public Set<Map.Entry<ResourceKey<BlockEntityType<?>>, BlockEntityType<?>>> entrySet() {
            return null;
        }

        public Optional<Holder.Reference<BlockEntityType<?>>> getRandom(RandomSource random) {
            return Optional.empty();
        }

        public Registry<BlockEntityType<?>> freeze() {
            return null;
        }

        public Holder.Reference<BlockEntityType<?>> createIntrusiveHolder(BlockEntityType<?> value) {
            return null;
        }

        public Optional<Holder.Reference<BlockEntityType<?>>> get(int rawId) {
            return Optional.empty();
        }

        public Optional<Holder.Reference<BlockEntityType<?>>> get(Identifier id) {
            return Optional.empty();
        }

        public Stream<Holder.Reference<BlockEntityType<?>>> listElements() {
            return null;
        }

        public Stream<HolderSet.Named<BlockEntityType<?>>> getTags() {
            return null;
        }

        public Set<ResourceKey<BlockEntityType<?>>> registryKeySet() {
            return null;
        }
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, List<BlockEntityType<?>>, StorageBlockListSetting> {
        public Builder() {
            super(new ArrayList(0));
        }

        @Override
        public Builder defaultValue(BlockEntityType<?> ... defaults) {
            return (Builder)this.defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList());
        }

        @Override
        public StorageBlockListSetting build() {
            return new StorageBlockListSetting(this.name, this.description, (List)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}
