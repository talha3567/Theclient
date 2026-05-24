package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.ModuleArgumentType;
import meteordevelopment.meteorclient.commands.arguments.SettingArgumentType;
import meteordevelopment.meteorclient.commands.arguments.SettingValueArgumentType;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.tabs.builtin.ConfigTab;
import meteordevelopment.meteorclient.gui.tabs.builtin.HudTab;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class SettingCommand
extends Command {
    public SettingCommand() {
        super("settings", "Allows you to view and change module settings.", "s");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(SettingCommand.literal("hud").executes(commandContext -> {
            TabScreen screen = Tabs.get(HudTab.class).createScreen(GuiThemes.get());
            screen.parent = null;
            Utils.screenToOpen = screen;
            return 1;
        }));
        builder.then(SettingCommand.literal("config").executes(commandContext -> {
            TabScreen screen = Tabs.get(ConfigTab.class).createScreen(GuiThemes.get());
            screen.parent = null;
            Utils.screenToOpen = screen;
            return 1;
        }));
        builder.then(SettingCommand.literal("config").then(((RequiredArgumentBuilder)SettingCommand.argument("setting", SettingArgumentType.create()).executes(context -> {
            Setting<?> setting = SettingArgumentType.get(context, Config.get().settings);
            ChatUtils.infoPrefix("Config", "Setting (highlight)%s(default) is (highlight)%s(default).", setting.title, setting.get());
            return 1;
        })).suggests((commandContext, suggestionsBuilder) -> SettingArgumentType.listSuggestions(suggestionsBuilder, Config.get().settings)).then((ArgumentBuilder)((RequiredArgumentBuilder)SettingCommand.argument("value", SettingValueArgumentType.create()).executes(context -> {
            String value;
            Setting<?> setting = SettingArgumentType.get(context, Config.get().settings);
            if (setting.parse(value = SettingValueArgumentType.get(context))) {
                ChatUtils.infoPrefix("Config", "Setting (highlight)%s(default) changed to (highlight)%s(default).", setting.title, value);
            }
            return 1;
        })).suggests((context, suggestionsBuilder) -> SettingValueArgumentType.listSuggestions(context, suggestionsBuilder, Config.get().settings)))));
        builder.then(SettingCommand.argument("module", ModuleArgumentType.create()).executes(context -> {
            Module module = (Module)context.getArgument("module", Module.class);
            WidgetScreen screen = GuiThemes.get().moduleScreen(module);
            screen.parent = null;
            Utils.screenToOpen = screen;
            return 1;
        }));
        builder.then(SettingCommand.argument("module", ModuleArgumentType.create()).then(((RequiredArgumentBuilder)SettingCommand.argument("setting", SettingArgumentType.create()).executes(context -> {
            Setting<?> setting = SettingArgumentType.get(context);
            ModuleArgumentType.get(context).info("Setting (highlight)%s(default) is (highlight)%s(default).", setting.title, setting.get());
            return 1;
        })).then(SettingCommand.argument("value", SettingValueArgumentType.create()).executes(context -> {
            String value;
            Setting<?> setting = SettingArgumentType.get(context);
            if (setting.parse(value = SettingValueArgumentType.get(context))) {
                ModuleArgumentType.get(context).info("Setting (highlight)%s(default) changed to (highlight)%s(default).", setting.title, value);
            }
            return 1;
        }))));
    }
}
