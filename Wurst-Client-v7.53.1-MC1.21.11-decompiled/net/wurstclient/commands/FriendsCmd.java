package net.wurstclient.commands;

import java.util.ArrayList;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.MathUtils;

public class FriendsCmd
extends Command {
    private static final int FRIENDS_PER_PAGE = 8;
    private final CheckboxSetting middleClickFriends = new CheckboxSetting("Middle click friends", "Add/remove friends by clicking them with the middle mouse button.", true);

    public FriendsCmd() {
        super("friends", "Manages your friends list.", ".friends add <name>", ".friends remove <name>", ".friends remove-all", ".friends list [<page>]");
        this.addSetting(this.middleClickFriends);
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length < 1 || args.length > 2) {
            throw new CmdSyntaxError();
        }
        switch (args[0].toLowerCase()) {
            case "add": {
                this.add(args);
                break;
            }
            case "remove": {
                this.remove(args);
                break;
            }
            case "remove-all": {
                this.removeAll(args);
                break;
            }
            case "list": {
                this.list(args);
                break;
            }
            default: {
                throw new CmdSyntaxError();
            }
        }
    }

    private void add(String[] args) throws CmdException {
        if (args.length != 2) {
            throw new CmdSyntaxError();
        }
        String name = args[1];
        if (WURST.getFriends().contains(name)) {
            throw new CmdError("\"" + name + "\" is already in your friends list.");
        }
        WURST.getFriends().addAndSave(name);
        ChatUtils.message("Added friend \"" + name + "\".");
    }

    private void remove(String[] args) throws CmdException {
        if (args.length != 2) {
            throw new CmdSyntaxError();
        }
        String name = args[1];
        if (!WURST.getFriends().contains(name)) {
            throw new CmdError("\"" + name + "\" is not in your friends list.");
        }
        WURST.getFriends().removeAndSave(name);
        ChatUtils.message("Removed friend \"" + name + "\".");
    }

    private void removeAll(String[] args) throws CmdException {
        if (args.length > 1) {
            throw new CmdSyntaxError();
        }
        WURST.getFriends().removeAllAndSave();
        ChatUtils.message("All friends removed. Oof.");
    }

    private void list(String[] args) throws CmdException {
        if (args.length > 2) {
            throw new CmdSyntaxError();
        }
        ArrayList<String> friends = WURST.getFriends().toList();
        int page = this.parsePage(args);
        int pages = (int)Math.ceil((double)friends.size() / 8.0);
        if (page > (pages = Math.max(pages, 1)) || page < 1) {
            throw new CmdSyntaxError("Invalid page: " + page);
        }
        ChatUtils.message("Current friends: " + friends.size());
        int start = (page - 1) * 8;
        int end = Math.min(page * 8, friends.size());
        ChatUtils.message("Friends list (page " + page + "/" + pages + ")");
        for (int i = start; i < end; ++i) {
            ChatUtils.message(friends.get(i).toString());
        }
    }

    private int parsePage(String[] args) throws CmdSyntaxError {
        if (args.length < 2) {
            return 1;
        }
        if (!MathUtils.isInteger(args[1])) {
            throw new CmdSyntaxError("Not a number: " + args[1]);
        }
        return Integer.parseInt(args[1]);
    }

    public CheckboxSetting getMiddleClickFriends() {
        return this.middleClickFriends;
    }
}
