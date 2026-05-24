package net.wurstclient.events;

import java.util.ArrayList;
import net.wurstclient.event.Event;
import net.wurstclient.event.Listener;

public interface KeyPressListener
extends Listener {
    public void onKeyPress(KeyPressEvent var1);

    public static class KeyPressEvent
    extends Event<KeyPressListener> {
        private final int keyCode;
        private final int scanCode;
        private final int action;
        private final int modifiers;

        public KeyPressEvent(int keyCode, int scanCode, int action, int modifiers) {
            this.keyCode = keyCode;
            this.scanCode = scanCode;
            this.action = action;
            this.modifiers = modifiers;
        }

        @Override
        public void fire(ArrayList<KeyPressListener> listeners) {
            for (KeyPressListener listener : listeners) {
                listener.onKeyPress(this);
            }
        }

        @Override
        public Class<KeyPressListener> getListenerType() {
            return KeyPressListener.class;
        }

        public int getKeyCode() {
            return this.keyCode;
        }

        public int getScanCode() {
            return this.scanCode;
        }

        public int getAction() {
            return this.action;
        }

        public int getModifiers() {
            return this.modifiers;
        }
    }
}
