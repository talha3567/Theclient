package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1661;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_1935;
import net.minecraft.class_2561;
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import net.minecraft.class_9334;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.InventoryUtils;

@SearchTags(value={"troll potion", "TrollingPotion", "trolling potion"})
public final class TrollPotionHack
extends Hack {
    private final EnumSetting<PotionType> potionType = new EnumSetting("Potion type", "The type of potion to generate.", (Enum[])PotionType.values(), (Enum)PotionType.SPLASH);

    public TrollPotionHack() {
        super("TrollPotion");
        this.setCategory(Category.ITEMS);
        this.addSetting(this.potionType);
    }

    @Override
    protected void onEnable() {
        if (!TrollPotionHack.MC.field_1724.method_31549().field_7477) {
            ChatUtils.error("Creative mode only.");
            this.setEnabled(false);
            return;
        }
        class_1799 stack = this.potionType.getSelected().createPotionStack();
        class_1661 inventory = TrollPotionHack.MC.field_1724.method_31548();
        int slot = inventory.method_7376();
        if (slot < 0) {
            ChatUtils.error("Cannot give potion. Your inventory is full.");
        } else {
            InventoryUtils.setCreativeStack(slot, stack);
            ChatUtils.message("Potion created.");
        }
        this.setEnabled(false);
    }

    private static enum PotionType {
        NORMAL("Normal", "Potion", class_1802.field_8574),
        SPLASH("Splash", "Splash Potion", class_1802.field_8436),
        LINGERING("Lingering", "Lingering Potion", class_1802.field_8150),
        ARROW("Arrow", "Arrow", class_1802.field_8087);

        private final String name;
        private final String itemName;
        private final class_1792 item;

        private PotionType(String name, String itemName, class_1792 item) {
            this.name = name;
            this.itemName = itemName;
            this.item = item;
        }

        public String toString() {
            return this.name;
        }

        public class_1799 createPotionStack() {
            class_1799 stack = new class_1799((class_1935)this.item);
            ArrayList<class_1293> effects = new ArrayList<class_1293>();
            for (int i = 1; i <= 23; ++i) {
                class_1291 effect = (class_1291)((class_6880.class_6883)class_7923.field_41174.method_40265(i).get()).comp_349();
                class_6880 entry = class_7923.field_41174.method_47983((Object)effect);
                effects.add(new class_1293(entry, Integer.MAX_VALUE, Integer.MAX_VALUE));
            }
            stack.method_57379(class_9334.field_49651, (Object)new class_1844(Optional.empty(), Optional.empty(), effects, Optional.empty()));
            String name = "\u00a7f" + this.itemName + " of Trolling";
            stack.method_57379(class_9334.field_49631, (Object)class_2561.method_43470((String)name));
            return stack;
        }
    }
}
