package com.pulseclient.events;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventManager {
    private static final List<Consumer<Object>> listeners = new ArrayList<>();

    public static void subscribe(Consumer<Object> listener) {
        listeners.add(listener);
    }

    public static void post(Object event) {
        for (Consumer<Object> listener : listeners) {
            listener.accept(event);
        }
    }
}
