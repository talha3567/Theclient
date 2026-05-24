package meteordevelopment.meteorclient.mixin;

import java.util.List;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ChatComponent.class})
public interface ChatComponentAccessor {
    @Accessor(value="trimmedMessages")
    public List<GuiMessage.Line> meteor$getTrimmedMessages();

    @Accessor(value="allMessages")
    public List<GuiMessage> meteor$getAllMessages();
}
