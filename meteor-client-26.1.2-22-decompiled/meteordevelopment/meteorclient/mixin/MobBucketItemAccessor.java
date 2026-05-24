package meteordevelopment.meteorclient.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.MobBucketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={MobBucketItem.class})
public interface MobBucketItemAccessor {
    @Accessor(value="type")
    public EntityType<?> meteor$getType();
}
