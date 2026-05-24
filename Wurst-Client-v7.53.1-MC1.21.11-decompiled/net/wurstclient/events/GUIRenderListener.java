package net.wurstclient.events;

import java.util.ArrayList;
import net.minecraft.class_332;
import net.wurstclient.event.Event;
import net.wurstclient.event.Listener;

public interface GUIRenderListener
extends Listener {
    public void onRenderGUI(class_332 var1, float var2);

    public static class GUIRenderEvent
    extends Event<GUIRenderListener> {
        private final float partialTicks;
        private final class_332 context;

        public GUIRenderEvent(class_332 context, float partialTicks) {
            this.context = context;
            this.partialTicks = partialTicks;
        }

        @Override
        public void fire(ArrayList<GUIRenderListener> listeners) {
            for (GUIRenderListener listener : listeners) {
                listener.onRenderGUI(this.context, this.partialTicks);
            }
        }

        @Override
        public Class<GUIRenderListener> getListenerType() {
            return GUIRenderListener.class;
        }
    }
}
