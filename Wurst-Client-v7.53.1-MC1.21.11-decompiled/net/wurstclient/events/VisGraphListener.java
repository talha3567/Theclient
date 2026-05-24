package net.wurstclient.events;

import java.util.ArrayList;
import net.wurstclient.event.CancellableEvent;
import net.wurstclient.event.Listener;

public interface VisGraphListener
extends Listener {
    public void onVisGraph(VisGraphEvent var1);

    public static class VisGraphEvent
    extends CancellableEvent<VisGraphListener> {
        @Override
        public void fire(ArrayList<VisGraphListener> listeners) {
            for (VisGraphListener listener : listeners) {
                listener.onVisGraph(this);
                if (!this.isCancelled()) continue;
                break;
            }
        }

        @Override
        public Class<VisGraphListener> getListenerType() {
            return VisGraphListener.class;
        }
    }
}
