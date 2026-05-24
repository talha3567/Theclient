package net.wurstclient.commands;

import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_746;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;

public final class DigCmd
extends Command {
    public DigCmd() {
        super("dig", "Automatically digs out the selected area,\nstarting in the front-left-top corner.", ".dig <length> <width> <height>", ".dig stop");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {
            DigCmd.WURST.getHax().excavatorHack.setEnabled(false);
        } else {
            this.startDigging(args);
        }
    }

    private void startDigging(String[] args) throws CmdSyntaxError {
        if (args.length != 3) {
            throw new CmdSyntaxError();
        }
        int length = this.tryParseInt(args[0], "length");
        int width = this.tryParseInt(args[1], "width");
        int height = this.tryParseInt(args[2], "height");
        class_746 player = DigCmd.MC.field_1724;
        class_2350 direction = player.method_5735();
        class_2338 pos1 = class_2338.method_49638((class_2374)player.method_73189().method_1031(0.0, (double)player.method_18381(player.method_18376()), 0.0));
        if (height < 0) {
            pos1 = pos1.method_10074();
        }
        class_2338 pos2 = pos1.method_10079(direction, length > 0 ? length - 1 : length + 1);
        pos2 = pos2.method_10079(direction.method_10170(), width > 0 ? width - 1 : width + 1);
        pos2 = pos2.method_10087(height > 0 ? height - 1 : height + 1);
        DigCmd.WURST.getHax().excavatorHack.enableWithArea(pos1, pos2);
    }

    private int tryParseInt(String input, String name) throws CmdSyntaxError {
        int i;
        try {
            i = Integer.parseInt(input);
        }
        catch (NumberFormatException e) {
            throw new CmdSyntaxError("Invalid " + name + ": " + input);
        }
        if (i == 0) {
            throw new CmdSyntaxError(name + " can't be zero");
        }
        return i;
    }
}
