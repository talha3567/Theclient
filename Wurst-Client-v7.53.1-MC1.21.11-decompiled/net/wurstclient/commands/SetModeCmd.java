package net.wurstclient.commands;

import net.wurstclient.DontBlock;
import net.wurstclient.Feature;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.CmdUtils;

@DontBlock
public final class SetModeCmd
extends Command {
    public SetModeCmd() {
        super("setmode", "Changes a mode setting of a feature. Allows you to\nswitch modes through keybinds.", ".setmode <feature> <setting> <mode>", ".setmode <feature> <setting> (prev|next)");
    }

    @Override
    public void call(String[] args) throws CmdException {
        if (args.length != 3) {
            throw new CmdSyntaxError();
        }
        Feature feature = CmdUtils.findFeature(args[0]);
        Setting setting = CmdUtils.findSetting(feature, args[1]);
        EnumSetting<?> enumSetting = this.getAsEnumSetting(feature, setting);
        this.setMode(feature, enumSetting, args[2]);
    }

    private EnumSetting<?> getAsEnumSetting(Feature feature, Setting setting) throws CmdError {
        if (!(setting instanceof EnumSetting)) {
            throw new CmdError(feature.getName() + " " + setting.getName() + " is not a mode setting.");
        }
        return (EnumSetting)setting;
    }

    private void setMode(Feature feature, EnumSetting<?> setting, String mode) throws CmdError {
        switch (mode = mode.replace("_", " ").toLowerCase()) {
            case "prev": {
                setting.selectPrev();
                break;
            }
            case "next": {
                setting.selectNext();
                break;
            }
            default: {
                boolean successful = setting.setSelected(mode);
                if (successful) break;
                throw new CmdError("A mode named '" + mode + "' in " + feature.getName() + " " + setting.getName() + " could not be found.");
            }
        }
    }
}
