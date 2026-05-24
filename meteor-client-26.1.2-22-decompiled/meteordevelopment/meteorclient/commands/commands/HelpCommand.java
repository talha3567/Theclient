package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.Map;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.commands.arguments.CommandArgumentType;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class HelpCommand
extends Command {
    public HelpCommand() {
        super("help", "Shows you what a command does.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(HelpCommand.argument("command", CommandArgumentType.create()).executes(context -> {
            this.showHelp(CommandArgumentType.get(context));
            return 1;
        }));
        builder.executes(commandContext -> {
            this.showHelp(this);
            return 1;
        });
    }

    private void showHelp(Command cmd) {
        MutableComponent msg = Component.literal((String)"");
        msg.append((Component)Component.literal((String)"Help for ").withStyle(ChatFormatting.GRAY).append((Component)Component.literal((String)cmd.getName()).withStyle(ChatFormatting.YELLOW)));
        msg.append((Component)Component.literal((String)"\n ")).append((Component)Component.literal((String)"Description: ").withStyle(ChatFormatting.GRAY).append((Component)Component.literal((String)cmd.getDescription()).withStyle(ChatFormatting.WHITE)));
        if (!cmd.getAliases().isEmpty()) {
            msg.append((Component)Component.literal((String)"\n ")).append((Component)Component.literal((String)"Aliases: ").withStyle(ChatFormatting.GRAY));
            msg.append((Component)Component.literal((String)String.join((CharSequence)", ", cmd.getAliases())).withStyle(ChatFormatting.AQUA));
        }
        msg.append((Component)this.getUsageText(cmd));
        ChatUtils.sendMsg((Component)msg);
    }

    private MutableComponent getUsageText(Command cmd) {
        ClientSuggestionProvider source = mc.getConnection().getSuggestionsProvider();
        RootCommandNode root = Commands.DISPATCHER.getRoot();
        CommandNode node = root.getChild(cmd.getName());
        MutableComponent usagesText = Component.literal((String)"");
        if (node != null) {
            Map usages = Commands.DISPATCHER.getSmartUsage(node, (Object)source);
            for (String usage : usages.values()) {
                usagesText.append((Component)Component.literal((String)("\n " + String.valueOf(cmd) + " ")).withStyle(ChatFormatting.GREEN)).append((Component)Component.literal((String)usage).withStyle(ChatFormatting.GREEN));
            }
        }
        if (usagesText.getString().isEmpty()) {
            usagesText.append((Component)Component.literal((String)("\n " + String.valueOf(cmd))).withStyle(ChatFormatting.GREEN));
        }
        return Component.literal((String)"\n Usage:").withStyle(ChatFormatting.GRAY).append((Component)usagesText);
    }
}
