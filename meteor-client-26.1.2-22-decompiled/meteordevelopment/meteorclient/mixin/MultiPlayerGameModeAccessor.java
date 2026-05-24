package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={MultiPlayerGameMode.class})
public interface MultiPlayerGameModeAccessor {
    @Accessor(value="destroyProgress")
    public float meteor$getBreakingProgress();

    @Accessor(value="destroyProgress")
    public void meteor$setDestroyProgress(float var1);

    @Accessor(value="destroyBlockPos")
    public BlockPos meteor$getCurrentBreakingBlockPos();
}
