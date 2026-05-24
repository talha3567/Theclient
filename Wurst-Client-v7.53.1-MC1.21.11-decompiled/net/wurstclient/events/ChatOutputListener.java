package net.wurstclient.events;

import java.util.ArrayList;
import java.util.Objects;
import net.wurstclient.event.CancellableEvent;
import net.wurstclient.event.Listener;

public interface ChatOutputListener
extends Listener {
    public void onSentMessage(ChatOutputEvent var1);

    public static class ChatOutputEvent
    extends CancellableEvent<ChatOutputListener> {
        private final String originalMessage;
        private String message;

        public ChatOutputEvent(String message) {
            this.message = Objects.requireNonNull(message);
            this.originalMessage = message;
        }

        public String getMessage() {
            return this.message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getOriginalMessage() {
            return this.originalMessage;
        }

        public boolean isModified() {
            return !this.originalMessage.equals(this.message);
        }

        @Override
        public void fire(ArrayList<ChatOutputListener> listeners) {
            for (ChatOutputListener listener : listeners) {
                listener.onSentMessage(this);
                if (!this.isCancelled()) continue;
                break;
            }
        }

        @Override
        public Class<ChatOutputListener> getListenerType() {
            return ChatOutputListener.class;
        }
    }
}
