package net.wurstclient.commands;

import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_151;
import net.minecraft.class_1799;
import net.minecraft.class_1812;
import net.minecraft.class_1842;
import net.minecraft.class_1844;
import net.minecraft.class_2960;
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import net.minecraft.class_9334;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.MathUtils;

public final class PotionCmd
extends Command {
    public PotionCmd() {
        super("potion", "Changes the effects of the held potion.", ".potion add (<effect> <amplifier> <duration>)...", ".potion set (<effect> <amplifier> <duration>)...", ".potion remove <effect>");
    }

    @Override
    public void call(String[] args) throws CmdException {
        ArrayList<class_1293> effects;
        if (args.length == 0) {
            throw new CmdSyntaxError();
        }
        if (!PotionCmd.MC.field_1724.method_31549().field_7477) {
            throw new CmdError("Creative mode only.");
        }
        class_1799 stack = PotionCmd.MC.field_1724.method_31548().method_7391();
        if (!(stack.method_7909() instanceof class_1812)) {
            throw new CmdError("You must hold a potion in your main hand.");
        }
        if (args[0].equalsIgnoreCase("remove")) {
            this.remove(stack, args);
            return;
        }
        if ((args.length - 1) % 3 != 0) {
            throw new CmdSyntaxError();
        }
        class_1844 oldContents = (class_1844)stack.method_57353().method_58695(class_9334.field_49651, (Object)class_1844.field_49274);
        Optional potion = switch (args[0].toLowerCase()) {
            case "add" -> {
                effects = new ArrayList(oldContents.comp_2380());
                yield oldContents.comp_2378();
            }
            case "set" -> {
                effects = new ArrayList<class_1293>();
                yield Optional.empty();
            }
            default -> throw new CmdSyntaxError();
        };
        for (int i = 0; i < (args.length - 1) / 3; ++i) {
            class_6880<class_1291> effect = this.parseEffect(args[1 + i * 3]);
            int amplifier = this.parseInt(args[2 + i * 3]) - 1;
            int duration = this.parseInt(args[3 + i * 3]) * 20;
            effects.add(new class_1293(effect, duration, amplifier));
        }
        stack.method_57379(class_9334.field_49651, (Object)new class_1844(potion, oldContents.comp_2379(), effects, oldContents.comp_3209()));
        ChatUtils.message("Potion modified.");
    }

    private void remove(class_1799 stack, String[] args) throws CmdSyntaxError {
        if (args.length != 2) {
            throw new CmdSyntaxError();
        }
        class_6880<class_1291> targetEffect = this.parseEffect(args[1]);
        class_1844 oldContents = (class_1844)stack.method_57353().method_58695(class_9334.field_49651, (Object)class_1844.field_49274);
        boolean mainPotionContainsTargetEffect = oldContents.comp_2378().isPresent() && ((class_1842)((class_6880)oldContents.comp_2378().get()).comp_349()).method_8049().stream().anyMatch(effect -> effect.method_5579() == targetEffect);
        ArrayList<class_1293> newEffects = new ArrayList<class_1293>();
        if (mainPotionContainsTargetEffect) {
            oldContents.method_57397().forEach(newEffects::add);
        } else {
            oldContents.comp_2380().forEach(newEffects::add);
        }
        newEffects.removeIf(effect -> effect.method_5579() == targetEffect);
        Optional newPotion = mainPotionContainsTargetEffect ? Optional.empty() : oldContents.comp_2378();
        stack.method_57379(class_9334.field_49651, (Object)new class_1844(newPotion, oldContents.comp_2379(), newEffects, oldContents.comp_3209()));
        ChatUtils.message("Effect removed.");
    }

    private class_6880<class_1291> parseEffect(String input) throws CmdSyntaxError {
        class_1291 effect;
        if (MathUtils.isInteger(input)) {
            effect = (class_1291)class_7923.field_41174.method_10200(Integer.parseInt(input));
        } else {
            try {
                class_2960 identifier = class_2960.method_60654((String)input);
                effect = (class_1291)class_7923.field_41174.method_63535(identifier);
            }
            catch (class_151 e) {
                throw new CmdSyntaxError("Invalid effect: " + input);
            }
        }
        if (effect == null) {
            throw new CmdSyntaxError("Invalid effect: " + input);
        }
        return class_7923.field_41174.method_47983((Object)effect);
    }

    private int parseInt(String s) throws CmdSyntaxError {
        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
            throw new CmdSyntaxError("Not a number: " + s);
        }
    }
}
