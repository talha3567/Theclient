package net.wurstclient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_310;
import net.wurstclient.Feature;
import net.wurstclient.FriendsList;
import net.wurstclient.RotationFaker;
import net.wurstclient.WurstTranslator;
import net.wurstclient.altmanager.AltManager;
import net.wurstclient.altmanager.Encryption;
import net.wurstclient.analytics.PlausibleAnalytics;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.command.CmdList;
import net.wurstclient.command.CmdProcessor;
import net.wurstclient.command.Command;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.ChatOutputListener;
import net.wurstclient.events.GUIRenderListener;
import net.wurstclient.events.KeyPressListener;
import net.wurstclient.events.MouseButtonPressListener;
import net.wurstclient.events.PostMotionListener;
import net.wurstclient.events.PreMotionListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hack.HackList;
import net.wurstclient.hud.IngameHUD;
import net.wurstclient.keybinds.KeybindList;
import net.wurstclient.keybinds.KeybindProcessor;
import net.wurstclient.mixinterface.IMinecraftClient;
import net.wurstclient.navigator.Navigator;
import net.wurstclient.other_feature.OtfList;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.SettingsFile;
import net.wurstclient.update.ProblematicResourcePackDetector;
import net.wurstclient.update.WurstUpdater;
import net.wurstclient.util.json.JsonException;

public enum WurstClient {
    INSTANCE;

    public static class_310 MC;
    public static IMinecraftClient IMC;
    public static final String VERSION = "7.53.1";
    public static final String MC_VERSION = "1.21.11";
    private PlausibleAnalytics plausible;
    private EventManager eventManager;
    private AltManager altManager;
    private HackList hax;
    private CmdList cmds;
    private OtfList otfs;
    private SettingsFile settingsFile;
    private Path settingsProfileFolder;
    private KeybindList keybinds;
    private ClickGui gui;
    private Navigator navigator;
    private CmdProcessor cmdProcessor;
    private IngameHUD hud;
    private RotationFaker rotationFaker;
    private FriendsList friends;
    private WurstTranslator translator;
    private boolean enabled = true;
    private static boolean guiInitialized;
    private WurstUpdater updater;
    private ProblematicResourcePackDetector problematicPackDetector;
    private Path wurstFolder;

    public void initialize() {
        System.out.println("Starting Wurst Client...");
        MC = class_310.method_1551();
        IMC = (IMinecraftClient)MC;
        this.wurstFolder = this.createWurstFolder();
        Path analyticsFile = this.wurstFolder.resolve("analytics.json");
        this.plausible = new PlausibleAnalytics(analyticsFile);
        this.plausible.pageview("/");
        this.eventManager = new EventManager(this);
        Path enabledHacksFile = this.wurstFolder.resolve("enabled-hacks.json");
        this.hax = new HackList(enabledHacksFile);
        this.cmds = new CmdList();
        this.otfs = new OtfList();
        Path settingsFile = this.wurstFolder.resolve("settings.json");
        this.settingsProfileFolder = this.wurstFolder.resolve("settings");
        this.settingsFile = new SettingsFile(settingsFile, this.hax, this.cmds, this.otfs);
        this.settingsFile.load();
        this.hax.tooManyHaxHack.loadBlockedHacksFile();
        Path keybindsFile = this.wurstFolder.resolve("keybinds.json");
        this.keybinds = new KeybindList(keybindsFile);
        Path guiFile = this.wurstFolder.resolve("windows.json");
        this.gui = new ClickGui(guiFile);
        Path preferencesFile = this.wurstFolder.resolve("preferences.json");
        this.navigator = new Navigator(preferencesFile, this.hax, this.cmds, this.otfs);
        Path friendsFile = this.wurstFolder.resolve("friends.json");
        this.friends = new FriendsList(friendsFile);
        this.friends.load();
        this.translator = new WurstTranslator();
        this.cmdProcessor = new CmdProcessor(this.cmds);
        this.eventManager.add(ChatOutputListener.class, this.cmdProcessor);
        KeybindProcessor keybindProcessor = new KeybindProcessor(this.hax, this.keybinds, this.cmdProcessor);
        this.eventManager.add(KeyPressListener.class, keybindProcessor);
        this.eventManager.add(MouseButtonPressListener.class, keybindProcessor);
        this.hud = new IngameHUD();
        this.eventManager.add(GUIRenderListener.class, this.hud);
        this.rotationFaker = new RotationFaker();
        this.eventManager.add(PreMotionListener.class, this.rotationFaker);
        this.eventManager.add(PostMotionListener.class, this.rotationFaker);
        this.updater = new WurstUpdater();
        this.eventManager.add(UpdateListener.class, this.updater);
        this.problematicPackDetector = new ProblematicResourcePackDetector();
        this.problematicPackDetector.start();
        Path altsFile = this.wurstFolder.resolve("alts.encrypted_json");
        Path encFolder = Encryption.chooseEncryptionFolder();
        this.altManager = new AltManager(altsFile, encFolder);
    }

    private Path createWurstFolder() {
        Path dotMinecraftFolder = WurstClient.MC.field_1697.toPath().normalize();
        Path wurstFolder = dotMinecraftFolder.resolve("wurst");
        try {
            Files.createDirectories(wurstFolder, new FileAttribute[0]);
        }
        catch (IOException e) {
            throw new RuntimeException("Couldn't create .minecraft/wurst folder.", e);
        }
        return wurstFolder;
    }

    public String translate(String key, Object ... args) {
        return this.translator.translate(key, args);
    }

    public PlausibleAnalytics getPlausible() {
        return this.plausible;
    }

    public EventManager getEventManager() {
        return this.eventManager;
    }

    public void saveSettings() {
        this.settingsFile.save();
    }

    public ArrayList<Path> listSettingsProfiles() {
        ArrayList arrayList;
        block9: {
            if (!Files.isDirectory(this.settingsProfileFolder, new LinkOption[0])) {
                return new ArrayList<Path>();
            }
            Stream<Path> files = Files.list(this.settingsProfileFolder);
            try {
                arrayList = files.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).collect(Collectors.toCollection(ArrayList::new));
                if (files == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (files != null) {
                        try {
                            files.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            files.close();
        }
        return arrayList;
    }

    public void loadSettingsProfile(String fileName) throws IOException, JsonException {
        this.settingsFile.loadProfile(this.settingsProfileFolder.resolve(fileName));
    }

    public void saveSettingsProfile(String fileName) throws IOException, JsonException {
        this.settingsFile.saveProfile(this.settingsProfileFolder.resolve(fileName));
    }

    public HackList getHax() {
        return this.hax;
    }

    public CmdList getCmds() {
        return this.cmds;
    }

    public OtfList getOtfs() {
        return this.otfs;
    }

    public Feature getFeatureByName(String name) {
        Hack hack = this.getHax().getHackByName(name);
        if (hack != null) {
            return hack;
        }
        Command cmd = this.getCmds().getCmdByName(name.substring(1));
        if (cmd != null) {
            return cmd;
        }
        OtherFeature otf = this.getOtfs().getOtfByName(name);
        return otf;
    }

    public KeybindList getKeybinds() {
        return this.keybinds;
    }

    public ClickGui getGui() {
        if (!guiInitialized) {
            guiInitialized = true;
            this.gui.init();
        }
        return this.gui;
    }

    public Navigator getNavigator() {
        return this.navigator;
    }

    public CmdProcessor getCmdProcessor() {
        return this.cmdProcessor;
    }

    public IngameHUD getHud() {
        return this.hud;
    }

    public RotationFaker getRotationFaker() {
        return this.rotationFaker;
    }

    public FriendsList getFriends() {
        return this.friends;
    }

    public WurstTranslator getTranslator() {
        return this.translator;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            this.hax.panicHack.setEnabled(true);
            this.hax.panicHack.onUpdate();
        }
    }

    public WurstUpdater getUpdater() {
        return this.updater;
    }

    public ProblematicResourcePackDetector getProblematicPackDetector() {
        return this.problematicPackDetector;
    }

    public Path getWurstFolder() {
        return this.wurstFolder;
    }

    public AltManager getAltManager() {
        return this.altManager;
    }
}
