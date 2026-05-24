package net.wurstclient;

import net.minecraft.class_310;
import net.minecraft.class_744;
import net.minecraft.class_746;
import net.wurstclient.WurstClient;

public final class InputFaker
extends Enum<InputFaker> {
    private static final class_310 MC;
    private static class_744 realInput;
    private static int swapDepth;
    private static final /* synthetic */ InputFaker[] $VALUES;

    public static InputFaker[] values() {
        return (InputFaker[])$VALUES.clone();
    }

    public static InputFaker valueOf(String name) {
        return Enum.valueOf(InputFaker.class, name);
    }

    public static void swapIfNeeded() {
        if (!WurstClient.INSTANCE.getHax().freecamHack.isMovingCamera()) {
            return;
        }
        if (++swapDepth > 1) {
            return;
        }
        class_746 player = InputFaker.MC.field_1724;
        realInput = player.field_3913;
        player.field_3913.method_3129();
        player.field_3913 = new class_744();
    }

    public static void restoreIfNeeded() {
        if (swapDepth > 0) {
            --swapDepth;
        }
        if (swapDepth > 0 || realInput == null) {
            return;
        }
        InputFaker.MC.field_1724.field_3913 = realInput;
        realInput = null;
    }

    private static /* synthetic */ InputFaker[] $values() {
        return new InputFaker[0];
    }

    static {
        $VALUES = InputFaker.$values();
        MC = WurstClient.MC;
    }

    public static class TempRealInput
    implements AutoCloseable {
        public TempRealInput() {
            if (realInput != null) {
                InputFaker.MC.field_1724.field_3913 = realInput;
            }
        }

        @Override
        public void close() {
            if (realInput != null) {
                InputFaker.MC.field_1724.field_3913 = new class_744();
            }
        }
    }
}
