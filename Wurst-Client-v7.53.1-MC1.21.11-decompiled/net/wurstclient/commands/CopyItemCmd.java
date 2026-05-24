package net.wurstclient.commands;

import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_742;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.CmdUtils;

public final class CopyItemCmd
extends Command {
    public CopyItemCmd() {
        super("copyitem", "Allows you to copy items that other people are holding\nor wearing. Requires creative mode.", ".copyitem <player> <slot>", "Valid slots: hand, head, chest, legs, feet");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length != 2) {
            throw new CmdSyntaxError();
        }
        if (!CopyItemCmd.MC.field_1724.method_31549().field_7477) {
            throw new CmdError("Creative mode only.");
        }
        class_742 player = this.getPlayer(args[0]);
        class_1799 item = this.getItem(player, args[1]);
        CmdUtils.giveItem(item);
        ChatUtils.message("Item copied.");
    }

    private class_742 getPlayer(String name) throws CmdError {
        for (class_742 player : CopyItemCmd.MC.field_1687.method_18456()) {
            if (!player.method_5477().getString().equalsIgnoreCase(name)) continue;
            return player;
        }
        throw new CmdError("Player \"" + name + "\" could not be found.");
    }

    private class_1799 getItem(class_742 player, String slot) throws CmdSyntaxError {
        switch (slot.toLowerCase()) {
            case "hand": {
                return player.method_6047();
            }
            case "head": {
                return player.method_6118(class_1304.field_6169);
            }
            case "chest": {
                return player.method_6118(class_1304.field_6174);
            }
            case "legs": {
                return player.method_6118(class_1304.field_6172);
            }
            case "feet": {
                return player.method_6118(class_1304.field_6166);
            }
        }
        throw new CmdSyntaxError();
    }
}
