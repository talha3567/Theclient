package net.wurstclient.commands;

import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;

public final class GetPosCmd
extends Command {
    public GetPosCmd() {
        super("getpos", "Shows your current position.", ".getpos", "Copy to clipboard: .getpos copy");
    }

    @Override
    public void call(String[] args) throws CmdException {
        class_2338 pos = class_2338.method_49638((class_2374)GetPosCmd.MC.field_1724.method_73189());
        String posString = pos.method_10263() + " " + pos.method_10264() + " " + pos.method_10260();
        switch (String.join((CharSequence)" ", args).toLowerCase()) {
            case "": {
                ChatUtils.message("Position: " + posString);
                break;
            }
            case "copy": {
                GetPosCmd.MC.field_1774.method_1455(posString);
                ChatUtils.message("Position copied to clipboard.");
                break;
            }
            default: {
                throw new CmdSyntaxError();
            }
        }
    }

    @Override
    public String getPrimaryAction() {
        return "Get Position";
    }

    @Override
    public void doPrimaryAction() {
        WURST.getCmdProcessor().process("getpos");
    }
}
