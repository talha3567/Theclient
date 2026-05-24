package net.wurstclient.mixin;

import java.util.stream.Stream;
import net.minecraft.class_2561;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_419;
import net.minecraft.class_437;
import net.minecraft.class_8021;
import net.minecraft.class_8667;
import net.minecraft.class_9812;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.AutoReconnectHack;
import net.wurstclient.nochatreports.ForcedChatReportsScreen;
import net.wurstclient.nochatreports.NcrModRequiredScreen;
import net.wurstclient.util.LastServerRememberer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_419.class})
public class DisconnectedScreenMixin
extends class_437 {
    private int autoReconnectTimer;
    private class_4185 autoReconnectButton;
    @Shadow
    @Final
    private class_9812 field_52131;
    @Shadow
    @Final
    private class_437 field_2456;
    @Shadow
    @Final
    private class_8667 field_44552;

    private DisconnectedScreenMixin(WurstClient wurst, class_2561 title) {
        super(title);
    }

    @Inject(method={"method_25426"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo ci) {
        if (!WurstClient.INSTANCE.isEnabled()) {
            return;
        }
        class_2561 reason = this.field_52131.comp_2853();
        System.out.println("Disconnected: " + String.valueOf(reason));
        if (ForcedChatReportsScreen.isCausedByNoChatReports(reason)) {
            this.field_22787.method_1507((class_437)new ForcedChatReportsScreen(this.field_2456));
            return;
        }
        if (NcrModRequiredScreen.isCausedByLackOfNCR(reason)) {
            this.field_22787.method_1507((class_437)new NcrModRequiredScreen(this.field_2456));
            return;
        }
        this.addReconnectButtons();
    }

    private void addReconnectButtons() {
        class_4185 reconnectButton = (class_4185)this.field_44552.method_52736((class_8021)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Reconnect"), b -> LastServerRememberer.reconnect(this.field_2456)).method_46432(200).method_46431());
        this.autoReconnectButton = (class_4185)this.field_44552.method_52736((class_8021)class_4185.method_46430((class_2561)class_2561.method_43470((String)"AutoReconnect"), b -> this.pressAutoReconnect()).method_46432(200).method_46431());
        this.field_44552.method_48222();
        Stream.of(reconnectButton, this.autoReconnectButton).forEach(x$0 -> {
            class_4185 cfr_ignored_0 = (class_4185)this.method_37063((class_364)x$0);
        });
        AutoReconnectHack autoReconnect = WurstClient.INSTANCE.getHax().autoReconnectHack;
        if (autoReconnect.isEnabled()) {
            this.autoReconnectTimer = autoReconnect.getWaitTicks();
        }
    }

    private void pressAutoReconnect() {
        AutoReconnectHack autoReconnect;
        autoReconnect.setEnabled(!(autoReconnect = WurstClient.INSTANCE.getHax().autoReconnectHack).isEnabled());
        if (autoReconnect.isEnabled()) {
            this.autoReconnectTimer = autoReconnect.getWaitTicks();
        }
    }

    public void method_25393() {
        if (!WurstClient.INSTANCE.isEnabled() || this.autoReconnectButton == null) {
            return;
        }
        AutoReconnectHack autoReconnect = WurstClient.INSTANCE.getHax().autoReconnectHack;
        if (!autoReconnect.isEnabled()) {
            this.autoReconnectButton.method_25355((class_2561)class_2561.method_43470((String)"AutoReconnect"));
            return;
        }
        this.autoReconnectButton.method_25355((class_2561)class_2561.method_43470((String)("AutoReconnect (" + (int)Math.ceil((double)this.autoReconnectTimer / 20.0) + ")")));
        if (this.autoReconnectTimer > 0) {
            --this.autoReconnectTimer;
            return;
        }
        LastServerRememberer.reconnect(this.field_2456);
    }
}
