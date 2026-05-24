package com.pulseclient;

import com.pulseclient.events.EventManager;
import java.util.concurrent.atomic.AtomicBoolean;

public class PulseTest {
    public static void main(String[] args) {
        testModuleRegistration();
        testEventSystem();
        System.out.println("All tests passed!");
    }

    private static void testModuleRegistration() {
        PulseClient.INSTANCE.init();
        if (PulseClient.INSTANCE.moduleManager.getModules().isEmpty()) {
            throw new RuntimeException("Module manager has no modules!");
        }
        System.out.println("Module registration test passed. Count: " + PulseClient.INSTANCE.moduleManager.getModules().size());
    }

    private static void testEventSystem() {
        AtomicBoolean received = new AtomicBoolean(false);
        EventManager.subscribe(event -> {
            if ("test".equals(event)) {
                received.set(true);
            }
        });
        EventManager.post("test");
        if (!received.get()) {
            throw new RuntimeException("Event system failed!");
        }
        System.out.println("Event system test passed.");
    }
}
