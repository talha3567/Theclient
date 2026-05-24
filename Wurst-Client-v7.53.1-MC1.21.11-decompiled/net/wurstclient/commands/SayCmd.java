package net.wurstclient.commands;

import net.wurstclient.SearchTags;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;

@SearchTags(value={".legit", "dots in chat", "command bypass", "prefix"})
public final class SayCmd
extends Command {
    public SayCmd() {
        super("say", "Sends the given chat message, even if it starts with a\ndot.", ".say <message>");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length < 1) {
            throw new CmdSyntaxError();
        }
        String message = String.join((CharSequence)" ", args);
        if (message.startsWith("/")) {
            MC.method_1562().method_45730(message.substring(1));
        } else {
            MC.method_1562().method_45729(message);
        }
    }
}
