package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.KeyMappingAccessor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.resources.language.I18n;

public class InputCommand
extends Command {
    private static final List<KeypressHandler> activeHandlers = new ArrayList<KeypressHandler>();
    private static final List<Pair<KeyMapping, String>> holdKeys = List.of(new Pair((Object)InputCommand.mc.options.keyUp, (Object)"forwards"), new Pair((Object)InputCommand.mc.options.keyDown, (Object)"backwards"), new Pair((Object)InputCommand.mc.options.keyLeft, (Object)"left"), new Pair((Object)InputCommand.mc.options.keyRight, (Object)"right"), new Pair((Object)InputCommand.mc.options.keyJump, (Object)"jump"), new Pair((Object)InputCommand.mc.options.keyShift, (Object)"sneak"), new Pair((Object)InputCommand.mc.options.keySprint, (Object)"sprint"), new Pair((Object)InputCommand.mc.options.keyUse, (Object)"use"), new Pair((Object)InputCommand.mc.options.keyAttack, (Object)"attack"));
    private static final List<Pair<KeyMapping, String>> pressKeys = List.of(new Pair((Object)InputCommand.mc.options.keySwapOffhand, (Object)"swap"), new Pair((Object)InputCommand.mc.options.keyDrop, (Object)"drop"));

    public InputCommand() {
        super("input", "Keyboard input simulation.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        for (Pair<KeyMapping, String> keyBinding : holdKeys) {
            builder.then(((LiteralArgumentBuilder)InputCommand.literal((String)keyBinding.getSecond()).executes(commandContext -> {
                activeHandlers.add(new KeypressHandler((KeyMapping)keyBinding.getFirst(), 1));
                return 1;
            })).then(InputCommand.argument("ticks", IntegerArgumentType.integer((int)1)).executes(context -> {
                activeHandlers.add(new KeypressHandler((KeyMapping)keyBinding.getFirst(), (Integer)context.getArgument("ticks", Integer.class)));
                return 1;
            })));
        }
        for (Pair<KeyMapping, String> keyBinding : pressKeys) {
            builder.then(InputCommand.literal((String)keyBinding.getSecond()).executes(commandContext -> {
                InputCommand.press((KeyMapping)keyBinding.getFirst());
                return 1;
            }));
        }
        for (KeyMapping keyBinding : InputCommand.mc.options.keyHotbarSlots) {
            builder.then(InputCommand.literal(keyBinding.getName().substring(4)).executes(commandContext -> {
                InputCommand.press(keyBinding);
                return 1;
            }));
        }
        builder.then(InputCommand.literal("clear").executes(commandContext -> {
            if (activeHandlers.isEmpty()) {
                this.warning("No active keypress handlers.", new Object[0]);
            } else {
                this.info("Cleared all keypress handlers.", new Object[0]);
                activeHandlers.forEach(MeteorClient.EVENT_BUS::unsubscribe);
                activeHandlers.clear();
            }
            return 1;
        }));
        builder.then(InputCommand.literal("list").executes(commandContext -> {
            if (activeHandlers.isEmpty()) {
                this.warning("No active keypress handlers.", new Object[0]);
            } else {
                this.info("Active keypress handlers: ", new Object[0]);
                for (int i = 0; i < activeHandlers.size(); ++i) {
                    KeypressHandler handler = activeHandlers.get(i);
                    this.info("(highlight)%d(default) - (highlight)%s %d(default) ticks left out of (highlight)%d(default).", i, I18n.get((String)handler.key.getName(), (Object[])new Object[0]), handler.ticks, handler.totalTicks);
                }
            }
            return 1;
        }));
        builder.then(InputCommand.literal("remove").then(InputCommand.argument("index", IntegerArgumentType.integer((int)0)).executes(ctx -> {
            int index = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"index");
            if (index >= activeHandlers.size()) {
                this.warning("Index out of range.", new Object[0]);
            } else {
                this.info("Removed keypress handler.", new Object[0]);
                MeteorClient.EVENT_BUS.unsubscribe(activeHandlers.get(index));
                activeHandlers.remove(index);
            }
            return 1;
        })));
    }

    private static void press(KeyMapping keyBinding) {
        KeyMappingAccessor accessor = (KeyMappingAccessor)keyBinding;
        accessor.meteor$setClickCount(accessor.meteor$getClickCount() + 1);
    }

    private static class KeypressHandler {
        private final KeyMapping key;
        private final int totalTicks;
        private int ticks;

        public KeypressHandler(KeyMapping key, int ticks) {
            this.key = key;
            this.totalTicks = ticks;
            this.ticks = ticks;
            MeteorClient.EVENT_BUS.subscribe(this);
        }

        @EventHandler
        private void onTick(TickEvent.Post event) {
            if (this.ticks == this.totalTicks) {
                InputCommand.press(this.key);
            }
            if (this.ticks-- > 0) {
                this.key.setDown(true);
            } else {
                this.key.setDown(false);
                MeteorClient.EVENT_BUS.unsubscribe(this);
                activeHandlers.remove(this);
            }
        }
    }
}
