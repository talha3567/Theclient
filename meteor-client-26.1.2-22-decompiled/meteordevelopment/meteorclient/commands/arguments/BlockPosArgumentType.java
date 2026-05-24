package meteordevelopment.meteorclient.commands.arguments;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class BlockPosArgumentType
implements ArgumentType<PosArgument> {
    private static final BlockPosArgumentType INSTANCE = new BlockPosArgumentType();
    private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "^ ^ ^", "^1 ^ ^-5", "~0.5 ~1 ~-5");
    public static final SimpleCommandExceptionType UNLOADED_EXCEPTION = new SimpleCommandExceptionType((Message)Component.translatable((String)"argument.pos.unloaded"));
    public static final SimpleCommandExceptionType OUT_OF_WORLD_EXCEPTION = new SimpleCommandExceptionType((Message)Component.translatable((String)"argument.pos.outofworld"));
    public static final SimpleCommandExceptionType OUT_OF_BOUNDS_EXCEPTION = new SimpleCommandExceptionType((Message)Component.translatable((String)"argument.pos.outofbounds"));

    private BlockPosArgumentType() {
    }

    public static BlockPosArgumentType blockPos() {
        return INSTANCE;
    }

    public static <S> BlockPos getLoadedBlockPos(CommandContext<S> context, String name) throws CommandSyntaxException {
        ClientLevel clientLevel = MeteorClient.mc.level;
        return BlockPosArgumentType.getLoadedBlockPos(context, clientLevel, name);
    }

    public static <S> BlockPos getLoadedBlockPos(CommandContext<S> context, ClientLevel level, String name) throws CommandSyntaxException {
        BlockPos blockPos = BlockPosArgumentType.getBlockPos(context, name);
        ChunkPos chunkPos = new ChunkPos(blockPos.getX(), blockPos.getZ());
        if (!level.getChunkSource().hasChunk(chunkPos.x(), chunkPos.z())) {
            throw UNLOADED_EXCEPTION.create();
        }
        if (!level.isInWorldBounds(blockPos)) {
            throw OUT_OF_WORLD_EXCEPTION.create();
        }
        return blockPos;
    }

    public static <S> BlockPos getBlockPos(CommandContext<S> context, String name) {
        return ((PosArgument)context.getArgument(name, PosArgument.class)).getBlockPos(context.getSource());
    }

    public static <S> BlockPos getValidBlockPos(CommandContext<S> context, String name) throws CommandSyntaxException {
        BlockPos blockPos = BlockPosArgumentType.getBlockPos(context, name);
        if (!Level.isInSpawnableBounds((BlockPos)blockPos)) {
            throw OUT_OF_BOUNDS_EXCEPTION.create();
        }
        return blockPos;
    }

    public PosArgument parse(StringReader stringReader) throws CommandSyntaxException {
        return stringReader.canRead() && stringReader.peek() == '^' ? LookingPosArgument.parse(stringReader) : DefaultPosArgument.parse(stringReader);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Object object = context.getSource();
        if (!(object instanceof SharedSuggestionProvider)) {
            return Suggestions.empty();
        }
        SharedSuggestionProvider sharedSuggestionProvider = (SharedSuggestionProvider)object;
        String string = builder.getRemaining();
        Set<SharedSuggestionProvider.TextCoordinates> collection = !string.isEmpty() && string.charAt(0) == '^' ? Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_LOCAL) : sharedSuggestionProvider.getRelevantCoordinates();
        return SharedSuggestionProvider.suggestCoordinates((String)string, collection, (SuggestionsBuilder)builder, (Predicate)Commands.createValidator(this::parse));
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static interface PosArgument {
        public <S> Vec3 getPosition(S var1);

        public <S> Vec2 getRotation(S var1);

        default public <S> BlockPos getBlockPos(S source) {
            return BlockPos.containing((Position)this.getPosition(source));
        }

        public boolean isXRelative();

        public boolean isYRelative();

        public boolean isZRelative();
    }

    public static class LookingPosArgument
    implements PosArgument {
        private final double x;
        private final double y;
        private final double z;

        public LookingPosArgument(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public <S> Vec3 getPosition(S source) {
            Vec2 vec2 = MeteorClient.mc.player.getRotationVector();
            Vec3 vec3 = EntityAnchorArgument.Anchor.FEET.apply((Entity)MeteorClient.mc.player);
            float f = Mth.cos((double)((vec2.y + 90.0f) * ((float)Math.PI / 180)));
            float g = Mth.sin((double)((vec2.y + 90.0f) * ((float)Math.PI / 180)));
            float h = Mth.cos((double)(-vec2.x * ((float)Math.PI / 180)));
            float i = Mth.sin((double)(-vec2.x * ((float)Math.PI / 180)));
            float j = Mth.cos((double)((-vec2.x + 90.0f) * ((float)Math.PI / 180)));
            float k = Mth.sin((double)((-vec2.x + 90.0f) * ((float)Math.PI / 180)));
            Vec3 vec32 = new Vec3((double)(f * h), (double)i, (double)(g * h));
            Vec3 vec33 = new Vec3((double)(f * j), (double)k, (double)(g * j));
            Vec3 vec34 = vec32.cross(vec33).scale(-1.0);
            double d = vec32.x * this.z + vec33.x * this.y + vec34.x * this.x;
            double e = vec32.y * this.z + vec33.y * this.y + vec34.y * this.x;
            double l = vec32.z * this.z + vec33.z * this.y + vec34.z * this.x;
            return new Vec3(vec3.x + d, vec3.y + e, vec3.z + l);
        }

        @Override
        public <S> Vec2 getRotation(S source) {
            return Vec2.ZERO;
        }

        @Override
        public boolean isXRelative() {
            return true;
        }

        @Override
        public boolean isYRelative() {
            return true;
        }

        @Override
        public boolean isZRelative() {
            return true;
        }

        public static LookingPosArgument parse(StringReader reader) throws CommandSyntaxException {
            int cursor = reader.getCursor();
            double d = LookingPosArgument.readCoordinate(reader, cursor);
            if (!reader.canRead() || reader.peek() != ' ') {
                reader.setCursor(cursor);
                throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext((ImmutableStringReader)reader);
            }
            reader.skip();
            double e = LookingPosArgument.readCoordinate(reader, cursor);
            if (!reader.canRead() || reader.peek() != ' ') {
                reader.setCursor(cursor);
                throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext((ImmutableStringReader)reader);
            }
            reader.skip();
            double f = LookingPosArgument.readCoordinate(reader, cursor);
            return new LookingPosArgument(d, e, f);
        }

        private static double readCoordinate(StringReader reader, int startingCursorPos) throws CommandSyntaxException {
            if (!reader.canRead()) {
                throw WorldCoordinate.ERROR_EXPECTED_DOUBLE.createWithContext((ImmutableStringReader)reader);
            }
            if (reader.peek() != '^') {
                reader.setCursor(startingCursorPos);
                throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext((ImmutableStringReader)reader);
            }
            reader.skip();
            return reader.canRead() && reader.peek() != ' ' ? reader.readDouble() : 0.0;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof LookingPosArgument)) {
                return false;
            }
            LookingPosArgument lookingPosArgument = (LookingPosArgument)o;
            return this.x == lookingPosArgument.x && this.y == lookingPosArgument.y && this.z == lookingPosArgument.z;
        }

        public int hashCode() {
            return Objects.hash(this.x, this.y, this.z);
        }
    }

    public static class DefaultPosArgument
    implements PosArgument {
        private final WorldCoordinate x;
        private final WorldCoordinate y;
        private final WorldCoordinate z;

        public DefaultPosArgument(WorldCoordinate x, WorldCoordinate y, WorldCoordinate z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public <S> Vec3 getPosition(S source) {
            Vec3 vec3 = MeteorClient.mc.player.position();
            return new Vec3(this.x.get(vec3.x), this.y.get(vec3.y), this.z.get(vec3.z));
        }

        @Override
        public <S> Vec2 getRotation(S source) {
            Vec2 vec2 = MeteorClient.mc.player.getRotationVector();
            return new Vec2((float)this.x.get((double)vec2.x), (float)this.y.get((double)vec2.y));
        }

        @Override
        public boolean isXRelative() {
            return this.x.isRelative();
        }

        @Override
        public boolean isYRelative() {
            return this.y.isRelative();
        }

        @Override
        public boolean isZRelative() {
            return this.z.isRelative();
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DefaultPosArgument)) {
                return false;
            }
            DefaultPosArgument defaultPosArgument = (DefaultPosArgument)o;
            return this.x.equals((Object)defaultPosArgument.x) && this.y.equals((Object)defaultPosArgument.y) && this.z.equals((Object)defaultPosArgument.z);
        }

        public static DefaultPosArgument parse(StringReader reader) throws CommandSyntaxException {
            int cursor = reader.getCursor();
            WorldCoordinate worldCoordinate = WorldCoordinate.parseInt((StringReader)reader);
            if (reader.canRead() && reader.peek() == ' ') {
                reader.skip();
                WorldCoordinate worldCoordinate2 = WorldCoordinate.parseInt((StringReader)reader);
                if (reader.canRead() && reader.peek() == ' ') {
                    reader.skip();
                    WorldCoordinate worldCoordinate3 = WorldCoordinate.parseInt((StringReader)reader);
                    return new DefaultPosArgument(worldCoordinate, worldCoordinate2, worldCoordinate3);
                }
            }
            reader.setCursor(cursor);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext((ImmutableStringReader)reader);
        }

        public static DefaultPosArgument parse(StringReader reader, boolean centerIntegers) throws CommandSyntaxException {
            int cursor = reader.getCursor();
            WorldCoordinate worldCoordinate = WorldCoordinate.parseDouble((StringReader)reader, (boolean)centerIntegers);
            if (reader.canRead() && reader.peek() == ' ') {
                reader.skip();
                WorldCoordinate worldCoordinate2 = WorldCoordinate.parseDouble((StringReader)reader, (boolean)false);
                if (reader.canRead() && reader.peek() == ' ') {
                    reader.skip();
                    WorldCoordinate worldCoordinate3 = WorldCoordinate.parseDouble((StringReader)reader, (boolean)centerIntegers);
                    return new DefaultPosArgument(worldCoordinate, worldCoordinate2, worldCoordinate3);
                }
            }
            reader.setCursor(cursor);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext((ImmutableStringReader)reader);
        }

        public static DefaultPosArgument absolute(double x, double y, double z) {
            return new DefaultPosArgument(new WorldCoordinate(false, x), new WorldCoordinate(false, y), new WorldCoordinate(false, z));
        }

        public static DefaultPosArgument absolute(Vec2 vec) {
            return new DefaultPosArgument(new WorldCoordinate(false, (double)vec.x), new WorldCoordinate(false, (double)vec.y), new WorldCoordinate(true, 0.0));
        }

        public static DefaultPosArgument current() {
            return new DefaultPosArgument(new WorldCoordinate(true, 0.0), new WorldCoordinate(true, 0.0), new WorldCoordinate(true, 0.0));
        }

        public int hashCode() {
            int i = this.x.hashCode();
            i = 31 * i + this.y.hashCode();
            return 31 * i + this.z.hashCode();
        }
    }
}
