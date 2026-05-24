package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={BlockEntityRenderState.class})
public interface BlockEntityRenderStateAccessor {
    @Accessor(value="blockState")
    public BlockState meteor$getBlockState();
}
