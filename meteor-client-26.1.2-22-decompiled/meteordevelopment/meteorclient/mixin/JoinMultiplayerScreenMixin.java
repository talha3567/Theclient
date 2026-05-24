package meteordevelopment.meteorclient.mixin;

import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.NameProtect;
import meteordevelopment.meteorclient.systems.proxies.Proxies;
import meteordevelopment.meteorclient.systems.proxies.Proxy;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={JoinMultiplayerScreen.class})
public abstract class JoinMultiplayerScreenMixin
extends Screen {
    @Unique
    private int textColor1;
    @Unique
    private int textColor2;
    @Unique
    private String loggedInAs;
    @Unique
    private int loggedInAsLength;
    @Unique
    private Button accounts;
    @Unique
    private Button proxies;
    @Unique
    private static final int BUTTON_WIDTH = 75;
    @Unique
    private static final int BUTTON_HEIGHT = 20;
    @Unique
    private static final int MARGIN = 3;
    @Unique
    private static final int GAP = 2;

    public JoinMultiplayerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method={"repositionElements"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo ci) {
        this.textColor1 = Color.fromRGBA(255, 255, 255, 255);
        this.textColor2 = Color.fromRGBA(175, 175, 175, 255);
        this.loggedInAs = "Logged in as ";
        this.loggedInAsLength = this.font.width(this.loggedInAs);
        if (this.accounts == null) {
            this.accounts = (Button)this.addRenderableWidget((GuiEventListener)new Button.Builder((Component)Component.literal((String)"Accounts"), button -> this.minecraft.setScreen((Screen)GuiThemes.get().accountsScreen())).size(75, 20).build());
        }
        if (this.proxies == null) {
            this.proxies = (Button)this.addRenderableWidget((GuiEventListener)new Button.Builder((Component)Component.literal((String)"Proxies"), button -> this.minecraft.setScreen((Screen)GuiThemes.get().proxiesScreen())).size(75, 20).build());
        }
        Config config = Config.get();
        Config.ButtonPosition accountPos = config.accountButtonAnchor.get();
        Config.ButtonPosition proxiesPos = config.proxiesButtonAnchor.get();
        boolean accountsVisible = accountPos != Config.ButtonPosition.Hidden;
        boolean proxiesVisible = proxiesPos != Config.ButtonPosition.Hidden;
        this.accounts.visible = accountsVisible;
        this.proxies.visible = proxiesVisible;
        this.positionButton(this.accounts, accountPos, proxiesVisible && proxiesPos == accountPos, true);
        this.positionButton(this.proxies, proxiesPos, accountsVisible && accountPos == proxiesPos, false);
    }

    @Unique
    private void positionButton(Button button, Config.ButtonPosition anchor, boolean sharingCorner, boolean isAccounts) {
        int leftOffset = sharingCorner && isAccounts ? 77 : 0;
        int rightOffset = sharingCorner && !isAccounts ? 77 : 0;
        switch (anchor) {
            case TopRight: {
                button.setPosition(this.width - 3 - 75 - rightOffset, 3);
                break;
            }
            case TopLeft: {
                button.setPosition(3 + leftOffset, 3);
                break;
            }
            case BottomLeft: {
                button.setPosition(3 + leftOffset, this.height - 3 - 20);
                break;
            }
            case BottomRight: {
                button.setPosition(this.width - 3 - 75 - rightOffset, this.height - 3 - 20);
                break;
            }
            default: {
                button.setPosition(this.width - 3 - 75 - rightOffset, 3);
            }
        }
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        String left;
        super.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
        Config config = Config.get();
        if (!config.showAccountStatus.get().booleanValue() && !config.showProxiesStatus.get().booleanValue()) {
            return;
        }
        int x = 3;
        if (config.proxiesButtonAnchor.get() != Config.ButtonPosition.Hidden && config.proxiesButtonAnchor.get() == Config.ButtonPosition.TopLeft) {
            x += 77;
        }
        if (config.accountButtonAnchor.get() != Config.ButtonPosition.Hidden && config.accountButtonAnchor.get() == Config.ButtonPosition.TopLeft) {
            x += 77;
        }
        int y = 3;
        if (config.showAccountStatus.get().booleanValue()) {
            graphics.text(MeteorClient.mc.font, this.loggedInAs, x, y, this.textColor1);
            graphics.text(MeteorClient.mc.font, Modules.get().get(NameProtect.class).getName(this.minecraft.getUser().getName()), x + this.loggedInAsLength, y, this.textColor2);
            Objects.requireNonNull(this.font);
            y += 9 + 2;
        }
        if (!config.showProxiesStatus.get().booleanValue()) {
            return;
        }
        Proxy proxy = Proxies.get().getEnabled();
        String string = left = proxy != null ? "Using proxy " : "Not using a proxy";
        String right = proxy != null ? (String)(proxy.name.get() != null && !proxy.name.get().isEmpty() ? "(" + proxy.name.get() + ") " : "") + proxy.address.get() + ":" + String.valueOf(proxy.port.get()) : null;
        graphics.text(MeteorClient.mc.font, left, x, y, this.textColor1);
        if (right != null) {
            graphics.text(MeteorClient.mc.font, right, x + this.font.width(left), y, this.textColor2);
        }
    }
}
