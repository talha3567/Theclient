package meteordevelopment.meteorclient.mixin;

import java.util.concurrent.atomic.AtomicReferenceArray;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ClientChunkCache.Storage.class})
public interface ClientChunkMapAccessor {
    @Accessor(value="chunks")
    public AtomicReferenceArray<LevelChunk> meteor$getChunks();
}
