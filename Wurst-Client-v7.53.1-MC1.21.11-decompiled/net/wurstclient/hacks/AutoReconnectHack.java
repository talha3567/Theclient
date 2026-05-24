package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;

@SearchTags(value={"auto reconnect", "AutoRejoin", "auto rejoin"})
@DontBlock
public final class AutoReconnectHack
extends Hack {
    private final SliderSetting waitTime = new SliderSetting("Wait time", "Time before reconnecting in seconds.", 5.0, 0.0, 60.0, 0.5, SliderSetting.ValueDisplay.DECIMAL.withSuffix("s"));

    public AutoReconnectHack() {
        super("AutoReconnect");
        this.setCategory(Category.OTHER);
        this.addSetting(this.waitTime);
    }

    public int getWaitTicks() {
        return (int)(this.waitTime.getValue() * 20.0);
    }
}
