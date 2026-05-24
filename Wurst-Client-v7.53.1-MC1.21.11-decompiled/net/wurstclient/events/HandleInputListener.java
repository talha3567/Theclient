package net.wurstclient.events;

import java.util.ArrayList;
import net.wurstclient.event.Event;
import net.wurstclient.event.Listener;

public interface HandleInputListener
extends Listener {
    public void onHandleInput();

    public static class HandleInputEvent
    extends Event<HandleInputListener> {
        public static final HandleInputEvent INSTANCE = new HandleInputEvent();

        @Override
        public void fire(ArrayList<HandleInputListener> listeners) {
            for (HandleInputListener listener : listeners) {
                listener.onHandleInput();
            }
        }

        @Override
        public Class<HandleInputListener> getListenerType() {
            return HandleInputListener.class;
        }
    }
}
