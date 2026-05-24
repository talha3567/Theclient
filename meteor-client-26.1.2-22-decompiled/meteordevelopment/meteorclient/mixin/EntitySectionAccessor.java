package meteordevelopment.meteorclient.mixin;

import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.level.entity.EntitySection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={EntitySection.class})
public interface EntitySectionAccessor {
    @Accessor(value="storage")
    public <T> ClassInstanceMultiMap<T> meteor$getStorage();
}
