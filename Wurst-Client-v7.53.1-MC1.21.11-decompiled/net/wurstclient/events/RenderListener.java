package net.wurstclient.events;

import java.util.ArrayList;
import net.minecraft.class_4587;
import net.wurstclient.event.Event;
import net.wurstclient.event.Listener;

public interface RenderListener
extends Listener {
    public void onRender(class_4587 var1, float var2);

    public static class RenderEvent
    extends Event<RenderListener> {
        private final class_4587 matrixStack;
        private final float partialTicks;

        public RenderEvent(class_4587 matrixStack, float partialTicks) {
            this.matrixStack = matrixStack;
            this.partialTicks = partialTicks;
        }

        @Override
        public void fire(ArrayList<RenderListener> listeners) {
            for (RenderListener listener : listeners) {
                listener.onRender(this.matrixStack, this.partialTicks);
            }
        }

        @Override
        public Class<RenderListener> getListenerType() {
            return RenderListener.class;
        }
    }
}
