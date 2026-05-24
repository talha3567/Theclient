package net.wurstclient.util.chunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2791;
import net.minecraft.class_2874;
import net.wurstclient.util.MinPriorityThreadFactory;
import net.wurstclient.util.chunk.ChunkUtils;

public final class ChunkSearcher {
    private static final ExecutorService BACKGROUND_THREAD_POOL = MinPriorityThreadFactory.newFixedThreadPool();
    private final BiPredicate<class_2338, class_2680> query;
    private final class_2791 chunk;
    private final class_2874 dimension;
    private CompletableFuture<ArrayList<Result>> future;
    private boolean interrupted;

    public ChunkSearcher(BiPredicate<class_2338, class_2680> query, class_2791 chunk, class_2874 dimension) {
        this.query = query;
        this.chunk = chunk;
        this.dimension = dimension;
    }

    public void start() {
        if (this.future != null || this.interrupted) {
            throw new IllegalStateException();
        }
        this.future = CompletableFuture.supplyAsync(this::searchNow, BACKGROUND_THREAD_POOL);
    }

    private ArrayList<Result> searchNow() {
        ArrayList<Result> results = new ArrayList<Result>();
        class_1923 chunkPos = this.chunk.method_12004();
        int minX = chunkPos.method_8326();
        int minY = this.chunk.method_31607();
        int minZ = chunkPos.method_8328();
        int maxX = chunkPos.method_8327();
        int maxY = ChunkUtils.getHighestNonEmptySectionYOffset(this.chunk) + 16;
        int maxZ = chunkPos.method_8329();
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    if (this.interrupted) {
                        return results;
                    }
                    class_2338 pos = new class_2338(x, y, z);
                    class_2680 state = this.chunk.method_8320(pos);
                    if (!this.query.test(pos, state)) continue;
                    results.add(new Result(pos, state));
                }
            }
        }
        return results;
    }

    public void cancel() {
        if (this.future == null || this.future.isDone()) {
            return;
        }
        this.interrupted = true;
        this.future.cancel(false);
    }

    public boolean isInterrupted() {
        return this.interrupted;
    }

    public class_1923 getPos() {
        return this.chunk.method_12004();
    }

    public class_2874 getDimension() {
        return this.dimension;
    }

    public Stream<Result> getMatches() {
        if (this.future == null || this.future.isCancelled()) {
            return Stream.empty();
        }
        return this.future.join().stream();
    }

    public List<Result> getMatchesList() {
        if (this.future == null || this.future.isCancelled()) {
            return List.of();
        }
        return Collections.unmodifiableList((List)this.future.join());
    }

    public boolean isDone() {
        return this.future != null && this.future.isDone();
    }

    public record Result(class_2338 pos, class_2680 state) {
    }
}
