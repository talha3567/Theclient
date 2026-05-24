package net.wurstclient.command;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.TreeMap;
import net.minecraft.class_128;
import net.minecraft.class_148;
import net.wurstclient.command.Command;
import net.wurstclient.commands.AddAltCmd;
import net.wurstclient.commands.AnnoyCmd;
import net.wurstclient.commands.AuthorCmd;
import net.wurstclient.commands.BindCmd;
import net.wurstclient.commands.BindsCmd;
import net.wurstclient.commands.BlinkCmd;
import net.wurstclient.commands.BlockListCmd;
import net.wurstclient.commands.ClearCmd;
import net.wurstclient.commands.CopyItemCmd;
import net.wurstclient.commands.DamageCmd;
import net.wurstclient.commands.DigCmd;
import net.wurstclient.commands.DropCmd;
import net.wurstclient.commands.EnabledHaxCmd;
import net.wurstclient.commands.EnchantCmd;
import net.wurstclient.commands.ExcavateCmd;
import net.wurstclient.commands.FeaturesCmd;
import net.wurstclient.commands.FollowCmd;
import net.wurstclient.commands.FriendsCmd;
import net.wurstclient.commands.GetPosCmd;
import net.wurstclient.commands.GiveCmd;
import net.wurstclient.commands.GmCmd;
import net.wurstclient.commands.GoToCmd;
import net.wurstclient.commands.HelpCmd;
import net.wurstclient.commands.InvseeCmd;
import net.wurstclient.commands.IpCmd;
import net.wurstclient.commands.ItemListCmd;
import net.wurstclient.commands.JumpCmd;
import net.wurstclient.commands.LeaveCmd;
import net.wurstclient.commands.ModifyCmd;
import net.wurstclient.commands.PathCmd;
import net.wurstclient.commands.PotionCmd;
import net.wurstclient.commands.ProtectCmd;
import net.wurstclient.commands.RenameCmd;
import net.wurstclient.commands.RepairCmd;
import net.wurstclient.commands.RvCmd;
import net.wurstclient.commands.SayCmd;
import net.wurstclient.commands.SetBlockCmd;
import net.wurstclient.commands.SetCheckboxCmd;
import net.wurstclient.commands.SetColorCmd;
import net.wurstclient.commands.SetModeCmd;
import net.wurstclient.commands.SetSliderCmd;
import net.wurstclient.commands.SettingsCmd;
import net.wurstclient.commands.SvCmd;
import net.wurstclient.commands.TCmd;
import net.wurstclient.commands.TacoCmd;
import net.wurstclient.commands.TooManyHaxCmd;
import net.wurstclient.commands.TpCmd;
import net.wurstclient.commands.UnbindCmd;
import net.wurstclient.commands.VClipCmd;
import net.wurstclient.commands.ViewCompCmd;
import net.wurstclient.commands.ViewNbtCmd;
import net.wurstclient.commands.XrayCmd;

public final class CmdList {
    public final AddAltCmd addAltCmd = new AddAltCmd();
    public final AnnoyCmd annoyCmd = new AnnoyCmd();
    public final AuthorCmd authorCmd = new AuthorCmd();
    public final BindCmd bindCmd = new BindCmd();
    public final BindsCmd bindsCmd = new BindsCmd();
    public final BlinkCmd blinkCmd = new BlinkCmd();
    public final BlockListCmd blockListCmd = new BlockListCmd();
    public final ClearCmd clearCmd = new ClearCmd();
    public final CopyItemCmd copyitemCmd = new CopyItemCmd();
    public final DamageCmd damageCmd = new DamageCmd();
    public final DigCmd digCmd = new DigCmd();
    public final DropCmd dropCmd = new DropCmd();
    public final EnabledHaxCmd enabledHaxCmd = new EnabledHaxCmd();
    public final EnchantCmd enchantCmd = new EnchantCmd();
    public final ExcavateCmd excavateCmd = new ExcavateCmd();
    public final FeaturesCmd featuresCmd = new FeaturesCmd();
    public final FollowCmd followCmd = new FollowCmd();
    public final FriendsCmd friendsCmd = new FriendsCmd();
    public final GetPosCmd getPosCmd = new GetPosCmd();
    public final GiveCmd giveCmd = new GiveCmd();
    public final GmCmd gmCmd = new GmCmd();
    public final GoToCmd goToCmd = new GoToCmd();
    public final HelpCmd helpCmd = new HelpCmd();
    public final InvseeCmd invseeCmd = new InvseeCmd();
    public final IpCmd ipCmd = new IpCmd();
    public final ItemListCmd itemListCmd = new ItemListCmd();
    public final JumpCmd jumpCmd = new JumpCmd();
    public final LeaveCmd leaveCmd = new LeaveCmd();
    public final ModifyCmd modifyCmd = new ModifyCmd();
    public final PathCmd pathCmd = new PathCmd();
    public final PotionCmd potionCmd = new PotionCmd();
    public final ProtectCmd protectCmd = new ProtectCmd();
    public final RenameCmd renameCmd = new RenameCmd();
    public final RepairCmd repairCmd = new RepairCmd();
    public final RvCmd rvCmd = new RvCmd();
    public final SvCmd svCmd = new SvCmd();
    public final SayCmd sayCmd = new SayCmd();
    public final SetBlockCmd setBlockCmd = new SetBlockCmd();
    public final SetCheckboxCmd setCheckboxCmd = new SetCheckboxCmd();
    public final SetColorCmd setColorCmd = new SetColorCmd();
    public final SetModeCmd setModeCmd = new SetModeCmd();
    public final SetSliderCmd setSliderCmd = new SetSliderCmd();
    public final SettingsCmd settingsCmd = new SettingsCmd();
    public final TacoCmd tacoCmd = new TacoCmd();
    public final TCmd tCmd = new TCmd();
    public final TooManyHaxCmd tooManyHaxCmd = new TooManyHaxCmd();
    public final TpCmd tpCmd = new TpCmd();
    public final UnbindCmd unbindCmd = new UnbindCmd();
    public final VClipCmd vClipCmd = new VClipCmd();
    public final ViewCompCmd viewCompCmd = new ViewCompCmd();
    public final ViewNbtCmd viewNbtCmd = new ViewNbtCmd();
    public final XrayCmd xrayCmd = new XrayCmd();
    private final TreeMap<String, Command> cmds = new TreeMap(String::compareToIgnoreCase);

    public CmdList() {
        try {
            for (Field field : CmdList.class.getDeclaredFields()) {
                if (!field.getName().endsWith("Cmd")) continue;
                Command cmd = (Command)field.get(this);
                this.cmds.put(cmd.getName(), cmd);
            }
        }
        catch (Exception e) {
            String message = "Initializing Wurst commands";
            class_128 report = class_128.method_560((Throwable)e, (String)message);
            throw new class_148(report);
        }
    }

    public Command getCmdByName(String name) {
        return this.cmds.get("." + name);
    }

    public Collection<Command> getAllCmds() {
        return this.cmds.values();
    }

    public int countCmds() {
        return this.cmds.size();
    }
}
