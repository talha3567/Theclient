package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ClientPacketListener.class})
public interface ClientPacketListenerAccessor {
    @Accessor(value="serverChunkRadius")
    public int meteor$getServerChunkRadius();

    @Accessor(value="signedMessageEncoder")
    public SignedMessageChain.Encoder meteor$getSignedMessageEncoder();

    @Accessor(value="lastSeenMessages")
    public LastSeenMessagesTracker meteor$getLastSeenMessages();

    @Accessor(value="registryAccess")
    public RegistryAccess.Frozen meteor$getRegistryAccess();

    @Accessor(value="enabledFeatures")
    public FeatureFlagSet meteor$getEnabledFeatures();

    @Accessor(value="COMMAND_NODE_BUILDER")
    public static ClientboundCommandsPacket.NodeBuilder<ClientSuggestionProvider> meteor$getCommandNodeFactory() {
        return null;
    }
}
