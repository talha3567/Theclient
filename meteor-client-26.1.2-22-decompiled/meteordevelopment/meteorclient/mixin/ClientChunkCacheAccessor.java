package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ClientChunkCache.class})
public interface ClientChunkCacheAccessor {
    @Accessor(value="storage")
    public ClientChunkCache.Storage meteor$getStorage();
}
