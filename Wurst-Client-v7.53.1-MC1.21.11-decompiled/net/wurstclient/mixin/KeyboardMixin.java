package net.wurstclient.mixin;

import net.minecraft.class_11908;
import net.minecraft.class_309;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.KeyPressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_309.class})
public class KeyboardMixin {
    @Inject(method={"method_1466"}, at={@At(value="HEAD")})
    private void onOnKey(long windowHandle, int action, class_11908 arg, CallbackInfo ci) {
        EventManager.fire(new KeyPressListener.KeyPressEvent(arg.comp_4795(), arg.comp_4796(), action, arg.comp_4797()));
    }
}
