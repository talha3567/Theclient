package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.ResourceLoadStateTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ResourceLoadStateTracker.ReloadState.class})
public interface ReloadStateAccessor {
    @Accessor(value="finished")
    public boolean meteor$isFinished();
}
