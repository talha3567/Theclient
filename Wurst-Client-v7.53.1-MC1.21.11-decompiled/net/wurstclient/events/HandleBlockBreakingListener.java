package net.wurstclient.events;

import java.util.ArrayList;
import net.wurstclient.event.CancellableEvent;
import net.wurstclient.event.Listener;

public interface HandleBlockBreakingListener
extends Listener {
    public void onHandleBlockBreaking(HandleBlockBreakingEvent var1);

    public static class HandleBlockBreakingEvent
    extends CancellableEvent<HandleBlockBreakingListener> {
        @Override
        public void fire(ArrayList<HandleBlockBreakingListener> listeners) {
            for (HandleBlockBreakingListener listener : listeners) {
                listener.onHandleBlockBreaking(this);
                if (!this.isCancelled()) continue;
                break;
            }
        }

        @Override
        public Class<HandleBlockBreakingListener> getListenerType() {
            return HandleBlockBreakingListener.class;
        }
    }
}
