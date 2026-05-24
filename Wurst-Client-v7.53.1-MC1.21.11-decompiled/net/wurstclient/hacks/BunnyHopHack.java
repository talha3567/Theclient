package net.wurstclient.hacks;

import java.util.function.Predicate;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.EnumSetting;

@SearchTags(value={"AutoJump", "BHop", "bunny hop", "auto jump"})
public final class BunnyHopHack
extends Hack
implements UpdateListener {
    private final EnumSetting<JumpIf> jumpIf = new EnumSetting("Jump if", (Enum[])JumpIf.values(), (Enum)JumpIf.SPRINTING);

    public BunnyHopHack() {
        super("BunnyHop");
        this.setCategory(Category.MOVEMENT);
        this.addSetting(this.jumpIf);
    }

    @Override
    public String getRenderName() {
        return this.getName() + " [" + this.jumpIf.getSelected().name + "]";
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
        class_746 player = BunnyHopHack.MC.field_1724;
        if (!player.method_24828() || player.method_5715()) {
            return;
        }
        if (this.jumpIf.getSelected().condition.test(player)) {
            player.method_6043();
        }
    }

    private static enum JumpIf {
        SPRINTING("Sprinting", p -> p.method_5624() && (p.field_6250 != 0.0f || p.field_6212 != 0.0f)),
        WALKING("Walking", p -> p.field_6250 != 0.0f || p.field_6212 != 0.0f),
        ALWAYS("Always", p -> true);

        private final String name;
        private final Predicate<class_746> condition;

        private JumpIf(String name, Predicate<class_746> condition) {
            this.name = name;
            this.condition = condition;
        }

        public String toString() {
            return this.name;
        }
    }
}
