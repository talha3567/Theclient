package net.wurstclient.events;

import java.util.ArrayList;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.wurstclient.event.Event;
import net.wurstclient.event.Listener;

public interface BlockBreakingProgressListener
extends Listener {
    public void onBlockBreakingProgress(BlockBreakingProgressEvent var1);

    public static class BlockBreakingProgressEvent
    extends Event<BlockBreakingProgressListener> {
        private final class_2338 blockPos;
        private final class_2350 direction;

        public BlockBreakingProgressEvent(class_2338 blockPos, class_2350 direction) {
            this.blockPos = blockPos;
            this.direction = direction;
        }

        @Override
        public void fire(ArrayList<BlockBreakingProgressListener> listeners) {
            for (BlockBreakingProgressListener listener : listeners) {
                listener.onBlockBreakingProgress(this);
            }
        }

        @Override
        public Class<BlockBreakingProgressListener> getListenerType() {
            return BlockBreakingProgressListener.class;
        }

        public class_2338 getBlockPos() {
            return this.blockPos;
        }

        public class_2350 getDirection() {
            return this.direction;
        }
    }
}
