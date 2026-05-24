package net.wurstclient.events;

import java.util.ArrayList;
import net.wurstclient.event.CancellableEvent;
import net.wurstclient.event.Listener;

public interface RightClickListener
extends Listener {
    public void onRightClick(RightClickEvent var1);

    public static class RightClickEvent
    extends CancellableEvent<RightClickListener> {
        @Override
        public void fire(ArrayList<RightClickListener> listeners) {
            for (RightClickListener listener : listeners) {
                listener.onRightClick(this);
                if (!this.isCancelled()) continue;
                break;
            }
        }

        @Override
        public Class<RightClickListener> getListenerType() {
            return RightClickListener.class;
        }
    }
}
