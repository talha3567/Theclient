package net.wurstclient.util.chunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import net.minecraft.class_1923;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2791;
import net.minecraft.class_2874;
import net.wurstclient.WurstClient;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.settings.ChunkAreaSetting;
import net.wurstclient.util.chunk.ChunkSearcher;

public abstract class AbstractChunkCoordinator
implements PacketInputListener {
    protected final HashMap<class_1923, ChunkSearcher> searchers = new HashMap();
    protected final ChunkAreaSetting area;
    private BiPredicate<class_2338, class_2680> query;
    protected final Set<class_1923> chunksToUpdate = Collections.synchronizedSet(new HashSet());

    public AbstractChunkCoordinator(BiPredicate<class_2338, class_2680> query, ChunkAreaSetting area) {
        this.query = Objects.requireNonNull(query);
        this.area = Objects.requireNonNull(area);
    }

    public boolean update() {
        class_2874 dimension = WurstClient.MC.field_1687.method_8597();
        HashSet<class_1923> chunkUpdates = this.clearChunksToUpdate();
        boolean searchersChanged = false;
        for (ChunkSearcher searcher : new ArrayList<ChunkSearcher>(this.searchers.values())) {
            boolean remove = false;
            class_1923 searcherPos = searcher.getPos();
            if (dimension != searcher.getDimension()) {
                remove = true;
            } else if (!this.area.isInRange(searcherPos)) {
                remove = true;
            } else if (chunkUpdates.contains(searcherPos)) {
                remove = true;
            }
            if (!remove) continue;
            this.searchers.remove(searcherPos);
            searcher.cancel();
            this.onRemove(searcher);
            searchersChanged = true;
        }
        for (class_2791 chunk : this.area.getChunksInRange()) {
            class_1923 chunkPos = chunk.method_12004();
            if (this.searchers.containsKey(chunkPos)) continue;
            ChunkSearcher searcher = new ChunkSearcher(this.query, chunk, dimension);
            this.searchers.put(chunkPos, searcher);
            searcher.start();
            searchersChanged = true;
        }
        return searchersChanged;
    }

    protected void onRemove(ChunkSearcher searcher) {
    }

    public void reset() {
        this.searchers.values().forEach(ChunkSearcher::cancel);
        this.searchers.clear();
        this.chunksToUpdate.clear();
    }

    public boolean isDone() {
        return this.searchers.values().stream().allMatch(ChunkSearcher::isDone);
    }

    public void setQuery(BiPredicate<class_2338, class_2680> query) {
        this.query = Objects.requireNonNull(query);
        this.searchers.values().forEach(ChunkSearcher::cancel);
        this.searchers.clear();
    }

    public void setTargetBlock(class_2248 block) {
        this.setQuery((pos, state) -> block == state.method_26204());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected HashSet<class_1923> clearChunksToUpdate() {
        Set<class_1923> set = this.chunksToUpdate;
        synchronized (set) {
            HashSet<class_1923> chunks = new HashSet<class_1923>(this.chunksToUpdate);
            this.chunksToUpdate.clear();
            return chunks;
        }
    }
}
