package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.ChatInputListener;
import net.wurstclient.events.ChatOutputListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.chattranslator.FilterOwnMessagesSetting;
import net.wurstclient.hacks.chattranslator.GoogleTranslate;
import net.wurstclient.hacks.chattranslator.LanguageSetting;
import net.wurstclient.hacks.chattranslator.WhatToTranslateSetting;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags(value={"chat translator", "ChatTranslate", "chat translate", "ChatTranslation", "chat translation", "AutoTranslate", "auto translate", "AutoTranslator", "auto translator", "AutoTranslation", "auto translation", "GoogleTranslate", "google translate", "GoogleTranslator", "google translator", "GoogleTranslation", "google translation"})
public final class ChatTranslatorHack
extends Hack
implements ChatInputListener,
ChatOutputListener {
    private final WhatToTranslateSetting whatToTranslate = new WhatToTranslateSetting();
    private final LanguageSetting playerLanguage = LanguageSetting.withoutAutoDetect("Your language", "description.wurst.setting.chattranslator.your_language", LanguageSetting.Language.ENGLISH);
    private final LanguageSetting otherLanguage = LanguageSetting.withoutAutoDetect("Other language", "description.wurst.setting.chattranslator.other_language", LanguageSetting.Language.CHINESE_SIMPLIFIED);
    private final CheckboxSetting autoDetectReceived = new CheckboxSetting("Detect received language", "description.wurst.setting.chattranslator.detect_received_language", true);
    private final CheckboxSetting autoDetectSent = new CheckboxSetting("Detect sent language", "description.wurst.setting.chattranslator.detect_sent_language", true);
    private final FilterOwnMessagesSetting filterOwnMessages = new FilterOwnMessagesSetting();

    public ChatTranslatorHack() {
        super("ChatTranslator");
        this.setCategory(Category.CHAT);
        this.addSetting(this.whatToTranslate);
        this.addSetting(this.playerLanguage);
        this.addSetting(this.otherLanguage);
        this.addSetting(this.autoDetectReceived);
        this.addSetting(this.autoDetectSent);
        this.addSetting(this.filterOwnMessages);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(ChatInputListener.class, this);
        EVENTS.add(ChatOutputListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(ChatInputListener.class, this);
        EVENTS.remove(ChatOutputListener.class, this);
    }

    @Override
    public void onReceivedMessage(ChatInputListener.ChatInputEvent event) {
        if (!this.whatToTranslate.includesReceived()) {
            return;
        }
        String message = event.getComponent().getString();
        LanguageSetting.Language fromLang = this.autoDetectReceived.isChecked() ? LanguageSetting.Language.AUTO_DETECT : (LanguageSetting.Language)((Object)this.otherLanguage.getSelected());
        LanguageSetting.Language toLang = (LanguageSetting.Language)((Object)this.playerLanguage.getSelected());
        if (message.startsWith("\u00a7c[\u00a76Wurst\u00a7c]\u00a7r ") || message.startsWith(toLang.getPrefix())) {
            return;
        }
        if (this.filterOwnMessages.isChecked() && this.filterOwnMessages.isOwnMessage(message)) {
            return;
        }
        Thread.ofVirtual().name("ChatTranslator").uncaughtExceptionHandler((t, e) -> e.printStackTrace()).start(() -> this.showTranslated(message, fromLang, toLang));
    }

    private void showTranslated(String message, LanguageSetting.Language fromLang, LanguageSetting.Language toLang) {
        String translated = GoogleTranslate.translate(message, fromLang.getValue(), toLang.getValue());
        if (translated != null) {
            ChatTranslatorHack.MC.field_1705.method_1743().method_1812(toLang.prefixText(translated));
        }
    }

    @Override
    public void onSentMessage(ChatOutputListener.ChatOutputEvent event) {
        if (!this.whatToTranslate.includesSent()) {
            return;
        }
        String message = event.getMessage();
        LanguageSetting.Language fromLang = this.autoDetectSent.isChecked() ? LanguageSetting.Language.AUTO_DETECT : (LanguageSetting.Language)((Object)this.playerLanguage.getSelected());
        LanguageSetting.Language toLang = (LanguageSetting.Language)((Object)this.otherLanguage.getSelected());
        if (message.startsWith("/") || message.startsWith(".")) {
            return;
        }
        event.cancel();
        Thread.ofVirtual().name("ChatTranslator").uncaughtExceptionHandler((t, e) -> e.printStackTrace()).start(() -> this.sendTranslated(message, fromLang, toLang));
    }

    private void sendTranslated(String message, LanguageSetting.Language fromLang, LanguageSetting.Language toLang) {
        String translated = GoogleTranslate.translate(message, fromLang.getValue(), toLang.getValue());
        if (translated == null) {
            translated = message;
        }
        MC.method_1562().method_45729(translated);
    }
}
