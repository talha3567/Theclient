package meteordevelopment.meteorclient.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.ApplyTransformationEvent;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemTransform.class})
public abstract class ItemTransformMixin {
    @Inject(method={"apply"}, at={@At(value="HEAD")}, cancellable=true)
    private void onApply(boolean applyLeftHandFix, PoseStack.Pose pose, CallbackInfo ci) {
        ApplyTransformationEvent event = MeteorClient.EVENT_BUS.post(ApplyTransformationEvent.get((ItemTransform)this, applyLeftHandFix));
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
