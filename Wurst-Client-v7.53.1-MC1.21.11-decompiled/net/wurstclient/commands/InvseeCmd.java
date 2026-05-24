package net.wurstclient.commands;

import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import net.minecraft.class_490;
import net.minecraft.class_745;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.events.RenderListener;
import net.wurstclient.util.ChatUtils;

public final class InvseeCmd
extends Command
implements RenderListener {
    private String targetName;

    public InvseeCmd() {
        super("invsee", "Allows you to see parts of another player's inventory.", ".invsee <player>");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length != 1) {
            throw new CmdSyntaxError();
        }
        if (InvseeCmd.MC.field_1724.method_31549().field_7477) {
            ChatUtils.error("Survival mode only.");
            return;
        }
        this.targetName = args[0];
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        boolean found = false;
        for (class_1297 entity : InvseeCmd.MC.field_1687.method_18112()) {
            class_745 player;
            String otherPlayerName;
            if (!(entity instanceof class_745) || !(otherPlayerName = (player = (class_745)entity).method_5477().getString()).equalsIgnoreCase(this.targetName)) continue;
            ChatUtils.message("Showing inventory of " + otherPlayerName + ".");
            MC.method_1507((class_437)new class_490((class_1657)player));
            found = true;
            break;
        }
        if (!found) {
            ChatUtils.error("Player not found.");
        }
        this.targetName = null;
        EVENTS.remove(RenderListener.class, this);
    }
}
