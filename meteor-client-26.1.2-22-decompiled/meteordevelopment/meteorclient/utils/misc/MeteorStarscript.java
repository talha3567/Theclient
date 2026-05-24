package meteordevelopment.meteorclient.utils.misc;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.process.IBaritoneProcess;
import java.lang.runtime.SwitchBootstraps;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.MinecraftAccessor;
import meteordevelopment.meteorclient.mixin.MultiPlayerGameModeAccessor;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.CPSUtils;
import meteordevelopment.meteorclient.utils.misc.HorizontalDirection;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.meteorclient.utils.world.TickRate;
import net.minecraft.IdentifierException;
import net.minecraft.SharedConstants;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.meteordev.starscript.Script;
import org.meteordev.starscript.Section;
import org.meteordev.starscript.StandardLib;
import org.meteordev.starscript.Starscript;
import org.meteordev.starscript.compiler.Compiler;
import org.meteordev.starscript.compiler.Parser;
import org.meteordev.starscript.utils.Error;
import org.meteordev.starscript.utils.StarscriptError;
import org.meteordev.starscript.value.Value;
import org.meteordev.starscript.value.ValueMap;

public class MeteorStarscript {
    public static Starscript ss = new Starscript();
    private static final BlockPos.MutableBlockPos BP = new BlockPos.MutableBlockPos();
    private static final StringBuilder SB = new StringBuilder();
    private static long lastRequestedStatsTime = 0L;

    @PreInit(dependencies={PathManagers.class})
    public static void init() {
        StandardLib.init(ss);
        ss.set("mc_version", SharedConstants.getCurrentVersion().name());
        ss.set("fps", () -> Value.number(MinecraftAccessor.meteor$getFps()));
        ss.set("ping", MeteorStarscript::ping);
        ss.set("time", () -> Value.string(LocalTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))));
        ss.set("cps", () -> Value.number(CPSUtils.getCpsAverage()));
        ss.set("meteor", new ValueMap().set("name", MeteorClient.NAME).set("version", (String)(MeteorClient.VERSION != null ? (MeteorClient.BUILD_NUMBER.isEmpty() ? MeteorClient.VERSION.toString() : String.valueOf(MeteorClient.VERSION) + " " + MeteorClient.BUILD_NUMBER) : "")).set("modules", () -> Value.number(Modules.get().getAll().size())).set("active_modules", () -> Value.number(Modules.get().getActive().size())).set("is_module_active", MeteorStarscript::isModuleActive).set("get_module_info", MeteorStarscript::getModuleInfo).set("get_module_setting", MeteorStarscript::getModuleSetting).set("prefix", MeteorStarscript::getMeteorPrefix));
        if (BaritoneUtils.IS_AVAILABLE) {
            ss.set("baritone", new ValueMap().set("is_pathing", () -> Value.bool(BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing())).set("distance_to_goal", MeteorStarscript::baritoneDistanceToGoal).set("process", MeteorStarscript::baritoneProcess).set("process_name", MeteorStarscript::baritoneProcessName).set("eta", MeteorStarscript::baritoneETA));
        }
        ss.set("camera", new ValueMap().set("pos", new ValueMap().set("_toString", () -> MeteorStarscript.posString(false, true)).set("x", () -> Value.number(MeteorClient.mc.gameRenderer.getMainCamera().position().x)).set("y", () -> Value.number(MeteorClient.mc.gameRenderer.getMainCamera().position().y)).set("z", () -> Value.number(MeteorClient.mc.gameRenderer.getMainCamera().position().z))).set("opposite_dim_pos", new ValueMap().set("_toString", () -> MeteorStarscript.posString(true, true)).set("x", () -> MeteorStarscript.oppositeX(true)).set("y", () -> Value.number(MeteorClient.mc.gameRenderer.getMainCamera().position().y)).set("z", () -> MeteorStarscript.oppositeZ(true))).set("yaw", () -> MeteorStarscript.yaw(true)).set("pitch", () -> MeteorStarscript.pitch(true)).set("direction", () -> MeteorStarscript.direction(true)));
        ss.set("player", new ValueMap().set("_toString", () -> Value.string(MeteorClient.mc.getUser().getName())).set("health", () -> Value.number(MeteorClient.mc.player != null ? (double)MeteorClient.mc.player.getHealth() : 0.0)).set("absorption", () -> Value.number(MeteorClient.mc.player != null ? (double)MeteorClient.mc.player.getAbsorptionAmount() : 0.0)).set("hunger", () -> Value.number(MeteorClient.mc.player != null ? (double)MeteorClient.mc.player.getFoodData().getFoodLevel() : 0.0)).set("saturation", () -> Value.number(MeteorClient.mc.player != null ? (double)MeteorClient.mc.player.getFoodData().getSaturationLevel() : 0.0)).set("speed", () -> Value.number(Utils.getPlayerSpeed().horizontalDistance())).set("speed_all", new ValueMap().set("_toString", () -> Value.string(MeteorClient.mc.player != null ? Utils.getPlayerSpeed().toString() : "")).set("x", () -> Value.number(MeteorClient.mc.player != null ? Utils.getPlayerSpeed().x : 0.0)).set("y", () -> Value.number(MeteorClient.mc.player != null ? Utils.getPlayerSpeed().y : 0.0)).set("z", () -> Value.number(MeteorClient.mc.player != null ? Utils.getPlayerSpeed().z : 0.0))).set("breaking_progress", () -> Value.number(MeteorClient.mc.gameMode != null ? (double)((MultiPlayerGameModeAccessor)MeteorClient.mc.gameMode).meteor$getBreakingProgress() : 0.0)).set("biome", MeteorStarscript::biome).set("dimension", () -> Value.string(PlayerUtils.getDimension().name())).set("opposite_dimension", () -> Value.string(PlayerUtils.getDimension().opposite().name())).set("gamemode", () -> PlayerUtils.getGameMode() != null ? Value.string(StringUtils.capitalize((String)PlayerUtils.getGameMode().getName())) : Value.null_()).set("pos", new ValueMap().set("_toString", () -> MeteorStarscript.posString(false, false)).set("x", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getX() : 0.0)).set("y", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getY() : 0.0)).set("z", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getZ() : 0.0))).set("opposite_dim_pos", new ValueMap().set("_toString", () -> MeteorStarscript.posString(true, false)).set("x", () -> MeteorStarscript.oppositeX(false)).set("y", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getY() : 0.0)).set("z", () -> MeteorStarscript.oppositeZ(false))).set("yaw", () -> MeteorStarscript.yaw(false)).set("pitch", () -> MeteorStarscript.pitch(false)).set("direction", () -> MeteorStarscript.direction(false)).set("hand", () -> MeteorClient.mc.player != null ? MeteorStarscript.wrap(MeteorClient.mc.player.getMainHandItem()) : Value.null_()).set("offhand", () -> MeteorClient.mc.player != null ? MeteorStarscript.wrap(MeteorClient.mc.player.getOffhandItem()) : Value.null_()).set("hand_or_offhand", MeteorStarscript::handOrOffhand).set("get_item", MeteorStarscript::getItem).set("count_items", MeteorStarscript::countItems).set("xp", new ValueMap().set("level", () -> Value.number(MeteorClient.mc.player != null ? (double)MeteorClient.mc.player.experienceLevel : 0.0)).set("progress", () -> Value.number(MeteorClient.mc.player != null ? (double)MeteorClient.mc.player.experienceProgress : 0.0)).set("total", () -> Value.number(MeteorClient.mc.player != null ? (double)MeteorClient.mc.player.totalExperience : 0.0))).set("has_potion_effect", MeteorStarscript::hasPotionEffect).set("get_potion_effect", MeteorStarscript::getPotionEffect).set("get_stat", MeteorStarscript::getStat));
        ss.set("crosshair_target", new ValueMap().set("type", MeteorStarscript::crosshairType).set("value", MeteorStarscript::crosshairValue));
        ss.set("server", new ValueMap().set("_toString", () -> Value.string(Utils.getWorldName())).set("tps", () -> Value.number(TickRate.INSTANCE.getTickRate())).set("time", () -> Value.string(Utils.getWorldTime())).set("weather", MeteorStarscript::weather).set("player_count", () -> Value.number(MeteorClient.mc.getConnection() != null ? (double)MeteorClient.mc.getConnection().getOnlinePlayers().size() : 0.0)).set("difficulty", () -> Value.string(MeteorClient.mc.level != null ? MeteorClient.mc.level.getDifficulty().name() : "")));
    }

    public static Script compile(String source) {
        Parser.Result result = Parser.parse(source);
        if (result.hasErrors()) {
            for (Error error : result.errors) {
                MeteorStarscript.printChatError(error);
            }
            return null;
        }
        return Compiler.compile(result);
    }

    public static Section runSection(Script script, StringBuilder sb) {
        try {
            return ss.run(script, sb);
        }
        catch (StarscriptError error) {
            MeteorStarscript.printChatError(error);
            return null;
        }
    }

    public static String run(Script script, StringBuilder sb) {
        Section section = MeteorStarscript.runSection(script, sb);
        return section != null ? section.toString() : null;
    }

    public static Section runSection(Script script) {
        return MeteorStarscript.runSection(script, new StringBuilder());
    }

    public static String run(Script script) {
        return MeteorStarscript.run(script, new StringBuilder());
    }

    public static void printChatError(int i, Error error) {
        String caller = MeteorStarscript.getCallerName();
        if (caller != null) {
            if (i != -1) {
                ChatUtils.errorPrefix("Starscript", "%d, %d '%c': %s (from %s)", i, error.character, Character.valueOf(error.ch), error.message, caller);
            } else {
                ChatUtils.errorPrefix("Starscript", "%d '%c': %s (from %s)", error.character, Character.valueOf(error.ch), error.message, caller);
            }
        } else if (i != -1) {
            ChatUtils.errorPrefix("Starscript", "%d, %d '%c': %s", i, error.character, Character.valueOf(error.ch), error.message);
        } else {
            ChatUtils.errorPrefix("Starscript", "%d '%c': %s", error.character, Character.valueOf(error.ch), error.message);
        }
    }

    public static void printChatError(Error error) {
        MeteorStarscript.printChatError(-1, error);
    }

    public static void printChatError(StarscriptError e) {
        String caller = MeteorStarscript.getCallerName();
        if (caller != null) {
            ChatUtils.errorPrefix("Starscript", "%s (from %s)", e.getMessage(), caller);
        } else {
            ChatUtils.errorPrefix("Starscript", "%s", e.getMessage());
        }
    }

    private static String getCallerName() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements.length == 0) {
            return null;
        }
        for (int i = 1; i < elements.length; ++i) {
            String name = elements[i].getClassName();
            if (name.startsWith(Starscript.class.getPackageName()) || name.equals(MeteorStarscript.class.getName())) continue;
            return name.substring(name.lastIndexOf(46) + 1);
        }
        return null;
    }

    private static Value hasPotionEffect(Starscript ss, int argCount) {
        if (argCount < 1) {
            ss.error("player.has_potion_effect() requires 1 argument, got %d.", argCount);
        }
        if (MeteorClient.mc.player == null) {
            return Value.bool(false);
        }
        Identifier name = MeteorStarscript.popIdentifier(ss, "First argument to player.has_potion_effect() needs to a string.");
        Optional effect = BuiltInRegistries.MOB_EFFECT.get(name);
        if (effect.isEmpty()) {
            return Value.bool(false);
        }
        MobEffectInstance effectInstance = MeteorClient.mc.player.getEffect((Holder)effect.get());
        return Value.bool(effectInstance != null);
    }

    private static Value getPotionEffect(Starscript ss, int argCount) {
        if (argCount < 1) {
            ss.error("player.get_potion_effect() requires 1 argument, got %d.", argCount);
        }
        if (MeteorClient.mc.player == null) {
            return Value.null_();
        }
        Identifier name = MeteorStarscript.popIdentifier(ss, "First argument to player.get_potion_effect() needs to a string.");
        Optional effect = BuiltInRegistries.MOB_EFFECT.get(name);
        if (effect.isEmpty()) {
            return Value.null_();
        }
        MobEffectInstance effectInstance = MeteorClient.mc.player.getEffect((Holder)effect.get());
        if (effectInstance == null) {
            return Value.null_();
        }
        return MeteorStarscript.wrap(effectInstance);
    }

    private static Value getStat(Starscript ss, int argCount) {
        if (argCount < 1) {
            ss.error("player.get_stat() requires 1 argument, got %d.", argCount);
        }
        if (MeteorClient.mc.player == null) {
            return Value.number(0.0);
        }
        long time = System.currentTimeMillis();
        if ((double)(time - lastRequestedStatsTime) / 1000.0 >= 1.0 && MeteorClient.mc.getConnection() != null) {
            MeteorClient.mc.getConnection().send((Packet)new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
            lastRequestedStatsTime = time;
        }
        String type = argCount > 1 ? ss.popString("First argument to player.get_stat() needs to be a string.") : "custom";
        Identifier name = MeteorStarscript.popIdentifier(ss, (argCount > 1 ? "Second" : "First") + " argument to player.get_stat() needs to be a string.");
        Stat stat = switch (type) {
            case "mined" -> Stats.BLOCK_MINED.get((Object)((Block)BuiltInRegistries.BLOCK.getValue(name)));
            case "crafted" -> Stats.ITEM_CRAFTED.get((Object)((Item)BuiltInRegistries.ITEM.getValue(name)));
            case "used" -> Stats.ITEM_USED.get((Object)((Item)BuiltInRegistries.ITEM.getValue(name)));
            case "broken" -> Stats.ITEM_BROKEN.get((Object)((Item)BuiltInRegistries.ITEM.getValue(name)));
            case "picked_up" -> Stats.ITEM_PICKED_UP.get((Object)((Item)BuiltInRegistries.ITEM.getValue(name)));
            case "dropped" -> Stats.ITEM_DROPPED.get((Object)((Item)BuiltInRegistries.ITEM.getValue(name)));
            case "killed" -> Stats.ENTITY_KILLED.get((Object)((EntityType)BuiltInRegistries.ENTITY_TYPE.getValue(name)));
            case "killed_by" -> Stats.ENTITY_KILLED_BY.get((Object)((EntityType)BuiltInRegistries.ENTITY_TYPE.getValue(name)));
            case "custom" -> {
                name = (Identifier)BuiltInRegistries.CUSTOM_STAT.getValue(name);
                if (name != null) {
                    yield Stats.CUSTOM.get((Object)name);
                }
                yield null;
            }
            default -> null;
        };
        return Value.number(stat != null ? (double)MeteorClient.mc.player.getStats().getValue(stat) : 0.0);
    }

    private static Value getModuleInfo(Starscript ss, int argCount) {
        Module module;
        if (argCount != 1) {
            ss.error("meteor.get_module_info() requires 1 argument, got %d.", argCount);
        }
        if ((module = Modules.get().get(ss.popString("First argument to meteor.get_module_info() needs to be a string."))) != null && module.isActive()) {
            String info = module.getInfoString();
            return Value.string(info == null ? "" : info);
        }
        return Value.string("");
    }

    private static Value getModuleSetting(Starscript ss, int argCount) {
        Object value;
        Setting<?> setting;
        if (argCount != 2) {
            ss.error("meteor.get_module_setting() requires 2 arguments, got %d.", argCount);
        }
        String settingName = ss.popString("Second argument to meteor.get_module_setting() needs to be a string.");
        String moduleName = ss.popString("First argument to meteor.get_module_setting() needs to be a string.");
        Module module = Modules.get().get(moduleName);
        if (module == null) {
            ss.error("Unable to get module %s for meteor.get_module_setting()", moduleName);
        }
        if ((setting = module.settings.get(settingName)) == null) {
            ss.error("Unable to get setting %s for module %s for meteor.get_module_setting()", settingName, moduleName);
        }
        Object obj = value = setting.get();
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Double.class, Integer.class, Boolean.class, List.class}, obj, n)) {
            case 0 -> {
                Double d = (Double)obj;
                yield Value.number(d);
            }
            case 1 -> {
                Integer i = (Integer)obj;
                yield Value.number(i.intValue());
            }
            case 2 -> {
                Boolean b = (Boolean)obj;
                yield Value.bool(b);
            }
            case 3 -> {
                List list = (List)obj;
                yield Value.number(list.size());
            }
            default -> Value.string(value.toString());
        };
    }

    private static Value isModuleActive(Starscript ss, int argCount) {
        Module module;
        if (argCount != 1) {
            ss.error("meteor.is_module_active() requires 1 argument, got %d.", argCount);
        }
        return Value.bool((module = Modules.get().get(ss.popString("First argument to meteor.is_module_active() needs to be a string."))) != null && module.isActive());
    }

    private static Value getItem(Starscript ss, int argCount) {
        int i;
        if (argCount != 1) {
            ss.error("player.get_item() requires 1 argument, got %d.", argCount);
        }
        if ((i = (int)ss.popNumber("First argument to player.get_item() needs to be a number.")) < 0) {
            ss.error("First argument to player.get_item() needs to be a non-negative integer.", i);
        }
        return MeteorClient.mc.player != null ? MeteorStarscript.wrap(MeteorClient.mc.player.getInventory().getItem(i)) : Value.null_();
    }

    private static Value countItems(Starscript ss, int argCount) {
        String idRaw;
        Identifier id;
        if (argCount != 1) {
            ss.error("player.count_items() requires 1 argument, got %d.", argCount);
        }
        if ((id = Identifier.tryParse((String)(idRaw = ss.popString("First argument to player.count_items() needs to be a string.")))) == null) {
            return Value.number(0.0);
        }
        Item item = (Item)BuiltInRegistries.ITEM.getValue(id);
        if (item == Items.AIR || MeteorClient.mc.player == null) {
            return Value.number(0.0);
        }
        int count = 0;
        for (int i = 0; i < MeteorClient.mc.player.getInventory().getContainerSize(); ++i) {
            ItemStack itemStack = MeteorClient.mc.player.getInventory().getItem(i);
            if (itemStack.getItem() != item) continue;
            count += itemStack.getCount();
        }
        return Value.number(count);
    }

    private static Value getMeteorPrefix() {
        if (Config.get() == null) {
            return Value.null_();
        }
        return Value.string(Config.get().prefix.get());
    }

    private static Value baritoneProcess() {
        Optional process = BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager().mostRecentInControl();
        return Value.string(process.isEmpty() ? "" : ((IBaritoneProcess)process.get()).displayName0());
    }

    private static Value baritoneProcessName() {
        Optional process = BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager().mostRecentInControl();
        if (process.isEmpty()) {
            return Value.string("");
        }
        String className = ((IBaritoneProcess)process.get()).getClass().getSimpleName();
        if (className.endsWith("Process")) {
            className = className.substring(0, className.length() - 7);
        }
        SB.append(className);
        int i = 0;
        for (int j = 0; j < className.length(); ++j) {
            if (j > 0 && Character.isUpperCase(className.charAt(j))) {
                SB.insert(i, ' ');
                ++i;
            }
            ++i;
        }
        String name = SB.toString();
        SB.setLength(0);
        return Value.string(name);
    }

    private static Value baritoneETA() {
        if (MeteorClient.mc.player == null) {
            return Value.number(0.0);
        }
        Optional ticksTillGoal = BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().estimatedTicksToGoal();
        return ticksTillGoal.map(aDouble -> Value.number(aDouble / 20.0)).orElseGet(() -> Value.number(0.0));
    }

    private static Value oppositeX(boolean camera) {
        double x = camera ? MeteorClient.mc.gameRenderer.getMainCamera().position().x : (MeteorClient.mc.player != null ? MeteorClient.mc.player.getX() : 0.0);
        Dimension dimension = PlayerUtils.getDimension();
        if (dimension == Dimension.Overworld) {
            x /= 8.0;
        } else if (dimension == Dimension.Nether) {
            x *= 8.0;
        }
        return Value.number(x);
    }

    private static Value oppositeZ(boolean camera) {
        double z = camera ? MeteorClient.mc.gameRenderer.getMainCamera().position().z : (MeteorClient.mc.player != null ? MeteorClient.mc.player.getZ() : 0.0);
        Dimension dimension = PlayerUtils.getDimension();
        if (dimension == Dimension.Overworld) {
            z /= 8.0;
        } else if (dimension == Dimension.Nether) {
            z *= 8.0;
        }
        return Value.number(z);
    }

    private static Value yaw(boolean camera) {
        float yaw = camera ? MeteorClient.mc.gameRenderer.getMainCamera().yRot() : (MeteorClient.mc.player != null ? MeteorClient.mc.player.getYRot() : 0.0f);
        if ((yaw %= 360.0f) < 0.0f) {
            yaw += 360.0f;
        }
        if (yaw > 180.0f) {
            yaw -= 360.0f;
        }
        return Value.number(yaw);
    }

    private static Value pitch(boolean camera) {
        float pitch = camera ? MeteorClient.mc.gameRenderer.getMainCamera().xRot() : (MeteorClient.mc.player != null ? MeteorClient.mc.player.getXRot() : 0.0f);
        if ((pitch %= 360.0f) < 0.0f) {
            pitch += 360.0f;
        }
        if (pitch > 180.0f) {
            pitch -= 360.0f;
        }
        return Value.number(pitch);
    }

    private static Value direction(boolean camera) {
        float yaw = camera ? MeteorClient.mc.gameRenderer.getMainCamera().yRot() : (MeteorClient.mc.player != null ? MeteorClient.mc.player.getYRot() : 0.0f);
        return MeteorStarscript.wrap(HorizontalDirection.get(yaw));
    }

    private static Value biome() {
        if (MeteorClient.mc.player == null || MeteorClient.mc.level == null) {
            return Value.string("");
        }
        BP.set(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY(), MeteorClient.mc.player.getZ());
        return MeteorClient.mc.level.registryAccess().lookup(Registries.BIOME).map(biomeRegistry -> {
            Identifier id = biomeRegistry.getKey((Object)((Biome)MeteorClient.mc.level.getBiome((BlockPos)BP).value()));
            if (id == null) {
                return Value.string("Unknown");
            }
            return Value.string(Arrays.stream(id.getPath().split("_")).map(StringUtils::capitalize).collect(Collectors.joining(" ")));
        }).orElse(Value.string("Unknown"));
    }

    private static Value weather() {
        if (MeteorClient.mc.level == null) {
            return Value.string("");
        }
        return Value.string(MeteorClient.mc.level.isThundering() ? "Thunder" : (MeteorClient.mc.level.isRaining() ? "Rain" : "Clear"));
    }

    private static Value handOrOffhand() {
        if (MeteorClient.mc.player == null) {
            return Value.null_();
        }
        ItemStack itemStack = MeteorClient.mc.player.getMainHandItem();
        if (itemStack.isEmpty()) {
            itemStack = MeteorClient.mc.player.getOffhandItem();
        }
        return itemStack != null ? MeteorStarscript.wrap(itemStack) : Value.null_();
    }

    private static Value ping() {
        if (MeteorClient.mc.getConnection() == null || MeteorClient.mc.player == null) {
            return Value.number(0.0);
        }
        PlayerInfo playerListEntry = MeteorClient.mc.getConnection().getPlayerInfo(MeteorClient.mc.player.getUUID());
        return Value.number(playerListEntry != null ? (double)playerListEntry.getLatency() : 0.0);
    }

    private static Value baritoneDistanceToGoal() {
        Goal goal = BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().getGoal();
        return Value.number(goal != null && MeteorClient.mc.player != null ? goal.heuristic(MeteorClient.mc.player.blockPosition()) : 0.0);
    }

    private static Value posString(boolean opposite, boolean camera) {
        Vec3 pos = camera ? MeteorClient.mc.gameRenderer.getMainCamera().position() : (MeteorClient.mc.player != null ? MeteorClient.mc.player.position() : Vec3.ZERO);
        double x = pos.x;
        double z = pos.z;
        if (opposite) {
            Dimension dimension = PlayerUtils.getDimension();
            if (dimension == Dimension.Overworld) {
                x /= 8.0;
                z /= 8.0;
            } else if (dimension == Dimension.Nether) {
                x *= 8.0;
                z *= 8.0;
            }
        }
        return MeteorStarscript.posString(x, pos.y, z);
    }

    private static Value posString(double x, double y, double z) {
        return Value.string(String.format("X: %.0f Y: %.0f Z: %.0f", x, y, z));
    }

    private static Value crosshairType() {
        if (MeteorClient.mc.hitResult == null) {
            return Value.string("miss");
        }
        return Value.string(switch (MeteorClient.mc.hitResult.getType()) {
            default -> throw new MatchException(null, null);
            case HitResult.Type.MISS -> "miss";
            case HitResult.Type.BLOCK -> "block";
            case HitResult.Type.ENTITY -> "entity";
        });
    }

    private static Value crosshairValue() {
        if (MeteorClient.mc.level == null || MeteorClient.mc.hitResult == null) {
            return Value.null_();
        }
        if (MeteorClient.mc.hitResult.getType() == HitResult.Type.MISS) {
            return Value.string("");
        }
        HitResult hitResult = MeteorClient.mc.hitResult;
        if (hitResult instanceof BlockHitResult) {
            BlockHitResult hit = (BlockHitResult)hitResult;
            return MeteorStarscript.wrap(hit.getBlockPos(), MeteorClient.mc.level.getBlockState(hit.getBlockPos()));
        }
        return MeteorStarscript.wrap(((EntityHitResult)MeteorClient.mc.hitResult).getEntity());
    }

    public static Identifier popIdentifier(Starscript ss, String errorMessage) {
        try {
            return Identifier.parse((String)ss.popString(errorMessage));
        }
        catch (IdentifierException e) {
            ss.error(e.getMessage(), new Object[0]);
            return null;
        }
    }

    public static Value wrap(ItemStack itemStack) {
        String name = itemStack.isEmpty() ? "" : Names.get(itemStack.getItem());
        int durability = 0;
        if (!itemStack.isEmpty() && itemStack.isDamageableItem()) {
            durability = itemStack.getMaxDamage() - itemStack.getDamageValue();
        }
        return Value.map(new ValueMap().set("_toString", Value.string(itemStack.getCount() <= 1 ? name : String.format("%s %dx", name, itemStack.getCount()))).set("name", Value.string(name)).set("id", Value.string(BuiltInRegistries.ITEM.getKey((Object)itemStack.getItem()).toString())).set("count", Value.number(itemStack.getCount())).set("durability", Value.number(durability)).set("max_durability", Value.number(itemStack.getMaxDamage())));
    }

    public static Value wrap(BlockPos blockPos, BlockState blockState) {
        return Value.map(new ValueMap().set("_toString", Value.string(Names.get(blockState.getBlock()))).set("id", Value.string(BuiltInRegistries.BLOCK.getKey((Object)blockState.getBlock()).toString())).set("pos", Value.map(new ValueMap().set("_toString", MeteorStarscript.posString(blockPos.getX(), blockPos.getY(), blockPos.getZ())).set("x", Value.number(blockPos.getX())).set("y", Value.number(blockPos.getY())).set("z", Value.number(blockPos.getZ())))));
    }

    public static Value wrap(Entity entity) {
        double d;
        double d2;
        LivingEntity e;
        ValueMap valueMap = new ValueMap().set("_toString", Value.string(entity.getName().getString())).set("id", Value.string(BuiltInRegistries.ENTITY_TYPE.getKey((Object)entity.getType()).toString()));
        if (entity instanceof LivingEntity) {
            e = (LivingEntity)entity;
            d2 = e.getHealth();
        } else {
            d2 = 0.0;
        }
        ValueMap valueMap2 = valueMap.set("health", Value.number(d2));
        if (entity instanceof LivingEntity) {
            e = (LivingEntity)entity;
            d = e.getAbsorptionAmount();
        } else {
            d = 0.0;
        }
        return Value.map(valueMap2.set("absorption", Value.number(d)).set("pos", Value.map(new ValueMap().set("_toString", MeteorStarscript.posString(entity.getX(), entity.getY(), entity.getZ())).set("x", Value.number(entity.getX())).set("y", Value.number(entity.getY())).set("z", Value.number(entity.getZ())))));
    }

    public static Value wrap(HorizontalDirection dir) {
        return Value.map(new ValueMap().set("_toString", Value.string(dir.name + " " + dir.axis)).set("name", Value.string(dir.name)).set("axis", Value.string(dir.axis)));
    }

    public static Value wrap(MobEffectInstance effectInstance) {
        return Value.map(new ValueMap().set("duration", effectInstance.getDuration()).set("level", effectInstance.getAmplifier() + 1));
    }
}
