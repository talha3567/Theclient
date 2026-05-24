package meteordevelopment.meteorclient.mixin;

import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={FishingHook.class})
public interface FishingHookAccessor {
    @Accessor(value="biting")
    public boolean meteor$hasCaughtFish();
}
