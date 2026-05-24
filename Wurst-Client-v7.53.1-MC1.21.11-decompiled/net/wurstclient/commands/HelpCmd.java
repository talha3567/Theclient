package net.wurstclient.commands;

import java.util.ArrayList;
import net.wurstclient.DontBlock;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.MathUtils;

@DontBlock
public final class HelpCmd
extends Command {
    private static final int CMDS_PER_PAGE = 8;

    public HelpCmd() {
        super("help", "Shows help for a command or a list of commands.", ".help <command>", "List commands: .help [<page>]");
    }

    @Override
    public void call(String[] args) throws CmdException {
        String arg;
        if (args.length > 1) {
            throw new CmdSyntaxError();
        }
        String string = arg = args.length > 0 ? args[0] : "1";
        if (MathUtils.isInteger(arg)) {
            this.listCommands(Integer.parseInt(arg));
        } else {
            this.help(arg);
        }
    }

    private void listCommands(int page) throws CmdException {
        ArrayList<Command> cmds = new ArrayList<Command>(WURST.getCmds().getAllCmds());
        int pages = (int)Math.ceil((double)cmds.size() / 8.0);
        if (page > (pages = Math.max(pages, 1)) || page < 1) {
            throw new CmdSyntaxError("Invalid page: " + page);
        }
        String total = "Total: " + cmds.size() + " command";
        total = total + (cmds.size() != 1 ? "s" : "");
        ChatUtils.message(total);
        int start = (page - 1) * 8;
        int end = Math.min(page * 8, cmds.size());
        ChatUtils.message("Command list (page " + page + "/" + pages + ")");
        for (int i = start; i < end; ++i) {
            ChatUtils.message("- " + cmds.get(i).getName());
        }
    }

    private void help(String cmdName) throws CmdException {
        Command cmd;
        if (cmdName.startsWith(".")) {
            cmdName = cmdName.substring(1);
        }
        if ((cmd = WURST.getCmds().getCmdByName(cmdName)) == null) {
            throw new CmdSyntaxError("Unknown command: ." + cmdName);
        }
        ChatUtils.message("Available help for ." + cmdName + ":");
        cmd.printHelp();
    }
}
