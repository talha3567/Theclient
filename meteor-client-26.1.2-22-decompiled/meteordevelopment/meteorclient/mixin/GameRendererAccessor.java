package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={GameRenderer.class})
public interface GameRendererAccessor {
    @Accessor(value="renderBuffers")
    public RenderBuffers meteor$renderBuffers();

    @Accessor(value="fogRenderer")
    public FogRenderer meteor$fogRenderer();
}
