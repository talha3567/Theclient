package net.wurstclient.commands;

import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;

public final class ClearCmd
extends Command {
    public ClearCmd() {
        super("clear", "Clears the chat completely.", ".clear");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length > 0) {
            throw new CmdSyntaxError();
        }
        ClearCmd.MC.field_1705.method_1743().method_1808(true);
    }
}
