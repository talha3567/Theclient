package net.wurstclient.util;

import java.util.stream.Stream;
import net.minecraft.class_1661;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.wurstclient.Feature;
import net.wurstclient.WurstClient;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.InventoryUtils;
import net.wurstclient.util.ItemUtils;

public final class CmdUtils
extends Enum<CmdUtils> {
    private static final class_310 MC;
    private static final /* synthetic */ CmdUtils[] $VALUES;

    public static CmdUtils[] values() {
        return (CmdUtils[])$VALUES.clone();
    }

    public static CmdUtils valueOf(String name) {
        return Enum.valueOf(CmdUtils.class, name);
    }

    public static Feature findFeature(String name) throws CmdError {
        Stream<Object> stream = WurstClient.INSTANCE.getNavigator().getList().stream();
        Feature feature = (stream = stream.filter(f -> name.equalsIgnoreCase(f.getName()))).findFirst().orElse(null);
        if (feature == null) {
            throw new CmdError("A feature named \"" + name + "\" could not be found.");
        }
        return feature;
    }

    public static Setting findSetting(Feature feature, String name) throws CmdError {
        name = name.replace("_", " ").toLowerCase();
        Setting setting = feature.getSettings().get(name);
        if (setting == null) {
            throw new CmdError("A setting named \"" + name + "\" could not be found in " + feature.getName() + ".");
        }
        return setting;
    }

    public static class_1792 parseItem(String nameOrId) throws CmdSyntaxError {
        class_1792 item = ItemUtils.getItemFromNameOrID(nameOrId);
        if (item == null) {
            throw new CmdSyntaxError("\"" + nameOrId + "\" is not a valid item.");
        }
        return item;
    }

    public static void giveItem(class_1799 stack) throws CmdError {
        class_1661 inventory = CmdUtils.MC.field_1724.method_31548();
        int slot = inventory.method_7376();
        if (slot < 0) {
            throw new CmdError("Cannot give item. Your inventory is full.");
        }
        InventoryUtils.setCreativeStack(slot, stack);
    }

    private static /* synthetic */ CmdUtils[] $values() {
        return new CmdUtils[0];
    }

    static {
        $VALUES = CmdUtils.$values();
        MC = WurstClient.MC;
    }
}
