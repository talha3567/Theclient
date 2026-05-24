package net.wurstclient.settings;

import java.util.ArrayList;
import net.minecraft.class_1923;
import net.minecraft.class_2791;
import net.minecraft.class_2812;
import net.minecraft.class_2818;
import net.minecraft.class_310;
import net.wurstclient.WurstClient;
import net.wurstclient.settings.EnumSetting;

public final class ChunkAreaSetting
extends EnumSetting<ChunkArea> {
    private static final class_310 MC = WurstClient.MC;

    public ChunkAreaSetting(String name, String description) {
        super(name, description, (Enum[])ChunkArea.values(), (Enum)ChunkArea.A11);
    }

    public ChunkAreaSetting(String name, String description, ChunkArea selected) {
        super(name, description, (Enum[])ChunkArea.values(), (Enum)selected);
    }

    public ArrayList<class_2791> getChunksInRange() {
        return ((ChunkArea)((Object)this.getSelected())).getChunksInRange();
    }

    public boolean isInRange(class_1923 pos) {
        return ((ChunkArea)((Object)this.getSelected())).isInRange(pos);
    }

    public static enum ChunkArea {
        A3("3x3 chunks", 1),
        A5("5x5 chunks", 2),
        A7("7x7 chunks", 3),
        A9("9x9 chunks", 4),
        A11("11x11 chunks", 5),
        A13("13x13 chunks", 6),
        A15("15x15 chunks", 7),
        A17("17x17 chunks", 8),
        A19("19x19 chunks", 9),
        A21("21x21 chunks", 10),
        A23("23x23 chunks", 11),
        A25("25x25 chunks", 12),
        A27("27x27 chunks", 13),
        A29("29x29 chunks", 14),
        A31("31x31 chunks", 15),
        A33("33x33 chunks", 16);

        private final String name;
        private final int chunkRange;

        private ChunkArea(String name, int chunkRange) {
            this.name = name;
            this.chunkRange = chunkRange;
        }

        public ArrayList<class_2791> getChunksInRange() {
            class_1923 center = ChunkAreaSetting.MC.field_1724.method_31476();
            ArrayList<class_2791> chunksInRange = new ArrayList<class_2791>();
            for (int x = center.field_9181 - this.chunkRange; x <= center.field_9181 + this.chunkRange; ++x) {
                for (int z = center.field_9180 - this.chunkRange; z <= center.field_9180 + this.chunkRange; ++z) {
                    class_2818 chunk = ChunkAreaSetting.MC.field_1687.method_8497(x, z);
                    if (chunk instanceof class_2812) continue;
                    chunksInRange.add((class_2791)chunk);
                }
            }
            return chunksInRange;
        }

        public boolean isInRange(class_1923 pos) {
            class_1923 center = ChunkAreaSetting.MC.field_1724.method_31476();
            return Math.abs(pos.field_9181 - center.field_9181) <= this.chunkRange && Math.abs(pos.field_9180 - center.field_9180) <= this.chunkRange;
        }

        public String toString() {
            return this.name;
        }
    }
}
