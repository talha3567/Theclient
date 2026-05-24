package net.wurstclient.command;

import net.wurstclient.command.CmdException;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;

public final class CmdSyntaxError
extends CmdException {
    public CmdSyntaxError() {
    }

    public CmdSyntaxError(String message) {
        super(message);
    }

    @Override
    public void printToChat(Command cmd) {
        String message = this.getMessage();
        if (message != null) {
            ChatUtils.syntaxError(message);
        }
        for (String line : cmd.getSyntax()) {
            ChatUtils.message(line);
        }
    }
}
