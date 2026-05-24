package net.wurstclient.events;

import java.util.ArrayList;
import net.wurstclient.event.Event;
import net.wurstclient.event.Listener;

public interface MouseUpdateListener
extends Listener {
    public void onMouseUpdate(MouseUpdateEvent var1);

    public static class MouseUpdateEvent
    extends Event<MouseUpdateListener> {
        private double deltaX;
        private double deltaY;
        private final double defaultDeltaX;
        private final double defaultDeltaY;

        public MouseUpdateEvent(double deltaX, double deltaY) {
            this.deltaX = deltaX;
            this.deltaY = deltaY;
            this.defaultDeltaX = deltaX;
            this.defaultDeltaY = deltaY;
        }

        @Override
        public void fire(ArrayList<MouseUpdateListener> listeners) {
            for (MouseUpdateListener listener : listeners) {
                listener.onMouseUpdate(this);
            }
        }

        @Override
        public Class<MouseUpdateListener> getListenerType() {
            return MouseUpdateListener.class;
        }

        public double getDeltaX() {
            return this.deltaX;
        }

        public void setDeltaX(double deltaX) {
            this.deltaX = deltaX;
        }

        public double getDeltaY() {
            return this.deltaY;
        }

        public void setDeltaY(double deltaY) {
            this.deltaY = deltaY;
        }

        public double getDefaultDeltaX() {
            return this.defaultDeltaX;
        }

        public double getDefaultDeltaY() {
            return this.defaultDeltaY;
        }
    }
}
