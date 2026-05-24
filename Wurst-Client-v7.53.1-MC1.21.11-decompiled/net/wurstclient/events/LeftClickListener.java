package net.wurstclient.events;

import java.util.ArrayList;
import net.wurstclient.event.CancellableEvent;
import net.wurstclient.event.Listener;

public interface LeftClickListener
extends Listener {
    public void onLeftClick(LeftClickEvent var1);

    public static class LeftClickEvent
    extends CancellableEvent<LeftClickListener> {
        @Override
        public void fire(ArrayList<LeftClickListener> listeners) {
            for (LeftClickListener listener : listeners) {
                listener.onLeftClick(this);
                if (!this.isCancelled()) continue;
                break;
            }
        }

        @Override
        public Class<LeftClickListener> getListenerType() {
            return LeftClickListener.class;
        }
    }
}
