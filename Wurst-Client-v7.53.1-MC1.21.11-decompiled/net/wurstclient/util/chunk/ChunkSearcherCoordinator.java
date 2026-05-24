package net.wurstclient.util.chunk;

import java.util.function.BiPredicate;
import java.util.stream.Stream;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.settings.ChunkAreaSetting;
import net.wurstclient.util.chunk.AbstractChunkCoordinator;
import net.wurstclient.util.chunk.ChunkSearcher;
import net.wurstclient.util.chunk.ChunkUtils;

public final class ChunkSearcherCoordinator
extends AbstractChunkCoordinator {
    public ChunkSearcherCoordinator(ChunkAreaSetting area) {
        this((pos, state) -> false, area);
    }

    public ChunkSearcherCoordinator(BiPredicate<class_2338, class_2680> query, ChunkAreaSetting area) {
        super(query, area);
    }

    @Override
    public void onReceivedPacket(PacketInputListener.PacketInputEvent event) {
        class_1923 chunkPos = ChunkUtils.getAffectedChunk(event.getPacket());
        if (chunkPos != null) {
            this.chunksToUpdate.add(chunkPos);
        }
    }

    public Stream<ChunkSearcher.Result> getMatches() {
        return this.searchers.values().stream().flatMap(ChunkSearcher::getMatches);
    }
}
