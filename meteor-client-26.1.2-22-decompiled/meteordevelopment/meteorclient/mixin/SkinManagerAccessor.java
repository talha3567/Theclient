package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.resources.SkinManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={SkinManager.class})
public interface SkinManagerAccessor {
    @Accessor(value="skinTextures")
    public SkinManager.TextureCache meteor$getSkinTextures();
}
