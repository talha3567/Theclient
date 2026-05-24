package meteordevelopment.meteorclient;

import java.io.File;
import java.lang.invoke.MethodHandles;
import meteordevelopment.meteorclient.addons.AddonManager;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.meteor.KeyInputEvent;
import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.hud.screens.AddHudElementScreen;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.systems.hud.screens.HudElementScreen;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.DiscordPresence;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.ReflectInit;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Version;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.misc.input.KeyBinds;
import meteordevelopment.meteorclient.utils.network.OnlinePlayers;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class MeteorClient
implements ClientModInitializer {
    public static final String MOD_ID = "meteor-client";
    public static final ModMetadata MOD_META;
    public static final String NAME;
    public static final Version VERSION;
    public static final String BUILD_NUMBER;
    public static MeteorClient INSTANCE;
    public static MeteorAddon ADDON;
    public static Minecraft mc;
    public static final IEventBus EVENT_BUS;
    public static final File FOLDER;
    public static final Logger LOG;
    private boolean wasWidgetScreen;
    private boolean wasHudHiddenRoot;

    public void onInitializeClient() {
        if (INSTANCE == null) {
            INSTANCE = this;
            return;
        }
        mc = Minecraft.getInstance();
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            LOG.info("Force loading mixins");
            MixinEnvironment.getCurrentEnvironment().audit();
        }
        LOG.info("Initializing {}", (Object)NAME);
        if (!FOLDER.exists()) {
            FOLDER.getParentFile().mkdirs();
            FOLDER.mkdir();
            Systems.addPreLoadTask(() -> Modules.get().get(DiscordPresence.class).enable());
        }
        AddonManager.init();
        AddonManager.ADDONS.forEach(addon -> {
            try {
                EVENT_BUS.registerLambdaFactory(addon.getPackage(), (lookupInMethod, klass) -> (MethodHandles.Lookup)lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
            }
            catch (AbstractMethodError e) {
                throw new RuntimeException("Addon \"%s\" is too old and cannot be ran.".formatted(addon.name), e);
            }
        });
        ReflectInit.registerPackages();
        ReflectInit.init(PreInit.class);
        Categories.init();
        Systems.init();
        EVENT_BUS.subscribe(this);
        AddonManager.ADDONS.forEach(MeteorAddon::onInitialize);
        Modules.get().sortModules();
        Systems.load();
        ReflectInit.init(PostInit.class);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            OnlinePlayers.leave();
            Systems.save();
            GuiThemes.save();
        }));
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (MeteorClient.mc.screen == null && mc.getOverlay() == null && KeyBinds.OPEN_COMMANDS.consumeClick()) {
            mc.setScreen((Screen)new ChatScreen(Config.get().prefix.get(), true));
        }
    }

    @EventHandler
    private void onKey(KeyInputEvent event) {
        if (event.action == KeyAction.Press && KeyBinds.OPEN_GUI.matches(event.input)) {
            this.toggleGui();
        }
    }

    @EventHandler
    private void onMouseClick(MouseClickEvent event) {
        if (event.action == KeyAction.Press && KeyBinds.OPEN_GUI.matchesMouse(event.click)) {
            this.toggleGui();
        }
    }

    private void toggleGui() {
        if (Utils.canCloseGui()) {
            MeteorClient.mc.screen.onClose();
        } else if (Utils.canOpenGui()) {
            Tabs.get().getFirst().openScreen(GuiThemes.get());
        }
    }

    @EventHandler(priority=-200)
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof WidgetScreen) {
            if (!this.wasWidgetScreen) {
                this.wasHudHiddenRoot = MeteorClient.mc.options.hideGui;
            }
            if (GuiThemes.get().hideHUD() || this.wasHudHiddenRoot) {
                MeteorClient.mc.options.hideGui = !(event.screen instanceof HudEditorScreen) && !(event.screen instanceof AddHudElementScreen) && !(event.screen instanceof HudElementScreen);
            }
        } else {
            if (this.wasWidgetScreen) {
                MeteorClient.mc.options.hideGui = this.wasHudHiddenRoot;
            }
            this.wasHudHiddenRoot = MeteorClient.mc.options.hideGui;
        }
        this.wasWidgetScreen = event.screen instanceof WidgetScreen;
    }

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath((String)MOD_ID, (String)path);
    }

    static {
        EVENT_BUS = new EventBus();
        FOLDER = FabricLoader.getInstance().getGameDir().resolve(MOD_ID).toFile();
        MOD_META = ((ModContainer)FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow()).getMetadata();
        NAME = MOD_META.getName();
        LOG = LoggerFactory.getLogger(NAME);
        String versionString = MOD_META.getVersion().getFriendlyString();
        if (versionString.contains("-")) {
            versionString = versionString.split("-")[0];
        }
        if (versionString.equals("${version}")) {
            versionString = "0.0.0";
        }
        VERSION = new Version(versionString);
        BUILD_NUMBER = MOD_META.getCustomValue("meteor-client:build_number").getAsString();
    }
}
