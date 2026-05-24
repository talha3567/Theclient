package net.wurstclient.mixin;

import net.minecraft.class_2561;
import net.minecraft.class_342;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.wurstclient.WurstClient;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.ChatOutputListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_408.class})
public abstract class ChatScreenMixin
extends class_437 {
    @Shadow
    protected class_342 field_2382;

    private ChatScreenMixin(WurstClient wurst, class_2561 title) {
        super(title);
    }

    @Inject(method={"method_25426"}, at={@At(value="TAIL")})
    protected void onInit(CallbackInfo ci) {
        if (WurstClient.INSTANCE.getHax().infiniChatHack.isEnabled()) {
            this.field_2382.method_1880(Integer.MAX_VALUE);
        }
    }

    @Inject(method={"method_44056"}, at={@At(value="HEAD")}, cancellable=true)
    public void onSendMessage(String message, boolean addToHistory, CallbackInfo ci) {
        if ((message = this.method_44054(message)).isEmpty()) {
            return;
        }
        ChatOutputListener.ChatOutputEvent event = new ChatOutputListener.ChatOutputEvent(message);
        EventManager.fire(event);
        boolean cancelled = event.isCancelled();
        if (!cancelled && !event.isModified()) {
            return;
        }
        ci.cancel();
        String newMessage = event.getMessage();
        if (addToHistory) {
            this.field_22787.field_1705.method_1743().method_1803(newMessage);
        }
        if (!cancelled) {
            if (newMessage.startsWith("/")) {
                this.field_22787.field_1724.field_3944.method_45730(newMessage.substring(1));
            } else {
                this.field_22787.field_1724.field_3944.method_45729(newMessage);
            }
        }
    }

    @Shadow
    public abstract String method_44054(String var1);
}
