package net.wurstclient.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.Arrays;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import net.minecraft.class_9323;
import net.minecraft.class_9331;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.InventoryUtils;

public final class ModifyCmd
extends Command {
    public ModifyCmd() {
        super("modify", "Allows you to modify component data of items.", ".modify set <type> <value>", ".modify remove <type>", "Use $ for colors, use $$ for $.", "", "Example:", ".modify set custom_name {\"text\":\"$cRed Name\"}", "(changes the item's name to \u00a7cRed Name\u00a7r)");
    }

    @Override
    public void call(String[] args) throws CmdException {
        class_746 player = ModifyCmd.MC.field_1724;
        if (!player.method_31549().field_7477) {
            throw new CmdError("Creative mode only.");
        }
        if (args.length < 2) {
            throw new CmdSyntaxError();
        }
        class_1661 inventory = player.method_31548();
        int slot = inventory.method_67532();
        class_1799 stack = inventory.method_7391();
        if (stack == null) {
            throw new CmdError("You must hold an item in your main hand.");
        }
        switch (args[0].toLowerCase()) {
            case "set": {
                this.set(stack, args);
                break;
            }
            case "remove": {
                this.remove(stack, args);
                break;
            }
            default: {
                throw new CmdSyntaxError();
            }
        }
        InventoryUtils.setCreativeStack(slot, stack);
        ChatUtils.message("Item modified.");
    }

    private void set(class_1799 stack, String[] args) throws CmdException {
        if (args.length < 3) {
            throw new CmdSyntaxError();
        }
        class_9331<?> type = this.parseComponentType(args[1]);
        String valueString = String.join((CharSequence)" ", Arrays.copyOfRange(args, 2, args.length)).replace("$", "\u00a7").replace("\u00a7\u00a7", "$");
        JsonElement valueJson = this.parseJson(valueString);
        DataResult valueResult = type.method_57875().parse((DynamicOps)ModifyCmd.MC.field_1724.method_56673().method_57093((DynamicOps)JsonOps.INSTANCE), (Object)valueJson);
        Object value = valueResult.resultOrPartial().orElse(null);
        class_9323.class_9324 builder = class_9323.method_57827();
        builder.method_58756(type, value);
        stack.method_57365(builder.method_57838());
    }

    private void remove(class_1799 stack, String[] args) throws CmdException {
        if (args.length > 2) {
            throw new CmdSyntaxError();
        }
        stack.method_57379(this.parseComponentType(args[1]), null);
    }

    private class_9331<?> parseComponentType(String typeName) throws CmdError {
        class_9331 type = (class_9331)class_7923.field_49658.method_63535(class_2960.method_12829((String)typeName));
        if (type == null) {
            throw new CmdError("Component type \"" + typeName + "\" does not exist.");
        }
        return type;
    }

    private JsonElement parseJson(String jsonString) throws CmdError {
        try {
            return JsonParser.parseString(jsonString);
        }
        catch (JsonParseException e) {
            if (e.getCause() != null) {
                throw new CmdError(e.getCause().getMessage());
            }
            throw new CmdError(e.getMessage());
        }
    }
}
