package net.wurstclient.hacks.autolibrarian;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.class_1887;
import net.minecraft.class_2378;
import net.minecraft.class_2561;
import net.minecraft.class_2588;
import net.minecraft.class_2960;
import net.minecraft.class_5455;
import net.minecraft.class_6880;
import net.minecraft.class_7417;
import net.minecraft.class_7924;
import net.minecraft.class_9636;
import net.wurstclient.WurstClient;
import net.wurstclient.WurstTranslator;

public record BookOffer(String id, int level, int price) implements Comparable<BookOffer>
{
    public static BookOffer create(class_1887 enchantment) {
        class_5455 drm = WurstClient.MC.field_1687.method_30349();
        class_2378 registry = drm.method_30530(class_7924.field_41265);
        class_2960 id = registry.method_10221((Object)enchantment);
        return new BookOffer(String.valueOf(id), enchantment.method_8183(), 64);
    }

    public Optional<? extends class_6880<class_1887>> getEnchantmentEntry() {
        if (WurstClient.MC.field_1687 == null) {
            return Optional.empty();
        }
        class_5455 drm = WurstClient.MC.field_1687.method_30349();
        class_2378 registry = drm.method_30530(class_7924.field_41265);
        return registry.method_10223(class_2960.method_60654((String)this.id));
    }

    public class_1887 getEnchantment() {
        return this.getEnchantmentEntry().map(class_6880::comp_349).orElse(null);
    }

    public String getEnchantmentName() {
        class_2561 description = this.getEnchantment().comp_2686();
        class_7417 class_74172 = description.method_10851();
        if (class_74172 instanceof class_2588) {
            class_2588 tr = (class_2588)class_74172;
            return WurstClient.INSTANCE.getTranslator().translateMcEnglish(tr.method_11022(), new Object[0]);
        }
        return description.getString();
    }

    public String getEnchantmentNameWithLevel() {
        Object name;
        WurstTranslator translator = WurstClient.INSTANCE.getTranslator();
        class_1887 enchantment = this.getEnchantment();
        class_7417 class_74172 = enchantment.comp_2686().method_10851();
        if (class_74172 instanceof class_2588) {
            class_2588 tr = (class_2588)class_74172;
            name = translator.translateMcEnglish(tr.method_11022(), new Object[0]);
        } else {
            name = enchantment.comp_2686().getString();
        }
        if (enchantment.method_8183() > 1) {
            name = (String)name + " " + translator.translateMcEnglish("enchantment.level." + this.level, new Object[0]);
        }
        return name;
    }

    public String getFormattedPrice() {
        return this.price + " emerald" + (this.price == 1 ? "" : "s");
    }

    public boolean isFullyValid() {
        return this.isMostlyValid() && this.getEnchantmentEntry().map(entry -> entry.method_40220(class_9636.field_51545) && this.level <= ((class_1887)entry.comp_349()).method_8183()).orElse(false) != false;
    }

    public boolean isMostlyValid() {
        return class_2960.method_12829((String)this.id) != null && this.level >= 1 && this.price >= 1 && this.price <= 64;
    }

    @Override
    public int compareTo(BookOffer other) {
        int idCompare = this.id.compareTo(other.id);
        if (idCompare != 0) {
            return idCompare;
        }
        return Integer.compare(this.level, other.level);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        BookOffer other = (BookOffer)obj;
        return this.id.equals(other.id) && this.level == other.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.level);
    }
}
