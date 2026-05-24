package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.ResourceLoadStateTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ResourceLoadStateTracker.class})
public interface ResourceLoadStateTrackerAccessor {
    @Accessor(value="reloadState")
    public ResourceLoadStateTracker.ReloadState meteor$getReloadState();
}
