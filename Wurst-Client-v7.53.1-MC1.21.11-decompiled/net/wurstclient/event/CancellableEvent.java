package net.wurstclient.event;

import net.wurstclient.event.Event;
import net.wurstclient.event.Listener;

public abstract class CancellableEvent<T extends Listener>
extends Event<T> {
    private boolean cancelled = false;

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }
}
