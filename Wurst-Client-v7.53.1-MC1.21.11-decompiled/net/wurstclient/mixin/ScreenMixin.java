package net.wurstclient.mixin;

import net.minecraft.class_332;
import net.minecraft.class_362;
import net.minecraft.class_4068;
import net.minecraft.class_437;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_437.class})
public abstract class ScreenMixin
extends class_362
implements class_4068 {
    @Inject(method={"method_52752"}, at={@At(value="HEAD")}, cancellable=true)
    public void onRenderInGameBackground(class_332 context, CallbackInfo ci) {
        if (WurstClient.INSTANCE.getHax().noBackgroundHack.shouldCancelBackground((class_437)this)) {
            ci.cancel();
        }
    }
}
