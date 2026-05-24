package meteordevelopment.meteorclient.events.meteor;

import meteordevelopment.meteorclient.events.Cancellable;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import net.minecraft.client.input.KeyEvent;

public class KeyInputEvent
extends Cancellable {
    private static final KeyInputEvent INSTANCE = new KeyInputEvent();
    public KeyEvent input;
    public KeyAction action;

    public static KeyInputEvent get(KeyEvent input, KeyAction action) {
        INSTANCE.setCancelled(false);
        KeyInputEvent.INSTANCE.input = input;
        KeyInputEvent.INSTANCE.action = action;
        return INSTANCE;
    }

    public int key() {
        return KeyInputEvent.INSTANCE.input.key();
    }

    public int modifiers() {
        return KeyInputEvent.INSTANCE.input.modifiers();
    }
}
