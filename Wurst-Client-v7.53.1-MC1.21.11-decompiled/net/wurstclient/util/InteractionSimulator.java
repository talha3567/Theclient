package net.wurstclient.util;

import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.wurstclient.WurstClient;
import net.wurstclient.settings.SwingHandSetting;

public final class InteractionSimulator
extends Enum<InteractionSimulator> {
    private static final class_310 MC;
    private static final /* synthetic */ InteractionSimulator[] $VALUES;

    public static InteractionSimulator[] values() {
        return (InteractionSimulator[])$VALUES.clone();
    }

    public static InteractionSimulator valueOf(String name) {
        return Enum.valueOf(InteractionSimulator.class, name);
    }

    public static void rightClickBlock(class_3965 hitResult) {
        InteractionSimulator.rightClickBlock(hitResult, SwingHandSetting.SwingHand.CLIENT);
    }

    public static void rightClickBlock(class_3965 hitResult, SwingHandSetting.SwingHand swing) {
        for (class_1268 hand : class_1268.values()) {
            class_1799 stack = InteractionSimulator.MC.field_1724.method_5998(hand);
            if (!stack.method_45435(InteractionSimulator.MC.field_1687.method_45162())) {
                return;
            }
            if (InteractionSimulator.interactBlockAndSwing(hitResult, swing, hand, stack)) {
                return;
            }
            if (!InteractionSimulator.interactItemAndSwing(stack, swing, hand)) continue;
            return;
        }
    }

    public static void rightClickBlock(class_3965 hitResult, class_1268 hand) {
        InteractionSimulator.rightClickBlock(hitResult, hand, SwingHandSetting.SwingHand.CLIENT);
    }

    public static void rightClickBlock(class_3965 hitResult, class_1268 hand, SwingHandSetting.SwingHand swing) {
        class_1799 stack = InteractionSimulator.MC.field_1724.method_5998(hand);
        if (InteractionSimulator.interactBlockAndSwing(hitResult, swing, hand, stack)) {
            return;
        }
        InteractionSimulator.interactItemAndSwing(stack, swing, hand);
    }

    private static boolean interactBlockAndSwing(class_3965 hitResult, SwingHandSetting.SwingHand swing, class_1268 hand, class_1799 stack) {
        class_1269.class_9860 success;
        int oldCount = stack.method_7947();
        class_1269 result = InteractionSimulator.MC.field_1761.method_2896(InteractionSimulator.MC.field_1724, hand, hitResult);
        if (result instanceof class_1269.class_9860 && (success = (class_1269.class_9860)result).comp_2909() == class_1269.class_9861.field_52427) {
            swing.swing(hand);
            if (!stack.method_7960() && (stack.method_7947() != oldCount || InteractionSimulator.MC.field_1724.method_56992())) {
                InteractionSimulator.MC.field_1773.field_4012.method_3215(hand);
            }
        }
        return result instanceof class_1269.class_9860 || result instanceof class_1269.class_9857;
    }

    private static boolean interactItemAndSwing(class_1799 stack, SwingHandSetting.SwingHand swing, class_1268 hand) {
        if (stack.method_7960()) {
            return false;
        }
        class_1269 result = InteractionSimulator.MC.field_1761.method_2919((class_1657)InteractionSimulator.MC.field_1724, hand);
        if (!(result instanceof class_1269.class_9860)) {
            return false;
        }
        class_1269.class_9860 success = (class_1269.class_9860)result;
        if (success.comp_2909() == class_1269.class_9861.field_52427) {
            swing.swing(hand);
        }
        InteractionSimulator.MC.field_1773.field_4012.method_3215(hand);
        return true;
    }

    private static /* synthetic */ InteractionSimulator[] $values() {
        return new InteractionSimulator[0];
    }

    static {
        $VALUES = InteractionSimulator.$values();
        MC = WurstClient.MC;
    }
}
