package net.wurstclient.command;

import net.wurstclient.command.Command;

public abstract class CmdException
extends Exception {
    public CmdException() {
    }

    public CmdException(String message) {
        super(message);
    }

    public abstract void printToChat(Command var1);
}
