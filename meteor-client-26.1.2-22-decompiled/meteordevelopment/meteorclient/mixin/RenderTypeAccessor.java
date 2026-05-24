package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={RenderType.class})
public interface RenderTypeAccessor {
    @Accessor(value="state")
    public RenderSetup getState();
}
