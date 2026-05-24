package net.wurstclient.options;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.class_11907;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.wurstclient.WurstClient;
import net.wurstclient.analytics.PlausibleAnalytics;
import net.wurstclient.commands.FriendsCmd;
import net.wurstclient.hacks.XRayHack;
import net.wurstclient.options.KeybindManagerScreen;
import net.wurstclient.options.ZoomManagerScreen;
import net.wurstclient.other_features.VanillaSpoofOtf;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.ChatUtils;

public class WurstOptionsScreen
extends class_437 {
    private class_437 prevScreen;

    public WurstOptionsScreen(class_437 prevScreen) {
        super((class_2561)class_2561.method_43470((String)""));
        this.prevScreen = prevScreen;
    }

    public void method_25426() {
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Back"), b -> this.field_22787.method_1507(this.prevScreen)).method_46434(this.field_22789 / 2 - 100, this.field_22790 / 4 + 144 - 16, 200, 20).method_46431());
        this.addSettingButtons();
        this.addManagerButtons();
        this.addLinkButtons();
    }

    private void addSettingButtons() {
        WurstClient wurst = WurstClient.INSTANCE;
        FriendsCmd friendsCmd = wurst.getCmds().friendsCmd;
        CheckboxSetting middleClickFriends = friendsCmd.getMiddleClickFriends();
        PlausibleAnalytics plausible = wurst.getPlausible();
        VanillaSpoofOtf vanillaSpoofOtf = wurst.getOtfs().vanillaSpoofOtf;
        CheckboxSetting forceEnglish = wurst.getOtfs().translationsOtf.getForceEnglish();
        new WurstOptionsButton(this, -154, 24, () -> "Click Friends: " + (middleClickFriends.isChecked() ? "ON" : "OFF"), middleClickFriends.getWrappedDescription(200), b -> middleClickFriends.setChecked(!middleClickFriends.isChecked()));
        new WurstOptionsButton(this, -154, 48, () -> "Count Users: " + (plausible.isEnabled() ? "ON" : "OFF"), "Counts how many people are using Wurst and which versions are the most popular. This data helps me to decide when I can stop supporting old versions.\n\nThese statistics are completely anonymous, never sold, and stay in the EU (I'm self-hosting Plausible in Germany). There are no cookies or persistent identifiers (see plausible.io).", b -> plausible.setEnabled(!plausible.isEnabled()));
        new WurstOptionsButton(this, -154, 72, () -> "Spoof Vanilla: " + (vanillaSpoofOtf.isEnabled() ? "ON" : "OFF"), vanillaSpoofOtf.getDescription(), b -> vanillaSpoofOtf.doPrimaryAction());
        new WurstOptionsButton(this, -154, 96, () -> "Translations: " + (!forceEnglish.isChecked() ? "ON" : "OFF"), "Allows text in Wurst to be displayed in other languages than English. It will use the same language that Minecraft is set to.\n\nThis is an experimental feature!", b -> forceEnglish.setChecked(!forceEnglish.isChecked()));
    }

    private void addManagerButtons() {
        XRayHack xRayHack = WurstClient.INSTANCE.getHax().xRayHack;
        new WurstOptionsButton(this, -50, 24, () -> "Keybinds", "Keybinds allow you to toggle any hack or command by simply pressing a button.", b -> this.field_22787.method_1507((class_437)new KeybindManagerScreen(this)));
        new WurstOptionsButton(this, -50, 48, () -> "X-Ray Blocks", "Manager for the blocks that X-Ray will show.", b -> xRayHack.openBlockListEditor(this));
        new WurstOptionsButton(this, -50, 72, () -> "Zoom", "The Zoom Manager allows you to change the zoom key and how far it will zoom in.", b -> this.field_22787.method_1507((class_437)new ZoomManagerScreen(this)));
    }

    private void addLinkButtons() {
        class_156.class_158 os = class_156.method_668();
        new WurstOptionsButton(this, 54, 24, () -> "Official Website", "\u00a7n\u00a7lWurstClient.net", b -> os.method_670("https://www.wurstclient.net/options-website/"));
        new WurstOptionsButton(this, 54, 48, () -> "Wurst Wiki", "\u00a7n\u00a7lWurst.Wiki", b -> os.method_670("https://www.wurstclient.net/options-wiki/"));
        new WurstOptionsButton(this, 54, 72, () -> "WurstForum", "\u00a7n\u00a7lWurstForum.net", b -> os.method_670("https://www.wurstclient.net/options-forum/"));
        new WurstOptionsButton(this, 54, 96, () -> "Twitter", "@Wurst_Imperium", b -> os.method_670("https://www.wurstclient.net/options-twitter/"));
        new WurstOptionsButton(this, 54, 120, () -> "Donate", "\u00a7n\u00a7lWurstClient.net/donate\nDonate now to help me keep the Wurst Client alive and free to use for everyone.\n\nEvery bit helps and is much appreciated! You can also get a few cool perks in return.", b -> os.method_670("https://www.wurstclient.net/options-donate/"));
    }

    public void method_25419() {
        this.field_22787.method_1507(this.prevScreen);
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        this.renderTitles(context);
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
        }
        this.renderButtonTooltip(context, mouseX, mouseY);
    }

    private void renderTitles(class_332 context) {
        class_327 tr = this.field_22787.field_1772;
        int middleX = this.field_22789 / 2;
        int y1 = 40;
        int y2 = this.field_22790 / 4 + 24 - 28;
        context.method_25300(tr, "Wurst Options", middleX, y1, -1);
        context.method_25300(tr, "Settings", middleX - 104, y2, -986896);
        context.method_25300(tr, "Managers", middleX, y2, -986896);
        context.method_25300(tr, "Links", middleX + 104, y2, -986896);
    }

    private void renderButtonTooltip(class_332 context, int mouseX, int mouseY) {
        for (class_339 button : Screens.getButtons((class_437)this)) {
            if (!button.method_25367() || !(button instanceof WurstOptionsButton)) continue;
            WurstOptionsButton woButton = (WurstOptionsButton)button;
            if (woButton.tooltip.isEmpty()) continue;
            context.method_51434(this.field_22793, woButton.tooltip, mouseX, mouseY);
            break;
        }
    }

    private final class WurstOptionsButton
    extends class_4185 {
        private final Supplier<String> messageSupplier;
        private final List<class_2561> tooltip;

        public WurstOptionsButton(WurstOptionsScreen wurstOptionsScreen, int xOffset, int yOffset, Supplier<String> messageSupplier, String tooltip, class_4185.class_4241 pressAction) {
            super(wurstOptionsScreen.field_22789 / 2 + xOffset, wurstOptionsScreen.field_22790 / 4 - 16 + yOffset, 100, 20, (class_2561)class_2561.method_43470((String)messageSupplier.get()), pressAction, class_4185.field_40754);
            this.messageSupplier = messageSupplier;
            if (tooltip.isEmpty()) {
                this.tooltip = Arrays.asList(new class_2561[0]);
            } else {
                String[] lines = ChatUtils.wrapText(tooltip, 200).split("\n");
                class_2561[] lines2 = new class_2561[lines.length];
                for (int i = 0; i < lines.length; ++i) {
                    lines2[i] = class_2561.method_43470((String)lines[i]);
                }
                this.tooltip = Arrays.asList(lines2);
            }
            wurstOptionsScreen.method_37063((class_364)this);
        }

        public void method_25306(class_11907 context) {
            super.method_25306(context);
            this.method_25355((class_2561)class_2561.method_43470((String)this.messageSupplier.get()));
        }

        protected void method_75752(class_332 drawContext, int i, int j, float f) {
            this.method_75794(drawContext);
            this.method_75793(drawContext.method_75787((class_339)this, class_332.class_12228.field_63850));
        }
    }
}
