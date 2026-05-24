package meteordevelopment.meteorclient.utils.player;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.AddonManager;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.screens.CommitsScreen;
import meteordevelopment.meteorclient.mixininterface.IComponent;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

public class TitleScreenCredits {
    private static final List<Credit> credits = new ArrayList<Credit>();

    private TitleScreenCredits() {
    }

    private static void init() {
        for (MeteorAddon addon : AddonManager.ADDONS) {
            TitleScreenCredits.add(addon);
        }
        credits.sort(Comparator.comparingInt(value -> value.addon == MeteorClient.ADDON ? Integer.MIN_VALUE : -MeteorClient.mc.font.width((FormattedText)value.text)));
        MeteorExecutor.execute(() -> {
            block9: for (Credit credit : credits) {
                if (credit.addon.getRepo() == null || credit.addon.getCommit() == null) continue;
                GithubRepo repo = credit.addon.getRepo();
                Http.Request request = Http.get("https://api.github.com/repos/%s/branches/%s".formatted(repo.getOwnerName(), repo.branch()));
                request.exceptionHandler(e -> MeteorClient.LOG.error("Could not fetch repository information for addon '{}'.", (Object)credit.addon.name, e));
                repo.authenticate(request);
                HttpResponse res = request.sendJsonResponse((Type)((Object)Response.class));
                switch (res.statusCode()) {
                    case 401: {
                        String message = "Invalid authentication token for repository '%s'".formatted(repo.getOwnerName());
                        MeteorToast toast = new MeteorToast.Builder("GitHub: Unauthorized").icon(Items.BARRIER).text(message).build();
                        MeteorClient.mc.getToastManager().addToast((Toast)toast);
                        MeteorClient.LOG.warn(message);
                        if (System.getenv("meteor.github.authorization") != null) continue block9;
                        MeteorClient.LOG.info("Consider setting an authorization token with the 'meteor.github.authorization' environment variable.");
                        MeteorClient.LOG.info("See: https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens");
                        break;
                    }
                    case 403: {
                        MeteorClient.LOG.warn("Could not fetch updates for addon '{}': Rate-limited by GitHub.", (Object)credit.addon.name);
                        break;
                    }
                    case 404: {
                        MeteorClient.LOG.warn("Could not fetch updates for addon '{}': GitHub repository '{}' not found.", (Object)credit.addon.name, (Object)repo.getOwnerName());
                        break;
                    }
                    case 200: {
                        if (credit.addon.getCommit().equals(((Response)res.body()).commit.sha)) break;
                        MutableComponent mutableComponent = credit.text;
                        synchronized (mutableComponent) {
                            credit.text.append((Component)Component.literal((String)"*").withStyle(ChatFormatting.RED));
                            ((IComponent)credit.text).meteor$invalidateCache();
                            break;
                        }
                    }
                }
            }
        });
    }

    private static void add(MeteorAddon addon) {
        Credit credit = new Credit(addon);
        credit.text.append((Component)Component.literal((String)addon.name).withStyle(style -> style.withColor(addon.color.getPacked())));
        credit.text.append((Component)Component.literal((String)" by ").withStyle(ChatFormatting.GRAY));
        for (int i = 0; i < addon.authors.length; ++i) {
            if (i > 0) {
                credit.text.append((Component)Component.literal((String)(i == addon.authors.length - 1 ? " & " : ", ")).withStyle(ChatFormatting.GRAY));
            }
            credit.text.append((Component)Component.literal((String)addon.authors[i]).withStyle(ChatFormatting.WHITE));
        }
        credits.add(credit);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(GuiGraphicsExtractor graphics) {
        if (credits.isEmpty()) {
            TitleScreenCredits.init();
        }
        int y = 3;
        for (Credit credit : credits) {
            MutableComponent mutableComponent = credit.text;
            synchronized (mutableComponent) {
                int x = MeteorClient.mc.screen.width - 3 - MeteorClient.mc.font.width((FormattedText)credit.text);
                graphics.text(MeteorClient.mc.font, (Component)credit.text, x, y, -1);
            }
            Objects.requireNonNull(MeteorClient.mc.font);
            y += 9 + 2;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean onClicked(double mouseX, double mouseY) {
        int y = 3;
        for (Credit credit : credits) {
            int width;
            MutableComponent mutableComponent = credit.text;
            synchronized (mutableComponent) {
                width = MeteorClient.mc.font.width((FormattedText)credit.text);
            }
            int x = MeteorClient.mc.screen.width - 3 - width;
            if (mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y) {
                Objects.requireNonNull(MeteorClient.mc.font);
                if (mouseY <= (double)(y + 9 + 2) && credit.addon.getRepo() != null && credit.addon.getCommit() != null) {
                    MeteorClient.mc.setScreen((Screen)new CommitsScreen(GuiThemes.get(), credit.addon));
                    return true;
                }
            }
            Objects.requireNonNull(MeteorClient.mc.font);
            y += 9 + 2;
        }
        return false;
    }

    private static class Credit {
        public final MeteorAddon addon;
        public final MutableComponent text = Component.empty();

        public Credit(MeteorAddon addon) {
            this.addon = addon;
        }
    }

    private static class Response {
        public Commit commit;

        private Response() {
        }
    }

    private static class Commit {
        public String sha;

        private Commit() {
        }
    }
}
