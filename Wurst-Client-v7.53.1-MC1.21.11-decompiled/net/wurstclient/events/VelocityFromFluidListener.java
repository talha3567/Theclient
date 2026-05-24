package net.wurstclient.events;

import java.util.ArrayList;
import net.minecraft.class_1297;
import net.wurstclient.event.CancellableEvent;
import net.wurstclient.event.Listener;

public interface VelocityFromFluidListener
extends Listener {
    public void onVelocityFromFluid(VelocityFromFluidEvent var1);

    public static class VelocityFromFluidEvent
    extends CancellableEvent<VelocityFromFluidListener> {
        private final class_1297 entity;

        public VelocityFromFluidEvent(class_1297 entity) {
            this.entity = entity;
        }

        public class_1297 getEntity() {
            return this.entity;
        }

        @Override
        public void fire(ArrayList<VelocityFromFluidListener> listeners) {
            for (VelocityFromFluidListener listener : listeners) {
                listener.onVelocityFromFluid(this);
                if (!this.isCancelled()) continue;
                break;
            }
        }

        @Override
        public Class<VelocityFromFluidListener> getListenerType() {
            return VelocityFromFluidListener.class;
        }
    }
}
