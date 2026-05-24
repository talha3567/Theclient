package net.wurstclient.ai;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import net.wurstclient.ai.PathPos;

public class PathQueue {
    private final PriorityQueue<Entry> queue = new PriorityQueue<Entry>(Comparator.comparing(e1 -> Float.valueOf(e1.priority)));

    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    public boolean add(PathPos pos, float priority) {
        return this.queue.add(new Entry(pos, priority));
    }

    public PathPos[] toArray() {
        PathPos[] array = new PathPos[this.size()];
        Iterator<Entry> itr = this.queue.iterator();
        for (int i = 0; i < this.size() && itr.hasNext(); ++i) {
            array[i] = itr.next().pos;
        }
        return array;
    }

    public int size() {
        return this.queue.size();
    }

    public void clear() {
        this.queue.clear();
    }

    public PathPos poll() {
        return this.queue.poll().pos;
    }

    private static class Entry {
        private PathPos pos;
        private float priority;

        public Entry(PathPos pos, float priority) {
            this.pos = pos;
            this.priority = priority;
        }
    }
}
