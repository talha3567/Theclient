package net.wurstclient.events;

import java.util.ArrayList;
import net.minecraft.class_265;
import net.wurstclient.event.Event;
import net.wurstclient.event.Listener;

public interface CactusCollisionShapeListener
extends Listener {
    public void onCactusCollisionShape(CactusCollisionShapeEvent var1);

    public static class CactusCollisionShapeEvent
    extends Event<CactusCollisionShapeListener> {
        private class_265 collisionShape;

        public class_265 getCollisionShape() {
            return this.collisionShape;
        }

        public void setCollisionShape(class_265 collisionShape) {
            this.collisionShape = collisionShape;
        }

        @Override
        public void fire(ArrayList<CactusCollisionShapeListener> listeners) {
            for (CactusCollisionShapeListener listener : listeners) {
                listener.onCactusCollisionShape(this);
            }
        }

        @Override
        public Class<CactusCollisionShapeListener> getListenerType() {
            return CactusCollisionShapeListener.class;
        }
    }
}
