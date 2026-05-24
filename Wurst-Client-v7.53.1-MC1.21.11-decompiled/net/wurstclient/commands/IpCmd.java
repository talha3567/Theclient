package net.wurstclient.commands;

import net.minecraft.class_642;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.LastServerRememberer;

public final class IpCmd
extends Command {
    public IpCmd() {
        super("ip", "Shows the IP of the server you are currently\nconnected to or copies it to the clipboard.", ".ip", "Copy to clipboard: .ip copy");
    }

    @Override
    public void call(String[] args) throws CmdException {
        String ip = this.getIP();
        switch (String.join((CharSequence)" ", args).toLowerCase()) {
            case "": {
                ChatUtils.message("IP: " + ip);
                break;
            }
            case "copy": {
                IpCmd.MC.field_1774.method_1455(ip);
                ChatUtils.message("IP copied to clipboard.");
                break;
            }
            default: {
                throw new CmdSyntaxError();
            }
        }
    }

    private String getIP() {
        class_642 lastServer = LastServerRememberer.getLastServer();
        if (lastServer == null || MC.method_1496()) {
            return "127.0.0.1:25565";
        }
        Object ip = lastServer.field_3761;
        if (!((String)ip).contains(":")) {
            ip = (String)ip + ":25565";
        }
        return ip;
    }

    @Override
    public String getPrimaryAction() {
        return "Get IP";
    }

    @Override
    public void doPrimaryAction() {
        WURST.getCmdProcessor().process("ip");
    }
}
