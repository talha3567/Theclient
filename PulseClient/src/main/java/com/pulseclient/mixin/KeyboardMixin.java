package com.pulseclient.mixin;

import com.pulseclient.PulseClient;
import com.pulseclient.modules.render.PulseClickGui;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void onKey(int key, int scancode, int action, int mods, CallbackInfo ci) {
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT && action == GLFW.GLFW_PRESS) {
            PulseClient.INSTANCE.moduleManager.getModules().stream()
                .filter(m -> m instanceof PulseClickGui)
                .findFirst()
                .ifPresent(m -> m.setEnabled(true));
        }
    }
}
