package net.wurstclient.commands;

import net.minecraft.class_1799;
import net.minecraft.class_1893;
import net.minecraft.class_2378;
import net.minecraft.class_5455;
import net.minecraft.class_6880;
import net.minecraft.class_7924;
import net.minecraft.class_9636;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;

public final class EnchantCmd
extends Command {
    public EnchantCmd() {
        super("enchant", "Enchants an item with everything,\nexcept for silk touch and curses.", ".enchant");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (!EnchantCmd.MC.field_1724.method_31549().field_7477) {
            throw new CmdError("Creative mode only.");
        }
        if (args.length > 1) {
            throw new CmdSyntaxError();
        }
        this.enchant(this.getHeldItem(), 127);
        ChatUtils.message("Item enchanted.");
    }

    private class_1799 getHeldItem() throws CmdError {
        class_1799 stack = EnchantCmd.MC.field_1724.method_6047();
        if (stack.method_7960()) {
            stack = EnchantCmd.MC.field_1724.method_6079();
        }
        if (stack.method_7960()) {
            throw new CmdError("There is no item in your hand.");
        }
        return stack;
    }

    private void enchant(class_1799 stack, int level) {
        class_5455 drm = EnchantCmd.MC.field_1687.method_30349();
        class_2378 registry = drm.method_30530(class_7924.field_41265);
        for (class_6880 entry : registry.method_40295()) {
            if (entry.method_40220(class_9636.field_51551) || entry.method_40230().orElse(null) == class_1893.field_9099) continue;
            if (entry.method_40230().orElse(null) == class_1893.field_9098) {
                stack.method_7978(entry, Math.min(level, 5));
                continue;
            }
            stack.method_7978(entry, level);
        }
    }

    @Override
    public String getPrimaryAction() {
        return "Enchant Held Item";
    }

    @Override
    public void doPrimaryAction() {
        WURST.getCmdProcessor().process("enchant");
    }
}
