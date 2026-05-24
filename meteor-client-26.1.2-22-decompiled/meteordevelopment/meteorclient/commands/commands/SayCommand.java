package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.time.Instant;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.mixin.ClientPacketListenerAccessor;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.util.Crypt;
import org.meteordev.starscript.Script;

public class SayCommand
extends Command {
    public SayCommand() {
        super("say", "Sends messages in chat.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(SayCommand.argument("message", StringArgumentType.greedyString()).executes(context -> {
            String message;
            String msg = (String)context.getArgument("message", String.class);
            Script script = MeteorStarscript.compile(msg);
            if (script != null && (message = MeteorStarscript.run(script)) != null) {
                Instant instant = Instant.now();
                long l = Crypt.SaltSupplier.getLong();
                ClientPacketListener handler = mc.getConnection();
                LastSeenMessagesTracker.Update lastSeenMessages = ((ClientPacketListenerAccessor)handler).meteor$getLastSeenMessages().generateAndApplyUpdate();
                MessageSignature messageSignatureData = ((ClientPacketListenerAccessor)handler).meteor$getSignedMessageEncoder().pack(new SignedMessageBody(message, instant, l, lastSeenMessages.lastSeen()));
                handler.send((Packet)new ServerboundChatPacket(message, instant, l, messageSignatureData, lastSeenMessages.update()));
            }
            return 1;
        }));
    }
}
