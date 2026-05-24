package net.wurstclient.commands;

import java.util.Comparator;
import java.util.stream.StreamSupport;
import net.minecraft.class_1309;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_2596;
import net.minecraft.class_2828;
import net.minecraft.class_634;
import net.minecraft.class_746;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.FakePlayerEntity;
import net.wurstclient.util.MathUtils;

public final class TpCmd
extends Command {
    private final CheckboxSetting disableFreecam = new CheckboxSetting("Disable Freecam", "Disables Freecam just before teleporting.", false);

    public TpCmd() {
        super("tp", "Teleports you up to 22 blocks away.", ".tp <x> <y> <z>", ".tp <entity>");
        this.addSetting(this.disableFreecam);
    }

    @Override
    public void call(String[] args) throws CmdException {
        class_2338 pos = this.argsToPos(args);
        class_746 player = TpCmd.MC.field_1724;
        if (this.disableFreecam.isChecked() && TpCmd.WURST.getHax().freecamHack.isEnabled()) {
            TpCmd.WURST.getHax().freecamHack.setEnabled(false);
        }
        if (player.method_5707(pos.method_61082()) < 100.0) {
            player.method_5814((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.method_10260());
            return;
        }
        for (int i = 0; i < 4; ++i) {
            this.sendPos(player.method_23317(), player.method_23318(), player.method_23321(), true);
        }
        this.sendPos(pos.method_10263(), pos.method_10264(), pos.method_10260(), true);
        this.sendPos(pos.method_10263(), pos.method_10264(), pos.method_10260(), false);
    }

    private void sendPos(double x, double y, double z, boolean onGround) {
        class_634 netHandler = TpCmd.MC.field_1724.field_3944;
        netHandler.method_52787((class_2596)new class_2828.class_2829(x, y, z, onGround, TpCmd.MC.field_1724.field_5976));
    }

    private class_2338 argsToPos(String ... args) throws CmdException {
        switch (args.length) {
            default: {
                throw new CmdSyntaxError("Invalid coordinates.");
            }
            case 1: {
                return this.argsToEntityPos(args[0]);
            }
            case 3: 
        }
        return this.argsToXyzPos(args);
    }

    private class_2338 argsToEntityPos(String name) throws CmdError {
        class_1309 entity = StreamSupport.stream(TpCmd.MC.field_1687.method_18112().spliterator(), true).filter(class_1309.class::isInstance).map(e -> (class_1309)e).filter(e -> !e.method_31481() && e.method_6032() > 0.0f).filter(e -> e != TpCmd.MC.field_1724).filter(e -> !(e instanceof FakePlayerEntity)).filter(e -> name.equalsIgnoreCase(e.method_5476().getString())).min(Comparator.comparingDouble(EntityUtils::distanceToHitboxSq)).orElse(null);
        if (entity == null) {
            throw new CmdError("Entity \"" + name + "\" could not be found.");
        }
        return class_2338.method_49638((class_2374)entity.method_73189());
    }

    private class_2338 argsToXyzPos(String ... xyz) throws CmdSyntaxError {
        class_2338 playerPos = class_2338.method_49638((class_2374)TpCmd.MC.field_1724.method_73189());
        int[] player = new int[]{playerPos.method_10263(), playerPos.method_10264(), playerPos.method_10260()};
        int[] pos = new int[3];
        for (int i = 0; i < 3; ++i) {
            if (MathUtils.isInteger(xyz[i])) {
                pos[i] = Integer.parseInt(xyz[i]);
                continue;
            }
            if (xyz[i].equals("~")) {
                pos[i] = player[i];
                continue;
            }
            if (xyz[i].startsWith("~") && MathUtils.isInteger(xyz[i].substring(1))) {
                pos[i] = player[i] + Integer.parseInt(xyz[i].substring(1));
                continue;
            }
            throw new CmdSyntaxError("Invalid coordinates.");
        }
        return new class_2338(pos[0], pos[1], pos[2]);
    }
}
