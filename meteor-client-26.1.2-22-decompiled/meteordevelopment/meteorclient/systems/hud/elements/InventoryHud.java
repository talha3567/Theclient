package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.MeteorClient;
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
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.DisplayItemUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class InventoryHud
extends HudElement {
    public static final HudElementInfo<InventoryHud> INFO = new HudElementInfo<InventoryHud>(Hud.GROUP, "inventory", "Displays your inventory.", InventoryHud::new);
    private static final Identifier TEXTURE = MeteorClient.identifier("textures/container.png");
    private static final Identifier TEXTURE_TRANSPARENT = MeteorClient.identifier("textures/container-transparent.png");
    private final SettingGroup sgGeneral;
    private final SettingGroup sgScale;
    private final SettingGroup sgBackground;
    private final Setting<Boolean> containers;
    public final Setting<Boolean> customScale;
    public final Setting<Double> scale;
    private final Setting<Background> background;
    public final Setting<SettingColor> backgroundColor;
    private static final ItemStack[] PREVIEW_ITEMS = new ItemStack[]{DisplayItemUtils.toStack(Items.OBSIDIAN, 64), DisplayItemUtils.toStack(Items.END_CRYSTAL, 64), DisplayItemUtils.toStack(Items.GOLDEN_APPLE, 64)};
    private final ItemStack[] containerItems;

    private InventoryHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgScale = this.settings.createGroup("Scale");
        this.sgBackground = this.settings.createGroup("Background");
        this.containers = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("containers")).description("Shows the contents of a container when holding them.")).defaultValue(false)).build());
        this.customScale = this.sgScale.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-scale")).description("Applies a custom scale to this hud element.")).defaultValue(false)).onChanged(bl -> this.calculateSize())).build());
        this.scale = this.sgScale.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("Custom scale.")).visible(this.customScale::get)).defaultValue(2.0).onChanged(d -> this.calculateSize())).min(0.5).sliderRange(0.5, 3.0).build());
        this.background = this.sgBackground.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("background")).description("Background of inventory viewer.")).defaultValue(Background.Texture)).onChanged(background -> this.calculateSize())).build());
        this.backgroundColor = this.sgBackground.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color used for the background.")).visible(() -> this.background.get() != Background.None)).defaultValue(new SettingColor(255, 255, 255)).build());
        this.containerItems = new ItemStack[27];
        this.calculateSize();
    }

    @Override
    public void render(HudRenderer renderer) {
        Color drawColor;
        boolean hasContainer;
        double x = this.x;
        double y = this.y;
        ItemStack container = this.getContainer();
        boolean bl = hasContainer = this.containers.get() != false && container != null;
        if (hasContainer) {
            Utils.getItemsInContainerItem(container, this.containerItems);
        }
        Color color = drawColor = hasContainer ? Utils.getShulkerColor(container) : (Color)this.backgroundColor.get();
        if (this.background.get() != Background.None) {
            this.drawBackground(renderer, (int)x, (int)y, drawColor);
        }
        renderer.post(() -> {
            for (int row = 0; row < 3; ++row) {
                for (int i = 0; i < 9; ++i) {
                    ItemStack stack;
                    int index = row * 9 + i;
                    if (MeteorClient.mc.player == null) {
                        stack = index < PREVIEW_ITEMS.length ? PREVIEW_ITEMS[index] : null;
                    } else {
                        ItemStack itemStack = stack = hasContainer ? this.containerItems[index] : MeteorClient.mc.player.getInventory().getItem(index + 9);
                    }
                    if (stack == null) continue;
                    int itemX = this.background.get() == Background.Texture ? (int)(x + (double)(8 + i * 18) * this.getScale()) : (int)(x + (double)(1 + i * 18) * this.getScale());
                    int itemY = this.background.get() == Background.Texture ? (int)(y + (double)(7 + row * 18) * this.getScale()) : (int)(y + (double)(1 + row * 18) * this.getScale());
                    renderer.item(stack, itemX, itemY, (float)this.getScale(), true);
                }
            }
        });
    }

    private void calculateSize() {
        this.setSize((double)this.background.get().width * this.getScale(), (double)this.background.get().height * this.getScale());
    }

    private void drawBackground(HudRenderer renderer, int x, int y, Color color) {
        int w = this.getWidth();
        int h = this.getHeight();
        switch (this.background.get().ordinal()) {
            case 1: 
            case 2: {
                renderer.texture(this.background.get() == Background.Texture ? TEXTURE : TEXTURE_TRANSPARENT, x, y, w, h, color);
                break;
            }
            case 3: {
                renderer.quad(x, y, w, h, color);
            }
        }
    }

    private ItemStack getContainer() {
        if (this.isInEditor() || MeteorClient.mc.player == null) {
            return null;
        }
        ItemStack stack = MeteorClient.mc.player.getOffhandItem();
        if (Utils.hasItems(stack) || stack.getItem() == Items.ENDER_CHEST) {
            return stack;
        }
        stack = MeteorClient.mc.player.getMainHandItem();
        if (Utils.hasItems(stack) || stack.getItem() == Items.ENDER_CHEST) {
            return stack;
        }
        return null;
    }

    private double getScale() {
        return this.customScale.get() != false ? this.scale.get() : this.scale.getDefaultValue();
    }

    public static enum Background {
        None(162, 54),
        Texture(176, 67),
        Outline(162, 54),
        Flat(162, 54);

        private final int width;
        private final int height;

        private Background(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
