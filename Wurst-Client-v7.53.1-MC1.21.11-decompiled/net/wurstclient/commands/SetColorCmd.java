package net.wurstclient.commands;

import net.wurstclient.DontBlock;
import net.wurstclient.Feature;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.CmdUtils;
import net.wurstclient.util.ColorUtils;
import net.wurstclient.util.json.JsonException;

@DontBlock
public final class SetColorCmd
extends Command {
    public SetColorCmd() {
        super("setcolor", "Changes a color setting of a feature. Allows you\nto set RGB values through keybinds.", ".setcolor <feature> <setting> <RGB>", "Example: .setcolor ClickGUI AC #FF0000");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length != 3) {
            throw new CmdSyntaxError();
        }
        Feature feature = CmdUtils.findFeature(args[0]);
        Setting setting = CmdUtils.findSetting(feature, args[1]);
        ColorSetting colorSetting = this.getAsColor(feature, setting);
        this.setColor(colorSetting, args[2]);
    }

    private ColorSetting getAsColor(Feature feature, Setting setting) throws CmdError {
        if (!(setting instanceof ColorSetting)) {
            throw new CmdError(feature.getName() + " " + setting.getName() + " is not a color setting.");
        }
        return (ColorSetting)setting;
    }

    private void setColor(ColorSetting setting, String value) throws CmdSyntaxError {
        try {
            setting.setColor(ColorUtils.parseHex(value));
        }
        catch (JsonException e) {
            throw new CmdSyntaxError("Invalid color: " + value);
        }
    }
}
