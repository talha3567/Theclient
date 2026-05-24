package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;

@SearchTags(value={"no pumpkin", "AntiPumpkin", "anti pumpkin"})
public final class NoPumpkinHack
extends Hack {
    public NoPumpkinHack() {
        super("NoPumpkin");
        this.setCategory(Category.RENDER);
    }
}
