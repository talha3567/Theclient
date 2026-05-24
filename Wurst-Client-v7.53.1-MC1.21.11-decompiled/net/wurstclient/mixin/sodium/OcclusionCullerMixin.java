package net.wurstclient.mixin.sodium;

import net.wurstclient.event.EventManager;
import net.wurstclient.events.VisGraphListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets={"net/caffeinemc/mods/sodium/client/render/chunk/occlusion/OcclusionCuller"})
public class OcclusionCullerMixin {
    @ModifyVariable(method={"findVisible(Lnet/caffeinemc/mods/sodium/client/render/chunk/lists/RenderSectionVisitor;Lnet/caffeinemc/mods/sodium/client/render/viewport/Viewport;FZI)V"}, at=@At(value="HEAD"), argsOnly=true, ordinal=0, require=0, remap=false)
    private boolean onFindVisible(boolean useOcclusionCulling) {
        VisGraphListener.VisGraphEvent event = new VisGraphListener.VisGraphEvent();
        EventManager.fire(event);
        if (event.isCancelled()) {
            return false;
        }
        return useOcclusionCulling;
    }
}
