package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.ArrayList;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.ModuleArgumentType;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class ToggleCommand
extends Command {
    public ToggleCommand() {
        super("toggle", "Toggles a module.", "t");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        ((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(((LiteralArgumentBuilder)ToggleCommand.literal("all").then(ToggleCommand.literal("on").executes(commandContext -> {
            new ArrayList<Module>(Modules.get().getAll()).forEach(Module::enable);
            Hud.get().active = true;
            return 1;
        }))).then(ToggleCommand.literal("off").executes(commandContext -> {
            new ArrayList<Module>(Modules.get().getActive()).forEach(Module::toggle);
            Hud.get().active = false;
            return 1;
        })))).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)ToggleCommand.argument("module", ModuleArgumentType.create()).executes(context -> {
            Module m = ModuleArgumentType.get(context);
            m.toggle();
            m.sendToggledMsg();
            return 1;
        })).then(ToggleCommand.literal("on").executes(context -> {
            Module m = ModuleArgumentType.get(context);
            m.enable();
            return 1;
        }))).then(ToggleCommand.literal("off").executes(context -> {
            Module m = ModuleArgumentType.get(context);
            m.disable();
            return 1;
        })))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)ToggleCommand.literal("hud").executes(commandContext -> {
            Hud.get().active = !Hud.get().active;
            return 1;
        })).then(ToggleCommand.literal("on").executes(commandContext -> {
            Hud.get().active = true;
            return 1;
        }))).then(ToggleCommand.literal("off").executes(commandContext -> {
            Hud.get().active = false;
            return 1;
        })));
    }
}
