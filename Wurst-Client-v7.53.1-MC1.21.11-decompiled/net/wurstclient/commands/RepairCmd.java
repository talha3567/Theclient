package net.wurstclient.commands;

import net.minecraft.class_1799;
import net.minecraft.class_746;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.InventoryUtils;

public final class RepairCmd
extends Command {
    public RepairCmd() {
        super("repair", "Repairs the held item. Requires creative mode.", ".repair");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length > 0) {
            throw new CmdSyntaxError();
        }
        class_746 player = RepairCmd.MC.field_1724;
        if (!player.method_31549().field_7477) {
            throw new CmdError("Creative mode only.");
        }
        int slot = player.method_31548().method_67532();
        class_1799 stack = this.getHeldStack(player);
        stack.method_7974(0);
        InventoryUtils.setCreativeStack(slot, stack);
        ChatUtils.message("Item repaired.");
    }

    private class_1799 getHeldStack(class_746 player) throws CmdError {
        class_1799 stack = player.method_31548().method_7391();
        if (stack.method_7960()) {
            throw new CmdError("You need an item in your hand.");
        }
        if (!stack.method_7963()) {
            throw new CmdError("This item can't take damage.");
        }
        if (!stack.method_7986()) {
            throw new CmdError("This item is not damaged.");
        }
        return stack;
    }

    @Override
    public String getPrimaryAction() {
        return "Repair Current Item";
    }

    @Override
    public void doPrimaryAction() {
        WURST.getCmdProcessor().process("repair");
    }
}
