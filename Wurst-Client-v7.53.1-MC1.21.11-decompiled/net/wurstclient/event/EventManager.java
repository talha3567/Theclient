package net.wurstclient.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import net.minecraft.class_128;
import net.minecraft.class_129;
import net.minecraft.class_148;
import net.wurstclient.WurstClient;
import net.wurstclient.event.Event;
import net.wurstclient.event.Listener;

public final class EventManager {
    private final WurstClient wurst;
    private final HashMap<Class<? extends Listener>, ArrayList<? extends Listener>> listenerMap = new HashMap();

    public EventManager(WurstClient wurst) {
        this.wurst = wurst;
    }

    public static <L extends Listener, E extends Event<L>> void fire(E event) {
        EventManager eventManager = WurstClient.INSTANCE.getEventManager();
        if (eventManager == null) {
            return;
        }
        eventManager.fireImpl(event);
    }

    private <L extends Listener, E extends Event<L>> void fireImpl(E event) {
        if (!this.wurst.isEnabled()) {
            return;
        }
        try {
            Class<L> type = event.getListenerType();
            ArrayList<? extends Listener> listeners = this.listenerMap.get(type);
            if (listeners == null || listeners.isEmpty()) {
                return;
            }
            ArrayList<? extends Listener> listeners2 = new ArrayList<Listener>(listeners);
            listeners2.removeIf(Objects::isNull);
            event.fire(listeners2);
        }
        catch (Throwable e) {
            e.printStackTrace();
            class_128 report = class_128.method_560((Throwable)e, (String)"Firing Wurst event");
            class_129 section = report.method_562("Affected event");
            section.method_577("Event class", () -> event.getClass().getName());
            throw new class_148(report);
        }
    }

    public <L extends Listener> void add(Class<L> type, L listener) {
        try {
            ArrayList<? extends Listener> listeners = this.listenerMap.get(type);
            if (listeners == null) {
                listeners = new ArrayList<Listener>(Arrays.asList(listener));
                this.listenerMap.put(type, listeners);
                return;
            }
            listeners.add(listener);
        }
        catch (Throwable e) {
            e.printStackTrace();
            class_128 report = class_128.method_560((Throwable)e, (String)"Adding Wurst event listener");
            class_129 section = report.method_562("Affected listener");
            section.method_577("Listener type", () -> type.getName());
            section.method_577("Listener class", () -> listener.getClass().getName());
            throw new class_148(report);
        }
    }

    public <L extends Listener> void remove(Class<L> type, L listener) {
        try {
            ArrayList<? extends Listener> listeners = this.listenerMap.get(type);
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
            class_128 report = class_128.method_560((Throwable)e, (String)"Removing Wurst event listener");
            class_129 section = report.method_562("Affected listener");
            section.method_577("Listener type", () -> type.getName());
            section.method_577("Listener class", () -> listener.getClass().getName());
            throw new class_148(report);
        }
    }
}
