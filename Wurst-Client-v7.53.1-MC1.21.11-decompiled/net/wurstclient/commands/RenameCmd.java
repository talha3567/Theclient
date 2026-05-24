package net.wurstclient.commands;

import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_9334;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;

public final class RenameCmd
extends Command {
    public RenameCmd() {
        super("rename", "Renames the item in your hand.", ".rename <new_name>", "Use $ for colors, use $$ for $.", "Example:", ".rename $cRed Name", "(changes the item's name to \u00a7cRed Name\u00a7r)");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (!RenameCmd.MC.field_1724.method_31549().field_7477) {
            throw new CmdError("Creative mode only.");
        }
        if (args.length == 0) {
            throw new CmdSyntaxError();
        }
        Object message = args[0];
        for (int i = 1; i < args.length; ++i) {
            message = (String)message + " " + args[i];
        }
        message = ((String)message).replace("$", "\u00a7").replace("\u00a7\u00a7", "$");
        class_1799 stack = RenameCmd.MC.field_1724.method_31548().method_7391();
        if (stack == null) {
            throw new CmdError("There is no item in your hand.");
        }
        stack.method_57379(class_9334.field_49631, (Object)class_2561.method_43470((String)message));
        ChatUtils.message("Renamed item to \"\u00a7o" + (String)message + "\u00a7r\".");
    }
}
