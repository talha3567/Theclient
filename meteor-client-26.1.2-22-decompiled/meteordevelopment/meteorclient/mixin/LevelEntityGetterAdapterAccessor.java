package meteordevelopment.meteorclient.mixin;

import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={LevelEntityGetterAdapter.class})
public interface LevelEntityGetterAdapterAccessor {
    @Accessor(value="sectionStorage")
    public <T extends EntityAccess> EntitySectionStorage<T> meteor$getSectionStorage();
}
