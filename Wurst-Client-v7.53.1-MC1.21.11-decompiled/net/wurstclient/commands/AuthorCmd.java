package net.wurstclient.commands;

import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_9302;
import net.minecraft.class_9334;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;

public final class AuthorCmd
extends Command {
    public AuthorCmd() {
        super("author", "Changes the author of a written book.\nRequires creative mode.", ".author <author>");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length == 0) {
            throw new CmdSyntaxError();
        }
        if (!AuthorCmd.MC.field_1724.method_31549().field_7477) {
            throw new CmdError("Creative mode only.");
        }
        class_1799 heldStack = AuthorCmd.MC.field_1724.method_31548().method_7391();
        if (!heldStack.method_31574(class_1802.field_8360)) {
            throw new CmdError("You must hold a written book in your main hand.");
        }
        class_9302 oldData = (class_9302)heldStack.method_57353().method_58694(class_9334.field_49606);
        if (oldData == null) {
            throw new CmdError("Can't find book data.");
        }
        String author = String.join((CharSequence)" ", args);
        class_9302 newData = new class_9302(oldData.comp_2419(), author, oldData.comp_2421(), oldData.comp_2422(), oldData.comp_2423());
        heldStack.method_57379(class_9334.field_49606, (Object)newData);
    }
}
