package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.level.GameType;

public class BetterTab
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Integer> tabSize;
    public final Setting<Integer> tabHeight;
    private final Setting<Boolean> self;
    private final Setting<SettingColor> selfColor;
    private final Setting<Boolean> friends;
    public final Setting<Boolean> accurateLatency;
    private final Setting<Boolean> gamemode;

    public BetterTab() {
        super(Categories.Render, "better-tab", "Various improvements to the tab list.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.tabSize = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("tablist-size")).description("How many players in total to display in the tablist.")).defaultValue(100)).min(1).sliderRange(1, 1000).build());
        this.tabHeight = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("column-height")).description("How many players to display in each column.")).defaultValue(20)).min(1).sliderRange(1, 1000).build());
        this.self = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("highlight-self")).description("Highlights yourself in the tablist.")).defaultValue(true)).build());
        this.selfColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("self-color")).description("The color to highlight your name with.")).defaultValue(new SettingColor(250, 130, 30)).visible(this.self::get)).build());
        this.friends = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("highlight-friends")).description("Highlights friends in the tablist.")).defaultValue(true)).build());
        this.accurateLatency = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("accurate-latency")).description("Shows latency as a number in the tablist.")).defaultValue(true)).build());
        this.gamemode = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("gamemode")).description("Display gamemode next to the nick.")).defaultValue(false)).build());
    }

    public Component getPlayerName(PlayerInfo playerListEntry) {
        Friend friend;
        Color color = null;
        Component name = playerListEntry.getTabListDisplayName();
        if (name == null) {
            name = Component.literal((String)playerListEntry.getProfile().name());
        }
        if (playerListEntry.getProfile().id().toString().equals(this.mc.player.getGameProfile().id().toString()) && this.self.get().booleanValue()) {
            color = this.selfColor.get();
        } else if (this.friends.get().booleanValue() && Friends.get().isFriend(playerListEntry) && (friend = Friends.get().get(playerListEntry)) != null) {
            color = Config.get().friendColor.get();
        }
        if (color != null) {
            String nameString = name.getString();
            for (ChatFormatting format : ChatFormatting.values()) {
                if (!format.isColor()) continue;
                nameString = nameString.replace(format.toString(), "");
            }
            name = Component.literal((String)nameString).setStyle(name.getStyle().withColor(TextColor.fromRgb((int)color.getPacked())));
        }
        if (this.gamemode.get().booleanValue()) {
            GameType gm = playerListEntry.getGameMode();
            String gmText = "?";
            if (gm != null) {
                gmText = switch (gm) {
                    default -> throw new MatchException(null, null);
                    case GameType.SPECTATOR -> "Sp";
                    case GameType.SURVIVAL -> "S";
                    case GameType.CREATIVE -> "C";
                    case GameType.ADVENTURE -> "A";
                };
            }
            MutableComponent text = Component.literal((String)"");
            text.append(name);
            text.append(" [" + gmText + "]");
            name = text;
        }
        return name;
    }
}
