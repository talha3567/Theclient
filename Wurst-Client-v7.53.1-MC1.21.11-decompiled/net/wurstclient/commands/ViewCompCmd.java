package net.wurstclient.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.class_1799;
import net.minecraft.class_746;
import net.minecraft.class_9336;
import net.wurstclient.SearchTags;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.json.JsonUtils;

@SearchTags(value={"view components", "ComponentViewer", "component viewer"})
public final class ViewCompCmd
extends Command {
    public ViewCompCmd() {
        super("viewcomp", "Shows you the component data of an item.", ".viewcomp", ".viewcomp type <query>", "Copy to clipboard: .viewcomp copy", "Example: .viewcomp type name");
    }

    @Override
    public void call(String[] args) throws CmdException {
        class_746 player = ViewCompCmd.MC.field_1724;
        class_1799 stack = player.method_31548().method_7391();
        if (stack.method_7960()) {
            throw new CmdError("You must hold an item in your main hand.");
        }
        String query = null;
        boolean copy = false;
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "copy": {
                    if (args.length != 1) {
                        throw new CmdSyntaxError();
                    }
                    copy = true;
                    break;
                }
                case "type": {
                    if (args.length != 2) {
                        throw new CmdSyntaxError();
                    }
                    query = args[1];
                    break;
                }
                default: {
                    throw new CmdSyntaxError();
                }
            }
        }
        String compString = this.getComponentString(stack, query);
        if (copy) {
            ViewCompCmd.MC.field_1774.method_1455(compString);
            ChatUtils.message("Component data copied to clipboard.");
        } else {
            ChatUtils.message("Components: " + compString);
        }
    }

    private String getComponentString(class_1799 stack, String query) {
        Object compString = "";
        for (class_9336<?> c : this.getMatchingComponents(stack, query)) {
            compString = (String)compString + "\n" + c.comp_2443().toString().replace("minecraft:", "") + " => ";
            DataResult result = c.method_57943((DynamicOps)ViewCompCmd.MC.field_1724.method_56673().method_57093((DynamicOps)JsonOps.INSTANCE));
            JsonElement json = result.resultOrPartial().orElse(JsonNull.INSTANCE);
            compString = (String)compString + JsonUtils.GSON.toJson(json).replace("$", "$$").replace("\u00a7", "$").replace("minecraft:", "");
        }
        return compString;
    }

    private List<class_9336<?>> getMatchingComponents(class_1799 stack, String query) {
        if (query == null) {
            return stack.method_57353().method_57833().toList();
        }
        String queryLower = query.toLowerCase();
        return stack.method_57353().method_57833().filter(c -> c.comp_2443().toString().toLowerCase().contains(queryLower)).toList();
    }
}
