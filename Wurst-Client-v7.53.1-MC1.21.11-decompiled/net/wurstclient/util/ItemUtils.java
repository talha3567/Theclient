package net.wurstclient.util;

import java.util.OptionalDouble;
import net.minecraft.class_10192;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1304;
import net.minecraft.class_1320;
import net.minecraft.class_1322;
import net.minecraft.class_151;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1844;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_5134;
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import net.minecraft.class_9285;
import net.minecraft.class_9334;
import net.wurstclient.WurstClient;
import net.wurstclient.util.MathUtils;

public final class ItemUtils
extends Enum<ItemUtils> {
    private static final class_310 MC;
    private static final /* synthetic */ ItemUtils[] $VALUES;

    public static ItemUtils[] values() {
        return (ItemUtils[])$VALUES.clone();
    }

    public static ItemUtils valueOf(String name) {
        return Enum.valueOf(ItemUtils.class, name);
    }

    public static class_1792 getItemFromNameOrID(String nameOrId) {
        if (MathUtils.isInteger(nameOrId)) {
            int id = Integer.parseInt(nameOrId);
            class_1792 item = (class_1792)class_7923.field_41178.method_10200(id);
            if (id != 0 && class_7923.field_41178.method_10206((Object)item) == 0) {
                return null;
            }
            return item;
        }
        try {
            return class_7923.field_41178.method_17966(class_2960.method_60654((String)nameOrId)).orElse(null);
        }
        catch (class_151 e) {
            return null;
        }
    }

    public static OptionalDouble getAttribute(class_1792 item, class_6880<class_1320> attribute) {
        return ((class_9285)item.method_57347().method_58695(class_9334.field_49636, (Object)class_9285.field_49326)).comp_2393().stream().filter(modifier -> modifier.comp_2395() == attribute).mapToDouble(modifier -> modifier.comp_2396().comp_2449()).findFirst();
    }

    public static double calculateModifiedAttribute(class_1792 item, class_6880<class_1320> attribute, double base, class_1304 slot) {
        class_9285 modifiers = (class_9285)item.method_57347().method_58695(class_9334.field_49636, (Object)class_9285.field_49326);
        double result = base;
        for (class_9285.class_9287 entry : modifiers.comp_2393()) {
            if (entry.comp_2395() != attribute || !entry.comp_2397().method_57286(slot)) continue;
            double value = entry.comp_2396().comp_2449();
            result += (switch (entry.comp_2396().comp_2450()) {
                default -> throw new MatchException(null, null);
                case class_1322.class_1323.field_6328 -> value;
                case class_1322.class_1323.field_6330 -> value * base;
                case class_1322.class_1323.field_6331 -> value * result;
            });
        }
        return result;
    }

    public static double getArmorAttribute(class_1792 item, class_6880<class_1320> attribute) {
        class_10192 equippable = (class_10192)item.method_57347().method_58694(class_9334.field_54196);
        double base = ItemUtils.MC.field_1724.method_45326(attribute);
        if (equippable == null) {
            return base;
        }
        return ItemUtils.calculateModifiedAttribute(item, attribute, base, equippable.comp_3174());
    }

    public static double getArmorPoints(class_1792 item) {
        return ItemUtils.getArmorAttribute(item, (class_6880<class_1320>)class_5134.field_23724);
    }

    public static double getToughness(class_1792 item) {
        return ItemUtils.getArmorAttribute(item, (class_6880<class_1320>)class_5134.field_23725);
    }

    public static class_1304 getArmorSlot(class_1792 item) {
        class_10192 equippable = (class_10192)item.method_57347().method_58694(class_9334.field_54196);
        return equippable != null ? equippable.comp_3174() : null;
    }

    public static boolean hasEffect(class_1799 stack, class_6880<class_1291> effect) {
        class_1844 potionContents = (class_1844)stack.method_57353().method_58695(class_9334.field_49651, (Object)class_1844.field_49274);
        for (class_1293 effectInstance : potionContents.method_57397()) {
            if (effectInstance.method_5579() != effect) continue;
            return true;
        }
        return false;
    }

    private static /* synthetic */ ItemUtils[] $values() {
        return new ItemUtils[0];
    }

    static {
        $VALUES = ItemUtils.$values();
        MC = WurstClient.MC;
    }
}
