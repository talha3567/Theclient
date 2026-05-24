package net.wurstclient.events;

import java.util.ArrayList;
import net.wurstclient.event.Event;
import net.wurstclient.event.Listener;

public interface MouseButtonPressListener
extends Listener {
    public void onMouseButtonPress(MouseButtonPressEvent var1);

    public static class MouseButtonPressEvent
    extends Event<MouseButtonPressListener> {
        private final int button;
        private final int action;

        public MouseButtonPressEvent(int button, int action) {
            this.button = button;
            this.action = action;
        }

        @Override
        public void fire(ArrayList<MouseButtonPressListener> listeners) {
            for (MouseButtonPressListener listener : listeners) {
                listener.onMouseButtonPress(this);
            }
        }

        @Override
        public Class<MouseButtonPressListener> getListenerType() {
            return MouseButtonPressListener.class;
        }

        public int getButton() {
            return this.button;
        }

        public int getAction() {
            return this.action;
        }
    }
}
