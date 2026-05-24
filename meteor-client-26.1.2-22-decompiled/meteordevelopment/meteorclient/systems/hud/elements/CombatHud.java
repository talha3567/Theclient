package meteordevelopment.meteorclient.systems.hud.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import java.util.ArrayList;
import java.util.Set;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnchantmentListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.DisplayItemUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.joml.Matrix4fStack;

public class CombatHud
extends HudElement {
    private static final Color GREEN = new Color(15, 255, 15);
    private static final Color RED = new Color(255, 15, 15);
    private static final Color BLACK = new Color(0, 0, 0, 255);
    public static final HudElementInfo<CombatHud> INFO = new HudElementInfo<CombatHud>(Hud.GROUP, "combat", "Displays information about your combat target.", CombatHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgEnchantments;
    private final SettingGroup sgHealth;
    private final SettingGroup sgDistance;
    private final SettingGroup sgPing;
    private final SettingGroup sgScale;
    private final SettingGroup sgBackground;
    private final Setting<Double> range;
    private final Setting<SettingColor> healthColor1;
    private final Setting<SettingColor> healthColor2;
    private final Setting<SettingColor> healthColor3;
    private final Setting<Set<ResourceKey<Enchantment>>> displayedEnchantments;
    private final Setting<SettingColor> enchantmentTextColor;
    private final Setting<Boolean> displayPing;
    private final Setting<SettingColor> pingColor1;
    private final Setting<SettingColor> pingColor2;
    private final Setting<SettingColor> pingColor3;
    private final Setting<Boolean> displayDistance;
    private final Setting<SettingColor> distColor1;
    private final Setting<SettingColor> distColor2;
    private final Setting<SettingColor> distColor3;
    public final Setting<Boolean> customScale;
    public final Setting<Double> scale;
    public final Setting<Boolean> background;
    public final Setting<SettingColor> backgroundColor;
    private Player playerEntity;

    public CombatHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgEnchantments = this.settings.createGroup("Enchantments");
        this.sgHealth = this.settings.createGroup("Health");
        this.sgDistance = this.settings.createGroup("Distance");
        this.sgPing = this.settings.createGroup("Ping");
        this.sgScale = this.settings.createGroup("Scale");
        this.sgBackground = this.settings.createGroup("Background");
        this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("The range to target players.")).defaultValue(100.0).min(1.0).sliderMax(200.0).build());
        this.healthColor1 = this.sgHealth.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("health-stage-1")).description("The color on the left of the health gradient.")).defaultValue(new SettingColor(255, 15, 15)).build());
        this.healthColor2 = this.sgHealth.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("health-stage-2")).description("The color in the middle of the health gradient.")).defaultValue(new SettingColor(255, 150, 15)).build());
        this.healthColor3 = this.sgHealth.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("health-stage-3")).description("The color on the right of the health gradient.")).defaultValue(new SettingColor(15, 255, 15)).build());
        this.displayedEnchantments = this.sgEnchantments.add(((EnchantmentListSetting.Builder)((EnchantmentListSetting.Builder)new EnchantmentListSetting.Builder().name("displayed-enchantments")).description("The enchantments that are shown on nametags.")).vanillaDefaults().build());
        this.enchantmentTextColor = this.sgEnchantments.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("enchantment-color")).description("Color of enchantment text.")).defaultValue(new SettingColor(255, 255, 255)).build());
        this.displayPing = this.sgPing.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ping")).description("Shows the player's ping.")).defaultValue(true)).build());
        this.pingColor1 = this.sgPing.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("ping-stage-1")).description("Color of ping text when under 75.")).defaultValue(new SettingColor(15, 255, 15)).visible(this.displayPing::get)).build());
        this.pingColor2 = this.sgPing.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("ping-stage-2")).description("Color of ping text when between 75 and 200.")).defaultValue(new SettingColor(255, 150, 15)).visible(this.displayPing::get)).build());
        this.pingColor3 = this.sgPing.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("ping-stage-3")).description("Color of ping text when over 200.")).defaultValue(new SettingColor(255, 15, 15)).visible(this.displayPing::get)).build());
        this.displayDistance = this.sgDistance.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("distance")).description("Shows the distance between you and the player.")).defaultValue(true)).build());
        this.distColor1 = this.sgDistance.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("distance-stage-1")).description("The color when a player is within 10 blocks of you.")).defaultValue(new SettingColor(255, 15, 15)).visible(this.displayDistance::get)).build());
        this.distColor2 = this.sgDistance.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("distance-stage-2")).description("The color when a player is within 50 blocks of you.")).defaultValue(new SettingColor(255, 150, 15)).visible(this.displayDistance::get)).build());
        this.distColor3 = this.sgDistance.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("distance-stage-3")).description("The color when a player is greater then 50 blocks away from you.")).defaultValue(new SettingColor(15, 255, 15)).visible(this.displayDistance::get)).build());
        this.customScale = this.sgScale.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-scale")).description("Applies a custom scale to this hud element.")).defaultValue(false)).onChanged(bl -> this.calculateSize())).build());
        this.scale = this.sgScale.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("Custom scale.")).visible(this.customScale::get)).defaultValue(2.0).onChanged(d -> this.calculateSize())).min(0.5).sliderRange(0.5, 3.0).build());
        this.background = this.sgBackground.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("background")).description("Displays background.")).defaultValue(false)).build());
        this.backgroundColor = this.sgBackground.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color used for the background.")).visible(this.background::get)).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        this.calculateSize();
    }

    private void calculateSize() {
        this.setSize(175.0 * this.getScale(), 95.0 * this.getScale());
    }

    @Override
    public void render(HudRenderer renderer) {
        renderer.post(() -> {
            double x = this.x;
            double y = this.y;
            Color primaryColor = TextHud.getSectionColor(0);
            Color secondaryColor = TextHud.getSectionColor(1);
            this.playerEntity = this.isInEditor() ? MeteorClient.mc.player : TargetUtils.getPlayerTarget(this.range.get(), SortPriority.LowestDistance);
            if (this.playerEntity == null && !this.isInEditor()) {
                return;
            }
            if (this.background.get().booleanValue()) {
                Renderer2D.COLOR.begin();
                Renderer2D.COLOR.quad(x, y, this.getWidth(), this.getHeight(), this.backgroundColor.get());
            }
            if (this.playerEntity == null) {
                if (this.isInEditor()) {
                    renderer.line(x, y, x + (double)this.getWidth(), y + (double)this.getHeight(), Color.GRAY);
                    renderer.line(x + (double)this.getWidth(), y, x, y + (double)this.getHeight(), Color.GRAY);
                    Renderer2D.COLOR.render();
                }
                return;
            }
            Renderer2D.COLOR.render();
            renderer.entity((LivingEntity)this.playerEntity, (int)(x + 5.0 * this.getScale()), (int)(y + 10.0 * this.getScale()), (int)(50.0 * this.getScale()), (int)(60.0 * this.getScale()), -Mth.wrapDegrees((float)(this.playerEntity.yRotO + (this.playerEntity.getYRot() - this.playerEntity.yRotO) * MeteorClient.mc.getDeltaTracker().getGameTimeDeltaPartialTick(true))), -this.playerEntity.getXRot());
            x += 50.0 * this.getScale();
            y += 5.0 * this.getScale();
            String breakText = " | ";
            String nameText = this.playerEntity.getName().getString();
            Color nameColor = PlayerUtils.getPlayerColor(this.playerEntity, primaryColor);
            int ping = EntityUtils.getPing(this.playerEntity);
            String pingText = ping + "ms";
            Color pingColor = ping <= 75 ? (Color)this.pingColor1.get() : (ping <= 200 ? (Color)this.pingColor2.get() : (Color)this.pingColor3.get());
            double dist = 0.0;
            if (!this.isInEditor()) {
                dist = (double)Math.round((double)MeteorClient.mc.player.distanceTo((Entity)this.playerEntity) * 100.0) / 100.0;
            }
            String distText = dist + "m";
            Color distColor = dist <= 10.0 ? (Color)this.distColor1.get() : (dist <= 50.0 ? (Color)this.distColor2.get() : (Color)this.distColor3.get());
            String friendText = "Unknown";
            Color friendColor = primaryColor;
            if (Friends.get().isFriend(this.playerEntity)) {
                friendText = "Friend";
                friendColor = Config.get().friendColor.get();
            } else {
                boolean naked = true;
                for (int position = 3; position >= 0; --position) {
                    ItemStack itemStack = this.getItem(position);
                    if (itemStack.isEmpty()) continue;
                    naked = false;
                }
                if (naked) {
                    friendText = "Naked";
                    friendColor = GREEN;
                } else {
                    boolean threat = false;
                    for (int position = 5; position >= 0; --position) {
                        ItemStack itemStack = this.getItem(position);
                        if (!itemStack.is(ItemTags.SWORDS) && itemStack.getItem() != Items.END_CRYSTAL && itemStack.getItem() != Items.RESPAWN_ANCHOR && !(itemStack.getItem() instanceof BedItem)) continue;
                        threat = true;
                    }
                    if (threat) {
                        friendText = "Threat";
                        friendColor = RED;
                    }
                }
            }
            TextRenderer.get().begin(0.45 * this.getScale(), false, true);
            double breakWidth = TextRenderer.get().getWidth(breakText);
            double pingWidth = TextRenderer.get().getWidth(pingText);
            double friendWidth = TextRenderer.get().getWidth(friendText);
            TextRenderer.get().render(nameText, x, y, nameColor != null ? nameColor : primaryColor);
            TextRenderer.get().render(friendText, x, y += TextRenderer.get().getHeight(), friendColor);
            if (this.displayPing.get().booleanValue()) {
                TextRenderer.get().render(breakText, x + friendWidth, y, secondaryColor);
                TextRenderer.get().render(pingText, x + friendWidth + breakWidth, y, pingColor);
                if (this.displayDistance.get().booleanValue()) {
                    TextRenderer.get().render(breakText, x + friendWidth + breakWidth + pingWidth, y, secondaryColor);
                    TextRenderer.get().render(distText, x + friendWidth + breakWidth + pingWidth + breakWidth, y, distColor);
                }
            } else if (this.displayDistance.get().booleanValue()) {
                TextRenderer.get().render(breakText, x + friendWidth, y, secondaryColor);
                TextRenderer.get().render(distText, x + friendWidth + breakWidth, y, distColor);
            }
            TextRenderer.get().end();
            y += 10.0 * this.getScale();
            int slot = 5;
            Matrix4fStack matrices = RenderSystem.getModelViewStack();
            matrices.pushMatrix();
            matrices.scale((float)this.getScale(), (float)this.getScale(), 1.0f);
            TextRenderer.get().begin(0.35, false, true);
            for (int position = 0; position < 6; ++position) {
                double armorX = x + (double)(position * 20) * this.getScale();
                double armorY = y;
                ItemStack itemStack = this.getItem(slot);
                renderer.item(itemStack, (int)armorX, (int)armorY, (float)this.getScale(), true);
                armorY = y / this.getScale() + 18.0;
                ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting((ItemStack)itemStack);
                ArrayList<ObjectIntImmutablePair> enchantmentsToShow = new ArrayList<ObjectIntImmutablePair>();
                for (Object2IntMap.Entry entry : enchantments.entrySet()) {
                    if (!((Holder)entry.getKey()).is(this.displayedEnchantments.get()::contains)) continue;
                    enchantmentsToShow.add(new ObjectIntImmutablePair((Object)((Holder)entry.getKey()), entry.getIntValue()));
                }
                for (ObjectIntPair objectIntPair : enchantmentsToShow) {
                    String enchantName = Utils.getEnchantSimpleName((Holder<Enchantment>)((Holder)objectIntPair.left()), 3) + " " + objectIntPair.rightInt();
                    double enchX = x / this.getScale() + (double)(position * 20) + 8.0 - TextRenderer.get().getWidth(enchantName) / 2.0;
                    TextRenderer.get().render(enchantName, enchX, armorY, ((Holder)objectIntPair.left()).is(EnchantmentTags.CURSE) ? RED : (Color)this.enchantmentTextColor.get());
                    armorY += TextRenderer.get().getHeight();
                }
                --slot;
            }
            TextRenderer.get().end();
            y = (int)((double)this.y + 75.0 * this.getScale());
            x = this.x;
            x /= this.getScale();
            y /= this.getScale();
            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.boxLines(x += 5.0, y += 5.0, 165.0, 11.0, BLACK);
            Renderer2D.COLOR.render();
            x += 2.0;
            y += 2.0;
            float maxHealth = this.playerEntity.getMaxHealth();
            int maxAbsorb = 16;
            int maxTotal = (int)(maxHealth + (float)maxAbsorb);
            int totalHealthWidth = (int)(161.0f * maxHealth / (float)maxTotal);
            int totalAbsorbWidth = 161 * maxAbsorb / maxTotal;
            float f = this.playerEntity.getHealth();
            float absorb = this.playerEntity.getAbsorptionAmount();
            double healthPercent = f / maxHealth;
            double absorbPercent = absorb / (float)maxAbsorb;
            int healthWidth = (int)((double)totalHealthWidth * healthPercent);
            int absorbWidth = (int)((double)totalAbsorbWidth * absorbPercent);
            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.quad(x, y, healthWidth, 7.0, this.healthColor1.get(), this.healthColor2.get(), this.healthColor2.get(), this.healthColor1.get());
            Renderer2D.COLOR.quad(x + (double)healthWidth, y, absorbWidth, 7.0, this.healthColor2.get(), this.healthColor3.get(), this.healthColor3.get(), this.healthColor2.get());
            Renderer2D.COLOR.render();
            matrices.popMatrix();
        });
    }

    private ItemStack getItem(int i) {
        if (this.isInEditor()) {
            return switch (i) {
                case 0 -> DisplayItemUtils.toStack(Items.NETHERITE_BOOTS);
                case 1 -> DisplayItemUtils.toStack(Items.NETHERITE_LEGGINGS);
                case 2 -> DisplayItemUtils.toStack(Items.NETHERITE_CHESTPLATE);
                case 3 -> DisplayItemUtils.toStack(Items.NETHERITE_HELMET);
                case 4 -> DisplayItemUtils.toStack(Items.TOTEM_OF_UNDYING);
                case 5 -> DisplayItemUtils.toStack(Items.END_CRYSTAL);
                default -> ItemStack.EMPTY;
            };
        }
        if (this.playerEntity == null) {
            return ItemStack.EMPTY;
        }
        return switch (i) {
            case 5 -> this.playerEntity.getMainHandItem();
            case 4 -> this.playerEntity.getOffhandItem();
            case 3 -> this.playerEntity.getItemBySlot(EquipmentSlot.HEAD);
            case 2 -> this.playerEntity.getItemBySlot(EquipmentSlot.CHEST);
            case 1 -> this.playerEntity.getItemBySlot(EquipmentSlot.LEGS);
            case 0 -> this.playerEntity.getItemBySlot(EquipmentSlot.FEET);
            default -> ItemStack.EMPTY;
        };
    }

    private double getScale() {
        return this.customScale.get() != false ? this.scale.get().doubleValue() : Hud.get().getTextScale();
    }
}
