package net.wurstclient.hacks;

import net.minecraft.class_259;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.CactusCollisionShapeListener;
import net.wurstclient.hack.Hack;

@SearchTags(value={"NoCactus", "anti cactus", "no cactus"})
public final class AntiCactusHack
extends Hack
implements CactusCollisionShapeListener {
    public AntiCactusHack() {
        super("AntiCactus");
        this.setCategory(Category.BLOCKS);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(CactusCollisionShapeListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(CactusCollisionShapeListener.class, this);
    }

    @Override
    public void onCactusCollisionShape(CactusCollisionShapeListener.CactusCollisionShapeEvent event) {
        event.setCollisionShape(class_259.method_1077());
    }
}
