package net.wurstclient.events;

import java.util.ArrayList;
import net.wurstclient.event.Event;
import net.wurstclient.event.Listener;

public interface IsPlayerInWaterListener
extends Listener {
    public void onIsPlayerInWater(IsPlayerInWaterEvent var1);

    public static class IsPlayerInWaterEvent
    extends Event<IsPlayerInWaterListener> {
        private boolean inWater;
        private final boolean normallyInWater;

        public IsPlayerInWaterEvent(boolean inWater) {
            this.inWater = inWater;
            this.normallyInWater = inWater;
        }

        public boolean isInWater() {
            return this.inWater;
        }

        public void setInWater(boolean inWater) {
            this.inWater = inWater;
        }

        public boolean isNormallyInWater() {
            return this.normallyInWater;
        }

        @Override
        public void fire(ArrayList<IsPlayerInWaterListener> listeners) {
            for (IsPlayerInWaterListener listener : listeners) {
                listener.onIsPlayerInWater(this);
            }
        }

        @Override
        public Class<IsPlayerInWaterListener> getListenerType() {
            return IsPlayerInWaterListener.class;
        }
    }
}
