package net.wurstclient.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_2561;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_500;
import net.minecraft.class_642;
import net.minecraft.class_8021;
import net.minecraft.class_8667;
import net.wurstclient.WurstClient;
import net.wurstclient.serverfinder.CleanUpScreen;
import net.wurstclient.serverfinder.ServerFinderScreen;
import net.wurstclient.util.LastServerRememberer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_500.class})
public class MultiplayerScreenMixin
extends class_437 {
    private class_4185 lastServerButton;

    private MultiplayerScreenMixin(WurstClient wurst, class_2561 title) {
        super(title);
    }

    @Inject(method={"method_25426"}, at={@At(value="HEAD")})
    private void beforeVanillaButtons(CallbackInfo ci) {
        if (!WurstClient.INSTANCE.isEnabled()) {
            return;
        }
        class_500 mpScreen = (class_500)this;
        this.lastServerButton = class_4185.method_46430((class_2561)class_2561.method_30163((String)"Last Server"), b -> LastServerRememberer.joinLastServer(mpScreen)).method_46432(100).method_46431();
        this.method_37063((class_364)this.lastServerButton);
    }

    @Inject(method={"method_25426"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_500;method_48640()V", ordinal=0)})
    private void afterVanillaButtons(CallbackInfo ci, @Local(ordinal=1) class_8667 footerTopRow, @Local(ordinal=2) class_8667 footerBottomRow) {
        if (!WurstClient.INSTANCE.isEnabled()) {
            return;
        }
        class_500 mpScreen = (class_500)this;
        class_4185 serverFinderButton = class_4185.method_46430((class_2561)class_2561.method_30163((String)"Server Finder"), b -> this.field_22787.method_1507((class_437)new ServerFinderScreen(mpScreen))).method_46432(100).method_46431();
        this.method_37063((class_364)serverFinderButton);
        footerTopRow.method_52736((class_8021)serverFinderButton);
        class_4185 cleanUpButton = class_4185.method_46430((class_2561)class_2561.method_30163((String)"Clean Up"), b -> this.field_22787.method_1507((class_437)new CleanUpScreen(mpScreen))).method_46432(100).method_46431();
        this.method_37063((class_364)cleanUpButton);
        footerBottomRow.method_52736((class_8021)cleanUpButton);
    }

    @Inject(method={"method_48640"}, at={@At(value="TAIL")})
    private void onRefreshWidgetPositions(CallbackInfo ci) {
        this.updateLastServerButton();
    }

    @Inject(method={"method_2548"}, at={@At(value="HEAD")})
    private void onConnect(class_642 entry, CallbackInfo ci) {
        LastServerRememberer.setLastServer(entry);
        this.updateLastServerButton();
    }

    @Unique
    private void updateLastServerButton() {
        if (this.lastServerButton == null) {
            return;
        }
        this.lastServerButton.field_22763 = LastServerRememberer.getLastServer() != null;
        this.lastServerButton.method_46421(this.field_22789 / 2 - 154);
        this.lastServerButton.method_46419(6);
    }
}
