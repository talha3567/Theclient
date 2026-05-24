package net.wurstclient.hacks;

import java.util.Random;
import net.minecraft.class_1664;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;

@SearchTags(value={"SpookySkin", "spooky skin", "SkinBlinker", "skin blinker"})
public final class SkinDerpHack
extends Hack
implements UpdateListener {
    private final Random random = new Random();

    public SkinDerpHack() {
        super("SkinDerp");
        this.setCategory(Category.FUN);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        for (class_1664 part : class_1664.values()) {
            SkinDerpHack.MC.field_1690.method_1635(part, true);
        }
    }

    @Override
    public void onUpdate() {
        if (this.random.nextInt(4) != 0) {
            return;
        }
        for (class_1664 part : class_1664.values()) {
            SkinDerpHack.MC.field_1690.method_1635(part, !SkinDerpHack.MC.field_1690.method_32594(part));
        }
    }
}
