package net.wurstclient.commands;

import net.wurstclient.command.CmdException;
import net.wurstclient.command.Command;

public final class BindCmd
extends Command {
    public BindCmd() {
        super("bind", "Shortcut for '.binds add'.", ".bind <key> <hacks>", ".bind <key> <commands>", "Multiple hacks/commands must be separated by ';'.", "Use .binds for more options.");
    }

    @Override
    public void call(String[] args) throws CmdException {
        WURST.getCmdProcessor().process("binds add " + String.join((CharSequence)" ", args));
    }
}
