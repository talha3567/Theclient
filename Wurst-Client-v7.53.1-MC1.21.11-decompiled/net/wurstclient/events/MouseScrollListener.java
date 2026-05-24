package net.wurstclient.events;

import java.util.ArrayList;
import net.wurstclient.event.Event;
import net.wurstclient.event.Listener;

public interface MouseScrollListener
extends Listener {
    public void onMouseScroll(double var1);

    public static class MouseScrollEvent
    extends Event<MouseScrollListener> {
        private final double amount;

        public MouseScrollEvent(double amount) {
            this.amount = amount;
        }

        @Override
        public void fire(ArrayList<MouseScrollListener> listeners) {
            for (MouseScrollListener listener : listeners) {
                listener.onMouseScroll(this.amount);
            }
        }

        @Override
        public Class<MouseScrollListener> getListenerType() {
            return MouseScrollListener.class;
        }
    }
}
