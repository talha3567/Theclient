package meteordevelopment.meteorclient.utils.misc;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class ComponentMapReader {
    private static final DynamicCommandExceptionType UNKNOWN_COMPONENT_EXCEPTION = new DynamicCommandExceptionType(id -> Component.translatableEscape((String)"arguments.item.component.unknown", (Object[])new Object[]{id}));
    private static final SimpleCommandExceptionType COMPONENT_EXPECTED_EXCEPTION = new SimpleCommandExceptionType((Message)Component.translatable((String)"arguments.item.component.expected"));
    private static final DynamicCommandExceptionType REPEATED_COMPONENT_EXCEPTION = new DynamicCommandExceptionType(type -> Component.translatableEscape((String)"arguments.item.component.repeated", (Object[])new Object[]{type}));
    private static final Dynamic2CommandExceptionType MALFORMED_COMPONENT_EXCEPTION = new Dynamic2CommandExceptionType((type, error) -> Component.translatableEscape((String)"arguments.item.component.malformed", (Object[])new Object[]{type, error}));
    private static final TagParser<Tag> SNBT_READER = TagParser.create((DynamicOps)NbtOps.INSTANCE);
    private final DynamicOps<Tag> nbtOps;

    public ComponentMapReader(CommandBuildContext commandRegistryAccess) {
        this.nbtOps = commandRegistryAccess.createSerializationContext((DynamicOps)NbtOps.INSTANCE);
    }

    public DataComponentMap consume(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        try {
            return new Reader(reader, this.nbtOps).read();
        }
        catch (CommandSyntaxException e) {
            reader.setCursor(cursor);
            throw e;
        }
    }

    public CompletableFuture<Suggestions> getSuggestions(SuggestionsBuilder builder) {
        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());
        Reader reader = new Reader(stringReader, this.nbtOps);
        try {
            reader.read();
        }
        catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        return reader.suggestor.apply(builder.createOffset(stringReader.getCursor()));
    }

    private static class Reader {
        private static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_DEFAULT = SuggestionsBuilder::buildFuture;
        private final StringReader reader;
        private final DynamicOps<Tag> nbtOps;
        public Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestor = this::suggestBracket;

        public Reader(StringReader reader, DynamicOps<Tag> nbtOps) {
            this.reader = reader;
            this.nbtOps = nbtOps;
        }

        public DataComponentMap read() throws CommandSyntaxException {
            DataComponentMap.Builder builder = DataComponentMap.builder();
            this.reader.expect('[');
            this.suggestor = this::suggestComponentType;
            ReferenceArraySet set = new ReferenceArraySet();
            while (this.reader.canRead() && this.reader.peek() != ']') {
                this.reader.skipWhitespace();
                DataComponentType<?> dataComponentType = Reader.readComponentType(this.reader);
                if (!set.add(dataComponentType)) {
                    throw REPEATED_COMPONENT_EXCEPTION.create(dataComponentType);
                }
                this.suggestor = this::suggestEqual;
                this.reader.skipWhitespace();
                this.reader.expect('=');
                this.suggestor = SUGGEST_DEFAULT;
                this.reader.skipWhitespace();
                this.readComponentValue(this.reader, builder, dataComponentType);
                this.reader.skipWhitespace();
                this.suggestor = this::suggestEndOfComponent;
                if (!this.reader.canRead() || this.reader.peek() != ',') break;
                this.reader.skip();
                this.reader.skipWhitespace();
                this.suggestor = this::suggestComponentType;
                if (this.reader.canRead()) continue;
                throw COMPONENT_EXPECTED_EXCEPTION.createWithContext((ImmutableStringReader)this.reader);
            }
            this.reader.expect(']');
            this.suggestor = SUGGEST_DEFAULT;
            return builder.build();
        }

        public static DataComponentType<?> readComponentType(StringReader reader) throws CommandSyntaxException {
            if (!reader.canRead()) {
                throw COMPONENT_EXPECTED_EXCEPTION.createWithContext((ImmutableStringReader)reader);
            }
            int i = reader.getCursor();
            Identifier identifier = Identifier.read((StringReader)reader);
            DataComponentType dataComponentType = (DataComponentType)BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(identifier);
            if (dataComponentType != null && !dataComponentType.isTransient()) {
                return dataComponentType;
            }
            reader.setCursor(i);
            throw UNKNOWN_COMPONENT_EXCEPTION.createWithContext((ImmutableStringReader)reader, (Object)identifier);
        }

        private CompletableFuture<Suggestions> suggestComponentType(SuggestionsBuilder builder) {
            String string = builder.getRemaining().toLowerCase(Locale.ROOT);
            SharedSuggestionProvider.filterResources((Iterable)BuiltInRegistries.DATA_COMPONENT_TYPE.entrySet(), (String)string, entry -> ((ResourceKey)entry.getKey()).identifier(), entry -> {
                DataComponentType dataComponentType = (DataComponentType)entry.getValue();
                if (dataComponentType.codec() != null) {
                    Identifier identifier = ((ResourceKey)entry.getKey()).identifier();
                    builder.suggest(String.valueOf(identifier) + "=");
                }
            });
            return builder.buildFuture();
        }

        private <T> void readComponentValue(StringReader reader, DataComponentMap.Builder builder, DataComponentType<T> type) throws CommandSyntaxException {
            int i = reader.getCursor();
            Tag nbtElement = (Tag)SNBT_READER.parseAsArgument(reader);
            DataResult dataResult = type.codecOrThrow().parse(this.nbtOps, (Object)nbtElement);
            builder.set(type, dataResult.getOrThrow(error -> {
                reader.setCursor(i);
                return MALFORMED_COMPONENT_EXCEPTION.createWithContext((ImmutableStringReader)reader, (Object)type.toString(), error);
            }));
        }

        private CompletableFuture<Suggestions> suggestBracket(SuggestionsBuilder builder) {
            if (builder.getRemaining().isEmpty()) {
                builder.suggest(String.valueOf('['));
            }
            return builder.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestEndOfComponent(SuggestionsBuilder builder) {
            if (builder.getRemaining().isEmpty()) {
                builder.suggest(String.valueOf(','));
                builder.suggest(String.valueOf(']'));
            }
            return builder.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestEqual(SuggestionsBuilder builder) {
            if (builder.getRemaining().isEmpty()) {
                builder.suggest(String.valueOf('='));
            }
            return builder.buildFuture();
        }
    }
}
