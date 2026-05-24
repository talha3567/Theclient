package net.wurstclient.events;

import java.util.ArrayList;
import net.wurstclient.event.CancellableEvent;
import net.wurstclient.event.Listener;

public interface CameraTransformViewBobbingListener
extends Listener {
    public void onCameraTransformViewBobbing(CameraTransformViewBobbingEvent var1);

    public static class CameraTransformViewBobbingEvent
    extends CancellableEvent<CameraTransformViewBobbingListener> {
        @Override
        public void fire(ArrayList<CameraTransformViewBobbingListener> listeners) {
            for (CameraTransformViewBobbingListener listener : listeners) {
                listener.onCameraTransformViewBobbing(this);
                if (!this.isCancelled()) continue;
                break;
            }
        }

        @Override
        public Class<CameraTransformViewBobbingListener> getListenerType() {
            return CameraTransformViewBobbingListener.class;
        }
    }
}
