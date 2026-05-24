package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ItemStackRenderState.LayerRenderState.class})
public interface LayerRenderStateAccessor {
    @Accessor(value="itemTransform")
    public ItemTransform meteor$getTransform();
}
