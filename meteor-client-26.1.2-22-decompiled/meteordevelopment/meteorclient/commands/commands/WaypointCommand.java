package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.BlockPosArgumentType;
import meteordevelopment.meteorclient.commands.arguments.WaypointArgumentType;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.BlockPos;

public class WaypointCommand
extends Command {
    public WaypointCommand() {
        super("waypoint", "Manages waypoints.", "wp");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(WaypointCommand.literal("list").executes(commandContext -> {
            if (Waypoints.get().isEmpty()) {
                this.error("No created waypoints.", new Object[0]);
            } else {
                this.info(String.valueOf(ChatFormatting.WHITE) + "Created Waypoints:", new Object[0]);
                for (Waypoint waypoint : Waypoints.get()) {
                    this.info("Name: (highlight)'%s'(default), Dimension: (highlight)%s(default), Pos: (highlight)%s(default)", new Object[]{waypoint.name.get(), waypoint.dimension.get(), this.waypointPos(waypoint)});
                }
            }
            return 1;
        }));
        builder.then(WaypointCommand.literal("get").then(WaypointCommand.argument("waypoint", WaypointArgumentType.create()).executes(context -> {
            Waypoint waypoint = WaypointArgumentType.get(context);
            this.info("Name: " + String.valueOf(ChatFormatting.WHITE) + waypoint.name.get(), new Object[0]);
            this.info("Actual Dimension: " + String.valueOf(ChatFormatting.WHITE) + String.valueOf((Object)waypoint.dimension.get()), new Object[0]);
            this.info("Position: " + String.valueOf(ChatFormatting.WHITE) + this.waypointFullPos(waypoint), new Object[0]);
            this.info("Visible: " + (waypoint.visible.get() != false ? String.valueOf(ChatFormatting.GREEN) + "True" : String.valueOf(ChatFormatting.RED) + "False"), new Object[0]);
            return 1;
        })));
        builder.then(((LiteralArgumentBuilder)WaypointCommand.literal("add").then(WaypointCommand.argument("pos", BlockPosArgumentType.blockPos()).then(WaypointCommand.argument("waypoint", StringArgumentType.greedyString()).executes(context -> this.addWaypoint((CommandContext<ClientSuggestionProvider>)context, true))))).then(WaypointCommand.argument("waypoint", StringArgumentType.greedyString()).executes(context -> this.addWaypoint((CommandContext<ClientSuggestionProvider>)context, false))));
        builder.then(WaypointCommand.literal("delete").then(WaypointCommand.argument("waypoint", WaypointArgumentType.create()).executes(context -> {
            Waypoint waypoint = WaypointArgumentType.get(context);
            this.info("The waypoint (highlight)'%s'(default) has been deleted.", waypoint.name.get());
            Waypoints.get().remove(waypoint);
            return 1;
        })));
        builder.then(WaypointCommand.literal("toggle").then(WaypointCommand.argument("waypoint", WaypointArgumentType.create()).executes(context -> {
            Waypoint waypoint = WaypointArgumentType.get(context);
            waypoint.visible.set(waypoint.visible.get() == false);
            Waypoints.get().save();
            return 1;
        })));
    }

    private String waypointPos(Waypoint waypoint) {
        return "X: " + waypoint.pos.get().getX() + " Z: " + waypoint.pos.get().getZ();
    }

    private String waypointFullPos(Waypoint waypoint) {
        return "X: " + waypoint.pos.get().getX() + ", Y: " + waypoint.pos.get().getY() + ", Z: " + waypoint.pos.get().getZ();
    }

    private int addWaypoint(CommandContext<ClientSuggestionProvider> context, boolean withCoords) {
        if (WaypointCommand.mc.player == null) {
            return -1;
        }
        BlockPos pos = withCoords ? BlockPosArgumentType.getBlockPos(context, "pos") : WaypointCommand.mc.player.blockPosition().above(2);
        Waypoint waypoint = new Waypoint.Builder().name(StringArgumentType.getString(context, (String)"waypoint")).pos(pos).dimension(PlayerUtils.getDimension()).build();
        Waypoints.get().add(waypoint);
        this.info("Created waypoint with name: (highlight)%s(default)", waypoint.name.get());
        return 1;
    }
}
