package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.VelocityFromEntityCollisionListener;
import net.wurstclient.hack.Hack;

@SearchTags(value={"anti entity push", "NoEntityPush", "no entity push"})
public final class AntiEntityPushHack
extends Hack
implements VelocityFromEntityCollisionListener {
    public AntiEntityPushHack() {
        super("AntiEntityPush");
        this.setCategory(Category.MOVEMENT);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(VelocityFromEntityCollisionListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(VelocityFromEntityCollisionListener.class, this);
    }

    @Override
    public void onVelocityFromEntityCollision(VelocityFromEntityCollisionListener.VelocityFromEntityCollisionEvent event) {
        if (event.getEntity() == AntiEntityPushHack.MC.field_1724) {
            event.cancel();
        }
    }
}
