package net.wurstclient.commands;

import net.minecraft.class_638;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;

public final class LeaveCmd
extends Command {
    public LeaveCmd() {
        super("leave", "Instantly disconnects from the server.", ".leave");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length == 1 && args[0].equalsIgnoreCase("taco")) {
            for (int i = 0; i < 128; ++i) {
                MC.method_1562().method_45729("Taco!");
            }
        } else if (args.length != 0) {
            throw new CmdSyntaxError();
        }
        LeaveCmd.MC.field_1687.method_8525(class_638.field_61021);
    }

    @Override
    public String getPrimaryAction() {
        return "Leave";
    }

    @Override
    public void doPrimaryAction() {
        WURST.getCmdProcessor().process("leave");
    }
}
