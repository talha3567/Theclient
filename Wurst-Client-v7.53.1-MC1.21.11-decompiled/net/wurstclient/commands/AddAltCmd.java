package net.wurstclient.commands;

import net.minecraft.class_3544;
import net.minecraft.class_640;
import net.wurstclient.altmanager.AltManager;
import net.wurstclient.altmanager.CrackedAlt;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;

public final class AddAltCmd
extends Command {
    public AddAltCmd() {
        super("addalt", "Adds a player to your alt list.", ".addalt <player>", "Add all players on the server: .addalt all");
    }

    @Override
    public void call(String[] args) throws CmdException {
        String name;
        if (args.length != 1) {
            throw new CmdSyntaxError();
        }
        switch (name = args[0]) {
            case "all": {
                this.addAll();
                break;
            }
            default: {
                this.add(name);
            }
        }
    }

    private void add(String name) {
        if (name.equalsIgnoreCase("Alexander01998")) {
            return;
        }
        WURST.getAltManager().add(new CrackedAlt(name));
        ChatUtils.message("Added 1 alt.");
    }

    private void addAll() {
        int alts = 0;
        AltManager altManager = WURST.getAltManager();
        String playerName = MC.method_1548().method_1676();
        for (class_640 entry : AddAltCmd.MC.field_1724.field_3944.method_2880()) {
            String name = entry.method_2966().name();
            if (altManager.contains(name = class_3544.method_15440((String)name)) || name.equalsIgnoreCase(playerName) || name.equalsIgnoreCase("Alexander01998")) continue;
            altManager.add(new CrackedAlt(name));
            ++alts;
        }
        ChatUtils.message("Added " + alts + (alts == 1 ? " alt." : " alts."));
    }
}
