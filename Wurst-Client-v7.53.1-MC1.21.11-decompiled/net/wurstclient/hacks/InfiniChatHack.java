package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;

@SearchTags(value={"infini chat", "InfiniteChat", "infinite chat"})
public final class InfiniChatHack
extends Hack {
    public InfiniChatHack() {
        super("InfiniChat");
        this.setCategory(Category.CHAT);
    }
}
