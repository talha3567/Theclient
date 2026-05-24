package net.wurstclient.commands;

import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.MathUtils;

public final class ExcavateCmd
extends Command {
    public ExcavateCmd() {
        super("excavate", "Automatically destroys all blocks in the selected area.", ".excavate <x1> <y1> <z1> <x2> <y2> <z2>");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length != 6) {
            throw new CmdSyntaxError();
        }
        class_2338 pos1 = this.argsToXyzPos(args[0], args[1], args[2]);
        class_2338 pos2 = this.argsToXyzPos(args[3], args[4], args[5]);
        ExcavateCmd.WURST.getHax().excavatorHack.enableWithArea(pos1, pos2);
    }

    private class_2338 argsToXyzPos(String ... xyz) throws CmdSyntaxError {
        class_2338 playerPos = class_2338.method_49638((class_2374)ExcavateCmd.MC.field_1724.method_73189());
        int[] player = new int[]{playerPos.method_10263(), playerPos.method_10264(), playerPos.method_10260()};
        int[] pos = new int[3];
        for (int i = 0; i < 3; ++i) {
            if (MathUtils.isInteger(xyz[i])) {
                pos[i] = Integer.parseInt(xyz[i]);
                continue;
            }
            if (xyz[i].equals("~")) {
                pos[i] = player[i];
                continue;
            }
            if (xyz[i].startsWith("~") && MathUtils.isInteger(xyz[i].substring(1))) {
                pos[i] = player[i] + Integer.parseInt(xyz[i].substring(1));
                continue;
            }
            throw new CmdSyntaxError("Invalid coordinates.");
        }
        return new class_2338(pos[0], pos[1], pos[2]);
    }
}
