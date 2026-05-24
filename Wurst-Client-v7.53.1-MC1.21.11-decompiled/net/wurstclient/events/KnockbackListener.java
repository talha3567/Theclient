package net.wurstclient.events;

import java.util.ArrayList;
import net.wurstclient.event.Event;
import net.wurstclient.event.Listener;

public interface KnockbackListener
extends Listener {
    public void onKnockback(KnockbackEvent var1);

    public static class KnockbackEvent
    extends Event<KnockbackListener> {
        private double x;
        private double y;
        private double z;
        private final double defaultX;
        private final double defaultY;
        private final double defaultZ;

        public KnockbackEvent(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.defaultX = x;
            this.defaultY = y;
            this.defaultZ = z;
        }

        @Override
        public void fire(ArrayList<KnockbackListener> listeners) {
            for (KnockbackListener listener : listeners) {
                listener.onKnockback(this);
            }
        }

        @Override
        public Class<KnockbackListener> getListenerType() {
            return KnockbackListener.class;
        }

        public double getX() {
            return this.x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return this.y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getZ() {
            return this.z;
        }

        public void setZ(double z) {
            this.z = z;
        }

        public double getDefaultX() {
            return this.defaultX;
        }

        public double getDefaultY() {
            return this.defaultY;
        }

        public double getDefaultZ() {
            return this.defaultZ;
        }
    }
}
