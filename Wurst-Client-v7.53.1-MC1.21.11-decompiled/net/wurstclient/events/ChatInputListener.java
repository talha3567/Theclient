package net.wurstclient.events;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_303;
import net.wurstclient.event.CancellableEvent;
import net.wurstclient.event.Listener;

public interface ChatInputListener
extends Listener {
    public void onReceivedMessage(ChatInputEvent var1);

    public static class ChatInputEvent
    extends CancellableEvent<ChatInputListener> {
        private class_2561 component;
        private List<class_303.class_7590> chatLines;

        public ChatInputEvent(class_2561 component, List<class_303.class_7590> visibleMessages) {
            this.component = component;
            this.chatLines = visibleMessages;
        }

        public class_2561 getComponent() {
            return this.component;
        }

        public void setComponent(class_2561 component) {
            this.component = component;
        }

        public List<class_303.class_7590> getChatLines() {
            return this.chatLines;
        }

        @Override
        public void fire(ArrayList<ChatInputListener> listeners) {
            for (ChatInputListener listener : listeners) {
                listener.onReceivedMessage(this);
                if (!this.isCancelled()) continue;
                break;
            }
        }

        @Override
        public Class<ChatInputListener> getListenerType() {
            return ChatInputListener.class;
        }
    }
}
