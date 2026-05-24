package net.wurstclient.commands;

import java.util.Collections;
import java.util.List;
import net.minecraft.class_1792;
import net.minecraft.class_7923;
import net.wurstclient.DontBlock;
import net.wurstclient.Feature;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.settings.ItemListSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.CmdUtils;
import net.wurstclient.util.MathUtils;

@DontBlock
public final class ItemListCmd
extends Command {
    public ItemListCmd() {
        super("itemlist", "Changes a ItemList setting of a feature. Allows you\nto change these settings through keybinds.", ".itemlist <feature> <setting> add <item>", ".itemlist <feature> <setting> remove <item>", ".itemlist <feature> <setting> list [<page>]", ".itemlist <feature> <setting> reset", "Example: .itemlist AutoDrop Items add dirt");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length < 3 || args.length > 4) {
            throw new CmdSyntaxError();
        }
        Feature feature = CmdUtils.findFeature(args[0]);
        Setting abstractSetting = CmdUtils.findSetting(feature, args[1]);
        ItemListSetting setting = this.getAsItemListSetting(feature, abstractSetting);
        switch (args[2].toLowerCase()) {
            case "add": {
                this.add(feature, setting, args);
                break;
            }
            case "remove": {
                this.remove(feature, setting, args);
                break;
            }
            case "list": {
                this.list(feature, setting, args);
                break;
            }
            case "reset": {
                setting.resetToDefaults();
                break;
            }
            default: {
                throw new CmdSyntaxError();
            }
        }
    }

    private void add(Feature feature, ItemListSetting setting, String[] args) throws CmdException {
        if (args.length != 4) {
            throw new CmdSyntaxError();
        }
        class_1792 item = CmdUtils.parseItem(args[3]);
        String itemName = class_7923.field_41178.method_10221((Object)item).toString();
        int index = Collections.binarySearch(setting.getItemNames(), itemName);
        if (index >= 0) {
            throw new CmdError(feature.getName() + " " + setting.getName() + " already contains " + itemName);
        }
        setting.add(item);
    }

    private void remove(Feature feature, ItemListSetting setting, String[] args) throws CmdException {
        if (args.length != 4) {
            throw new CmdSyntaxError();
        }
        class_1792 item = CmdUtils.parseItem(args[3]);
        String itemName = class_7923.field_41178.method_10221((Object)item).toString();
        int index = Collections.binarySearch(setting.getItemNames(), itemName);
        if (index < 0) {
            throw new CmdError(feature.getName() + " " + setting.getName() + " does not contain " + itemName);
        }
        setting.remove(index);
    }

    private void list(Feature feature, ItemListSetting setting, String[] args) throws CmdException {
        if (args.length > 4) {
            throw new CmdSyntaxError();
        }
        List<String> items = setting.getItemNames();
        int page = this.parsePage(args);
        int pages = (int)Math.ceil((double)items.size() / 8.0);
        if (page > (pages = Math.max(pages, 1)) || page < 1) {
            throw new CmdSyntaxError("Invalid page: " + page);
        }
        String total = "Total: " + items.size() + " item";
        total = total + (items.size() != 1 ? "s" : "");
        ChatUtils.message(total);
        int start = (page - 1) * 8;
        int end = Math.min(page * 8, items.size());
        ChatUtils.message(feature.getName() + " " + setting.getName() + " (page " + page + "/" + pages + ")");
        for (int i = start; i < end; ++i) {
            ChatUtils.message(items.get(i).toString());
        }
    }

    private int parsePage(String[] args) throws CmdSyntaxError {
        if (args.length < 4) {
            return 1;
        }
        if (!MathUtils.isInteger(args[3])) {
            throw new CmdSyntaxError("Not a number: " + args[3]);
        }
        return Integer.parseInt(args[3]);
    }

    private ItemListSetting getAsItemListSetting(Feature feature, Setting setting) throws CmdError {
        if (!(setting instanceof ItemListSetting)) {
            throw new CmdError(feature.getName() + " " + setting.getName() + " is not a ItemList setting.");
        }
        return (ItemListSetting)setting;
    }
}
