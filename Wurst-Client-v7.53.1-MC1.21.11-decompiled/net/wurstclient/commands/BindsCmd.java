package net.wurstclient.commands;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.class_3675;
import net.wurstclient.DontBlock;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.keybinds.Keybind;
import net.wurstclient.keybinds.KeybindList;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.MathUtils;
import net.wurstclient.util.json.JsonException;

@DontBlock
public final class BindsCmd
extends Command {
    public BindsCmd() {
        super("binds", "Allows you to manage keybinds through the chat.", ".binds add <key> <hacks>", ".binds add <key> <commands>", ".binds remove <key>", ".binds list [<page>]", ".binds load-profile <file>", ".binds save-profile <file>", ".binds list-profiles [<page>]", ".binds remove-all", ".binds reset", "Multiple hacks/commands must be separated by ';'.", "Profiles are saved in '.minecraft/wurst/keybinds'.");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length < 1) {
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
            case "list": {
                this.list(args);
                break;
            }
            case "load-profile": {
                this.loadProfile(args);
                break;
            }
            case "save-profile": {
                this.saveProfile(args);
                break;
            }
            case "list-profiles": {
                this.listProfiles(args);
                break;
            }
            case "remove-all": {
                this.removeAll();
                break;
            }
            case "reset": {
                this.reset();
                break;
            }
            default: {
                throw new CmdSyntaxError();
            }
        }
    }

    private void add(String[] args) throws CmdException {
        if (args.length < 3) {
            throw new CmdSyntaxError();
        }
        String displayKey = args[1];
        String key = this.parseKey(displayKey);
        CharSequence[] cmdArgs = Arrays.copyOfRange(args, 2, args.length);
        String commands = String.join((CharSequence)" ", cmdArgs);
        WURST.getKeybinds().add(key, commands);
        ChatUtils.message("Keybind set: " + displayKey + " -> " + commands);
    }

    private void remove(String[] args) throws CmdException {
        if (args.length != 2) {
            throw new CmdSyntaxError();
        }
        String displayKey = args[1];
        String key = this.parseKey(displayKey);
        String commands = WURST.getKeybinds().getCommands(key);
        if (commands == null) {
            throw new CmdError("Nothing to remove.");
        }
        WURST.getKeybinds().remove(key);
        ChatUtils.message("Keybind removed: " + displayKey + " -> " + commands);
    }

    private String parseKey(String displayKey) throws CmdSyntaxError {
        Object key = displayKey.toLowerCase();
        if (!((String)key).startsWith("key.mouse.") && !((String)key).startsWith("key.keyboard.")) {
            key = ((String)key).startsWith("mouse.") ? "key." + (String)key : "key.keyboard." + (String)key;
        }
        try {
            class_3675.method_15981((String)key);
            return key;
        }
        catch (IllegalArgumentException e) {
            throw new CmdSyntaxError("Unknown key: " + displayKey);
        }
    }

    private void list(String[] args) throws CmdException {
        if (args.length > 2) {
            throw new CmdSyntaxError();
        }
        List<Keybind> binds = WURST.getKeybinds().getAllKeybinds();
        int page = this.parsePage(args);
        int pages = (int)Math.ceil((double)binds.size() / 8.0);
        if (page > (pages = Math.max(pages, 1)) || page < 1) {
            throw new CmdSyntaxError("Invalid page: " + page);
        }
        String total = "Total: " + binds.size() + " keybind";
        total = total + (binds.size() != 1 ? "s" : "");
        ChatUtils.message(total);
        int start = (page - 1) * 8;
        int end = Math.min(page * 8, binds.size());
        ChatUtils.message("Keybind list (page " + page + "/" + pages + ")");
        for (int i = start; i < end; ++i) {
            ChatUtils.message(binds.get(i).toString());
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

    private void removeAll() {
        WURST.getKeybinds().removeAll();
        ChatUtils.message("All keybinds removed.");
    }

    private void reset() {
        WURST.getKeybinds().setKeybinds(KeybindList.DEFAULT_KEYBINDS);
        ChatUtils.message("All keybinds reset to defaults.");
    }

    private void loadProfile(String[] args) throws CmdException {
        if (args.length != 2) {
            throw new CmdSyntaxError();
        }
        String name = this.parseFileName(args[1]);
        try {
            WURST.getKeybinds().loadProfile(name);
            ChatUtils.message("Keybinds loaded: " + name);
        }
        catch (NoSuchFileException e) {
            throw new CmdError("Profile '" + name + "' doesn't exist.");
        }
        catch (JsonException e) {
            e.printStackTrace();
            throw new CmdError("Profile '" + name + "' is corrupted: " + e.getMessage());
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new CmdError("Couldn't load profile: " + e.getMessage());
        }
    }

    private void saveProfile(String[] args) throws CmdException {
        if (args.length != 2) {
            throw new CmdSyntaxError();
        }
        String name = this.parseFileName(args[1]);
        try {
            WURST.getKeybinds().saveProfile(name);
            ChatUtils.message("Keybinds saved: " + name);
        }
        catch (IOException | JsonException e) {
            e.printStackTrace();
            throw new CmdError("Couldn't save profile: " + e.getMessage());
        }
    }

    private String parseFileName(String input) {
        Object fileName = input;
        if (!((String)fileName).endsWith(".json")) {
            fileName = (String)fileName + ".json";
        }
        return fileName;
    }

    private void listProfiles(String[] args) throws CmdException {
        if (args.length > 2) {
            throw new CmdSyntaxError();
        }
        ArrayList<Path> files = WURST.getKeybinds().listProfiles();
        int page = this.parsePage(args);
        int pages = (int)Math.ceil((double)files.size() / 8.0);
        if (page > (pages = Math.max(pages, 1)) || page < 1) {
            throw new CmdSyntaxError("Invalid page: " + page);
        }
        String total = "Total: " + files.size() + " profile";
        total = total + (files.size() != 1 ? "s" : "");
        ChatUtils.message(total);
        int start = (page - 1) * 8;
        int end = Math.min(page * 8, files.size());
        ChatUtils.message("Keybind profile list (page " + page + "/" + pages + ")");
        for (int i = start; i < end; ++i) {
            ChatUtils.message(files.get(i).getFileName().toString());
        }
    }
}
