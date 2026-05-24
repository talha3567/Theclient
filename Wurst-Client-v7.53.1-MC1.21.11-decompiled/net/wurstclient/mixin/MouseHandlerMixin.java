package net.wurstclient.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.class_11910;
import net.minecraft.class_1661;
import net.minecraft.class_312;
import net.wurstclient.WurstClient;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.MouseButtonPressListener;
import net.wurstclient.events.MouseScrollListener;
import net.wurstclient.events.MouseUpdateListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_312.class})
public abstract class MouseHandlerMixin {
    @Shadow
    private double field_1789;
    @Shadow
    private double field_1787;

    @Inject(method={"method_1601"}, at={@At(value="HEAD")})
    private void onOnButton(long windowHandle, class_11910 mouseButtonInfo, int action, CallbackInfo ci) {
        EventManager.fire(new MouseButtonPressListener.MouseButtonPressEvent(mouseButtonInfo.comp_4801(), action));
    }

    @Inject(method={"method_1598"}, at={@At(value="RETURN")})
    private void onOnScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        EventManager.fire(new MouseScrollListener.MouseScrollEvent(vertical));
    }

    @Inject(method={"method_55793"}, at={@At(value="HEAD")})
    private void onHandleAccumulatedMovement(CallbackInfo ci) {
        MouseUpdateListener.MouseUpdateEvent event = new MouseUpdateListener.MouseUpdateEvent(this.field_1789, this.field_1787);
        EventManager.fire(event);
        this.field_1789 = event.getDeltaX();
        this.field_1787 = event.getDeltaY();
    }

    @WrapWithCondition(method={"method_1598"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1661;method_61496(I)V")})
    private boolean wrapOnScroll(class_1661 inventory, int slot) {
        WurstClient wurst = WurstClient.INSTANCE;
        return !wurst.getOtfs().zoomOtf.isControllingScrollEvents() && !wurst.getHax().freecamHack.isControllingScrollEvents() && !wurst.getHax().flightHack.isControllingScrollEvents();
    }
}
