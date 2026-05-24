package net.wurstclient.mixinterface;

import net.minecraft.class_320;
import net.wurstclient.mixinterface.IClientPlayerInteractionManager;
import net.wurstclient.mixinterface.ILocalPlayer;

public interface IMinecraftClient {
    public IClientPlayerInteractionManager getInteractionManager();

    public ILocalPlayer getPlayer();

    public class_320 getWurstSession();

    public void setWurstSession(class_320 var1);
}
