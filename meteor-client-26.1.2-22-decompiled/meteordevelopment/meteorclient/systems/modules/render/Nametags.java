package meteordevelopment.meteorclient.systems.modules.render;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnchantmentListSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.NameProtect;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class Nametags
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPlayers;
    private final SettingGroup sgItems;
    private final SettingGroup sgRender;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<Double> scale;
    private final Setting<Boolean> ignoreSelf;
    private final Setting<Boolean> ignoreFriends;
    private final Setting<Boolean> ignoreBots;
    private final Setting<Boolean> culling;
    private final Setting<Double> maxCullRange;
    private final Setting<Integer> maxCullCount;
    private final Setting<Boolean> displayHealth;
    private final Setting<Boolean> displayPrefix;
    private final Setting<Boolean> displayGameMode;
    private final Setting<Boolean> displayDistance;
    private final Setting<Boolean> displayPing;
    private final Setting<Boolean> displayItems;
    private final Setting<Double> itemSpacing;
    private final Setting<Boolean> ignoreEmpty;
    private final Setting<Durability> itemDurability;
    private final Setting<Boolean> displayEnchants;
    private final Setting<Set<ResourceKey<Enchantment>>> shownEnchantments;
    private final Setting<Position> enchantPos;
    private final Setting<Integer> enchantLength;
    private final Setting<Double> enchantTextScale;
    private final Setting<Boolean> itemCount;
    private final Setting<SettingColor> background;
    private final Setting<SettingColor> nameColor;
    private final Setting<SettingColor> pingColor;
    private final Setting<SettingColor> gamemodeColor;
    private final Setting<DistanceColorMode> distanceColorMode;
    private final Setting<SettingColor> distanceColor;
    private final Color WHITE;
    private final Color RED;
    private final Color AMBER;
    private final Color GREEN;
    private final Color GOLD;
    private final Vector3d pos;
    private final double[] itemWidths;
    private final List<Entity> entityList;

    public Nametags() {
        super(Categories.Render, "nametags", "Displays customizable nametags above players, items and other entities.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPlayers = this.settings.createGroup("Players");
        this.sgItems = this.settings.createGroup("Items");
        this.sgRender = this.settings.createGroup("Render");
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Select entities to draw nametags on.")).defaultValue(EntityType.PLAYER, EntityType.ITEM).build());
        this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("The scale of the nametag.")).defaultValue(1.1).min(0.1).build());
        this.ignoreSelf = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-self")).description("Ignore yourself when in third person or freecam.")).defaultValue(true)).build());
        this.ignoreFriends = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-friends")).description("Ignore rendering nametags for friends.")).defaultValue(false)).build());
        this.ignoreBots = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-bots")).description("Only render non-bot nametags.")).defaultValue(true)).build());
        this.culling = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("culling")).description("Only render a certain number of nametags at a certain distance.")).defaultValue(false)).build());
        this.maxCullRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("culling-range")).description("Only render nametags within this distance of your player.")).defaultValue(20.0).min(0.0).sliderMax(200.0).visible(this.culling::get)).build());
        this.maxCullCount = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("culling-count")).description("Only render this many nametags.")).defaultValue(50)).min(1).sliderRange(1, 100).visible(this.culling::get)).build());
        this.displayHealth = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("health")).description("Shows the player's health.")).defaultValue(true)).build());
        this.displayPrefix = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("use-display-name")).description("Uses the players server display name instead of their account name.")).defaultValue(false)).build());
        this.displayGameMode = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("gamemode")).description("Shows the player's GameMode.")).defaultValue(false)).build());
        this.displayDistance = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("distance")).description("Shows the distance between you and the player.")).defaultValue(false)).build());
        this.displayPing = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ping")).description("Shows the player's ping.")).defaultValue(true)).build());
        this.displayItems = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("items")).description("Displays armor and hand items above the name tags.")).defaultValue(true)).build());
        this.itemSpacing = this.sgPlayers.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("item-spacing")).description("The spacing between items.")).defaultValue(2.0).range(0.0, 10.0).visible(this.displayItems::get)).build());
        this.ignoreEmpty = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-empty-slots")).description("Doesn't add spacing where an empty item stack would be.")).defaultValue(true)).visible(this.displayItems::get)).build());
        this.itemDurability = this.sgPlayers.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("durability")).description("Displays item durability as either a total, percentage, or neither.")).defaultValue(Durability.None)).visible(this.displayItems::get)).build());
        this.displayEnchants = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("display-enchants")).description("Displays item enchantments on the items.")).defaultValue(false)).visible(this.displayItems::get)).build());
        this.shownEnchantments = this.sgPlayers.add(((EnchantmentListSetting.Builder)((EnchantmentListSetting.Builder)((EnchantmentListSetting.Builder)new EnchantmentListSetting.Builder().name("shown-enchantments")).description("The enchantments that are shown on nametags.")).visible(() -> this.displayItems.get() != false && this.displayEnchants.get() != false)).defaultValue(Enchantments.PROTECTION, Enchantments.BLAST_PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.PROJECTILE_PROTECTION).build());
        this.enchantPos = this.sgPlayers.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("enchantment-position")).description("Where the enchantments are rendered.")).defaultValue(Position.Above)).visible(() -> this.displayItems.get() != false && this.displayEnchants.get() != false)).build());
        this.enchantLength = this.sgPlayers.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("enchant-name-length")).description("The length enchantment names are trimmed to.")).defaultValue(3)).range(1, 5).sliderRange(1, 5).visible(() -> this.displayItems.get() != false && this.displayEnchants.get() != false)).build());
        this.enchantTextScale = this.sgPlayers.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("enchant-text-scale")).description("The scale of the enchantment text.")).defaultValue(1.0).range(0.1, 2.0).sliderRange(0.1, 2.0).visible(() -> this.displayItems.get() != false && this.displayEnchants.get() != false)).build());
        this.itemCount = this.sgItems.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-count")).description("Displays the number of items in the stack.")).defaultValue(true)).build());
        this.background = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("The color of the nametag background.")).defaultValue(new SettingColor(0, 0, 0, 75)).build());
        this.nameColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("name-color")).description("The color of the nametag names.")).defaultValue(new SettingColor()).build());
        this.pingColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("ping-color")).description("The color of the nametag ping.")).defaultValue(new SettingColor(20, 170, 170)).visible(this.displayPing::get)).build());
        this.gamemodeColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("gamemode-color")).description("The color of the nametag gamemode.")).defaultValue(new SettingColor(232, 185, 35)).visible(this.displayGameMode::get)).build());
        this.distanceColorMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("distance-color-mode")).description("The mode to color the nametag distance with.")).defaultValue(DistanceColorMode.Gradient)).visible(this.displayDistance::get)).build());
        this.distanceColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("distance-color")).description("The color of the nametag distance.")).defaultValue(new SettingColor(150, 150, 150)).visible(() -> this.displayDistance.get() != false && this.distanceColorMode.get() == DistanceColorMode.Flat)).build());
        this.WHITE = new Color(255, 255, 255);
        this.RED = new Color(255, 25, 25);
        this.AMBER = new Color(255, 105, 25);
        this.GREEN = new Color(25, 252, 25);
        this.GOLD = new Color(232, 185, 35);
        this.pos = new Vector3d();
        this.itemWidths = new double[6];
        this.entityList = new ArrayList<Entity>();
    }

    private static String ticksToTime(int ticks) {
        if (ticks > 72000) {
            int h = ticks / 20 / 3600;
            return h + " h";
        }
        if (ticks > 1200) {
            int m = ticks / 20 / 60;
            return m + " m";
        }
        int s = ticks / 20;
        int ms = ticks % 20 / 2;
        return s + "." + ms + " s";
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        this.entityList.clear();
        boolean freecamNotActive = !Modules.get().isActive(Freecam.class);
        boolean notThirdPerson = this.mc.options.getCameraType().isFirstPerson();
        Vec3 cameraPos = this.mc.gameRenderer.getMainCamera().position();
        for (Entity entity : this.mc.level.entitiesForRendering()) {
            EntityType type = entity.getType();
            if (!this.entities.get().contains(type) || type == EntityType.PLAYER && ((this.ignoreSelf.get().booleanValue() || freecamNotActive && notThirdPerson) && entity == this.mc.player || EntityUtils.getGameMode((Player)entity) == null && this.ignoreBots.get().booleanValue() || Friends.get().isFriend((Player)entity) && this.ignoreFriends.get().booleanValue()) || this.culling.get().booleanValue() && !PlayerUtils.isWithinCamera(entity, (double)this.maxCullRange.get())) continue;
            this.entityList.add(entity);
        }
        this.entityList.sort(Comparator.comparing(e -> e.distanceToSqr(cameraPos)));
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        int count = this.getRenderCount();
        boolean shadow = Config.get().customFont.get();
        block8: for (int i = count - 1; i > -1; --i) {
            Entity entity;
            Entity entity2 = this.entityList.get(i);
            Utils.set(this.pos, entity2, event.tickDelta);
            this.pos.add(0.0, this.getHeight(entity2), 0.0);
            if (!NametagUtils.to2D(this.pos, this.scale.get())) continue;
            Objects.requireNonNull(entity2);
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Player.class, ItemEntity.class, ItemFrame.class, PrimedTnt.class, MinecartTNT.class, LivingEntity.class}, (Entity)entity, n)) {
                case 0: {
                    Player player = (Player)entity;
                    this.renderNametagPlayer(event, player, shadow);
                    continue block8;
                }
                case 1: {
                    ItemEntity item = (ItemEntity)entity;
                    this.renderNametagItem(item.getItem(), shadow);
                    continue block8;
                }
                case 2: {
                    ItemFrame itemFrame = (ItemFrame)entity;
                    this.renderNametagItem(itemFrame.getItem(), shadow);
                    continue block8;
                }
                case 3: {
                    PrimedTnt tnt = (PrimedTnt)entity;
                    this.renderTntNametag(Nametags.ticksToTime(tnt.getFuse()), shadow);
                    continue block8;
                }
                case 4: {
                    MinecartTNT minecartTNT = (MinecartTNT)entity;
                    this.renderTntNametag(Nametags.ticksToTime(minecartTNT.getFuse()), shadow);
                    continue block8;
                }
                case 5: {
                    LivingEntity living = (LivingEntity)entity;
                    this.renderGenericLivingNametag(living, shadow);
                    continue block8;
                }
                default: {
                    this.renderGenericNametag(entity2, shadow);
                }
            }
        }
    }

    private int getRenderCount() {
        int count = this.culling.get() != false ? this.maxCullCount.get().intValue() : this.entityList.size();
        count = Mth.clamp((int)count, (int)0, (int)this.entityList.size());
        return count;
    }

    @Override
    public String getInfoString() {
        return Integer.toString(this.getRenderCount());
    }

    private double getHeight(Entity entity) {
        double height = entity.getEyeHeight(entity.getPose());
        height = entity.getType() == EntityType.ITEM || entity.getType() == EntityType.ITEM_FRAME || entity.getType() == EntityType.GLOW_ITEM_FRAME ? (height += 0.2) : (height += 0.5);
        return height;
    }

    private void renderNametagPlayer(Render2DEvent event, Player player, boolean shadow) {
        boolean renderPlayerDistance;
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos, event.graphics);
        GameType gm = EntityUtils.getGameMode(player);
        Object gmText = "BOT";
        if (gm != null) {
            gmText = switch (gm) {
                default -> throw new MatchException(null, null);
                case GameType.SPECTATOR -> "Sp";
                case GameType.SURVIVAL -> "S";
                case GameType.CREATIVE -> "C";
                case GameType.ADVENTURE -> "A";
            };
        }
        gmText = "[" + (String)gmText + "] ";
        Color nameColor = PlayerUtils.getPlayerColor(player, this.nameColor.get());
        String name = player == this.mc.player ? Modules.get().get(NameProtect.class).getName(player.getName().getString()) : (this.displayPrefix.get() != false ? player.getDisplayName().getString() : player.getName().getString());
        float absorption = player.getAbsorptionAmount();
        int health = Math.round(player.getHealth() + absorption);
        double healthPercentage = (float)health / (player.getMaxHealth() + absorption);
        String healthText = " " + health;
        Color healthColor = healthPercentage <= 0.333 ? this.RED : (healthPercentage <= 0.666 ? this.AMBER : this.GREEN);
        int ping = EntityUtils.getPing(player);
        String pingText = " [" + ping + "ms]";
        double dist = (double)Math.round(PlayerUtils.distanceToCamera((Entity)player) * 10.0) / 10.0;
        String distText = " " + dist + "m";
        double gmWidth = text.getWidth((String)gmText, shadow);
        double nameWidth = text.getWidth(name, shadow);
        double healthWidth = text.getWidth(healthText, shadow);
        double pingWidth = text.getWidth(pingText, shadow);
        double distWidth = text.getWidth(distText, shadow);
        double width = nameWidth;
        boolean bl = renderPlayerDistance = player != this.mc.getCameraEntity() || Modules.get().isActive(Freecam.class);
        if (this.displayHealth.get().booleanValue()) {
            width += healthWidth;
        }
        if (this.displayGameMode.get().booleanValue()) {
            width += gmWidth;
        }
        if (this.displayPing.get().booleanValue()) {
            width += pingWidth;
        }
        if (this.displayDistance.get().booleanValue() && renderPlayerDistance) {
            width += distWidth;
        }
        double widthHalf = width / 2.0;
        double heightDown = text.getHeight(shadow);
        this.drawBg(-widthHalf, -heightDown, width, heightDown);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        if (this.displayGameMode.get().booleanValue()) {
            hX = text.render((String)gmText, hX, hY, this.gamemodeColor.get(), shadow);
        }
        hX = text.render(name, hX, hY, nameColor, shadow);
        if (this.displayHealth.get().booleanValue()) {
            hX = text.render(healthText, hX, hY, healthColor, shadow);
        }
        if (this.displayPing.get().booleanValue()) {
            hX = text.render(pingText, hX, hY, this.pingColor.get(), shadow);
        }
        if (this.displayDistance.get().booleanValue() && renderPlayerDistance) {
            switch (this.distanceColorMode.get().ordinal()) {
                case 1: {
                    text.render(distText, hX, hY, this.distanceColor.get(), shadow);
                    break;
                }
                case 0: {
                    text.render(distText, hX, hY, EntityUtils.getColorFromDistance((Entity)player), shadow);
                }
            }
        }
        text.end();
        if (this.displayItems.get().booleanValue()) {
            Arrays.fill(this.itemWidths, 0.0);
            boolean hasItems = false;
            int maxEnchantCount = 0;
            for (int i = 0; i < 6; ++i) {
                ItemStack itemStack = this.getItem(player, i);
                if (!(this.itemWidths[i] != 0.0 || this.ignoreEmpty.get().booleanValue() && itemStack.isEmpty())) {
                    this.itemWidths[i] = 32.0 + this.itemSpacing.get();
                }
                if (!itemStack.isEmpty()) {
                    hasItems = true;
                }
                if (!this.displayEnchants.get().booleanValue()) continue;
                ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting((ItemStack)itemStack);
                int size = 0;
                Object object = enchantments.keySet().iterator();
                while (object.hasNext()) {
                    Holder enchantment = (Holder)object.next();
                    if (enchantment.unwrapKey().isPresent() && !this.shownEnchantments.get().contains(enchantment.unwrapKey().get())) continue;
                    String enchantName = Utils.getEnchantSimpleName((Holder<Enchantment>)enchantment, this.enchantLength.get()) + " " + enchantments.getLevel(enchantment);
                    this.itemWidths[i] = Math.max(this.itemWidths[i], text.getWidth(enchantName, shadow) / 2.0);
                    ++size;
                }
                maxEnchantCount = Math.max(maxEnchantCount, size);
            }
            double itemsHeight = hasItems ? 32 : 0;
            double itemWidthTotal = 0.0;
            for (double w : this.itemWidths) {
                itemWidthTotal += w;
            }
            double itemWidthHalf = itemWidthTotal / 2.0;
            double y = -heightDown - 7.0 - itemsHeight;
            double x = -itemWidthHalf;
            for (int i = 0; i < 6; ++i) {
                ItemStack stack = this.getItem(player, i);
                RenderUtils.drawItem(event.graphics, stack, (int)x, (int)y, 2.0f, true, null, false);
                if (stack.isDamageableItem() && this.itemDurability.get() != Durability.None) {
                    text.begin(0.75, false, true);
                    String damageText = switch (this.itemDurability.get().ordinal()) {
                        case 2 -> String.format("%.0f%%", Float.valueOf((float)(stack.getMaxDamage() - stack.getDamageValue()) * 100.0f / (float)stack.getMaxDamage()));
                        case 1 -> Integer.toString(stack.getMaxDamage() - stack.getDamageValue());
                        default -> "err";
                    };
                    Color damageColor = new Color(stack.getBarColor());
                    text.render(damageText, (int)x, (int)y, damageColor.a(255), true);
                    text.end();
                }
                if (maxEnchantCount > 0 && this.displayEnchants.get().booleanValue()) {
                    text.begin(0.5 * this.enchantTextScale.get(), false, true);
                    ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting((ItemStack)stack);
                    Object2IntOpenHashMap enchantmentsToShow = new Object2IntOpenHashMap();
                    for (Holder enchantment : enchantments.keySet()) {
                        if (!enchantment.is(this.shownEnchantments.get()::contains)) continue;
                        enchantmentsToShow.put((Object)enchantment, enchantments.getLevel(enchantment));
                    }
                    double aW = this.itemWidths[i];
                    double enchantY = 0.0;
                    double addY = switch (this.enchantPos.get().ordinal()) {
                        default -> throw new MatchException(null, null);
                        case 0 -> -((double)(enchantmentsToShow.size() + 1) * text.getHeight(shadow));
                        case 1 -> (itemsHeight - (double)enchantmentsToShow.size() * text.getHeight(shadow)) / 2.0;
                    };
                    for (Object2IntMap.Entry entry : Object2IntMaps.fastIterable((Object2IntMap)enchantmentsToShow)) {
                        String enchantName = Utils.getEnchantSimpleName((Holder<Enchantment>)((Holder)entry.getKey()), this.enchantLength.get()) + " " + entry.getIntValue();
                        Color enchantColor = this.WHITE;
                        if (((Holder)entry.getKey()).is(EnchantmentTags.CURSE)) {
                            enchantColor = this.RED;
                        }
                        double enchantX = switch (this.enchantPos.get().ordinal()) {
                            default -> throw new MatchException(null, null);
                            case 0 -> x + aW / 2.0 - text.getWidth(enchantName, shadow) / 2.0;
                            case 1 -> x + (aW - text.getWidth(enchantName, shadow)) / 2.0;
                        };
                        text.render(enchantName, enchantX, y + addY + enchantY, enchantColor, shadow);
                        enchantY += text.getHeight(shadow);
                    }
                    text.end();
                }
                x += this.itemWidths[i];
            }
        } else if (this.displayEnchants.get().booleanValue()) {
            this.displayEnchants.set(false);
        }
        NametagUtils.end(event.graphics);
    }

    private void renderNametagItem(ItemStack stack, boolean shadow) {
        if (stack.isEmpty()) {
            return;
        }
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        String name = Names.get(stack);
        String count = " x" + stack.getCount();
        double nameWidth = text.getWidth(name, shadow);
        double countWidth = text.getWidth(count, shadow);
        double heightDown = text.getHeight(shadow);
        double width = nameWidth;
        if (this.itemCount.get().booleanValue()) {
            width += countWidth;
        }
        double widthHalf = width / 2.0;
        this.drawBg(-widthHalf, -heightDown, width, heightDown);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        hX = text.render(name, hX, hY, this.nameColor.get(), shadow);
        if (this.itemCount.get().booleanValue()) {
            text.render(count, hX, hY, this.GOLD, shadow);
        }
        text.end();
        NametagUtils.end();
    }

    private void renderGenericLivingNametag(LivingEntity entity, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        Object nameText = entity.getType().getDescription().getString();
        nameText = (String)nameText + " ";
        float absorption = entity.getAbsorptionAmount();
        int health = Math.round(entity.getHealth() + absorption);
        double healthPercentage = (float)health / (entity.getMaxHealth() + absorption);
        String healthText = String.valueOf(health);
        Color healthColor = healthPercentage <= 0.333 ? this.RED : (healthPercentage <= 0.666 ? this.AMBER : this.GREEN);
        double nameWidth = text.getWidth((String)nameText, shadow);
        double healthWidth = text.getWidth(healthText, shadow);
        double heightDown = text.getHeight(shadow);
        double width = nameWidth + healthWidth;
        double widthHalf = width / 2.0;
        this.drawBg(-widthHalf, -heightDown, width, heightDown);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        hX = text.render((String)nameText, hX, hY, this.nameColor.get(), shadow);
        text.render(healthText, hX, hY, healthColor, shadow);
        text.end();
        NametagUtils.end();
    }

    private void renderGenericNametag(Entity entity, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        String nameText = entity.getType().getDescription().getString();
        double nameWidth = text.getWidth(nameText, shadow);
        double heightDown = text.getHeight(shadow);
        double widthHalf = nameWidth / 2.0;
        this.drawBg(-widthHalf, -heightDown, nameWidth, heightDown);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        text.render(nameText, hX, hY, this.nameColor.get(), shadow);
        text.end();
        NametagUtils.end();
    }

    private void renderTntNametag(String fuseText, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        double width = text.getWidth(fuseText, shadow);
        double heightDown = text.getHeight(shadow);
        double widthHalf = width / 2.0;
        this.drawBg(-widthHalf, -heightDown, width, heightDown);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        text.render(fuseText, hX, hY, this.nameColor.get(), shadow);
        text.end();
        NametagUtils.end();
    }

    private ItemStack getItem(Player entity, int index) {
        return switch (index) {
            case 0 -> entity.getMainHandItem();
            case 1 -> entity.getItemBySlot(EquipmentSlot.HEAD);
            case 2 -> entity.getItemBySlot(EquipmentSlot.CHEST);
            case 3 -> entity.getItemBySlot(EquipmentSlot.LEGS);
            case 4 -> entity.getItemBySlot(EquipmentSlot.FEET);
            case 5 -> entity.getOffhandItem();
            default -> ItemStack.EMPTY;
        };
    }

    private void drawBg(double x, double y, double width, double height) {
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad(x - 1.0, y - 1.0, width + 2.0, height + 2.0, this.background.get());
        Renderer2D.COLOR.render();
    }

    public boolean excludeBots() {
        return this.ignoreBots.get();
    }

    public boolean playerNametags() {
        return this.isActive() && this.entities.get().contains(EntityType.PLAYER);
    }

    public static enum Durability {
        None,
        Total,
        Percentage;

    }

    public static enum Position {
        Above,
        OnTop;

    }

    public static enum DistanceColorMode {
        Gradient,
        Flat;

    }
}
