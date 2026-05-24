package net.wurstclient.hacks;

import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1802;
import net.minecraft.class_2596;
import net.minecraft.class_2828;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PlayerAttacksEntityListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.EnumSetting;

@SearchTags(value={"Crits"})
public final class CriticalsHack
extends Hack
implements PlayerAttacksEntityListener {
    private final EnumSetting<Mode> mode = new EnumSetting("Mode", "\u00a7lPacket\u00a7r mode sends packets to server without actually moving you at all.\n\n\u00a7lMini Jump\u00a7r mode does a tiny jump that is just enough to get a critical hit.\n\n\u00a7lFull Jump\u00a7r mode makes you jump normally.", (Enum[])Mode.values(), (Enum)Mode.PACKET);

    public CriticalsHack() {
        super("Criticals");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.mode);
    }

    @Override
    public String getRenderName() {
        return this.getName() + " [" + String.valueOf((Object)this.mode.getSelected()) + "]";
    }

    @Override
    protected void onEnable() {
        EVENTS.add(PlayerAttacksEntityListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(PlayerAttacksEntityListener.class, this);
    }

    @Override
    public void onPlayerAttacksEntity(class_1297 target) {
        if (!(target instanceof class_1309)) {
            return;
        }
        if (CriticalsHack.WURST.getHax().maceDmgHack.isEnabled() && CriticalsHack.MC.field_1724.method_6047().method_31574(class_1802.field_49814)) {
            return;
        }
        if (!CriticalsHack.MC.field_1724.method_24828()) {
            return;
        }
        if (CriticalsHack.MC.field_1724.method_5799() || CriticalsHack.MC.field_1724.method_5771()) {
            return;
        }
        switch (this.mode.getSelected().ordinal()) {
            case 0: {
                this.doPacketJump();
                break;
            }
            case 1: {
                this.doMiniJump();
                break;
            }
            case 2: {
                this.doFullJump();
            }
        }
    }

    private void doPacketJump() {
        this.sendFakeY(0.0625, true);
        this.sendFakeY(0.0, false);
        this.sendFakeY(1.1E-5, false);
        this.sendFakeY(0.0, false);
    }

    private void sendFakeY(double offset, boolean onGround) {
        CriticalsHack.MC.field_1724.field_3944.method_52787((class_2596)new class_2828.class_2829(CriticalsHack.MC.field_1724.method_23317(), CriticalsHack.MC.field_1724.method_23318() + offset, CriticalsHack.MC.field_1724.method_23321(), onGround, CriticalsHack.MC.field_1724.field_5976));
    }

    private void doMiniJump() {
        CriticalsHack.MC.field_1724.method_5762(0.0, 0.1, 0.0);
        CriticalsHack.MC.field_1724.field_6017 = 0.1f;
        CriticalsHack.MC.field_1724.method_24830(false);
    }

    private void doFullJump() {
        CriticalsHack.MC.field_1724.method_6043();
    }

    private static enum Mode {
        PACKET("Packet"),
        MINI_JUMP("Mini Jump"),
        FULL_JUMP("Full Jump");

        private final String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}
