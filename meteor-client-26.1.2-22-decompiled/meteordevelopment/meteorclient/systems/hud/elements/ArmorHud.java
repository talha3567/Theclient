package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.DisplayItemUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ArmorHud
extends HudElement {
    public static final HudElementInfo<ArmorHud> INFO = new HudElementInfo<ArmorHud>(Hud.GROUP, "armor", "Displays your armor.", ArmorHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgDurability;
    private final SettingGroup sgScale;
    private final SettingGroup sgBackground;
    private final Setting<Orientation> orientation;
    private final Setting<Boolean> flipOrder;
    private final Setting<Boolean> showEmpty;
    private final Setting<Durability> durability;
    private final Setting<SettingColor> durabilityColor;
    private final Setting<Boolean> durabilityShadow;
    private final Setting<Boolean> customScale;
    private final Setting<Double> scale;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;

    public ArmorHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgDurability = this.settings.createGroup("Durability");
        this.sgScale = this.settings.createGroup("Scale");
        this.sgBackground = this.settings.createGroup("Background");
        this.orientation = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("orientation")).description("How to display armor.")).defaultValue(Orientation.Horizontal)).onChanged(orientation -> this.calculateSize())).build());
        this.flipOrder = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("flip-order")).description("Flips the order of armor items.")).defaultValue(true)).build());
        this.showEmpty = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-empty")).description("Renders barrier icons for empty slots.")).defaultValue(false)).build());
        this.durability = this.sgDurability.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("durability")).description("How to display armor durability.")).defaultValue(Durability.Bar)).onChanged(durability -> this.calculateSize())).build());
        this.durabilityColor = this.sgDurability.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("durability-color")).description("Color of the text.")).visible(() -> this.durability.get() == Durability.Total || this.durability.get() == Durability.Percentage)).defaultValue(new SettingColor()).build());
        this.durabilityShadow = this.sgDurability.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("durability-shadow")).description("Text shadow.")).visible(() -> this.durability.get() == Durability.Total || this.durability.get() == Durability.Percentage)).defaultValue(true)).build());
        this.customScale = this.sgScale.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-scale")).description("Applies a custom scale to this hud element.")).defaultValue(false)).onChanged(bl -> this.calculateSize())).build());
        this.scale = this.sgScale.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("Custom scale.")).visible(this.customScale::get)).defaultValue(2.0).onChanged(d -> this.calculateSize())).min(0.5).sliderRange(0.5, 3.0).build());
        this.background = this.sgBackground.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("background")).description("Displays background.")).defaultValue(false)).build());
        this.backgroundColor = this.sgBackground.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color used for the background.")).visible(this.background::get)).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        this.calculateSize();
    }

    private void calculateSize() {
        switch (this.orientation.get().ordinal()) {
            case 0: {
                this.setSize(72.0f * this.getScale(), 16.0f * this.getScale());
                break;
            }
            case 1: {
                this.setSize(16.0f * this.getScale(), 72.0f * this.getScale());
            }
        }
    }

    @Override
    public void render(HudRenderer renderer) {
        ItemStack[] armor;
        ItemStack[] itemStackArray;
        int emptySlots = 0;
        if (this.flipOrder.get().booleanValue()) {
            ItemStack[] itemStackArray2 = new ItemStack[4];
            itemStackArray2[0] = this.getItem(EquipmentSlot.HEAD);
            itemStackArray2[1] = this.getItem(EquipmentSlot.CHEST);
            itemStackArray2[2] = this.getItem(EquipmentSlot.LEGS);
            itemStackArray = itemStackArray2;
            itemStackArray2[3] = this.getItem(EquipmentSlot.FEET);
        } else {
            ItemStack[] itemStackArray3 = new ItemStack[4];
            itemStackArray3[0] = this.getItem(EquipmentSlot.FEET);
            itemStackArray3[1] = this.getItem(EquipmentSlot.LEGS);
            itemStackArray3[2] = this.getItem(EquipmentSlot.CHEST);
            itemStackArray = itemStackArray3;
            itemStackArray3[3] = this.getItem(EquipmentSlot.HEAD);
        }
        for (ItemStack stack : armor = itemStackArray) {
            if (!stack.isEmpty()) continue;
            ++emptySlots;
        }
        if (this.background.get().booleanValue() && emptySlots < 4) {
            renderer.quad(this.x, this.y, this.getWidth(), this.getHeight(), this.backgroundColor.get());
        }
        renderer.post(() -> {
            double x = this.x;
            double y = this.y;
            for (int position = 0; position < 4; ++position) {
                double armorY;
                double armorX;
                ItemStack itemStack = armor[position];
                if (this.orientation.get() == Orientation.Vertical) {
                    armorX = x;
                    armorY = y + (double)((float)(position * 18) * this.getScale());
                } else {
                    armorX = x + (double)((float)(position * 18) * this.getScale());
                    armorY = y;
                }
                renderer.item(itemStack, (int)armorX, (int)armorY, this.getScale(), itemStack.isDamageableItem() && this.durability.get() == Durability.Bar);
                if (!itemStack.isDamageableItem() || this.durability.get() == Durability.Bar || this.durability.get() == Durability.None) continue;
                String message = switch (this.durability.get().ordinal()) {
                    case 2 -> Integer.toString(itemStack.getMaxDamage() - itemStack.getDamageValue());
                    case 3 -> Integer.toString(Math.round((float)(itemStack.getMaxDamage() - itemStack.getDamageValue()) * 100.0f / (float)itemStack.getMaxDamage()));
                    default -> "err";
                };
                double messageWidth = renderer.textWidth(message);
                if (this.orientation.get() == Orientation.Vertical) {
                    armorX = x + (double)(8.0f * this.getScale()) - messageWidth / 2.0;
                    armorY = y + (double)((float)(18 * position) * this.getScale()) + ((double)(18.0f * this.getScale()) - renderer.textHeight());
                } else {
                    armorX = x + (double)((float)(18 * position) * this.getScale()) + (double)(8.0f * this.getScale()) - messageWidth / 2.0;
                    armorY = y + ((double)this.getHeight() - renderer.textHeight());
                }
                TextRenderer.get().render(message, armorX, armorY, this.durabilityColor.get(), this.durabilityShadow.get());
            }
        });
    }

    private ItemStack getItem(EquipmentSlot slot) {
        if (this.isInEditor()) {
            return switch (slot.getIndex()) {
                case 3 -> DisplayItemUtils.toStack(Items.NETHERITE_HELMET);
                case 2 -> DisplayItemUtils.toStack(Items.NETHERITE_CHESTPLATE);
                case 1 -> DisplayItemUtils.toStack(Items.NETHERITE_LEGGINGS);
                default -> DisplayItemUtils.toStack(Items.NETHERITE_BOOTS);
            };
        }
        ItemStack stack = MeteorClient.mc.player.getItemBySlot(slot);
        return stack.isEmpty() && this.showEmpty.get() != false ? DisplayItemUtils.toStack(Items.BARRIER) : stack;
    }

    private float getScale() {
        return this.customScale.get() != false ? this.scale.get().floatValue() : this.scale.getDefaultValue().floatValue();
    }

    public static enum Orientation {
        Horizontal,
        Vertical;

    }

    public static enum Durability {
        None,
        Bar,
        Total,
        Percentage;

    }
}
