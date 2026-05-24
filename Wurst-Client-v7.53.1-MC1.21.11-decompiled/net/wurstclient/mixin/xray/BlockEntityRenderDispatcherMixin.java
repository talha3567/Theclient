package net.wurstclient.mixin.xray;

import net.minecraft.class_11659;
import net.minecraft.class_11954;
import net.minecraft.class_12075;
import net.minecraft.class_4587;
import net.minecraft.class_824;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_824.class})
public class BlockEntityRenderDispatcherMixin {
    @Inject(method={"method_3555"}, at={@At(value="HEAD")}, cancellable=true)
    private <S extends class_11954> void onRenderRenderState(S renderState, class_4587 matrices, class_11659 queue, class_12075 cameraRenderState, CallbackInfo ci) {
        if (WurstClient.INSTANCE.getHax().xRayHack.shouldHideBlockEntity(renderState)) {
            ci.cancel();
        }
    }
}
