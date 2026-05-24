package net.wurstclient.events;

import java.util.ArrayList;
import net.wurstclient.event.Event;
import net.wurstclient.event.Listener;

public interface AirStrafingSpeedListener
extends Listener {
    public void onGetAirStrafingSpeed(AirStrafingSpeedEvent var1);

    public static class AirStrafingSpeedEvent
    extends Event<AirStrafingSpeedListener> {
        private float airStrafingSpeed;
        private final float defaultSpeed;

        public AirStrafingSpeedEvent(float airStrafingSpeed) {
            this.airStrafingSpeed = airStrafingSpeed;
            this.defaultSpeed = airStrafingSpeed;
        }

        public float getSpeed() {
            return this.airStrafingSpeed;
        }

        public void setSpeed(float airStrafingSpeed) {
            this.airStrafingSpeed = airStrafingSpeed;
        }

        public float getDefaultSpeed() {
            return this.defaultSpeed;
        }

        @Override
        public void fire(ArrayList<AirStrafingSpeedListener> listeners) {
            for (AirStrafingSpeedListener listener : listeners) {
                listener.onGetAirStrafingSpeed(this);
            }
        }

        @Override
        public Class<AirStrafingSpeedListener> getListenerType() {
            return AirStrafingSpeedListener.class;
        }
    }
}
