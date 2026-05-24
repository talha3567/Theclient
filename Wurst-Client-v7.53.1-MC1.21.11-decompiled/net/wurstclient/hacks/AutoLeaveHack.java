package net.wurstclient.hacks;

import net.minecraft.class_1297;
import net.minecraft.class_1802;
import net.minecraft.class_2596;
import net.minecraft.class_2824;
import net.minecraft.class_638;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.InventoryUtils;

@SearchTags(value={"auto leave", "AutoDisconnect", "auto disconnect", "AutoQuit", "auto quit"})
public final class AutoLeaveHack
extends Hack
implements UpdateListener {
    private final SliderSetting health = new SliderSetting("Health", "Leaves the server when your health reaches this value or falls below it.", 4.0, 0.5, 9.5, 0.5, SliderSetting.ValueDisplay.DECIMAL.withSuffix(" hearts"));
    public final EnumSetting<Mode> mode = new EnumSetting("Mode", "\u00a7lQuit\u00a7r mode just quits the game normally.\nBypasses NoCheat+ but not CombatLog.\n\n\u00a7lChars\u00a7r mode sends a special chat message that causes the server to kick you.\nBypasses NoCheat+ and some versions of CombatLog.\n\n\u00a7lSelfHurt\u00a7r mode sends the packet for attacking another player, but with yourself as both the attacker and the target, causing the server to kick you.\nBypasses both CombatLog and NoCheat+.", (Enum[])Mode.values(), (Enum)Mode.QUIT);
    private final CheckboxSetting disableAutoReconnect = new CheckboxSetting("Disable AutoReconnect", "Automatically turns off AutoReconnect when AutoLeave makes you leave the server.", true);
    private final SliderSetting totems = new SliderSetting("Totems", "Won't leave the server until the number of totems you have reaches this value or falls below it.\n\n11 = always able to leave", 11.0, 0.0, 11.0, 1.0, SliderSetting.ValueDisplay.INTEGER.withSuffix(" totems").withLabel(1.0, "1 totem").withLabel(11.0, "ignore"));

    public AutoLeaveHack() {
        super("AutoLeave");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.health);
        this.addSetting(this.mode);
        this.addSetting(this.disableAutoReconnect);
        this.addSetting(this.totems);
    }

    @Override
    public String getRenderName() {
        if (AutoLeaveHack.MC.field_1724 != null && AutoLeaveHack.MC.field_1724.method_31549().field_7477) {
            return this.getName() + " (paused)";
        }
        return this.getName() + " [" + String.valueOf((Object)this.mode.getSelected()) + "]";
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        if (AutoLeaveHack.MC.field_1724.method_31549().field_7477) {
            return;
        }
        float currentHealth = AutoLeaveHack.MC.field_1724.method_6032();
        if (currentHealth <= 0.0f || currentHealth > this.health.getValueF() * 2.0f) {
            return;
        }
        if (this.totems.getValueI() < 11 && InventoryUtils.count(class_1802.field_8288, 40, true) > this.totems.getValueI()) {
            return;
        }
        this.mode.getSelected().leave.run();
        this.setEnabled(false);
        if (this.disableAutoReconnect.isChecked()) {
            AutoLeaveHack.WURST.getHax().autoReconnectHack.setEnabled(false);
        }
    }

    public static enum Mode {
        QUIT("Quit", () -> MC.field_1687.method_8525(class_638.field_61021)),
        CHARS("Chars", () -> MC.method_1562().method_45729("\u00a7")),
        SELFHURT("SelfHurt", () -> MC.method_1562().method_52787((class_2596)class_2824.method_34206((class_1297)MC.field_1724, (boolean)MC.field_1724.method_5715())));

        private final String name;
        private final Runnable leave;

        private Mode(String name, Runnable leave) {
            this.name = name;
            this.leave = leave;
        }

        public String toString() {
            return this.name;
        }
    }
}
