package meteordevelopment.meteorclient.utils.misc.text;

import net.minecraft.network.chat.ClickEvent;

public class MeteorClickEvent
implements ClickEvent {
    public final String value;

    public MeteorClickEvent(String value) {
        this.value = value;
    }

    public ClickEvent.Action action() {
        return ClickEvent.Action.RUN_COMMAND;
    }
}
