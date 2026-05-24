package meteordevelopment.meteorclient.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={EntitySectionStorage.class})
public interface EntitySectionStorageAccessor {
    @Accessor(value="sectionIds")
    public LongSortedSet meteor$getSectionIds();

    @Accessor(value="sections")
    public <T extends EntityAccess> Long2ObjectMap<EntitySection<T>> meteor$getSections();
}
