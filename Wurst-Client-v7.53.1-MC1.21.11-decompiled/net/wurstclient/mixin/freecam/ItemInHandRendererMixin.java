package net.wurstclient.mixin.freecam;

import net.minecraft.class_11659;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import net.minecraft.class_759;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_759.class})
public abstract class ItemInHandRendererMixin {
    @Inject(method={"method_22976"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderHandsWithItems(float tickProgress, class_4587 matrices, class_11659 entityRenderCommandQueue, class_746 player, int light, CallbackInfo ci) {
        if (WurstClient.INSTANCE.getHax().freecamHack.shouldHideHand()) {
            ci.cancel();
        }
    }
}
