package net.wurstclient.commands;

import net.minecraft.class_1799;
import net.minecraft.class_2487;
import net.minecraft.class_746;
import net.minecraft.class_9279;
import net.minecraft.class_9334;
import net.wurstclient.SearchTags;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;

@SearchTags(value={"view nbt", "NBTViewer", "nbt viewer"})
public final class ViewNbtCmd
extends Command {
    public ViewNbtCmd() {
        super("viewnbt", "Shows you the NBT data of an item.", ".viewnbt", "Copy to clipboard: .viewnbt copy");
    }

    @Override
    public void call(String[] args) throws CmdException {
        class_746 player = ViewNbtCmd.MC.field_1724;
        class_1799 stack = player.method_31548().method_7391();
        if (stack.method_7960()) {
            throw new CmdError("You must hold an item in your main hand.");
        }
        class_2487 tag = ((class_9279)stack.method_58695(class_9334.field_49628, (Object)class_9279.field_49302)).method_57461();
        String nbtString = tag.toString();
        switch (String.join((CharSequence)" ", args).toLowerCase()) {
            case "": {
                ChatUtils.message("NBT: " + nbtString);
                break;
            }
            case "copy": {
                ViewNbtCmd.MC.field_1774.method_1455(nbtString);
                ChatUtils.message("NBT data copied to clipboard.");
                break;
            }
            default: {
                throw new CmdSyntaxError();
            }
        }
    }
}
