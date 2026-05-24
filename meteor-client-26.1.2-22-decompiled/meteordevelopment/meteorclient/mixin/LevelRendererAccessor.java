package meteordevelopment.meteorclient.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.server.level.BlockDestructionProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={LevelRenderer.class})
public interface LevelRendererAccessor {
    @Accessor(value="destroyingBlocks")
    public Int2ObjectMap<BlockDestructionProgress> meteor$getDestroyingBlocks();
}
