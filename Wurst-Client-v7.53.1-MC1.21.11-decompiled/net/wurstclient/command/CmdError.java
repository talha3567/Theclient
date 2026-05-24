package net.wurstclient.command;

import net.wurstclient.command.CmdException;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;

public final class CmdError
extends CmdException {
    public CmdError(String message) {
        super(message);
    }

    @Override
    public void printToChat(Command cmd) {
        ChatUtils.error(this.getMessage());
    }
}
