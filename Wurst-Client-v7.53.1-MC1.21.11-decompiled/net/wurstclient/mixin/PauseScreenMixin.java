package net.wurstclient.mixin;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.class_1074;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_4185;
import net.minecraft.class_433;
import net.minecraft.class_437;
import net.wurstclient.WurstClient;
import net.wurstclient.options.WurstOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_433.class})
public abstract class PauseScreenMixin
extends class_437 {
    @Unique
    private class_4185 wurstOptionsButton;

    private PauseScreenMixin(WurstClient wurst, class_2561 title) {
        super(title);
    }

    @Inject(method={"method_20543"}, at={@At(value="TAIL")})
    private void onInitWidgets(CallbackInfo ci) {
        if (WurstClient.INSTANCE.getOtfs().wurstOptionsOtf.isVisibleInGameMenu()) {
            this.addWurstOptionsButton();
        }
    }

    @Inject(method={"method_25394"}, at={@At(value="TAIL")})
    private void onRender(class_332 context, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        WurstClient wurst = WurstClient.INSTANCE;
        if (!wurst.isEnabled()) {
            return;
        }
        wurst.getOtfs().wurstOptionsOtf.drawWurstLogoOnButton(context, this.wurstOptionsButton);
    }

    @Unique
    private void addWurstOptionsButton() {
        List buttons = Screens.getButtons((class_437)this);
        int buttonX = this.field_22789 / 2 - 102;
        int buttonY = 60;
        int buttonWidth = 204;
        int buttonHeight = 20;
        for (class_339 button : buttons) {
            if (this.isTrKey(button, "menu.sendFeedback") || this.isTrKey(button, "menu.feedback")) {
                buttonY = button.method_46427();
                break;
            }
            if (!this.isTrKey(button, "menu.options")) continue;
            buttonY = button.method_46427() - 24;
            break;
        }
        this.hideFeedbackReportAndServerLinksButtons();
        this.ensureSpaceAvailable(buttonX, buttonY, buttonWidth, buttonHeight);
        this.wurstOptionsButton = WurstClient.INSTANCE.getOtfs().wurstOptionsOtf.buttonBuilder(this::openWurstOptions).method_46434(buttonX, buttonY, buttonWidth, buttonHeight).method_46431();
        buttons.add(this.wurstOptionsButton);
    }

    @Unique
    private void hideFeedbackReportAndServerLinksButtons() {
        for (class_339 button : Screens.getButtons((class_437)this)) {
            if (!this.isTrKey(button, "menu.sendFeedback") && !this.isTrKey(button, "menu.reportBugs") && !this.isTrKey(button, "menu.feedback") && !this.isTrKey(button, "menu.server_links")) continue;
            button.field_22764 = false;
        }
    }

    @Unique
    private void ensureSpaceAvailable(int x, int y, int width, int height) {
        ArrayList<class_339> buttonsInTheWay = new ArrayList<class_339>();
        for (class_339 button : Screens.getButtons((class_437)this)) {
            if (button.method_55442() < x || button.method_46426() > x + width || button.method_55443() < y || button.method_46427() > y + height || !button.field_22764) continue;
            buttonsInTheWay.add(button);
        }
        if (buttonsInTheWay.isEmpty()) {
            return;
        }
        this.ensureSpaceAvailable(x, y + 24, width, height);
        for (class_339 button : buttonsInTheWay) {
            button.method_46419(button.method_46427() + 24);
        }
    }

    @Unique
    private void openWurstOptions(class_4185 button) {
        this.field_22787.method_1507((class_437)new WurstOptionsScreen(this));
    }

    @Unique
    private boolean isTrKey(class_339 button, String key) {
        String message = button.method_25369().getString();
        return message != null && message.equals(class_1074.method_4662((String)key, (Object[])new Object[0]));
    }
}
