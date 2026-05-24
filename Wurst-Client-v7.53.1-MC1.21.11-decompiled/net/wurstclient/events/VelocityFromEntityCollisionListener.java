package net.wurstclient.events;

import java.util.ArrayList;
import net.minecraft.class_1297;
import net.wurstclient.event.CancellableEvent;
import net.wurstclient.event.Listener;

public interface VelocityFromEntityCollisionListener
extends Listener {
    public void onVelocityFromEntityCollision(VelocityFromEntityCollisionEvent var1);

    public static class VelocityFromEntityCollisionEvent
    extends CancellableEvent<VelocityFromEntityCollisionListener> {
        private final class_1297 entity;

        public VelocityFromEntityCollisionEvent(class_1297 entity) {
            this.entity = entity;
        }

        public class_1297 getEntity() {
            return this.entity;
        }

        @Override
        public void fire(ArrayList<VelocityFromEntityCollisionListener> listeners) {
            for (VelocityFromEntityCollisionListener listener : listeners) {
                listener.onVelocityFromEntityCollision(this);
                if (!this.isCancelled()) continue;
                break;
            }
        }

        @Override
        public Class<VelocityFromEntityCollisionListener> getListenerType() {
            return VelocityFromEntityCollisionListener.class;
        }
    }
}
