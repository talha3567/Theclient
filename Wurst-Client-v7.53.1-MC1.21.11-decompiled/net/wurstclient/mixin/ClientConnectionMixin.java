package net.wurstclient.mixin;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.class_2535;
import net.minecraft.class_2596;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.ConnectionPacketOutputListener;
import net.wurstclient.events.PacketInputListener;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_2535.class})
public abstract class ClientConnectionMixin
extends SimpleChannelInboundHandler<class_2596<?>> {
    private ConcurrentLinkedQueue<ConnectionPacketOutputListener.ConnectionPacketOutputEvent> events = new ConcurrentLinkedQueue();

    @Inject(method={"method_10770"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_2535;method_10759(Lnet/minecraft/class_2596;Lnet/minecraft/class_2547;)V", ordinal=0)}, cancellable=true)
    private void onChannelRead0(ChannelHandlerContext context, class_2596<?> packet, CallbackInfo ci) {
        PacketInputListener.PacketInputEvent event = new PacketInputListener.PacketInputEvent(packet);
        EventManager.fire(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @ModifyVariable(method={"method_10752"}, at=@At(value="HEAD"))
    public class_2596<?> modifyPacket(class_2596<?> packet) {
        ConnectionPacketOutputListener.ConnectionPacketOutputEvent event = new ConnectionPacketOutputListener.ConnectionPacketOutputEvent(packet);
        this.events.add(event);
        EventManager.fire(event);
        return event.getPacket();
    }

    @Inject(method={"method_10752"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSend(class_2596<?> packet, @Nullable ChannelFutureListener callback, CallbackInfo ci) {
        ConnectionPacketOutputListener.ConnectionPacketOutputEvent event = this.getEvent(packet);
        if (event == null) {
            return;
        }
        if (event.isCancelled()) {
            ci.cancel();
        }
        this.events.remove(event);
    }

    private ConnectionPacketOutputListener.ConnectionPacketOutputEvent getEvent(class_2596<?> packet) {
        for (ConnectionPacketOutputListener.ConnectionPacketOutputEvent event : this.events) {
            if (event.getPacket() != packet) continue;
            return event;
        }
        return null;
    }
}
