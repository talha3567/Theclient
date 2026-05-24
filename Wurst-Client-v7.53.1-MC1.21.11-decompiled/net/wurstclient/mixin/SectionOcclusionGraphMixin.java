package net.wurstclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_11517;
import net.minecraft.class_2350;
import net.minecraft.class_8679;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.VisGraphListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={class_8679.class})
public class SectionOcclusionGraphMixin {
    @WrapOperation(method={"method_52825"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_11517;method_3650(Lnet/minecraft/class_2350;Lnet/minecraft/class_2350;)Z")})
    private boolean wrapFacesCanSeeEachother(class_11517 mesh, class_2350 from, class_2350 to, Operation<Boolean> original) {
        VisGraphListener.VisGraphEvent event = new VisGraphListener.VisGraphEvent();
        EventManager.fire(event);
        if (event.isCancelled()) {
            return true;
        }
        return (Boolean)original.call(new Object[]{mesh, from, to});
    }
}
