package meteordevelopment.meteorclient.mixininterface;

import com.mojang.authlib.GameProfile;

public interface IGuiMessage {
    public String meteor$getText();

    public int meteor$getId();

    public void meteor$setId(int var1);

    public GameProfile meteor$getSender();

    public void meteor$setSender(GameProfile var1);
}
