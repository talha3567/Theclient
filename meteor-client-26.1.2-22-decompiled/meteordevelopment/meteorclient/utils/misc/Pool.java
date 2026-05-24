package meteordevelopment.meteorclient.utils.misc;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import java.util.function.Supplier;

public final class Pool<T> {
    private final Queue<T> items = new ArrayDeque<T>();
    private final Supplier<T> producer;

    public Pool(Supplier<T> producer) {
        this.producer = producer;
    }

    public synchronized T get() {
        if (!this.items.isEmpty()) {
            return this.items.poll();
        }
        return this.producer.get();
    }

    public synchronized void free(T obj) {
        this.items.offer(obj);
    }

    public synchronized void freeAll(Iterable<T> objects) {
        if (objects instanceof Collection) {
            Collection collection = (Collection)objects;
            this.items.addAll(collection);
        } else {
            objects.forEach(this.items::add);
        }
    }
}
