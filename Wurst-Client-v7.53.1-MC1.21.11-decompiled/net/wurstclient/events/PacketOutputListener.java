package net.wurstclient.events;

import java.util.ArrayList;
import net.minecraft.class_2596;
import net.wurstclient.event.CancellableEvent;
import net.wurstclient.event.Listener;

public interface PacketOutputListener
extends Listener {
    public void onSentPacket(PacketOutputEvent var1);

    public static class PacketOutputEvent
    extends CancellableEvent<PacketOutputListener> {
        private class_2596<?> packet;

        public PacketOutputEvent(class_2596<?> packet) {
            this.packet = packet;
        }

        public class_2596<?> getPacket() {
            return this.packet;
        }

        public void setPacket(class_2596<?> packet) {
            this.packet = packet;
        }

        @Override
        public void fire(ArrayList<PacketOutputListener> listeners) {
            for (PacketOutputListener listener : listeners) {
                listener.onSentPacket(this);
                if (!this.isCancelled()) continue;
                break;
            }
        }

        @Override
        public Class<PacketOutputListener> getListenerType() {
            return PacketOutputListener.class;
        }
    }
}
