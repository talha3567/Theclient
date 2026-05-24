package net.wurstclient.serverfinder;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.class_11907;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_155;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_4185;
import net.minecraft.class_4267;
import net.minecraft.class_437;
import net.minecraft.class_500;
import net.minecraft.class_642;

public class CleanUpScreen
extends class_437 {
    private class_500 prevScreen;
    private class_4185 cleanUpButton;
    private boolean removeAll;
    private boolean cleanupFailed = true;
    private boolean cleanupOutdated = true;
    private boolean cleanupRename = true;
    private boolean cleanupUnknown = true;
    private boolean cleanupGriefMe;

    public CleanUpScreen(class_500 prevScreen) {
        super((class_2561)class_2561.method_43470((String)""));
        this.prevScreen = prevScreen;
    }

    public void method_25426() {
        this.method_37063((class_364)new CleanUpButton(this, this.field_22789 / 2 - 100, this.field_22790 / 4 + 168 + 12, () -> "Cancel", "", b -> this.method_25419()));
        this.cleanUpButton = new CleanUpButton(this, this.field_22789 / 2 - 100, this.field_22790 / 4 + 144 + 12, () -> "Clean Up", "Start the Clean Up with the settings\nyou specified above.\nIt might look like the game is not\nresponding for a couple of seconds.", b -> this.cleanUp());
        this.method_37063((class_364)this.cleanUpButton);
        this.method_37063((class_364)new CleanUpButton(this, this.field_22789 / 2 - 100, this.field_22790 / 4 - 24 + 12, () -> "Unknown Hosts: " + this.removeOrKeep(this.cleanupUnknown), "Servers that clearly don't exist.", b -> {
            this.cleanupUnknown = !this.cleanupUnknown;
        }));
        this.method_37063((class_364)new CleanUpButton(this, this.field_22789 / 2 - 100, this.field_22790 / 4 + 0 + 12, () -> "Outdated Servers: " + this.removeOrKeep(this.cleanupOutdated), "Servers that run a different Minecraft\nversion than you.", b -> {
            this.cleanupOutdated = !this.cleanupOutdated;
        }));
        this.method_37063((class_364)new CleanUpButton(this, this.field_22789 / 2 - 100, this.field_22790 / 4 + 24 + 12, () -> "Failed Ping: " + this.removeOrKeep(this.cleanupFailed), "All servers that failed the last ping.\nMake sure that the last ping is complete\nbefore you do this. That means: Go back,\npress the refresh button and wait until\nall servers are done refreshing.", b -> {
            this.cleanupFailed = !this.cleanupFailed;
        }));
        this.method_37063((class_364)new CleanUpButton(this, this.field_22789 / 2 - 100, this.field_22790 / 4 + 48 + 12, () -> "\"Grief me\" Servers: " + this.removeOrKeep(this.cleanupGriefMe), "All servers where the name starts with \"Grief me\"\nUseful for removing servers found by ServerFinder.", b -> {
            this.cleanupGriefMe = !this.cleanupGriefMe;
        }));
        this.method_37063((class_364)new CleanUpButton(this, this.field_22789 / 2 - 100, this.field_22790 / 4 + 72 + 12, () -> "\u00a7cRemove all Servers: " + this.yesOrNo(this.removeAll), "This will completely clear your server\nlist. \u00a7cUse with caution!\u00a7r", b -> {
            this.removeAll = !this.removeAll;
        }));
        this.method_37063((class_364)new CleanUpButton(this, this.field_22789 / 2 - 100, this.field_22790 / 4 + 96 + 12, () -> "Rename all Servers: " + this.yesOrNo(this.cleanupRename), "Renames your servers to \"Grief me #1\",\n\"Grief me #2\", etc.", b -> {
            this.cleanupRename = !this.cleanupRename;
        }));
    }

    private String yesOrNo(boolean b) {
        return b ? "Yes" : "No";
    }

    private String removeOrKeep(boolean b) {
        return b ? "Remove" : "Keep";
    }

    private void cleanUp() {
        class_642 server;
        int i;
        for (i = this.prevScreen.method_2529().method_2984() - 1; i >= 0; --i) {
            server = this.prevScreen.method_2529().method_2982(i);
            if (!this.removeAll && !this.shouldRemove(server)) continue;
            this.prevScreen.method_2529().method_2983(server);
        }
        if (this.cleanupRename) {
            for (i = 0; i < this.prevScreen.method_2529().method_2984(); ++i) {
                server = this.prevScreen.method_2529().method_2982(i);
                server.field_3752 = "Grief me #" + (i + 1);
            }
        }
        this.saveServerList();
        this.field_22787.method_1507((class_437)this.prevScreen);
    }

    private boolean shouldRemove(class_642 server) {
        if (server == null) {
            return false;
        }
        if (this.cleanupUnknown && this.isUnknownHost(server)) {
            return true;
        }
        if (this.cleanupOutdated && !this.isSameProtocol(server)) {
            return true;
        }
        if (this.cleanupFailed && this.isFailedPing(server)) {
            return true;
        }
        return this.cleanupGriefMe && this.isGriefMeServer(server);
    }

    private boolean isUnknownHost(class_642 server) {
        if (server.field_3757 == null) {
            return false;
        }
        if (server.field_3757.getString() == null) {
            return false;
        }
        return server.field_3757.getString().equals("\u00a74Can't resolve hostname");
    }

    private boolean isSameProtocol(class_642 server) {
        return server.field_3756 == class_155.method_16673().comp_4027();
    }

    private boolean isFailedPing(class_642 server) {
        return server.field_3758 != -2L && server.field_3758 < 0L;
    }

    private boolean isGriefMeServer(class_642 server) {
        return server.field_3752 != null && server.field_3752.startsWith("Grief me");
    }

    private void saveServerList() {
        this.prevScreen.method_2529().method_2987();
        class_4267 listWidget = this.prevScreen.field_3043;
        listWidget.method_20122(null);
        listWidget.method_20125(this.prevScreen.method_2529());
    }

    public boolean method_25404(class_11908 context) {
        if (context.comp_4795() == 257) {
            this.cleanUpButton.method_25306((class_11907)context);
        }
        return super.method_25404(context);
    }

    public boolean method_25402(class_11909 context, boolean doubleClick) {
        if (context.method_74245() == 3) {
            this.method_25419();
            return true;
        }
        return super.method_25402(context, doubleClick);
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        context.method_25300(this.field_22793, "Clean Up", this.field_22789 / 2, 20, -1);
        context.method_25300(this.field_22793, "Please select the servers you want to remove:", this.field_22789 / 2, 36, -6250336);
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
        }
        this.renderButtonTooltip(context, mouseX, mouseY);
    }

    private void renderButtonTooltip(class_332 context, int mouseX, int mouseY) {
        for (class_339 button : Screens.getButtons((class_437)this)) {
            if (!button.method_25367() || !(button instanceof CleanUpButton)) continue;
            CleanUpButton cuButton = (CleanUpButton)button;
            if (cuButton.tooltip.isEmpty()) continue;
            context.method_51434(this.field_22793, cuButton.tooltip, mouseX, mouseY);
            break;
        }
    }

    public void method_25419() {
        this.field_22787.method_1507((class_437)this.prevScreen);
    }

    private final class CleanUpButton
    extends class_4185 {
        private final Supplier<String> messageSupplier;
        private final List<class_2561> tooltip;

        public CleanUpButton(CleanUpScreen cleanUpScreen, int x, int y, Supplier<String> messageSupplier, String tooltip, class_4185.class_4241 pressAction) {
            super(x, y, 200, 20, (class_2561)class_2561.method_43470((String)messageSupplier.get()), pressAction, class_4185.field_40754);
            this.messageSupplier = messageSupplier;
            if (tooltip.isEmpty()) {
                this.tooltip = Arrays.asList(new class_2561[0]);
            } else {
                String[] lines = tooltip.split("\n");
                class_2561[] lines2 = new class_2561[lines.length];
                for (int i = 0; i < lines.length; ++i) {
                    lines2[i] = class_2561.method_43470((String)lines[i]);
                }
                this.tooltip = Arrays.asList(lines2);
            }
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
