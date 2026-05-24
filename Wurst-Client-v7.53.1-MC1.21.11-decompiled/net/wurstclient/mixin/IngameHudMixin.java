package net.wurstclient.mixin;

import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import net.wurstclient.WurstClient;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.GUIRenderListener;
import net.wurstclient.hack.HackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_329.class})
public class IngameHudMixin {
    @Inject(method={"method_55804"}, at={@At(value="HEAD")})
    private void onRenderPlayerList(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
        if (WurstClient.MC.field_61504.method_72776()) {
            return;
        }
        float tickDelta = tickCounter.method_60637(true);
        EventManager.fire(new GUIRenderListener.GUIRenderEvent(context, tickDelta));
    }

    @Inject(method={"method_31977"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderOverlay(class_332 context, class_2960 texture, float opacity, CallbackInfo ci) {
        if (texture == null) {
            return;
        }
        String path = texture.method_12832();
        HackList hax = WurstClient.INSTANCE.getHax();
        if ("textures/misc/pumpkinblur.png".equals(path) && hax.noPumpkinHack.isEnabled()) {
            ci.cancel();
        }
        if ("textures/misc/powder_snow_outline.png".equals(path) && hax.noOverlayHack.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_1735"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderVignetteOverlay(class_332 context, class_1297 entity, CallbackInfo ci) {
        HackList hax = WurstClient.INSTANCE.getHax();
        if (hax == null || !hax.noVignetteHack.isEnabled()) {
            return;
        }
        ci.cancel();
    }
}
