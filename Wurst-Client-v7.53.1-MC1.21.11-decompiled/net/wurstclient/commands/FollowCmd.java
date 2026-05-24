package net.wurstclient.commands;

import java.util.Comparator;
import java.util.stream.StreamSupport;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.hacks.FollowHack;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.FakePlayerEntity;

public final class FollowCmd
extends Command {
    public FollowCmd() {
        super("follow", "Follows the given entity.", ".follow <entity>");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length != 1) {
            throw new CmdSyntaxError();
        }
        FollowHack followHack = FollowCmd.WURST.getHax().followHack;
        if (followHack.isEnabled()) {
            followHack.setEnabled(false);
        }
        class_1297 entity = StreamSupport.stream(FollowCmd.MC.field_1687.method_18112().spliterator(), true).filter(class_1309.class::isInstance).filter(e -> !e.method_31481() && ((class_1309)e).method_6032() > 0.0f).filter(e -> e != FollowCmd.MC.field_1724).filter(e -> !(e instanceof FakePlayerEntity)).filter(e -> args[0].equalsIgnoreCase(e.method_5477().getString())).min(Comparator.comparingDouble(EntityUtils::distanceToHitboxSq)).orElse(null);
        if (entity == null) {
            throw new CmdError("Entity \"" + args[0] + "\" could not be found.");
        }
        followHack.setEntity(entity);
        followHack.setEnabled(true);
    }
}
