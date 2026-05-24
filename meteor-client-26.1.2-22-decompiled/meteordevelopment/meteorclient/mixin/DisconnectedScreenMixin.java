package meteordevelopment.meteorclient.mixin;

import it.unimi.dsi.fastutil.Pair;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={DisconnectedScreen.class})
public abstract class DisconnectedScreenMixin
extends Screen {
    @Shadow
    @Final
    private LinearLayout layout;
    @Unique
    private Button reconnectBtn;
    @Unique
    private double time;

    protected DisconnectedScreenMixin(Component title) {
        super(title);
        this.time = Modules.get().get(AutoReconnect.class).time.get() * 20.0;
    }

    @Inject(method={"init"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/layouts/LinearLayout;arrangeElements()V", shift=At.Shift.BEFORE)})
    private void addButtons(CallbackInfo ci) {
        AutoReconnect autoReconnect = Modules.get().get(AutoReconnect.class);
        if (autoReconnect.lastServerConnection != null && !autoReconnect.button.get().booleanValue()) {
            this.reconnectBtn = new Button.Builder((Component)Component.literal((String)this.getText()), button -> this.tryConnecting()).build();
            this.layout.addChild((LayoutElement)this.reconnectBtn);
            this.layout.addChild((LayoutElement)new Button.Builder((Component)Component.literal((String)"Toggle Auto Reconnect"), button -> {
                autoReconnect.toggle();
                this.reconnectBtn.setMessage((Component)Component.literal((String)this.getText()));
                this.time = autoReconnect.time.get() * 20.0;
            }).build());
        }
    }

    public void tick() {
        AutoReconnect autoReconnect = Modules.get().get(AutoReconnect.class);
        if (!autoReconnect.isActive() || autoReconnect.lastServerConnection == null) {
            return;
        }
        if (this.time <= 0.0) {
            this.tryConnecting();
        } else {
            this.time -= 1.0;
            if (this.reconnectBtn != null) {
                this.reconnectBtn.setMessage((Component)Component.literal((String)this.getText()));
            }
        }
    }

    @Unique
    private String getText() {
        Object reconnectText = "Reconnect";
        if (Modules.get().isActive(AutoReconnect.class)) {
            reconnectText = (String)reconnectText + " " + String.format("(%.1f)", this.time / 20.0);
        }
        return reconnectText;
    }

    @Unique
    private void tryConnecting() {
        Pair<ServerAddress, ServerData> lastServer = Modules.get().get(AutoReconnect.class).lastServerConnection;
        ConnectScreen.startConnecting((Screen)new TitleScreen(), (Minecraft)MeteorClient.mc, (ServerAddress)((ServerAddress)lastServer.left()), (ServerData)((ServerData)lastServer.right()), (boolean)false, null);
    }
}
