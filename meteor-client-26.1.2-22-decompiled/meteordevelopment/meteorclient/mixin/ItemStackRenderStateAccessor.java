package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ItemStackRenderState.class})
public interface ItemStackRenderStateAccessor {
    @Accessor(value="activeLayerCount")
    public int meteor$getActiveLayerCount();

    @Accessor(value="layers")
    public ItemStackRenderState.LayerRenderState[] meteor$getLayers();
}
