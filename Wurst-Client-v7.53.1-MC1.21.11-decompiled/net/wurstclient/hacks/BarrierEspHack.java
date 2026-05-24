package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;

@SearchTags(value={"barrier esp"})
public class BarrierEspHack
extends Hack {
    public BarrierEspHack() {
        super("BarrierESP");
        this.setCategory(Category.RENDER);
    }
}
