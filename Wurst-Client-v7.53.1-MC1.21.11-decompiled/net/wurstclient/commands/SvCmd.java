package net.wurstclient.commands;

import net.minecraft.class_642;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.LastServerRememberer;

public final class SvCmd
extends Command {
    public SvCmd() {
        super("sv", "Shows the version of the server\nyou are currently connected to.", ".sv");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length != 0) {
            throw new CmdSyntaxError();
        }
        ChatUtils.message("Server version: " + this.getVersion());
    }

    private String getVersion() throws CmdError {
        if (MC.method_1496()) {
            throw new CmdError("Can't check server version in singleplayer.");
        }
        class_642 lastServer = LastServerRememberer.getLastServer();
        if (lastServer == null) {
            throw new IllegalStateException("LastServerRememberer doesn't remember the last server!");
        }
        return lastServer.field_3760.getString();
    }

    @Override
    public String getPrimaryAction() {
        return "Get Server Version";
    }

    @Override
    public void doPrimaryAction() {
        WURST.getCmdProcessor().process("sv");
    }
}
