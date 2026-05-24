package net.wurstclient.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_2338;

public final class BlockBreakingCache {
    private final ArrayDeque<Set<class_2338>> prevBlocks = new ArrayDeque();

    public ArrayList<class_2338> filterOutRecentBlocks(Stream<class_2338> stream) {
        for (Set<class_2338> set : this.prevBlocks) {
            stream = stream.filter(pos -> !set.contains(pos));
        }
        ArrayList blocks = stream.collect(Collectors.toCollection(ArrayList::new));
        this.prevBlocks.addLast(new HashSet(blocks));
        while (this.prevBlocks.size() > 5) {
            this.prevBlocks.removeFirst();
        }
        return blocks;
    }

    public void reset() {
        this.prevBlocks.clear();
    }
}
