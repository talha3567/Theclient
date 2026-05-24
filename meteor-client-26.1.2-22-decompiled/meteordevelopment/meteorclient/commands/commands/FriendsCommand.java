package meteordevelopment.meteorclient.commands.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.FriendArgumentType;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class FriendsCommand
extends Command {
    public FriendsCommand() {
        super("friends", "Manages friends.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(FriendsCommand.literal("add").then(FriendsCommand.argument("player", PlayerListEntryArgumentType.create()).executes(context -> {
            GameProfile profile = PlayerListEntryArgumentType.get(context).getProfile();
            Friend friend = new Friend(profile.name(), profile.id());
            if (Friends.get().add(friend)) {
                ChatUtils.sendMsg(friend.hashCode(), ChatFormatting.GRAY, "Added (highlight)%s (default)to friends.".formatted(friend.getName()), new Object[0]);
            } else {
                this.error("Already friends with that player.", new Object[0]);
            }
            return 1;
        })));
        builder.then(FriendsCommand.literal("remove").then(FriendsCommand.argument("friend", FriendArgumentType.create()).executes(context -> {
            Friend friend = FriendArgumentType.get(context);
            if (friend == null) {
                this.error("Not friends with that player.", new Object[0]);
                return 1;
            }
            if (Friends.get().remove(friend)) {
                ChatUtils.sendMsg(friend.hashCode(), ChatFormatting.GRAY, "Removed (highlight)%s (default)from friends.".formatted(friend.getName()), new Object[0]);
            } else {
                this.error("Failed to remove that friend.", new Object[0]);
            }
            return 1;
        })));
        builder.then(FriendsCommand.literal("list").executes(commandContext -> {
            this.info("--- Friends ((highlight)%s(default)) ---", Friends.get().count());
            Friends.get().forEach(friend -> ChatUtils.info("(highlight)%s".formatted(friend.getName()), new Object[0]));
            return 1;
        }));
    }
}
