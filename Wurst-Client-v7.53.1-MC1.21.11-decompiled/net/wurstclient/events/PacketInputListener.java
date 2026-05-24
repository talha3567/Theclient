package net.wurstclient.events;

import java.util.ArrayList;
import net.minecraft.class_2596;
import net.wurstclient.event.CancellableEvent;
import net.wurstclient.event.Listener;

public interface PacketInputListener
extends Listener {
    public void onReceivedPacket(PacketInputEvent var1);

    public static class PacketInputEvent
    extends CancellableEvent<PacketInputListener> {
        private final class_2596<?> packet;

        public PacketInputEvent(class_2596<?> packet) {
            this.packet = packet;
        }

        public class_2596<?> getPacket() {
            return this.packet;
        }

        @Override
        public void fire(ArrayList<PacketInputListener> listeners) {
            for (PacketInputListener listener : listeners) {
                listener.onReceivedPacket(this);
                if (!this.isCancelled()) continue;
                break;
            }
        }

        @Override
        public Class<PacketInputListener> getListenerType() {
            return PacketInputListener.class;
        }
    }
}
