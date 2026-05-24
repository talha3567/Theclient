package net.wurstclient.commands;

import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;

public final class GmCmd
extends Command {
    public GmCmd() {
        super("gm", "Shortcut for /gamemode.", ".gm <gamemode>");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length < 1) {
            throw new CmdSyntaxError();
        }
        String message = "gamemode " + (switch (args2 = String.join((CharSequence)" ", args)) {
            case "s", "0" -> "survival";
            case "c", "1" -> "creative";
            case "a", "2" -> "adventure";
            case "sp", "3" -> "spectator";
        });
        MC.method_1562().method_45730(message);
    }
}
