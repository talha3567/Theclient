package net.wurstclient.events;

import java.util.ArrayList;
import net.wurstclient.event.CancellableEvent;
import net.wurstclient.event.Listener;

public interface IsNormalCubeListener
extends Listener {
    public void onIsNormalCube(IsNormalCubeEvent var1);

    public static class IsNormalCubeEvent
    extends CancellableEvent<IsNormalCubeListener> {
        @Override
        public void fire(ArrayList<IsNormalCubeListener> listeners) {
            for (IsNormalCubeListener listener : listeners) {
                listener.onIsNormalCube(this);
                if (!this.isCancelled()) continue;
                break;
            }
        }

        @Override
        public Class<IsNormalCubeListener> getListenerType() {
            return IsNormalCubeListener.class;
        }
    }
}
