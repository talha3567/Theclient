package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ClientPacketListenerAccessor;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.commons.lang3.Strings;

public class ServerCommand
extends Command {
    private static final Set<String> ANTICHEAT_LIST = Set.of("nocheatplus", "negativity", "warden", "horizon", "illegalstack", "coreprotect", "exploitsx", "vulcan", "abc", "spartan", "kauri", "anticheatreloaded", "witherac", "godseye", "matrix", "wraith", "antixrayheuristics", "grimac", "themis", "foxaddition", "guardianac", "ggintegrity", "lightanticheat", "anarchyexploitfixes", "polar");
    private static final Set<String> VERSION_ALIASES = Set.of("version", "ver", "about", "bukkit:version", "bukkit:ver", "bukkit:about");
    private String alias;
    private int ticks = 0;
    private boolean tick = false;
    private final List<String> plugins = new ArrayList<String>();
    private final List<String> commandTreePlugins = new ArrayList<String>();
    private static final Random RANDOM = new Random();

    public ServerCommand() {
        super("server", "Prints server information", new String[0]);
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(commandContext -> {
            this.basicInfo();
            return 1;
        });
        builder.then(ServerCommand.literal("info").executes(commandContext -> {
            this.basicInfo();
            return 1;
        }));
        builder.then(ServerCommand.literal("plugins").executes(commandContext -> {
            this.plugins.addAll(this.commandTreePlugins);
            if (this.alias != null) {
                mc.getConnection().send((Packet)new ServerboundCommandSuggestionPacket(RANDOM.nextInt(200), this.alias + " "));
                this.tick = true;
            } else {
                this.printPlugins();
            }
            return 1;
        }));
        builder.then(ServerCommand.literal("tps").executes(commandContext -> {
            float tps = TickRate.INSTANCE.getTickRate();
            ChatFormatting color = tps > 17.0f ? ChatFormatting.GREEN : (tps > 12.0f ? ChatFormatting.YELLOW : ChatFormatting.RED);
            this.info("Current TPS: %s%.2f(default).", color, Float.valueOf(tps));
            return 1;
        }));
    }

    private void basicInfo() {
        MutableComponent ipText;
        if (mc.hasSingleplayerServer()) {
            IntegratedServer server = mc.getSingleplayerServer();
            this.info("Singleplayer", new Object[0]);
            if (server != null) {
                this.info("Version: %s", server.getServerVersion());
            }
            return;
        }
        ServerData server = mc.getCurrentServer();
        if (server == null) {
            this.info("Couldn't obtain any server information.", new Object[0]);
            return;
        }
        String ipv4 = "";
        try {
            ipv4 = InetAddress.getByName(server.ip).getHostAddress();
        }
        catch (UnknownHostException unknownHostException) {
            // empty catch block
        }
        if (ipv4.isEmpty()) {
            ipText = Component.literal((String)(String.valueOf(ChatFormatting.GRAY) + server.ip));
            ipText.setStyle(ipText.getStyle().withClickEvent((ClickEvent)new ClickEvent.CopyToClipboard(server.ip)).withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.literal((String)"Copy to clipboard"))));
        } else {
            ipText = Component.literal((String)(String.valueOf(ChatFormatting.GRAY) + server.ip));
            ipText.setStyle(ipText.getStyle().withClickEvent((ClickEvent)new ClickEvent.CopyToClipboard(server.ip)).withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.literal((String)"Copy to clipboard"))));
            MutableComponent ipv4Text = Component.literal((String)String.format("%s (%s)", ChatFormatting.GRAY, ipv4));
            ipv4Text.setStyle(ipText.getStyle().withClickEvent((ClickEvent)new ClickEvent.CopyToClipboard(ipv4)).withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.literal((String)"Copy to clipboard"))));
            ipText.append((Component)ipv4Text);
        }
        this.info((Component)Component.literal((String)String.format("%sIP: ", ChatFormatting.GRAY)).append((Component)ipText));
        this.info("Port: %d", ServerAddress.parseString((String)server.ip).getPort());
        this.info("Type: %s", mc.getConnection().serverBrand() != null ? mc.getConnection().serverBrand() : "unknown");
        this.info("Motd: %s", server.motd != null ? server.motd.getString() : "unknown");
        this.info("Version: %s", server.version.getString());
        this.info("Protocol version: %d", server.protocol);
        this.info("Difficulty: %s (Local: %.2f)", ServerCommand.mc.level.getDifficulty().getDisplayName().getString(), new DifficultyInstance(ServerCommand.mc.level.getDifficulty(), ServerCommand.mc.level.getGameTime(), ServerCommand.mc.level.getChunk(ServerCommand.mc.player.blockPosition()).getInhabitedTime(), DimensionType.MOON_BRIGHTNESS_PER_PHASE[((MoonPhase)ServerCommand.mc.level.environmentAttributes().getValue(EnvironmentAttributes.MOON_PHASE, ServerCommand.mc.player.blockPosition())).index()]).getDifficulty());
        this.info("Day: %d", ServerCommand.mc.level.getGameTime() / 24000L);
        this.info("Permission level: %s", this.formatPerms());
    }

    public String formatPerms() {
        PermissionSet permissions = ServerCommand.mc.player.permissions();
        if (permissions.hasPermission(Permissions.COMMANDS_OWNER)) {
            return "4 (Owner)";
        }
        if (permissions.hasPermission(Permissions.COMMANDS_ADMIN)) {
            return "3 (Admin)";
        }
        if (permissions.hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            return "2 (Gamemaster)";
        }
        if (permissions.hasPermission(Permissions.COMMANDS_MODERATOR)) {
            return "1 (Moderator)";
        }
        return "0 (No Perms)";
    }

    private void printPlugins() {
        this.plugins.sort(String.CASE_INSENSITIVE_ORDER);
        this.plugins.replaceAll(this::formatName);
        if (!this.plugins.isEmpty()) {
            this.info("Plugins (%d): %s ", this.plugins.size(), String.join((CharSequence)", ", this.plugins));
        } else {
            this.error("No plugins found.", new Object[0]);
        }
        this.tick = false;
        this.ticks = 0;
        this.plugins.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!this.tick) {
            return;
        }
        ++this.ticks;
        if (this.ticks >= 100) {
            this.printPlugins();
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (this.tick && event.packet instanceof ServerboundCommandSuggestionPacket) {
            event.cancel();
        }
    }

    @EventHandler
    private void onReadPacket(PacketEvent.Receive event) {
        Object handler;
        ClientboundCommandsPacket packet;
        Packet<?> packet2 = event.packet;
        if (packet2 instanceof ClientboundCommandsPacket) {
            packet = (ClientboundCommandsPacket)packet2;
            handler = (ClientPacketListenerAccessor)event.connection.getPacketListener();
            this.commandTreePlugins.clear();
            this.alias = null;
            packet.getRoot(CommandBuildContext.simple((HolderLookup.Provider)handler.meteor$getRegistryAccess(), (FeatureFlagSet)handler.meteor$getEnabledFeatures()), ClientPacketListenerAccessor.meteor$getCommandNodeFactory()).getChildren().forEach(node -> {
                String[] split = node.getName().split(":");
                if (split.length > 1 && !this.commandTreePlugins.contains(split[0])) {
                    this.commandTreePlugins.add(split[0]);
                }
                if (this.alias == null && VERSION_ALIASES.contains(node.getName())) {
                    this.alias = node.getName();
                }
            });
        }
        if (!this.tick) {
            return;
        }
        try {
            handler = event.packet;
            if (handler instanceof ClientboundCommandSuggestionsPacket) {
                packet = (ClientboundCommandSuggestionsPacket)handler;
                Suggestions matches = packet.toSuggestions();
                if (matches.isEmpty()) {
                    this.error("An error occurred while trying to find plugins.", new Object[0]);
                    return;
                }
                for (Suggestion suggestion : matches.getList()) {
                    String pluginName = suggestion.getText();
                    if (this.plugins.contains(pluginName.toLowerCase())) continue;
                    this.plugins.add(pluginName);
                }
                this.printPlugins();
            }
        }
        catch (Exception exception) {
            this.error("An error occurred while trying to find plugins.", new Object[0]);
        }
    }

    private String formatName(String name) {
        if (ANTICHEAT_LIST.contains(name.toLowerCase())) {
            return String.format("%s%s(default)", ChatFormatting.RED, name);
        }
        if (Strings.CI.contains((CharSequence)name, (CharSequence)"exploit") || Strings.CI.contains((CharSequence)name, (CharSequence)"cheat") || Strings.CI.contains((CharSequence)name, (CharSequence)"illegal")) {
            return String.format("%s%s(default)", ChatFormatting.RED, name);
        }
        return String.format("(highlight)%s(default)", name);
    }
}
