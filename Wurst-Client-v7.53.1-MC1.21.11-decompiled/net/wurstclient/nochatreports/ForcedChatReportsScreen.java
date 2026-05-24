package net.wurstclient.nochatreports;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.class_11735;
import net.minecraft.class_12225;
import net.minecraft.class_2561;
import net.minecraft.class_2588;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_5489;
import net.minecraft.class_7417;
import net.minecraft.class_8828;
import net.wurstclient.WurstClient;
import net.wurstclient.other_features.NoChatReportsOtf;
import net.wurstclient.util.LastServerRememberer;

public final class ForcedChatReportsScreen
extends class_437 {
    private static final List<String> TRANSLATABLE_DISCONNECT_REASONS = Arrays.asList("multiplayer.disconnect.missing_public_key", "multiplayer.disconnect.invalid_public_key_signature", "multiplayer.disconnect.invalid_public_key", "multiplayer.disconnect.unsigned_chat");
    private static final List<String> LITERAL_DISCONNECT_REASONS = Arrays.asList("An internal error occurred in your connection.", "A secure profile is required to join this server.", "Secure profile expired.", "Secure profile invalid.");
    private final class_437 prevScreen;
    private final class_2561 reason;
    private class_5489 reasonFormatted = class_5489.field_26528;
    private int reasonHeight;
    private class_4185 signatureButton;
    private final Supplier<String> sigButtonMsg;

    public ForcedChatReportsScreen(class_437 prevScreen) {
        super((class_2561)class_2561.method_43470((String)"\u00a7c[\u00a76Wurst\u00a7c]\u00a7r ").method_10852((class_2561)class_2561.method_43470((String)WurstClient.INSTANCE.translate("gui.wurst.nochatreports.unsafe_server.title", new Object[0]))));
        this.prevScreen = prevScreen;
        this.reason = class_2561.method_43470((String)WurstClient.INSTANCE.translate("gui.wurst.nochatreports.unsafe_server.message", new Object[0]));
        NoChatReportsOtf ncr = WurstClient.INSTANCE.getOtfs().noChatReportsOtf;
        this.sigButtonMsg = () -> WurstClient.INSTANCE.translate("button.wurst.nochatreports.signatures_status", new Object[0]) + this.blockedOrAllowed(ncr.isEnabled());
    }

    private String blockedOrAllowed(boolean blocked) {
        return WurstClient.INSTANCE.translate("gui.wurst.generic.allcaps_" + (blocked ? "blocked" : "allowed"), new Object[0]);
    }

    protected void method_25426() {
        this.reasonFormatted = class_5489.method_30890((class_327)this.field_22793, (class_2561)this.reason, (int)(this.field_22789 - 50));
        int n = this.reasonFormatted.method_30887();
        Objects.requireNonNull(this.field_22793);
        this.reasonHeight = n * 9;
        int buttonX = this.field_22789 / 2 - 100;
        int n2 = (this.field_22790 - 78) / 2 + this.reasonHeight / 2;
        Objects.requireNonNull(this.field_22793);
        int belowReasonY = n2 + 9 * 2;
        int signaturesY = Math.min(belowReasonY, this.field_22790 - 68);
        int reconnectY = signaturesY + 24;
        int backButtonY = reconnectY + 24;
        this.signatureButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)this.sigButtonMsg.get()), b -> this.toggleSignatures()).method_46434(buttonX, signaturesY, 200, 20).method_46431();
        this.method_37063((class_364)this.signatureButton);
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Reconnect"), b -> LastServerRememberer.reconnect(this.prevScreen)).method_46434(buttonX, reconnectY, 200, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43471((String)"gui.toMenu"), b -> this.field_22787.method_1507(this.prevScreen)).method_46434(buttonX, backButtonY, 200, 20).method_46431());
    }

    private void toggleSignatures() {
        WurstClient.INSTANCE.getOtfs().noChatReportsOtf.doPrimaryAction();
        this.signatureButton.method_25355((class_2561)class_2561.method_43470((String)this.sigButtonMsg.get()));
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        int centerX = this.field_22789 / 2;
        int reasonY = (this.field_22790 - 68) / 2 - this.reasonHeight / 2;
        Objects.requireNonNull(this.field_22793);
        int titleY = reasonY - 9 * 2;
        context.method_27534(this.field_22793, this.field_22785, centerX, titleY, -6250336);
        class_12225 otherContext = context.method_75788();
        this.reasonFormatted.method_75816(class_11735.field_62010, centerX, reasonY, 9, otherContext);
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
        }
    }

    public boolean method_25422() {
        return false;
    }

    public static boolean isCausedByNoChatReports(class_2561 disconnectReason) {
        class_8828.class_2585 lt;
        class_2588 tr;
        if (!WurstClient.INSTANCE.getOtfs().noChatReportsOtf.isActive()) {
            return false;
        }
        class_7417 class_74172 = disconnectReason.method_10851();
        if (class_74172 instanceof class_2588 && TRANSLATABLE_DISCONNECT_REASONS.contains((tr = (class_2588)class_74172).method_11022())) {
            return true;
        }
        class_74172 = disconnectReason.method_10851();
        return class_74172 instanceof class_8828.class_2585 && LITERAL_DISCONNECT_REASONS.contains((lt = (class_8828.class_2585)class_74172).comp_737());
    }
}
