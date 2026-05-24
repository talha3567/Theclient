package net.wurstclient.hacks.newchunks;

import java.util.Set;
import net.minecraft.class_1921;
import net.minecraft.class_1923;
import net.minecraft.class_4588;

public interface NewChunksChunkRenderer {
    public void buildBuffer(class_4588 var1, Set<class_1923> var2, int var3);

    public class_1921 getLayer();
}
