package net.wurstclient.other_features;

import java.net.URI;
import java.util.concurrent.Executor;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_2583;
import net.minecraft.class_2588;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_635;
import net.minecraft.class_7417;
import net.minecraft.class_7427;
import net.minecraft.class_7469;
import net.minecraft.class_7591;
import net.minecraft.class_7610;
import net.minecraft.class_7818;
import net.wurstclient.Category;
import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.events.ChatInputListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.ChatUtils;

@DontBlock
@SearchTags(value={"no chat reports", "NoEncryption", "no encryption", "NoChatSigning", "no chat signing"})
public final class NoChatReportsOtf
extends OtherFeature
implements UpdateListener,
ChatInputListener {
    private final CheckboxSetting disableSignatures = new CheckboxSetting("Disable signatures", true){

        @Override
        public void update() {
            EVENTS.add(UpdateListener.class, NoChatReportsOtf.this);
        }
    };

    public NoChatReportsOtf() {
        super("NoChatReports", "description.wurst.other_feature.nochatreports");
        this.addSetting(this.disableSignatures);
        ClientLoginConnectionEvents.INIT.register(this::onLoginStart);
        EVENTS.add(ChatInputListener.class, this);
    }

    @Override
    public void onUpdate() {
        class_634 netHandler = MC.method_1562();
        if (netHandler == null) {
            return;
        }
        if (this.isActive()) {
            netHandler.field_40799 = null;
            netHandler.field_39808 = class_7610.class_7612.field_40694;
        } else if (netHandler.field_40799 == null) {
            MC.method_43590().method_46522().thenAcceptAsync(optional -> optional.ifPresent(profileKeys -> {
                netHandler.field_40799 = class_7818.method_46273((class_7427)profileKeys);
            }), (Executor)MC);
        }
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onReceivedMessage(ChatInputListener.ChatInputEvent event) {
        if (!this.isActive()) {
            return;
        }
        class_2561 originalText = event.getComponent();
        class_7417 class_74172 = originalText.method_10851();
        if (!(class_74172 instanceof class_2588)) {
            return;
        }
        class_2588 trContent = (class_2588)class_74172;
        if (!trContent.method_11022().equals("chat.disabled.missingProfileKey")) {
            return;
        }
        event.cancel();
        class_2558.class_10608 clickEvent = new class_2558.class_10608(URI.create("https://www.wurstclient.net/chat-disabled-mpk/"));
        class_2568.class_10613 hoverEvent = new class_2568.class_10613((class_2561)class_2561.method_43470((String)"Original message: ").method_10852(originalText));
        ChatUtils.component((class_2561)class_2561.method_43470((String)"The server is refusing to let you chat without enabling chat reports. Click \u00a7nhere\u00a7r to learn more.").method_27694(arg_0 -> NoChatReportsOtf.lambda$onReceivedMessage$2((class_2558)clickEvent, (class_2568)hoverEvent, arg_0)));
    }

    private void onLoginStart(class_635 handler, class_310 client) {
        EVENTS.add(UpdateListener.class, this);
    }

    public class_7591 modifyIndicator(class_2561 message, class_7469 signature, class_7591 indicator) {
        if (!WURST.isEnabled() || MC.method_1542()) {
            return indicator;
        }
        if (indicator != null || signature == null) {
            return indicator;
        }
        return new class_7591(15224664, class_7591.class_7592.field_39763, (class_2561)class_2561.method_43470((String)("\u00a7c[\u00a76Wurst\u00a7c]\u00a7r \u00a7cReportable\u00a7r - " + WURST.translate("description.wurst.nochatreports.message_is_reportable", new Object[0]))), "Reportable");
    }

    @Override
    public boolean isEnabled() {
        return this.disableSignatures.isChecked();
    }

    public boolean isActive() {
        return this.isEnabled() && WURST.isEnabled() && !MC.method_1542();
    }

    @Override
    public String getPrimaryAction() {
        return WURST.translate("button.wurst.nochatreports." + (this.isEnabled() ? "re-enable_signatures" : "disable_signatures"), new Object[0]);
    }

    @Override
    public void doPrimaryAction() {
        this.disableSignatures.setChecked(!this.disableSignatures.isChecked());
    }

    @Override
    public Category getCategory() {
        return Category.CHAT;
    }

    private static /* synthetic */ class_2583 lambda$onReceivedMessage$2(class_2558 clickEvent, class_2568 hoverEvent, class_2583 s) {
        return s.method_10958(clickEvent).method_10949(hoverEvent);
    }
}
