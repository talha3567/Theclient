package net.wurstclient.events;

import java.util.ArrayList;
import net.minecraft.class_1297;
import net.wurstclient.event.Event;
import net.wurstclient.event.Listener;

public interface PlayerAttacksEntityListener
extends Listener {
    public void onPlayerAttacksEntity(class_1297 var1);

    public static class PlayerAttacksEntityEvent
    extends Event<PlayerAttacksEntityListener> {
        private final class_1297 target;

        public PlayerAttacksEntityEvent(class_1297 target) {
            this.target = target;
        }

        @Override
        public void fire(ArrayList<PlayerAttacksEntityListener> listeners) {
            for (PlayerAttacksEntityListener listener : listeners) {
                listener.onPlayerAttacksEntity(this.target);
            }
        }

        @Override
        public Class<PlayerAttacksEntityListener> getListenerType() {
            return PlayerAttacksEntityListener.class;
        }
    }
}
