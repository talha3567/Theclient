package meteordevelopment.meteorclient.systems.modules.render;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.DataOutput;
import java.lang.runtime.SwitchBootstraps;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.events.game.ItemStackTooltipEvent;
import meteordevelopment.meteorclient.events.render.TooltipDataEvent;
import meteordevelopment.meteorclient.gui.screens.ContainerInventoryScreen;
import meteordevelopment.meteorclient.mixin.EntityAccessor;
import meteordevelopment.meteorclient.mixin.MobBucketItemAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.ByteCountDataOutput;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.EChestMemory;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.tooltip.BannerTooltipComponent;
import meteordevelopment.meteorclient.utils.tooltip.BookTooltipComponent;
import meteordevelopment.meteorclient.utils.tooltip.BundleTooltipComponent;
import meteordevelopment.meteorclient.utils.tooltip.ContainerTooltipComponent;
import meteordevelopment.meteorclient.utils.tooltip.EntityTooltipComponent;
import meteordevelopment.meteorclient.utils.tooltip.MapTooltipComponent;
import meteordevelopment.meteorclient.utils.tooltip.TextTooltipComponent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.saveddata.maps.MapId;

public class BetterTooltips
extends Module {
    public static final Color ECHEST_COLOR = new Color(0, 50, 50);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPreviews;
    private final SettingGroup sgOther;
    private final SettingGroup sgHideFlags;
    private final Setting<DisplayWhen> displayWhen;
    private final Setting<Keybind> keybind;
    private final Setting<Boolean> openContents;
    private final Setting<Keybind> openContentsKey;
    private final Setting<Boolean> pauseInCreative;
    private final Setting<Boolean> shulkers;
    private final Setting<Boolean> shulkerCompactTooltip;
    private final Setting<Boolean> echest;
    private final Setting<Boolean> maps;
    public final Setting<Double> mapsScale;
    private final Setting<Boolean> books;
    private final Setting<Boolean> banners;
    private final Setting<Boolean> entitiesInBuckets;
    private final Setting<Boolean> bundles;
    private final Setting<Boolean> foodInfo;
    public final Setting<Boolean> byteSize;
    private final Setting<SortSize> sizeType;
    private final Setting<Boolean> statusEffects;
    public final Setting<Boolean> tooltip;
    public final Setting<Boolean> additional;
    private boolean updateTooltips;
    private static final ItemStack[] PREVIEW = new ItemStack[27];
    private static final ItemStack[] PEEK_SCREEN = new ItemStack[27];

    public BetterTooltips() {
        super(Categories.Render, "better-tooltips", "Displays more useful tooltips for certain items.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPreviews = this.settings.createGroup("Previews");
        this.sgOther = this.settings.createGroup("Other");
        this.sgHideFlags = this.settings.createGroup("Hide Flags");
        this.displayWhen = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("display-when")).description("When to display previews.")).defaultValue(DisplayWhen.Keybind)).onChanged(displayWhen -> {
            this.updateTooltips = true;
        })).build());
        this.keybind = this.sgGeneral.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("keybind")).description("The bind for keybind mode.")).defaultValue(Keybind.fromKey(342))).visible(() -> this.displayWhen.get() == DisplayWhen.Keybind)).onChanged(keybind -> {
            this.updateTooltips = true;
        })).build());
        this.openContents = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("open-contents")).description("Opens a GUI window with the inventory of the storage block or book when you click the item.")).defaultValue(true)).build());
        this.openContentsKey = this.sgGeneral.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("keybind")).description("Key to open contents (containers, books, etc.) when pressed on items.")).defaultValue(Keybind.fromButton(2))).visible(this.openContents::get)).build());
        this.pauseInCreative = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-in-creative")).description("Pauses middle click open while the player is in creative mode.")).defaultValue(true)).visible(this.openContents::get)).build());
        this.shulkers = this.sgPreviews.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("containers")).description("Shows a preview of a containers when hovering over it in an inventory.")).defaultValue(true)).onChanged(bl -> {
            this.updateTooltips = true;
        })).build());
        this.shulkerCompactTooltip = this.sgPreviews.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("compact-shulker-tooltip")).description("Compacts the lines of the shulker tooltip.")).defaultValue(true)).build());
        this.echest = this.sgPreviews.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("echests")).description("Shows a preview of your echest when hovering over it in an inventory.")).defaultValue(true)).onChanged(bl -> {
            this.updateTooltips = true;
        })).build());
        this.maps = this.sgPreviews.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("maps")).description("Shows a preview of a map when hovering over it in an inventory.")).defaultValue(true)).onChanged(bl -> {
            this.updateTooltips = true;
        })).build());
        this.mapsScale = this.sgPreviews.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("map-scale")).description("The scale of the map preview.")).defaultValue(1.0).min(0.001).sliderMax(1.0).visible(this.maps::get)).build());
        this.books = this.sgPreviews.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("books")).description("Shows contents of a book when hovering over it in an inventory.")).defaultValue(true)).onChanged(bl -> {
            this.updateTooltips = true;
        })).build());
        this.banners = this.sgPreviews.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("banners")).description("Shows banners' patterns when hovering over it in an inventory. Also works with shields.")).defaultValue(true)).onChanged(bl -> {
            this.updateTooltips = true;
        })).build());
        this.entitiesInBuckets = this.sgPreviews.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("entities-in-buckets")).description("Shows entities in buckets when hovering over it in an inventory.")).defaultValue(true)).onChanged(bl -> {
            this.updateTooltips = true;
        })).build());
        this.bundles = this.sgPreviews.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("bundles")).description("Shows a preview of bundle contents when hovering over it in an inventory.")).defaultValue(true)).onChanged(bl -> {
            this.updateTooltips = true;
        })).build());
        this.foodInfo = this.sgPreviews.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("food-info")).description("Shows hunger and saturation values for food items.")).defaultValue(true)).onChanged(bl -> {
            this.updateTooltips = true;
        })).build());
        this.byteSize = this.sgOther.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("byte-size")).description("Displays an item's size in bytes in the tooltip.")).defaultValue(true)).onChanged(bl -> {
            this.updateTooltips = true;
        })).build());
        this.sizeType = this.sgOther.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("byte-size-format")).description("The format by which to display the item's byte size.")).defaultValue(SortSize.Dynamic)).visible(this.byteSize::get)).build());
        this.statusEffects = this.sgOther.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("status-effects")).description("Adds list of status effects to tooltips of food items.")).defaultValue(true)).onChanged(bl -> {
            this.updateTooltips = true;
        })).build());
        this.tooltip = this.sgHideFlags.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("tooltip")).description("Show the tooltip when it's hidden.")).defaultValue(false)).build());
        this.additional = this.sgHideFlags.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("tooltip-components")).description("Shows tooltip components when they're hidden - e.g. enchantments, attributes, lore, etc.")).defaultValue(false)).build());
        this.updateTooltips = false;
    }

    @EventHandler
    private void appendTooltip(ItemStackTooltipEvent event) {
        if (!this.tooltip.get().booleanValue() && event.list().isEmpty()) {
            this.appendPreviewTooltipText(event, false);
            return;
        }
        if (this.statusEffects.get().booleanValue()) {
            if (event.itemStack().getItem() == Items.SUSPICIOUS_STEW) {
                SuspiciousStewEffects stewEffectsComponent = (SuspiciousStewEffects)event.itemStack().get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
                if (stewEffectsComponent != null) {
                    for (SuspiciousStewEffects.Entry effectTag : stewEffectsComponent.effects()) {
                        MobEffectInstance effect2 = new MobEffectInstance(effectTag.effect(), effectTag.duration(), 0);
                        event.appendStart((Component)this.getStatusText(effect2));
                    }
                }
            } else {
                Consumable consumable = (Consumable)event.itemStack().get(DataComponents.CONSUMABLE);
                if (consumable != null) {
                    consumable.onConsumeEffects().stream().filter(ApplyStatusEffectsConsumeEffect.class::isInstance).map(ApplyStatusEffectsConsumeEffect.class::cast).flatMap(apply -> apply.effects().stream()).forEach(effect -> event.appendStart((Component)this.getStatusText((MobEffectInstance)effect)));
                }
            }
        }
        if (this.foodInfo.get().booleanValue() && event.itemStack().has(DataComponents.FOOD)) {
            FoodProperties food = (FoodProperties)event.itemStack().get(DataComponents.FOOD);
            event.appendStart((Component)Component.literal((String)String.format("\ud83c\udf56 %d (\ud83d\udc9b %.1f)", food.nutrition(), Float.valueOf(food.saturation()))).withStyle(ChatFormatting.GRAY));
        }
        if (this.byteSize.get().booleanValue()) {
            DataResult dataResult = ItemStack.CODEC.encodeStart((DynamicOps)this.mc.player.registryAccess().createSerializationContext((DynamicOps)NbtOps.INSTANCE), (Object)event.itemStack());
            Objects.requireNonNull(dataResult);
            DataResult dataResult2 = dataResult;
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DataResult.Success.class, DataResult.Error.class}, (DataResult)dataResult2, n)) {
                case 0: {
                    DataResult.Success success = (DataResult.Success)dataResult2;
                    try {
                        ((Tag)success.value()).write((DataOutput)ByteCountDataOutput.INSTANCE);
                        int byteCount = ByteCountDataOutput.INSTANCE.getCount();
                        String count = switch (this.sizeType.get().ordinal()) {
                            default -> throw new MatchException(null, null);
                            case 0 -> String.format("%d bytes", byteCount);
                            case 1 -> String.format("%.2f kB", Float.valueOf((float)byteCount / 1024.0f));
                            case 2 -> String.format("%.4f MB", Float.valueOf((float)byteCount / 1048576.0f));
                            case 3 -> byteCount >= 0x100000 ? String.format("%.2f MB", Float.valueOf((float)byteCount / 1048576.0f)) : (byteCount >= 1024 ? String.format("%.2f kB", Float.valueOf((float)byteCount / 1024.0f)) : String.format("%d bytes", byteCount));
                        };
                        ByteCountDataOutput.INSTANCE.reset();
                        event.appendEnd((Component)Component.literal((String)count).withStyle(ChatFormatting.DARK_GRAY));
                    }
                    catch (Exception exception) {
                        event.appendEnd((Component)Component.literal((String)"Error getting bytes.").withStyle(ChatFormatting.RED));
                    }
                    break;
                }
                case 1: {
                    event.appendEnd((Component)Component.literal((String)"Error getting bytes.").withStyle(ChatFormatting.RED));
                    break;
                }
                default: {
                    throw new MatchException(null, null);
                }
            }
        }
        this.appendPreviewTooltipText(event, true);
    }

    /*
     * Enabled aggressive block sorting
     */
    @EventHandler
    private void getTooltipData(TooltipDataEvent event) {
        if (this.previewShulkers() && Utils.hasItems(event.itemStack)) {
            Utils.getItemsInContainerItem(event.itemStack, PREVIEW);
            event.tooltipData = new ContainerTooltipComponent(PREVIEW, Utils.getShulkerColor(event.itemStack));
            return;
        }
        if (event.itemStack.getItem() == Items.ENDER_CHEST && this.previewEChest()) {
            event.tooltipData = EChestMemory.isKnown() ? new ContainerTooltipComponent((ItemStack[])EChestMemory.ITEMS.toArray(ItemStack[]::new), ECHEST_COLOR) : new TextTooltipComponent((Component)Component.literal((String)"Unknown inventory.").withStyle(ChatFormatting.DARK_RED));
            return;
        }
        if (event.itemStack.getItem() == Items.FILLED_MAP && this.previewMaps()) {
            MapId mapIdComponent = (MapId)event.itemStack.get(DataComponents.MAP_ID);
            if (mapIdComponent == null) return;
            event.tooltipData = new MapTooltipComponent(mapIdComponent.id());
            return;
        }
        if ((event.itemStack.getItem() == Items.WRITABLE_BOOK || event.itemStack.getItem() == Items.WRITTEN_BOOK) && this.previewBooks()) {
            Component page = this.getFirstPage(event.itemStack);
            if (page == null) return;
            int pageCount = this.getBookPageCount(event.itemStack);
            MutableComponent pageWithCount = page.copy().append((Component)Component.literal((String)String.format(" (%d pages)", pageCount)).withStyle(ChatFormatting.GRAY));
            event.tooltipData = new BookTooltipComponent((Component)pageWithCount);
            return;
        }
        if (event.itemStack.getItem() instanceof BannerItem && this.previewBanners()) {
            event.tooltipData = new BannerTooltipComponent(event.itemStack);
            return;
        }
        if (event.itemStack.has(DataComponents.PROVIDES_BANNER_PATTERNS) && this.previewBanners()) {
            event.tooltipData = this.createBannerFromBannerPatternItem(event.itemStack);
            return;
        }
        if (event.itemStack.getItem() == Items.SHIELD && this.previewBanners()) {
            if (((BannerPatternLayers)event.itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, (Object)BannerPatternLayers.EMPTY)).layers().isEmpty()) return;
            event.tooltipData = this.createBannerFromShield(event.itemStack);
            return;
        }
        Item page = event.itemStack.getItem();
        if (page instanceof MobBucketItem) {
            MobBucketItem bucketItem = (MobBucketItem)page;
            if (this.previewEntities()) {
                EntityType<?> type = ((MobBucketItemAccessor)bucketItem).meteor$getType();
                LivingEntity entity = (LivingEntity)type.create((Level)this.mc.level, EntitySpawnReason.NATURAL);
                if (entity == null) return;
                CustomData nbtComponent = (CustomData)event.itemStack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, null);
                if (nbtComponent == null) {
                    return;
                }
                entity.applyComponentsFromItemStack(event.itemStack);
                ((Bucketable)entity).loadFromBucketTag(nbtComponent.copyTag());
                ((EntityAccessor)entity).meteor$setInWater(true);
                event.tooltipData = new EntityTooltipComponent(entity);
                return;
            }
        }
        if (!(event.itemStack.getItem() instanceof BundleItem)) return;
        if (!this.previewBundles()) return;
        if (!event.itemStack.has(DataComponents.BUNDLE_CONTENTS)) return;
        BundleContents bundleContents = (BundleContents)event.itemStack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents == null) return;
        if (bundleContents.isEmpty()) return;
        ItemStack[] bundleItems = new ItemStack[bundleContents.size()];
        int index = 0;
        Iterator iterator = bundleContents.items().iterator();
        while (true) {
            if (!iterator.hasNext()) {
                event.tooltipData = new BundleTooltipComponent(bundleItems, bundleContents);
                return;
            }
            ItemStackTemplate template = (ItemStackTemplate)iterator.next();
            bundleItems[index++] = template.create();
        }
    }

    public void applyCompactShulkerTooltip(List<Optional<ItemStackTemplate>> stacks, Consumer<Component> textConsumer) {
        Object2IntOpenHashMap counts = new Object2IntOpenHashMap();
        for (Optional<ItemStackTemplate> opt : stacks) {
            if (opt.isEmpty()) continue;
            Item stackItem = (Item)opt.get().item().value();
            int stackCount = opt.get().count();
            if (stackCount == 0) continue;
            int count = counts.getInt((Object)stackItem);
            counts.put((Object)stackItem, count + stackCount);
        }
        counts.keySet().stream().sorted(Comparator.comparingInt(arg_0 -> BetterTooltips.lambda$applyCompactShulkerTooltip$0((Object2IntMap)counts, arg_0))).limit(5L).forEach(arg_0 -> BetterTooltips.lambda$applyCompactShulkerTooltip$1((Object2IntMap)counts, textConsumer, arg_0));
        if (counts.size() > 5) {
            textConsumer.accept((Component)Component.translatable((String)"item.container.more_items", (Object[])new Object[]{counts.size() - 5}).withStyle(ChatFormatting.ITALIC));
        }
    }

    private void appendPreviewTooltipText(ItemStackTooltipEvent event, boolean spacer) {
        boolean showPreviewText;
        boolean bl = showPreviewText = !this.isPressed() && (this.shulkers.get() != false && Utils.hasItems(event.itemStack()) || event.itemStack().getItem() == Items.ENDER_CHEST && this.echest.get() != false || event.itemStack().getItem() == Items.FILLED_MAP && this.maps.get() != false || event.itemStack().getItem() == Items.WRITABLE_BOOK && this.books.get() != false || event.itemStack().getItem() == Items.WRITTEN_BOOK && this.books.get() != false || event.itemStack().getItem() instanceof MobBucketItem && this.entitiesInBuckets.get() != false || event.itemStack().getItem() instanceof BundleItem && this.bundles.get() != false || event.itemStack().getItem() instanceof BannerItem && this.banners.get() != false || event.itemStack().has(DataComponents.PROVIDES_BANNER_PATTERNS) && this.banners.get() != false || event.itemStack().getItem() == Items.SHIELD && this.banners.get() != false);
        if (showPreviewText) {
            if (spacer) {
                event.appendEnd((Component)Component.literal((String)""));
            }
            event.appendEnd((Component)Component.literal((String)("Hold " + String.valueOf(ChatFormatting.YELLOW) + String.valueOf(this.keybind) + String.valueOf(ChatFormatting.RESET) + " to preview")));
        }
    }

    private MutableComponent getStatusText(MobEffectInstance effect) {
        MutableComponent text = Component.translatable((String)effect.getDescriptionId());
        if (effect.getAmplifier() != 0) {
            text.append(String.format(" %d (%s)", effect.getAmplifier() + 1, MobEffectUtil.formatDuration((MobEffectInstance)effect, (float)1.0f, (float)this.mc.level.tickRateManager().tickrate()).getString()));
        } else {
            text.append(String.format(" (%s)", MobEffectUtil.formatDuration((MobEffectInstance)effect, (float)1.0f, (float)this.mc.level.tickRateManager().tickrate()).getString()));
        }
        if (((MobEffect)effect.getEffect().value()).isBeneficial()) {
            return text.withStyle(ChatFormatting.BLUE);
        }
        return text.withStyle(ChatFormatting.RED);
    }

    private Component getFirstPage(ItemStack bookItem) {
        if (bookItem.get(DataComponents.WRITABLE_BOOK_CONTENT) != null) {
            List pages = ((WritableBookContent)bookItem.get(DataComponents.WRITABLE_BOOK_CONTENT)).pages();
            if (pages.isEmpty()) {
                return null;
            }
            return Component.literal((String)((String)((Filterable)pages.getFirst()).get(false)));
        }
        if (bookItem.get(DataComponents.WRITTEN_BOOK_CONTENT) != null) {
            List pages = ((WrittenBookContent)bookItem.get(DataComponents.WRITTEN_BOOK_CONTENT)).pages();
            if (pages.isEmpty()) {
                return null;
            }
            return (Component)((Filterable)pages.getFirst()).get(false);
        }
        return null;
    }

    private int getBookPageCount(ItemStack bookItem) {
        if (bookItem.get(DataComponents.WRITABLE_BOOK_CONTENT) != null) {
            return ((WritableBookContent)bookItem.get(DataComponents.WRITABLE_BOOK_CONTENT)).pages().size();
        }
        if (bookItem.get(DataComponents.WRITTEN_BOOK_CONTENT) != null) {
            return ((WrittenBookContent)bookItem.get(DataComponents.WRITTEN_BOOK_CONTENT)).pages().size();
        }
        return 0;
    }

    private BannerTooltipComponent createBannerFromBannerPatternItem(ItemStack item) {
        HolderSet providedPatterns = (HolderSet)item.get(DataComponents.PROVIDES_BANNER_PATTERNS);
        if (providedPatterns == null || providedPatterns.size() == 0) {
            return new BannerTooltipComponent(DyeColor.GRAY, BannerPatternLayers.EMPTY);
        }
        BannerPatternLayers component = new BannerPatternLayers.Builder().add(providedPatterns.get(0), DyeColor.WHITE).build();
        return new BannerTooltipComponent(DyeColor.GRAY, component);
    }

    private BannerTooltipComponent createBannerFromShield(ItemStack shieldItem) {
        DyeColor dyeColor2 = (DyeColor)shieldItem.getOrDefault(DataComponents.BASE_COLOR, (Object)DyeColor.WHITE);
        BannerPatternLayers bannerPatternsComponent = (BannerPatternLayers)shieldItem.getOrDefault(DataComponents.BANNER_PATTERNS, (Object)BannerPatternLayers.EMPTY);
        return new BannerTooltipComponent(dyeColor2, bannerPatternsComponent);
    }

    public boolean openContents() {
        return this.isActive() && this.openContents.get() != false && (this.pauseInCreative.get() == false || !this.mc.player.hasInfiniteMaterials());
    }

    public boolean shouldOpenContents(InputWithModifiers input) {
        if (input instanceof MouseButtonEvent) {
            MouseButtonEvent click = (MouseButtonEvent)input;
            return this.openContents() && this.openContentsKey.get().matches(click.buttonInfo());
        }
        if (input instanceof KeyEvent) {
            KeyEvent keyInput = (KeyEvent)input;
            return this.openContents() && this.openContentsKey.get().matches(keyInput);
        }
        return false;
    }

    public boolean openContent(ItemStack itemStack) {
        if (!this.openContents() || itemStack.isEmpty()) {
            return false;
        }
        if (itemStack.getItem() instanceof BundleItem) {
            if (this.mc.screen instanceof AbstractContainerScreen) {
                this.mc.screen.onClose();
            }
            this.mc.setScreen((Screen)new ContainerInventoryScreen(itemStack));
            return true;
        }
        if (Utils.hasItems(itemStack) || itemStack.getItem() == Items.ENDER_CHEST) {
            Utils.openContainer(itemStack, PEEK_SCREEN, false);
            return true;
        }
        if (itemStack.getItem() == Items.WRITABLE_BOOK || itemStack.getItem() == Items.WRITTEN_BOOK) {
            if (this.mc.screen instanceof AbstractContainerScreen) {
                this.mc.screen.onClose();
            }
            this.mc.setScreen((Screen)new BookViewScreen(BookViewScreen.BookAccess.fromItem((ItemStack)itemStack)));
            return true;
        }
        return false;
    }

    public boolean previewShulkers() {
        return this.isActive() && this.isPressed() && this.shulkers.get() != false;
    }

    public boolean shulkerCompactTooltip() {
        return this.isActive() && this.shulkerCompactTooltip.get() != false;
    }

    private boolean previewEChest() {
        return this.isPressed() && this.echest.get() != false;
    }

    private boolean previewMaps() {
        return this.isPressed() && this.maps.get() != false;
    }

    private boolean previewBooks() {
        return this.isPressed() && this.books.get() != false;
    }

    private boolean previewBanners() {
        return this.isPressed() && this.banners.get() != false;
    }

    private boolean previewEntities() {
        return this.isPressed() && this.entitiesInBuckets.get() != false;
    }

    public boolean previewBundles() {
        return this.isPressed() && this.bundles.get() != false;
    }

    private boolean isPressed() {
        return this.keybind.get().isPressed() && this.displayWhen.get() == DisplayWhen.Keybind || this.displayWhen.get() == DisplayWhen.Always;
    }

    public boolean updateTooltips() {
        if (this.updateTooltips && this.isActive()) {
            this.updateTooltips = false;
            return true;
        }
        return false;
    }

    private static /* synthetic */ void lambda$applyCompactShulkerTooltip$1(Object2IntMap counts, Consumer textConsumer, Item item) {
        MutableComponent mutableText = ((Component)item.components().get(DataComponents.ITEM_NAME)).plainCopy();
        mutableText.append((Component)Component.literal((String)" x").append(String.valueOf(counts.getInt((Object)item))).withStyle(ChatFormatting.GRAY));
        textConsumer.accept(mutableText);
    }

    private static /* synthetic */ int lambda$applyCompactShulkerTooltip$0(Object2IntMap counts, Item value) {
        return -counts.getInt((Object)value);
    }

    public static enum DisplayWhen {
        Keybind,
        Always;

    }

    public static enum SortSize {
        Bytes,
        Kilobytes,
        Megabytes,
        Dynamic;

    }
}
