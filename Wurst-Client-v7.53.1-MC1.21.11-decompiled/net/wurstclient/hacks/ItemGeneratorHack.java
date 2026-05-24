package net.wurstclient.hacks;

import java.util.Optional;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1935;
import net.minecraft.class_5819;
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.InventoryUtils;

@SearchTags(value={"item generator", "drop infinite"})
public final class ItemGeneratorHack
extends Hack
implements UpdateListener {
    private final SliderSetting speed = new SliderSetting("Speed", "\u00a74\u00a7lWARNING:\u00a7r High speeds will cause a ton of lag and can easily crash the game!", 1.0, 1.0, 36.0, 1.0, SliderSetting.ValueDisplay.INTEGER);
    private final SliderSetting stackSize = new SliderSetting("Stack size", "How many items to place in each stack.\nDoesn't seem to affect performance.", 1.0, 1.0, 64.0, 1.0, SliderSetting.ValueDisplay.INTEGER);
    private final class_5819 random = class_5819.method_43053();

    public ItemGeneratorHack() {
        super("ItemGenerator");
        this.setCategory(Category.ITEMS);
        this.addSetting(this.speed);
        this.addSetting(this.stackSize);
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
        if (!ItemGeneratorHack.MC.field_1724.method_56992()) {
            ChatUtils.error("Creative mode only.");
            this.setEnabled(false);
        }
        int stacks = this.speed.getValueI();
        for (int slot = 9; slot < 9 + stacks; ++slot) {
            Optional optional = Optional.empty();
            while (optional.isEmpty()) {
                optional = class_7923.field_41178.method_10240(this.random);
            }
            class_1792 item = (class_1792)((class_6880.class_6883)optional.get()).comp_349();
            class_1799 stack = new class_1799((class_1935)item, this.stackSize.getValueI());
            InventoryUtils.setCreativeStack(slot, stack);
        }
        for (int i = 9; i < 9 + stacks; ++i) {
            IMC.getInteractionManager().windowClick_THROW(i);
        }
    }
}
