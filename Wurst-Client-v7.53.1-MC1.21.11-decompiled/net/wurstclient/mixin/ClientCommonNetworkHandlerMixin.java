package net.wurstclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_2535;
import net.minecraft.class_2596;
import net.minecraft.class_8673;
import net.minecraft.class_8705;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.PacketOutputListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={class_8673.class})
public abstract class ClientCommonNetworkHandlerMixin
implements class_8705 {
    @WrapOperation(method={"method_52787"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_2535;method_10743(Lnet/minecraft/class_2596;)V")})
    private void wrapSendPacket(class_2535 connection, class_2596<?> packet, Operation<Void> original) {
        PacketOutputListener.PacketOutputEvent event = new PacketOutputListener.PacketOutputEvent(packet);
        EventManager.fire(event);
        if (!event.isCancelled()) {
            original.call(new Object[]{connection, event.getPacket()});
        }
    }
}
