package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.DisplayItemUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemHud
extends HudElement {
    public static final HudElementInfo<ItemHud> INFO = new HudElementInfo<ItemHud>(Hud.GROUP, "item", "Displays the item count.", ItemHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgScale;
    private final SettingGroup sgBackground;
    private final Setting<Item> item;
    private final Setting<NoneMode> noneMode;
    public final Setting<Boolean> customScale;
    public final Setting<Double> scale;
    public final Setting<Boolean> background;
    public final Setting<SettingColor> backgroundColor;

    private ItemHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgScale = this.settings.createGroup("Scale");
        this.sgBackground = this.settings.createGroup("Background");
        this.item = this.sgGeneral.add(((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("item")).description("Item to display")).defaultValue(Items.TOTEM_OF_UNDYING)).build());
        this.noneMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("none-mode")).description("How to render the item when you don't have the specified item in your inventory.")).defaultValue(NoneMode.HideCount)).build());
        this.customScale = this.sgScale.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-scale")).description("Applies a custom scale to this hud element.")).defaultValue(false)).onChanged(bl -> this.calculateSize())).build());
        this.scale = this.sgScale.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("Custom scale.")).visible(this.customScale::get)).defaultValue(2.0).onChanged(d -> this.calculateSize())).min(0.5).sliderRange(0.5, 3.0).build());
        this.background = this.sgBackground.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("background")).description("Displays background.")).defaultValue(false)).build());
        this.backgroundColor = this.sgBackground.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color used for the background.")).visible(this.background::get)).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        this.calculateSize();
    }

    private void calculateSize() {
        this.setSize(17.0f * this.getScale(), 17.0f * this.getScale());
    }

    @Override
    public void render(HudRenderer renderer) {
        ItemStack itemStack = DisplayItemUtils.toStack(this.item.get(), InvUtils.find(this.item.get()).count());
        if (this.noneMode.get() == NoneMode.HideItem && itemStack.isEmpty()) {
            if (this.isInEditor()) {
                renderer.line(this.x, this.y, this.x + this.getWidth(), this.y + this.getHeight(), Color.GRAY);
                renderer.line(this.x, this.y + this.getHeight(), this.x + this.getWidth(), this.y, Color.GRAY);
            }
        } else {
            renderer.post(() -> this.render(renderer, itemStack, this.x, this.y));
        }
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, this.getWidth(), this.getHeight(), this.backgroundColor.get());
        }
    }

    private void render(HudRenderer renderer, ItemStack itemStack, int x, int y) {
        if (this.noneMode.get() == NoneMode.HideItem) {
            renderer.item(itemStack, x, y, this.getScale(), true);
            return;
        }
        String countOverride = null;
        boolean resetToZero = false;
        if (itemStack.isEmpty()) {
            if (this.noneMode.get() == NoneMode.ShowCount) {
                countOverride = "0";
            }
            itemStack.setCount(1);
            resetToZero = true;
        }
        renderer.item(itemStack, x, y, this.getScale(), true, countOverride);
        if (resetToZero) {
            itemStack.setCount(0);
        }
    }

    private float getScale() {
        return this.customScale.get() != false ? this.scale.get().floatValue() : this.scale.getDefaultValue().floatValue();
    }

    public static enum NoneMode {
        HideItem,
        HideCount,
        ShowCount;


        public String toString() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> "Hide Item";
                case 1 -> "Hide Count";
                case 2 -> "Show Count";
            };
        }
    }
}
