package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class CommandsCommand
extends Command {
    public CommandsCommand() {
        super("commands", "List of all commands.", "help");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(commandContext -> {
            ChatUtils.info("--- Commands ((highlight)%d(default)) ---", Commands.COMMANDS.size());
            MutableComponent commands = Component.literal((String)"");
            Commands.COMMANDS.forEach(command -> commands.append((Component)this.getCommandText((Command)command)));
            ChatUtils.sendMsg((Component)commands);
            return 1;
        });
    }

    private MutableComponent getCommandText(Command command) {
        MutableComponent tooltip = Component.literal((String)"");
        tooltip.append((Component)Component.literal((String)Utils.nameToTitle(command.getName())).withStyle(new ChatFormatting[]{ChatFormatting.BLUE, ChatFormatting.BOLD})).append("\n");
        MutableComponent aliases = Component.literal((String)(Config.get().prefix.get() + command.getName()));
        if (!command.getAliases().isEmpty()) {
            aliases.append(", ");
            for (String alias : command.getAliases()) {
                if (alias.isEmpty()) continue;
                aliases.append(Config.get().prefix.get() + alias);
                if (alias.equals(command.getAliases().getLast())) continue;
                aliases.append(", ");
            }
        }
        tooltip.append((Component)aliases.withStyle(ChatFormatting.GRAY)).append("\n\n");
        tooltip.append((Component)Component.literal((String)command.getDescription()).withStyle(ChatFormatting.WHITE));
        MutableComponent text = Component.literal((String)Utils.nameToTitle(command.getName()));
        if (command != Commands.COMMANDS.getLast()) {
            text.append((Component)Component.literal((String)", ").withStyle(ChatFormatting.GRAY));
        }
        text.setStyle(text.getStyle().withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)tooltip)).withClickEvent((ClickEvent)new ClickEvent.SuggestCommand(Config.get().prefix.get() + command.getName())));
        return text;
    }
}
