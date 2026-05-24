package meteordevelopment.meteorclient.mixin;

import java.nio.file.Path;
import net.minecraft.client.resources.SkinManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={SkinManager.TextureCache.class})
public interface FileCacheAccessor {
    @Accessor(value="root")
    public Path meteor$getRoot();
}
