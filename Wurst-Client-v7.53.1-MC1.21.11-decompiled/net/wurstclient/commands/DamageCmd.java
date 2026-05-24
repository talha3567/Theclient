package net.wurstclient.commands;

import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2828;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.MathUtils;

public final class DamageCmd
extends Command {
    public DamageCmd() {
        super("damage", "Applies the given amount of damage.", ".damage <amount>", "Note: The amount is in half-hearts.", "Example: .damage 7 (applies 3.5 hearts)", "To apply more damage, run the command multiple times.");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length == 0) {
            throw new CmdSyntaxError();
        }
        if (DamageCmd.MC.field_1724.method_31549().field_7477) {
            throw new CmdError("Cannot damage in creative mode.");
        }
        int amount = this.parseAmount(args[0]);
        this.applyDamage(amount);
    }

    private int parseAmount(String dmgString) throws CmdSyntaxError {
        if (!MathUtils.isInteger(dmgString)) {
            throw new CmdSyntaxError("Not a number: " + dmgString);
        }
        int dmg = Integer.parseInt(dmgString);
        if (dmg < 1) {
            throw new CmdSyntaxError("Minimum amount is 1.");
        }
        if (dmg > 7) {
            throw new CmdSyntaxError("Maximum amount is 7.");
        }
        return dmg;
    }

    private void applyDamage(int amount) {
        class_243 pos = DamageCmd.MC.field_1724.method_73189();
        for (int i = 0; i < 80; ++i) {
            this.sendPosition(pos.field_1352, pos.field_1351 + (double)amount + 2.1, pos.field_1350, false);
            this.sendPosition(pos.field_1352, pos.field_1351 + 0.05, pos.field_1350, false);
        }
        this.sendPosition(pos.field_1352, pos.field_1351, pos.field_1350, true);
    }

    private void sendPosition(double x, double y, double z, boolean onGround) {
        DamageCmd.MC.field_1724.field_3944.method_52787((class_2596)new class_2828.class_2829(x, y, z, onGround, DamageCmd.MC.field_1724.field_5976));
    }
}
