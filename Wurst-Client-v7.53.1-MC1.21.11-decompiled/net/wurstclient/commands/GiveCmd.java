package net.wurstclient.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1935;
import net.minecraft.class_2487;
import net.minecraft.class_2522;
import net.minecraft.class_9279;
import net.minecraft.class_9331;
import net.minecraft.class_9334;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.CmdUtils;
import net.wurstclient.util.MathUtils;

public final class GiveCmd
extends Command {
    public GiveCmd() {
        super("give", "Gives you an item with custom NBT data.\nRequires creative mode.", ".give <item> [<amount>] [<nbt>]", ".give <id> [<amount>] [<nbt>]");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length < 1) {
            throw new CmdSyntaxError();
        }
        if (!GiveCmd.MC.field_1724.method_31549().field_7477) {
            throw new CmdError("Creative mode only.");
        }
        class_1792 item = CmdUtils.parseItem(args[0]);
        int amount = 1;
        if (args.length >= 2) {
            if (!MathUtils.isInteger(args[1])) {
                throw new CmdSyntaxError("Not a number: " + args[1]);
            }
            amount = Integer.parseInt(args[1]);
            if (amount < 1) {
                throw new CmdError("Amount cannot be less than 1.");
            }
            if (amount > 64) {
                throw new CmdError("Amount cannot be more than 64.");
            }
        }
        String nbt = null;
        if (args.length >= 3) {
            nbt = String.join((CharSequence)" ", Arrays.copyOfRange(args, 2, args.length));
        }
        class_1799 stack = new class_1799((class_1935)item, amount);
        if (nbt != null) {
            try {
                class_2487 tag = class_2522.method_67315((String)nbt);
                class_9279.method_57453((class_9331)class_9334.field_49628, (class_1799)stack, (class_2487)tag);
            }
            catch (CommandSyntaxException e) {
                ChatUtils.message(e.getMessage());
                throw new CmdSyntaxError("NBT data is invalid.");
            }
        }
        CmdUtils.giveItem(stack);
        ChatUtils.message("Item" + (amount > 1 ? "s" : "") + " created.");
    }
}
