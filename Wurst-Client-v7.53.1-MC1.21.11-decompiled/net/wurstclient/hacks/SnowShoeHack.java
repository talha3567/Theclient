package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;

@SearchTags(value={"snow shoe", "SnowJesus", "snow jesus", "NoSnowSink", "no snow sink", "AntiSnowSink", "anti snow sink"})
public final class SnowShoeHack
extends Hack {
    public SnowShoeHack() {
        super("SnowShoe");
        this.setCategory(Category.MOVEMENT);
    }
}
