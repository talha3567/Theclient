package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.ModuleArgumentType;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class ResetCommand
extends Command {
    public ResetCommand() {
        super("reset", "Resets specified settings.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(((LiteralArgumentBuilder)ResetCommand.literal("settings").then(ResetCommand.argument("module", ModuleArgumentType.create()).executes(context -> {
            Module module = (Module)context.getArgument("module", Module.class);
            module.settings.forEach(group -> group.forEach(Setting::reset));
            module.info("Reset all settings.", new Object[0]);
            return 1;
        }))).then(ResetCommand.literal("all").executes(commandContext -> {
            Modules.get().getAll().forEach(module -> module.settings.forEach(group -> group.forEach(Setting::reset)));
            ChatUtils.infoPrefix("Modules", "Reset all module settings", new Object[0]);
            return 1;
        })))).then(ResetCommand.literal("gui").executes(commandContext -> {
            GuiThemes.get().clearWindowConfigs();
            GuiThemes.get().settings.reset();
            ChatUtils.info("Reset all GUI settings.", new Object[0]);
            return 1;
        }))).then(((LiteralArgumentBuilder)ResetCommand.literal("bind").then(ResetCommand.argument("module", ModuleArgumentType.create()).executes(context -> {
            Module module = (Module)context.getArgument("module", Module.class);
            module.keybind.reset();
            module.info("Reset bind.", new Object[0]);
            return 1;
        }))).then(ResetCommand.literal("all").executes(commandContext -> {
            Modules.get().getAll().forEach(module -> module.keybind.reset());
            ChatUtils.infoPrefix("Modules", "Reset all binds.", new Object[0]);
            return 1;
        })))).then(ResetCommand.literal("hud").executes(commandContext -> {
            Hud.get().resetToDefaultElements();
            ChatUtils.infoPrefix("HUD", "Reset all elements.", new Object[0]);
            return 1;
        }));
    }
}
