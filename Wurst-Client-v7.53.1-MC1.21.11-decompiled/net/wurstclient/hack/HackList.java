package net.wurstclient.hack;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_128;
import net.minecraft.class_148;
import net.wurstclient.WurstClient;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.EnabledHacksFile;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.AimAssistHack;
import net.wurstclient.hacks.AirPlaceHack;
import net.wurstclient.hacks.AnchorAuraHack;
import net.wurstclient.hacks.AntiAfkHack;
import net.wurstclient.hacks.AntiBlindHack;
import net.wurstclient.hacks.AntiCactusHack;
import net.wurstclient.hacks.AntiEntityPushHack;
import net.wurstclient.hacks.AntiHungerHack;
import net.wurstclient.hacks.AntiKnockbackHack;
import net.wurstclient.hacks.AntiSpamHack;
import net.wurstclient.hacks.AntiWaterPushHack;
import net.wurstclient.hacks.AntiWobbleHack;
import net.wurstclient.hacks.ArrowDmgHack;
import net.wurstclient.hacks.AutoArmorHack;
import net.wurstclient.hacks.AutoBuildHack;
import net.wurstclient.hacks.AutoCompleteHack;
import net.wurstclient.hacks.AutoDropHack;
import net.wurstclient.hacks.AutoEatHack;
import net.wurstclient.hacks.AutoFarmHack;
import net.wurstclient.hacks.AutoFishHack;
import net.wurstclient.hacks.AutoLeaveHack;
import net.wurstclient.hacks.AutoLibrarianHack;
import net.wurstclient.hacks.AutoMineHack;
import net.wurstclient.hacks.AutoPotionHack;
import net.wurstclient.hacks.AutoReconnectHack;
import net.wurstclient.hacks.AutoRespawnHack;
import net.wurstclient.hacks.AutoSignHack;
import net.wurstclient.hacks.AutoSoupHack;
import net.wurstclient.hacks.AutoSprintHack;
import net.wurstclient.hacks.AutoStealHack;
import net.wurstclient.hacks.AutoSwimHack;
import net.wurstclient.hacks.AutoSwitchHack;
import net.wurstclient.hacks.AutoSwordHack;
import net.wurstclient.hacks.AutoToolHack;
import net.wurstclient.hacks.AutoTotemHack;
import net.wurstclient.hacks.AutoWalkHack;
import net.wurstclient.hacks.BarrierEspHack;
import net.wurstclient.hacks.BaseFinderHack;
import net.wurstclient.hacks.BlinkHack;
import net.wurstclient.hacks.BoatFlyHack;
import net.wurstclient.hacks.BonemealAuraHack;
import net.wurstclient.hacks.BowAimbotHack;
import net.wurstclient.hacks.BuildRandomHack;
import net.wurstclient.hacks.BunnyHopHack;
import net.wurstclient.hacks.CameraDistanceHack;
import net.wurstclient.hacks.CameraNoClipHack;
import net.wurstclient.hacks.CaveFinderHack;
import net.wurstclient.hacks.ChatTranslatorHack;
import net.wurstclient.hacks.ChestEspHack;
import net.wurstclient.hacks.ClickAuraHack;
import net.wurstclient.hacks.ClickGuiHack;
import net.wurstclient.hacks.CrashChestHack;
import net.wurstclient.hacks.CreativeFlightHack;
import net.wurstclient.hacks.CriticalsHack;
import net.wurstclient.hacks.CrystalAuraHack;
import net.wurstclient.hacks.DerpHack;
import net.wurstclient.hacks.DolphinHack;
import net.wurstclient.hacks.ExcavatorHack;
import net.wurstclient.hacks.ExtraElytraHack;
import net.wurstclient.hacks.FancyChatHack;
import net.wurstclient.hacks.FastBreakHack;
import net.wurstclient.hacks.FastLadderHack;
import net.wurstclient.hacks.FastPlaceHack;
import net.wurstclient.hacks.FeedAuraHack;
import net.wurstclient.hacks.FightBotHack;
import net.wurstclient.hacks.FishHack;
import net.wurstclient.hacks.FlightHack;
import net.wurstclient.hacks.FollowHack;
import net.wurstclient.hacks.ForceOpHack;
import net.wurstclient.hacks.FreecamHack;
import net.wurstclient.hacks.FullbrightHack;
import net.wurstclient.hacks.GlideHack;
import net.wurstclient.hacks.HandNoClipHack;
import net.wurstclient.hacks.HeadRollHack;
import net.wurstclient.hacks.HealthTagsHack;
import net.wurstclient.hacks.HighJumpHack;
import net.wurstclient.hacks.InfiniChatHack;
import net.wurstclient.hacks.InstaBuildHack;
import net.wurstclient.hacks.InstantBunkerHack;
import net.wurstclient.hacks.InvWalkHack;
import net.wurstclient.hacks.ItemEspHack;
import net.wurstclient.hacks.ItemGeneratorHack;
import net.wurstclient.hacks.JesusHack;
import net.wurstclient.hacks.JetpackHack;
import net.wurstclient.hacks.KaboomHack;
import net.wurstclient.hacks.KillPotionHack;
import net.wurstclient.hacks.KillauraHack;
import net.wurstclient.hacks.KillauraLegitHack;
import net.wurstclient.hacks.LiquidsHack;
import net.wurstclient.hacks.LsdHack;
import net.wurstclient.hacks.MaceDmgHack;
import net.wurstclient.hacks.MassTpaHack;
import net.wurstclient.hacks.MileyCyrusHack;
import net.wurstclient.hacks.MobEspHack;
import net.wurstclient.hacks.MobSpawnEspHack;
import net.wurstclient.hacks.MultiAuraHack;
import net.wurstclient.hacks.NameProtectHack;
import net.wurstclient.hacks.NameTagsHack;
import net.wurstclient.hacks.NavigatorHack;
import net.wurstclient.hacks.NewChunksHack;
import net.wurstclient.hacks.NoBackgroundHack;
import net.wurstclient.hacks.NoClipHack;
import net.wurstclient.hacks.NoFallHack;
import net.wurstclient.hacks.NoFireOverlayHack;
import net.wurstclient.hacks.NoFogHack;
import net.wurstclient.hacks.NoHurtcamHack;
import net.wurstclient.hacks.NoLevitationHack;
import net.wurstclient.hacks.NoOverlayHack;
import net.wurstclient.hacks.NoPumpkinHack;
import net.wurstclient.hacks.NoShieldOverlayHack;
import net.wurstclient.hacks.NoSlowdownHack;
import net.wurstclient.hacks.NoVignetteHack;
import net.wurstclient.hacks.NoWeatherHack;
import net.wurstclient.hacks.NoWebHack;
import net.wurstclient.hacks.NukerHack;
import net.wurstclient.hacks.NukerLegitHack;
import net.wurstclient.hacks.OpenWaterEspHack;
import net.wurstclient.hacks.OverlayHack;
import net.wurstclient.hacks.PanicHack;
import net.wurstclient.hacks.ParkourHack;
import net.wurstclient.hacks.PlayerEspHack;
import net.wurstclient.hacks.PortalEspHack;
import net.wurstclient.hacks.PortalGuiHack;
import net.wurstclient.hacks.PotionSaverHack;
import net.wurstclient.hacks.ProphuntEspHack;
import net.wurstclient.hacks.ProtectHack;
import net.wurstclient.hacks.RadarHack;
import net.wurstclient.hacks.RainbowUiHack;
import net.wurstclient.hacks.ReachHack;
import net.wurstclient.hacks.RemoteViewHack;
import net.wurstclient.hacks.RestockHack;
import net.wurstclient.hacks.SafeWalkHack;
import net.wurstclient.hacks.ScaffoldWalkHack;
import net.wurstclient.hacks.SearchHack;
import net.wurstclient.hacks.SkinDerpHack;
import net.wurstclient.hacks.SneakHack;
import net.wurstclient.hacks.SnowShoeHack;
import net.wurstclient.hacks.SpeedHackHack;
import net.wurstclient.hacks.SpeedNukerHack;
import net.wurstclient.hacks.SpiderHack;
import net.wurstclient.hacks.StepHack;
import net.wurstclient.hacks.TemplateToolHack;
import net.wurstclient.hacks.ThrowHack;
import net.wurstclient.hacks.TillauraHack;
import net.wurstclient.hacks.TimerHack;
import net.wurstclient.hacks.TiredHack;
import net.wurstclient.hacks.TooManyHaxHack;
import net.wurstclient.hacks.TpAuraHack;
import net.wurstclient.hacks.TrajectoriesHack;
import net.wurstclient.hacks.TreeBotHack;
import net.wurstclient.hacks.TriggerBotHack;
import net.wurstclient.hacks.TrollPotionHack;
import net.wurstclient.hacks.TrueSightHack;
import net.wurstclient.hacks.TunnellerHack;
import net.wurstclient.hacks.VeinMinerHack;
import net.wurstclient.hacks.XRayHack;
import net.wurstclient.util.json.JsonException;

public final class HackList
implements UpdateListener {
    public final AimAssistHack aimAssistHack = new AimAssistHack();
    public final AirPlaceHack airPlaceHack = new AirPlaceHack();
    public final AnchorAuraHack anchorAuraHack = new AnchorAuraHack();
    public final AntiAfkHack antiAfkHack = new AntiAfkHack();
    public final AntiBlindHack antiBlindHack = new AntiBlindHack();
    public final AntiCactusHack antiCactusHack = new AntiCactusHack();
    public final AntiEntityPushHack antiEntityPushHack = new AntiEntityPushHack();
    public final AntiHungerHack antiHungerHack = new AntiHungerHack();
    public final AntiKnockbackHack antiKnockbackHack = new AntiKnockbackHack();
    public final AntiSpamHack antiSpamHack = new AntiSpamHack();
    public final AntiWaterPushHack antiWaterPushHack = new AntiWaterPushHack();
    public final AntiWobbleHack antiWobbleHack = new AntiWobbleHack();
    public final ArrowDmgHack arrowDmgHack = new ArrowDmgHack();
    public final AutoArmorHack autoArmorHack = new AutoArmorHack();
    public final AutoBuildHack autoBuildHack = new AutoBuildHack();
    public final AutoCompleteHack autoCompleteHack = new AutoCompleteHack();
    public final AutoDropHack autoDropHack = new AutoDropHack();
    public final AutoLeaveHack autoLeaveHack = new AutoLeaveHack();
    public final AutoLibrarianHack autoLibrarianHack = new AutoLibrarianHack();
    public final AutoEatHack autoEatHack = new AutoEatHack();
    public final AutoFarmHack autoFarmHack = new AutoFarmHack();
    public final AutoFishHack autoFishHack = new AutoFishHack();
    public final AutoMineHack autoMineHack = new AutoMineHack();
    public final AutoPotionHack autoPotionHack = new AutoPotionHack();
    public final AutoReconnectHack autoReconnectHack = new AutoReconnectHack();
    public final AutoRespawnHack autoRespawnHack = new AutoRespawnHack();
    public final AutoSignHack autoSignHack = new AutoSignHack();
    public final AutoSoupHack autoSoupHack = new AutoSoupHack();
    public final AutoSprintHack autoSprintHack = new AutoSprintHack();
    public final AutoStealHack autoStealHack = new AutoStealHack();
    public final AutoSwimHack autoSwimHack = new AutoSwimHack();
    public final AutoSwitchHack autoSwitchHack = new AutoSwitchHack();
    public final AutoSwordHack autoSwordHack = new AutoSwordHack();
    public final AutoToolHack autoToolHack = new AutoToolHack();
    public final AutoTotemHack autoTotemHack = new AutoTotemHack();
    public final AutoWalkHack autoWalkHack = new AutoWalkHack();
    public final BarrierEspHack barrierEspHack = new BarrierEspHack();
    public final BaseFinderHack baseFinderHack = new BaseFinderHack();
    public final BlinkHack blinkHack = new BlinkHack();
    public final BoatFlyHack boatFlyHack = new BoatFlyHack();
    public final BonemealAuraHack bonemealAuraHack = new BonemealAuraHack();
    public final BowAimbotHack bowAimbotHack = new BowAimbotHack();
    public final BuildRandomHack buildRandomHack = new BuildRandomHack();
    public final BunnyHopHack bunnyHopHack = new BunnyHopHack();
    public final CameraDistanceHack cameraDistanceHack = new CameraDistanceHack();
    public final CameraNoClipHack cameraNoClipHack = new CameraNoClipHack();
    public final CaveFinderHack caveFinderHack = new CaveFinderHack();
    public final ChatTranslatorHack chatTranslatorHack = new ChatTranslatorHack();
    public final ChestEspHack chestEspHack = new ChestEspHack();
    public final ClickAuraHack clickAuraHack = new ClickAuraHack();
    public final ClickGuiHack clickGuiHack = new ClickGuiHack();
    public final CrashChestHack crashChestHack = new CrashChestHack();
    public final CreativeFlightHack creativeFlightHack = new CreativeFlightHack();
    public final CriticalsHack criticalsHack = new CriticalsHack();
    public final CrystalAuraHack crystalAuraHack = new CrystalAuraHack();
    public final DerpHack derpHack = new DerpHack();
    public final DolphinHack dolphinHack = new DolphinHack();
    public final ExcavatorHack excavatorHack = new ExcavatorHack();
    public final ExtraElytraHack extraElytraHack = new ExtraElytraHack();
    public final FancyChatHack fancyChatHack = new FancyChatHack();
    public final FastBreakHack fastBreakHack = new FastBreakHack();
    public final FastLadderHack fastLadderHack = new FastLadderHack();
    public final FastPlaceHack fastPlaceHack = new FastPlaceHack();
    public final FeedAuraHack feedAuraHack = new FeedAuraHack();
    public final FightBotHack fightBotHack = new FightBotHack();
    public final FishHack fishHack = new FishHack();
    public final FlightHack flightHack = new FlightHack();
    public final FollowHack followHack = new FollowHack();
    public final ForceOpHack forceOpHack = new ForceOpHack();
    public final FreecamHack freecamHack = new FreecamHack();
    public final FullbrightHack fullbrightHack = new FullbrightHack();
    public final GlideHack glideHack = new GlideHack();
    public final HandNoClipHack handNoClipHack = new HandNoClipHack();
    public final HeadRollHack headRollHack = new HeadRollHack();
    public final HealthTagsHack healthTagsHack = new HealthTagsHack();
    public final HighJumpHack highJumpHack = new HighJumpHack();
    public final InfiniChatHack infiniChatHack = new InfiniChatHack();
    public final InstaBuildHack instaBuildHack = new InstaBuildHack();
    public final InstantBunkerHack instantBunkerHack = new InstantBunkerHack();
    public final InvWalkHack invWalkHack = new InvWalkHack();
    public final ItemEspHack itemEspHack = new ItemEspHack();
    public final ItemGeneratorHack itemGeneratorHack = new ItemGeneratorHack();
    public final JesusHack jesusHack = new JesusHack();
    public final JetpackHack jetpackHack = new JetpackHack();
    public final KaboomHack kaboomHack = new KaboomHack();
    public final KillauraLegitHack killauraLegitHack = new KillauraLegitHack();
    public final KillauraHack killauraHack = new KillauraHack();
    public final KillPotionHack killPotionHack = new KillPotionHack();
    public final LiquidsHack liquidsHack = new LiquidsHack();
    public final LsdHack lsdHack = new LsdHack();
    public final MaceDmgHack maceDmgHack = new MaceDmgHack();
    public final MassTpaHack massTpaHack = new MassTpaHack();
    public final MileyCyrusHack mileyCyrusHack = new MileyCyrusHack();
    public final MobEspHack mobEspHack = new MobEspHack();
    public final MobSpawnEspHack mobSpawnEspHack = new MobSpawnEspHack();
    public final MultiAuraHack multiAuraHack = new MultiAuraHack();
    public final NameProtectHack nameProtectHack = new NameProtectHack();
    public final NameTagsHack nameTagsHack = new NameTagsHack();
    public final NavigatorHack navigatorHack = new NavigatorHack();
    public final NewChunksHack newChunksHack = new NewChunksHack();
    public final NoBackgroundHack noBackgroundHack = new NoBackgroundHack();
    public final NoClipHack noClipHack = new NoClipHack();
    public final NoFallHack noFallHack = new NoFallHack();
    public final NoFireOverlayHack noFireOverlayHack = new NoFireOverlayHack();
    public final NoFogHack noFogHack = new NoFogHack();
    public final NoHurtcamHack noHurtcamHack = new NoHurtcamHack();
    public final NoLevitationHack noLevitationHack = new NoLevitationHack();
    public final NoOverlayHack noOverlayHack = new NoOverlayHack();
    public final NoPumpkinHack noPumpkinHack = new NoPumpkinHack();
    public final NoShieldOverlayHack noShieldOverlayHack = new NoShieldOverlayHack();
    public final NoSlowdownHack noSlowdownHack = new NoSlowdownHack();
    public final NoVignetteHack noVignetteHack = new NoVignetteHack();
    public final NoWeatherHack noWeatherHack = new NoWeatherHack();
    public final NoWebHack noWebHack = new NoWebHack();
    public final NukerHack nukerHack = new NukerHack();
    public final NukerLegitHack nukerLegitHack = new NukerLegitHack();
    public final OpenWaterEspHack openWaterEspHack = new OpenWaterEspHack();
    public final OverlayHack overlayHack = new OverlayHack();
    public final PanicHack panicHack = new PanicHack();
    public final ParkourHack parkourHack = new ParkourHack();
    public final PlayerEspHack playerEspHack = new PlayerEspHack();
    public final PortalEspHack portalEspHack = new PortalEspHack();
    public final PortalGuiHack portalGuiHack = new PortalGuiHack();
    public final PotionSaverHack potionSaverHack = new PotionSaverHack();
    public final ProphuntEspHack prophuntEspHack = new ProphuntEspHack();
    public final ProtectHack protectHack = new ProtectHack();
    public final RadarHack radarHack = new RadarHack();
    public final RainbowUiHack rainbowUiHack = new RainbowUiHack();
    public final ReachHack reachHack = new ReachHack();
    public final RemoteViewHack remoteViewHack = new RemoteViewHack();
    public final RestockHack restockHack = new RestockHack();
    public final SafeWalkHack safeWalkHack = new SafeWalkHack();
    public final ScaffoldWalkHack scaffoldWalkHack = new ScaffoldWalkHack();
    public final SearchHack searchHack = new SearchHack();
    public final SkinDerpHack skinDerpHack = new SkinDerpHack();
    public final SneakHack sneakHack = new SneakHack();
    public final SnowShoeHack snowShoeHack = new SnowShoeHack();
    public final SpeedHackHack speedHackHack = new SpeedHackHack();
    public final SpeedNukerHack speedNukerHack = new SpeedNukerHack();
    public final SpiderHack spiderHack = new SpiderHack();
    public final StepHack stepHack = new StepHack();
    public final TemplateToolHack templateToolHack = new TemplateToolHack();
    public final ThrowHack throwHack = new ThrowHack();
    public final TillauraHack tillauraHack = new TillauraHack();
    public final TimerHack timerHack = new TimerHack();
    public final TiredHack tiredHack = new TiredHack();
    public final TooManyHaxHack tooManyHaxHack = new TooManyHaxHack();
    public final TpAuraHack tpAuraHack = new TpAuraHack();
    public final TrajectoriesHack trajectoriesHack = new TrajectoriesHack();
    public final TreeBotHack treeBotHack = new TreeBotHack();
    public final TriggerBotHack triggerBotHack = new TriggerBotHack();
    public final TrollPotionHack trollPotionHack = new TrollPotionHack();
    public final TrueSightHack trueSightHack = new TrueSightHack();
    public final TunnellerHack tunnellerHack = new TunnellerHack();
    public final VeinMinerHack veinMinerHack = new VeinMinerHack();
    public final XRayHack xRayHack = new XRayHack();
    private final TreeMap<String, Hack> hax = new TreeMap(String::compareToIgnoreCase);
    private final EnabledHacksFile enabledHacksFile;
    private final Path profilesFolder = WurstClient.INSTANCE.getWurstFolder().resolve("enabled hacks");
    private final EventManager eventManager = WurstClient.INSTANCE.getEventManager();

    public HackList(Path enabledHacksFile) {
        this.enabledHacksFile = new EnabledHacksFile(enabledHacksFile);
        try {
            for (Field field : HackList.class.getDeclaredFields()) {
                if (!field.getName().endsWith("Hack")) continue;
                Hack hack = (Hack)field.get(this);
                this.hax.put(hack.getName(), hack);
            }
        }
        catch (Exception e) {
            String message = "Initializing Wurst hacks";
            class_128 report = class_128.method_560((Throwable)e, (String)message);
            throw new class_148(report);
        }
        this.eventManager.add(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        this.enabledHacksFile.load(this);
        this.eventManager.remove(UpdateListener.class, this);
    }

    public void saveEnabledHax() {
        this.enabledHacksFile.save(this);
    }

    public Hack getHackByName(String name) {
        return this.hax.get(name);
    }

    public Collection<Hack> getAllHax() {
        return Collections.unmodifiableCollection(this.hax.values());
    }

    public int countHax() {
        return this.hax.size();
    }

    public ArrayList<Path> listProfiles() {
        ArrayList arrayList;
        block9: {
            if (!Files.isDirectory(this.profilesFolder, new LinkOption[0])) {
                return new ArrayList<Path>();
            }
            Stream<Path> files = Files.list(this.profilesFolder);
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

    public void loadProfile(String fileName) throws IOException, JsonException {
        this.enabledHacksFile.loadProfile(this, this.profilesFolder.resolve(fileName));
    }

    public void saveProfile(String fileName) throws IOException, JsonException {
        this.enabledHacksFile.saveProfile(this, this.profilesFolder.resolve(fileName));
    }
}
