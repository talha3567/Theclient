package net.wurstclient.commands;

import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;

public final class JumpCmd
extends Command {
    public JumpCmd() {
        super("jump", "Makes you jump.", new String[0]);
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length != 0) {
            throw new CmdSyntaxError();
        }
        if (!JumpCmd.MC.field_1724.method_24828() && !JumpCmd.WURST.getHax().jetpackHack.isEnabled()) {
            throw new CmdError("Can't jump in mid-air.");
        }
        JumpCmd.MC.field_1724.method_6043();
    }

    @Override
    public String getPrimaryAction() {
        return "Jump";
    }

    @Override
    public void doPrimaryAction() {
        WURST.getCmdProcessor().process("jump");
    }
}
