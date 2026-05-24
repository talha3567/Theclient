package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstClient;
import net.wurstclient.events.DeathListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags(value={"auto respawn", "AutoRevive", "auto revive"})
public final class AutoRespawnHack
extends Hack
implements DeathListener {
    private final CheckboxSetting button = new CheckboxSetting("Death screen button", "Shows a button on the death screen that lets you quickly enable AutoRespawn.", true);

    public AutoRespawnHack() {
        super("AutoRespawn");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.button);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(DeathListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(DeathListener.class, this);
    }

    @Override
    public void onDeath() {
        AutoRespawnHack.MC.field_1724.method_7331();
        MC.method_1507(null);
    }

    public boolean shouldShowButton() {
        return WurstClient.INSTANCE.isEnabled() && !this.isEnabled() && this.button.isChecked();
    }
}
