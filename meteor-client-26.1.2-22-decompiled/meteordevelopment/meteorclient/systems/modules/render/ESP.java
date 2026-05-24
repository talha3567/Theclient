package meteordevelopment.meteorclient.systems.modules.render;

import java.util.Set;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.WireframeEntityRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3d;

public class ESP
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgColors;
    public final Setting<Mode> mode;
    public final Setting<Boolean> highlightTarget;
    public final Setting<Boolean> targetHitbox;
    public final Setting<Integer> outlineWidth;
    public final Setting<Double> glowMultiplier;
    public final Setting<Boolean> ignoreSelf;
    public final Setting<ShapeMode> shapeMode;
    public final Setting<Double> fillOpacity;
    private final Setting<Double> fadeDistance;
    private final Setting<Set<EntityType<?>>> entities;
    public final Setting<ESPColorMode> colorMode;
    public final Setting<Boolean> friendOverride;
    private final Setting<SettingColor> nonLivingEntityColor;
    private final Setting<SettingColor> playersColor;
    private final Setting<SettingColor> animalsColor;
    private final Setting<SettingColor> waterAnimalsColor;
    private final Setting<SettingColor> monstersColor;
    private final Setting<SettingColor> ambientColor;
    private final Setting<SettingColor> miscColor;
    private final Setting<SettingColor> targetColor;
    private final Setting<SettingColor> targetHitboxColor;
    private final Color lineColor;
    private final Color sideColor;
    private final Color baseColor;
    private final Vector3d pos1;
    private final Vector3d pos2;
    private final Vector3d pos;
    private int count;

    public ESP() {
        super(Categories.Render, "esp", "Renders entities through walls.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgColors = this.settings.createGroup("Colors");
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Rendering mode.")).defaultValue(Mode.Shader)).build());
        this.highlightTarget = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("highlight-target")).description("highlights the currently targeted entity differently")).defaultValue(false)).build());
        this.targetHitbox = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("target-hitbox")).description("draw the hitbox of the target entity")).defaultValue(true)).visible(this.highlightTarget::get)).build());
        this.outlineWidth = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("outline-width")).description("The width of the shader outline.")).visible(() -> this.mode.get() == Mode.Shader)).defaultValue(2)).range(1, 10).sliderRange(1, 5).build());
        this.glowMultiplier = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("glow-multiplier")).description("Multiplier for glow effect")).visible(() -> this.mode.get() == Mode.Shader)).decimalPlaces(3).defaultValue(3.5).min(0.0).sliderMax(10.0).build());
        this.ignoreSelf = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-self")).description("Ignores yourself drawing the shader.")).defaultValue(true)).build());
        this.shapeMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).visible(() -> this.mode.get() != Mode.Glow)).defaultValue(ShapeMode.Both)).build());
        this.fillOpacity = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("fill-opacity")).description("The opacity of the shape fill.")).visible(() -> this.shapeMode.get() != ShapeMode.Lines && this.mode.get() != Mode.Glow)).defaultValue(0.3).range(0.0, 1.0).sliderMax(1.0).build());
        this.fadeDistance = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("fade-distance")).description("The distance from an entity where the color begins to fade.")).defaultValue(3.0).min(0.0).sliderMax(12.0).build());
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Select specific entities.")).defaultValue(EntityType.PLAYER).build());
        this.colorMode = this.sgColors.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("color-mode")).description("Determines the colors used for entities.")).defaultValue(ESPColorMode.EntityType)).build());
        this.friendOverride = this.sgColors.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-friend-colors")).description("Whether or not to override the distance/health color of friends with the friend color.")).defaultValue(true)).visible(() -> this.colorMode.get() == ESPColorMode.Distance || this.colorMode.get() == ESPColorMode.Health)).build());
        this.nonLivingEntityColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("non-living-entity-color")).description("The color used for non living entities such as dropped items.")).defaultValue(new SettingColor(25, 25, 25)).visible(() -> this.colorMode.get() == ESPColorMode.Health)).build());
        this.playersColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("players-color")).description("The other player's color.")).defaultValue(new SettingColor(255, 255, 255)).visible(() -> this.colorMode.get() == ESPColorMode.EntityType)).build());
        this.animalsColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("animals-color")).description("The animal's color.")).defaultValue(new SettingColor(25, 255, 25, 255)).visible(() -> this.colorMode.get() == ESPColorMode.EntityType)).build());
        this.waterAnimalsColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("water-animals-color")).description("The water animal's color.")).defaultValue(new SettingColor(25, 25, 255, 255)).visible(() -> this.colorMode.get() == ESPColorMode.EntityType)).build());
        this.monstersColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("monsters-color")).description("The monster's color.")).defaultValue(new SettingColor(255, 25, 25, 255)).visible(() -> this.colorMode.get() == ESPColorMode.EntityType)).build());
        this.ambientColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("ambient-color")).description("The ambient's color.")).defaultValue(new SettingColor(25, 25, 25, 255)).visible(() -> this.colorMode.get() == ESPColorMode.EntityType)).build());
        this.miscColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("misc-color")).description("The misc color.")).defaultValue(new SettingColor(175, 175, 175, 255)).visible(() -> this.colorMode.get() == ESPColorMode.EntityType)).build());
        this.targetColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("target-color")).description("The target color.")).defaultValue(new SettingColor(200, 200, 200, 255)).visible(this.highlightTarget::get)).build());
        this.targetHitboxColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("target-hitbox-color")).description("The target hitbox color.")).defaultValue(new SettingColor(100, 200, 200, 255)).visible(() -> this.highlightTarget.get() != false && this.targetHitbox.get() != false)).build());
        this.lineColor = new Color();
        this.sideColor = new Color();
        this.baseColor = new Color();
        this.pos1 = new Vector3d();
        this.pos2 = new Vector3d();
        this.pos = new Vector3d();
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        HitResult hitResult;
        if (this.mode.get() == Mode._2D) {
            return;
        }
        this.count = 0;
        Entity target = null;
        if (this.highlightTarget.get().booleanValue() && this.targetHitbox.get().booleanValue() && (hitResult = this.mc.hitResult) instanceof EntityHitResult) {
            EntityHitResult hr = (EntityHitResult)hitResult;
            target = hr.getEntity();
        }
        for (Entity entity : this.mc.level.entitiesForRendering()) {
            if (target != entity && this.shouldSkip(entity)) continue;
            if (target == entity || this.mode.get() == Mode.Box || this.mode.get() == Mode.Wireframe) {
                this.drawBoundingBox(event, entity);
            }
            ++this.count;
        }
    }

    private void drawBoundingBox(Render3DEvent event, Entity entity) {
        Color color = this.getColor(entity);
        if (color != null) {
            this.lineColor.set(color);
            this.sideColor.set(color).a((int)((double)this.sideColor.a * this.fillOpacity.get()));
        }
        if (this.mode.get() == Mode.Wireframe) {
            WireframeEntityRenderer.render(event, entity, 1.0, this.sideColor, this.lineColor, this.shapeMode.get());
        }
        boolean target = this.drawAsTarget(entity);
        if (this.mode.get() == Mode.Box || this.targetHitbox.get().booleanValue() && target) {
            double x = Mth.lerp((double)event.tickDelta, (double)entity.xOld, (double)entity.getX()) - entity.getX();
            double y = Mth.lerp((double)event.tickDelta, (double)entity.yOld, (double)entity.getY()) - entity.getY();
            double z = Mth.lerp((double)event.tickDelta, (double)entity.zOld, (double)entity.getZ()) - entity.getZ();
            ShapeMode shape = this.shapeMode.get();
            if (target && this.mode.get() != Mode.Box) {
                shape = ShapeMode.Lines;
            }
            if (target) {
                this.lineColor.set(this.targetHitboxColor.get());
            }
            AABB box = entity.getBoundingBox();
            event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, this.sideColor, this.lineColor, shape, 0);
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (this.mode.get() != Mode._2D) {
            return;
        }
        Renderer2D.COLOR.begin();
        this.count = 0;
        for (Entity entity : this.mc.level.entitiesForRendering()) {
            if (this.shouldSkip(entity)) continue;
            AABB box = entity.getBoundingBox();
            double x = Mth.lerp((double)event.tickDelta, (double)entity.xOld, (double)entity.getX()) - entity.getX();
            double y = Mth.lerp((double)event.tickDelta, (double)entity.yOld, (double)entity.getY()) - entity.getY();
            double z = Mth.lerp((double)event.tickDelta, (double)entity.zOld, (double)entity.getZ()) - entity.getZ();
            this.pos1.set(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
            this.pos2.set(0.0, 0.0, 0.0);
            if (this.checkCorner(box.minX + x, box.minY + y, box.minZ + z, this.pos1, this.pos2) || this.checkCorner(box.maxX + x, box.minY + y, box.minZ + z, this.pos1, this.pos2) || this.checkCorner(box.minX + x, box.minY + y, box.maxZ + z, this.pos1, this.pos2) || this.checkCorner(box.maxX + x, box.minY + y, box.maxZ + z, this.pos1, this.pos2) || this.checkCorner(box.minX + x, box.maxY + y, box.minZ + z, this.pos1, this.pos2) || this.checkCorner(box.maxX + x, box.maxY + y, box.minZ + z, this.pos1, this.pos2) || this.checkCorner(box.minX + x, box.maxY + y, box.maxZ + z, this.pos1, this.pos2) || this.checkCorner(box.maxX + x, box.maxY + y, box.maxZ + z, this.pos1, this.pos2)) continue;
            Color color = this.getColor(entity);
            if (color != null) {
                this.lineColor.set(color);
                this.sideColor.set(color).a((int)((double)this.sideColor.a * this.fillOpacity.get()));
            }
            if (this.shapeMode.get() != ShapeMode.Lines && this.sideColor.a > 0) {
                Renderer2D.COLOR.quad(this.pos1.x, this.pos1.y, this.pos2.x - this.pos1.x, this.pos2.y - this.pos1.y, this.sideColor);
            }
            if (this.shapeMode.get() != ShapeMode.Sides) {
                Renderer2D.COLOR.line(this.pos1.x, this.pos1.y, this.pos1.x, this.pos2.y, this.lineColor);
                Renderer2D.COLOR.line(this.pos2.x, this.pos1.y, this.pos2.x, this.pos2.y, this.lineColor);
                Renderer2D.COLOR.line(this.pos1.x, this.pos1.y, this.pos2.x, this.pos1.y, this.lineColor);
                Renderer2D.COLOR.line(this.pos1.x, this.pos2.y, this.pos2.x, this.pos2.y, this.lineColor);
            }
            ++this.count;
        }
        Renderer2D.COLOR.render();
    }

    public boolean forceRender() {
        return this.isActive() && (this.mode.get() == Mode.Shader || this.mode.get() == Mode.Glow);
    }

    private boolean checkCorner(double x, double y, double z, Vector3d min, Vector3d max) {
        this.pos.set(x, y, z);
        if (!NametagUtils.to2D(this.pos, 1.0)) {
            return true;
        }
        if (this.pos.x < min.x) {
            min.x = this.pos.x;
        }
        if (this.pos.y < min.y) {
            min.y = this.pos.y;
        }
        if (this.pos.z < min.z) {
            min.z = this.pos.z;
        }
        if (this.pos.x > max.x) {
            max.x = this.pos.x;
        }
        if (this.pos.y > max.y) {
            max.y = this.pos.y;
        }
        if (this.pos.z > max.z) {
            max.z = this.pos.z;
        }
        return false;
    }

    public boolean drawAsTarget(Entity entity) {
        EntityHitResult hr;
        HitResult hitResult;
        return this.highlightTarget.get() != false && (hitResult = this.mc.hitResult) instanceof EntityHitResult && (hr = (EntityHitResult)hitResult).getEntity() == entity;
    }

    public boolean shouldSkip(Entity entity) {
        if (this.drawAsTarget(entity)) {
            return false;
        }
        if (!this.entities.get().contains(entity.getType())) {
            return true;
        }
        if (entity == this.mc.player && this.ignoreSelf.get().booleanValue()) {
            return true;
        }
        if (entity == this.mc.getCameraEntity() && this.mc.options.getCameraType().isFirstPerson()) {
            return true;
        }
        return !EntityUtils.isInRenderDistance(entity);
    }

    public boolean shouldSkip(EntityType<?> entityType) {
        return !this.entities.get().contains(entityType);
    }

    public Color getColor(Entity entity) {
        Color color;
        double alpha = 1.0;
        if (this.drawAsTarget(entity)) {
            color = this.targetColor.get();
        } else {
            if (!this.entities.get().contains(entity.getType())) {
                return null;
            }
            alpha = this.getFadeAlpha(entity);
            if (alpha == 0.0) {
                return null;
            }
            color = this.getEntityTypeColor(entity);
        }
        return this.baseColor.set(color.r, color.g, color.b, (int)((double)color.a * alpha));
    }

    private double getFadeAlpha(Entity entity) {
        double dist = PlayerUtils.squaredDistanceToCamera(entity.getX(), entity.getY() + (double)entity.getEyeHeight(entity.getPose()), entity.getZ());
        double fadeDist = Math.pow(this.fadeDistance.get(), 2.0);
        double alpha = 1.0;
        if (dist <= fadeDist * fadeDist) {
            alpha = (float)(Math.sqrt(dist) / fadeDist);
        }
        if (alpha <= 0.075) {
            alpha = 0.0;
        }
        return alpha;
    }

    public Color getEntityTypeColor(Entity entity) {
        if (this.colorMode.get() == ESPColorMode.EntityType) {
            if (entity instanceof Player) {
                Player player = (Player)entity;
                return PlayerUtils.getPlayerColor(player, this.playersColor.get());
            }
            return switch (entity.getType().getCategory()) {
                case MobCategory.CREATURE -> this.animalsColor.get();
                case MobCategory.WATER_AMBIENT, MobCategory.WATER_CREATURE, MobCategory.UNDERGROUND_WATER_CREATURE, MobCategory.AXOLOTLS -> this.waterAnimalsColor.get();
                case MobCategory.MONSTER -> this.monstersColor.get();
                case MobCategory.AMBIENT -> this.ambientColor.get();
                default -> this.miscColor.get();
            };
        }
        if (this.friendOverride.get().booleanValue() && entity instanceof Player) {
            Player player = (Player)entity;
            if (Friends.get().isFriend(player)) {
                return Config.get().friendColor.get();
            }
        }
        if (this.colorMode.get() == ESPColorMode.Health) {
            return EntityUtils.getColorFromHealth(entity, this.nonLivingEntityColor.get());
        }
        return EntityUtils.getColorFromDistance(entity);
    }

    @Override
    public String getInfoString() {
        return Integer.toString(this.count);
    }

    public boolean isShader() {
        return this.isActive() && this.mode.get() == Mode.Shader;
    }

    public boolean isGlow() {
        return this.isActive() && this.mode.get() == Mode.Glow;
    }

    public static enum Mode {
        Box,
        Wireframe,
        _2D,
        Shader,
        Glow;


        public String toString() {
            return this == _2D ? "2D" : super.toString();
        }
    }

    public static enum ESPColorMode {
        EntityType,
        Distance,
        Health;


        public String toString() {
            return this == EntityType ? "Entity Type" : super.toString();
        }
    }
}
