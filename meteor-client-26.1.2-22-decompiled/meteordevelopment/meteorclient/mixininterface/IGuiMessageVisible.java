package meteordevelopment.meteorclient.mixininterface;

import meteordevelopment.meteorclient.mixininterface.IGuiMessage;

public interface IGuiMessageVisible
extends IGuiMessage {
    public boolean meteor$isStartOfEntry();

    public void meteor$setStartOfEntry(boolean var1);
}
