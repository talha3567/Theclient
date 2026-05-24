package net.wurstclient.mixinterface;

import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2846;
import net.minecraft.class_3965;

public interface IClientPlayerInteractionManager {
    public void windowClick_PICKUP(int var1);

    public void windowClick_QUICK_MOVE(int var1);

    public void windowClick_THROW(int var1);

    public void windowClick_SWAP(int var1, int var2);

    public void rightClickItem();

    public void rightClickBlock(class_2338 var1, class_2350 var2, class_243 var3);

    public void sendPlayerActionC2SPacket(class_2846.class_2847 var1, class_2338 var2, class_2350 var3);

    public void sendPlayerInteractBlockPacket(class_1268 var1, class_3965 var2);
}
