package net.wurstclient.hacks.newchunks;

import net.wurstclient.hacks.newchunks.NewChunksChunkRenderer;
import net.wurstclient.hacks.newchunks.NewChunksOutlineRenderer;
import net.wurstclient.hacks.newchunks.NewChunksSquareRenderer;
import net.wurstclient.settings.EnumSetting;

public final class NewChunksStyleSetting
extends EnumSetting<Style> {
    public NewChunksStyleSetting() {
        super("Style", (Enum[])Style.values(), (Enum)Style.OUTLINE);
    }

    public static enum Style {
        OUTLINE("Outline", new NewChunksOutlineRenderer()),
        SQUARE("Square", new NewChunksSquareRenderer());

        private final String name;
        private final NewChunksChunkRenderer chunkRenderer;

        private Style(String name, NewChunksChunkRenderer chunkRenderer) {
            this.name = name;
            this.chunkRenderer = chunkRenderer;
        }

        public String toString() {
            return this.name;
        }

        public NewChunksChunkRenderer getChunkRenderer() {
            return this.chunkRenderer;
        }
    }
}
