package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.mixininterface.IChatHud;
import meteordevelopment.meteorclient.mixininterface.IChatListener;
import meteordevelopment.meteorclient.mixininterface.IGuiMessage;
import meteordevelopment.meteorclient.mixininterface.IGuiMessageVisible;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.BetterChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ChatComponent.class})
public abstract class ChatComponentMixin
implements IChatHud {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private List<GuiMessage.Line> trimmedMessages;
    @Shadow
    @Final
    private List<GuiMessage> allMessages;
    @Unique
    private BetterChat betterChat;
    @Unique
    private int nextId;

    @Shadow
    public abstract void addClientSystemMessage(Component var1);

    @Override
    public void meteor$add(Component message, int id) {
        this.nextId = id;
        this.addClientSystemMessage(message);
        this.nextId = 0;
    }

    @Inject(method={"addMessageToDisplayQueue"}, at={@At(value="INVOKE", target="Ljava/util/List;addFirst(Ljava/lang/Object;)V", shift=At.Shift.AFTER)})
    private void onAddMessageAfterNewGuiMessageVisible(GuiMessage message, CallbackInfo ci) {
        ((IGuiMessage)this.trimmedMessages.getFirst()).meteor$setId(this.nextId);
    }

    @Inject(method={"addMessageToQueue"}, at={@At(value="INVOKE", target="Ljava/util/List;addFirst(Ljava/lang/Object;)V", shift=At.Shift.AFTER)})
    private void onAddMessageAfterNewGuiMessage(GuiMessage message, CallbackInfo ci) {
        ((IGuiMessage)this.allMessages.getFirst()).meteor$setId(this.nextId);
    }

    @ModifyExpressionValue(method={"addMessageToDisplayQueue"}, at={@At(value="NEW", target="(Lnet/minecraft/client/multiplayer/chat/GuiMessage;Lnet/minecraft/util/FormattedCharSequence;Z)Lnet/minecraft/client/multiplayer/chat/GuiMessage$Line;")})
    private GuiMessage.Line onAddMessage_modifyGuiMessageLine(GuiMessage.Line line, @Local(name={"i"}) int i) {
        IChatListener handler = (IChatListener)this.minecraft.getChatListener();
        if (handler == null) {
            return line;
        }
        IGuiMessageVisible meteorLine = (IGuiMessageVisible)line;
        meteorLine.meteor$setSender(handler.meteor$getSender());
        meteorLine.meteor$setStartOfEntry(i == 0);
        return line;
    }

    @ModifyExpressionValue(method={"addMessage"}, at={@At(value="NEW", target="(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)Lnet/minecraft/client/multiplayer/chat/GuiMessage;")})
    private GuiMessage onAddMessage_modifyGuiMessage(GuiMessage line) {
        IChatListener handler = (IChatListener)this.minecraft.getChatListener();
        if (handler == null) {
            return line;
        }
        ((IGuiMessage)line).meteor$setSender(handler.meteor$getSender());
        return line;
    }

    @Inject(at={@At(value="HEAD")}, method={"addMessage"}, cancellable=true)
    private void onAddMessage(Component message, MessageSignature signature, GuiMessageSource source, GuiMessageTag indicator, CallbackInfo ci, @Local(argsOnly=true, name={"contents"}) LocalRef<Component> contents, @Local(argsOnly=true, name={"tag"}) LocalRef<GuiMessageTag> tag) {
        ReceiveMessageEvent event = MeteorClient.EVENT_BUS.post(ReceiveMessageEvent.get(message, indicator, this.nextId));
        if (event.isCancelled()) {
            ci.cancel();
        } else {
            this.trimmedMessages.removeIf(msg -> ((IGuiMessage)msg).meteor$getId() == this.nextId && this.nextId != 0);
            for (int i = this.allMessages.size() - 1; i > -1; --i) {
                if (((IGuiMessage)this.allMessages.get(i)).meteor$getId() != this.nextId || this.nextId == 0) continue;
                this.allMessages.remove(i);
                this.getBetterChat().removeLine(i);
            }
            if (event.isModified()) {
                contents.set((Object)event.getMessage());
                tag.set((Object)event.getIndicator());
            }
        }
    }

    @ModifyExpressionValue(method={"addMessageToQueue"}, at={@At(value="CONSTANT", args={"intValue=100"})})
    private int maxLength(int size) {
        if (Modules.get() == null || !this.getBetterChat().isLongerChat()) {
            return size;
        }
        return size + this.betterChat.getExtraChatLines();
    }

    @ModifyExpressionValue(method={"addMessageToDisplayQueue"}, at={@At(value="CONSTANT", args={"intValue=100"})})
    private int maxLengthVisible(int size) {
        if (Modules.get() == null || !this.getBetterChat().isLongerChat()) {
            return size;
        }
        return size + this.betterChat.getExtraChatLines();
    }

    @ModifyExpressionValue(method={"extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/Mth;ceil(F)I")})
    private int onRender_modifyWidth(int width) {
        return this.getBetterChat().modifyChatWidth(width);
    }

    @Inject(method={"addMessageToDisplayQueue"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/components/ChatComponent;isChatFocused()Z")})
    private void onBreakChatMessageLines(GuiMessage message, CallbackInfo ci, @Local(name={"lines"}) List<FormattedCharSequence> lines) {
        if (Modules.get() == null) {
            return;
        }
        this.getBetterChat().lines.addFirst((Object)lines.size());
    }

    @Inject(method={"addMessageToQueue"}, at={@At(value="INVOKE", target="Ljava/util/List;removeLast()Ljava/lang/Object;")})
    private void onRemoveMessage(GuiMessage message, CallbackInfo ci) {
        if (Modules.get() == null) {
            return;
        }
        int extra = this.getBetterChat().isLongerChat() ? this.getBetterChat().getExtraChatLines() : 0;
        for (int size = this.betterChat.lines.size(); size > 100 + extra; --size) {
            this.betterChat.lines.removeLast();
        }
    }

    @Inject(method={"clearMessages"}, at={@At(value="HEAD")})
    private void onClearMessages(boolean history, CallbackInfo ci) {
        this.getBetterChat().lines.clear();
    }

    @Inject(method={"refreshTrimmedMessages"}, at={@At(value="HEAD")})
    private void onRefreshTrimmedMessages(CallbackInfo ci) {
        this.getBetterChat().lines.clear();
    }

    @Unique
    private BetterChat getBetterChat() {
        if (this.betterChat == null) {
            this.betterChat = Modules.get().get(BetterChat.class);
        }
        return this.betterChat;
    }
}
