package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PostMotionListener;
import net.wurstclient.events.PreMotionListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IKeyMapping;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;

@SearchTags(value={"AutoSneaking"})
public final class SneakHack
extends Hack
implements PreMotionListener,
PostMotionListener {
    private final EnumSetting<SneakMode> mode = new EnumSetting("Mode", "\u00a7lPacket\u00a7r mode makes it look like you're sneaking without slowing you down.\n\u00a7lLegit\u00a7r mode actually makes you sneak.", (Enum[])SneakMode.values(), (Enum)SneakMode.LEGIT);
    private final CheckboxSetting offWhileFlying = new CheckboxSetting("Turn off while flying", "Automatically disables Legit Sneak while you are flying or using Freecam, so that it doesn't force you to fly down.\n\nKeep in mind that this also means you won't be hidden from other players while doing these things.", false);

    public SneakHack() {
        super("Sneak");
        this.setCategory(Category.MOVEMENT);
        this.addSetting(this.mode);
        this.addSetting(this.offWhileFlying);
    }

    @Override
    public String getRenderName() {
        return this.getName() + " [" + String.valueOf((Object)this.mode.getSelected()) + "]";
    }

    @Override
    protected void onEnable() {
        EVENTS.add(PreMotionListener.class, this);
        EVENTS.add(PostMotionListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(PreMotionListener.class, this);
        EVENTS.remove(PostMotionListener.class, this);
        switch (this.mode.getSelected().ordinal()) {
            case 1: {
                IKeyMapping.get(SneakHack.MC.field_1690.field_1832).resetPressedState();
                break;
            }
        }
    }

    @Override
    public void onPreMotion() {
        IKeyMapping sneakKey = IKeyMapping.get(SneakHack.MC.field_1690.field_1832);
        switch (this.mode.getSelected().ordinal()) {
            case 1: {
                if (this.offWhileFlying.isChecked() && this.isFlying()) {
                    sneakKey.resetPressedState();
                    break;
                }
                sneakKey.method_23481(true);
                break;
            }
            case 0: {
                sneakKey.resetPressedState();
            }
        }
    }

    @Override
    public void onPostMotion() {
    }

    private boolean isFlying() {
        if (SneakHack.MC.field_1724.method_31549().field_7479) {
            return true;
        }
        if (SneakHack.WURST.getHax().flightHack.isEnabled()) {
            return true;
        }
        return SneakHack.WURST.getHax().freecamHack.isEnabled();
    }

    private static enum SneakMode {
        PACKET("Packet"),
        LEGIT("Legit");

        private final String name;

        private SneakMode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}
