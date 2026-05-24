package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;

public class DisconnectCommand
extends Command {
    public DisconnectCommand() {
        super("disconnect", "Disconnect from the server", "dc");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(commandContext -> {
            DisconnectCommand.mc.player.connection.handleDisconnect(new ClientboundDisconnectPacket((Component)Component.literal((String)"%s[%sDisconnectCommand%s] Disconnected by user.".formatted(ChatFormatting.GRAY, ChatFormatting.BLUE, ChatFormatting.GRAY))));
            return 1;
        });
        builder.then(DisconnectCommand.argument("reason", StringArgumentType.greedyString()).executes(context -> {
            DisconnectCommand.mc.player.connection.handleDisconnect(new ClientboundDisconnectPacket((Component)Component.literal((String)StringArgumentType.getString((CommandContext)context, (String)"reason"))));
            return 1;
        }));
    }
}
