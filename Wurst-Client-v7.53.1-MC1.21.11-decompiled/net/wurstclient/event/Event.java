package net.wurstclient.event;

import java.util.ArrayList;
import net.wurstclient.event.Listener;

public abstract class Event<T extends Listener> {
    public abstract void fire(ArrayList<T> var1);

    public abstract Class<T> getListenerType();
}
