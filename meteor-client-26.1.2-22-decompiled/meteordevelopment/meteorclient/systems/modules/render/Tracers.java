package meteordevelopment.meteorclient.systems.modules.render;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
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
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3d;

public class Tracers
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgAppearance;
    private final SettingGroup sgColors;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<Boolean> ignoreSelf;
    public final Setting<Boolean> ignoreFriends;
    public final Setting<Boolean> showInvis;
    private final Setting<TracerStyle> style;
    private final Setting<Target> target;
    private final Setting<Boolean> stem;
    private final Setting<Integer> maxDist;
    private final Setting<Integer> distanceOffscreen;
    private final Setting<Integer> sizeOffscreen;
    private final Setting<Boolean> blinkOffscreen;
    private final Setting<Double> blinkOffscreenSpeed;
    public final Setting<Boolean> distance;
    public final Setting<Boolean> friendOverride;
    private final Setting<SettingColor> playersColor;
    private final Setting<SettingColor> animalsColor;
    private final Setting<SettingColor> waterAnimalsColor;
    private final Setting<SettingColor> monstersColor;
    private final Setting<SettingColor> ambientColor;
    private final Setting<SettingColor> miscColor;
    private int count;
    private final Instant initTimer;

    public Tracers() {
        super(Categories.Render, "tracers", "Displays tracer lines to specified entities.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgAppearance = this.settings.createGroup("Appearance");
        this.sgColors = this.settings.createGroup("Colors");
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Select specific entities.")).defaultValue(EntityType.PLAYER).build());
        this.ignoreSelf = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-self")).description("Doesn't draw tracers to yourself when in third person or freecam.")).defaultValue(false)).build());
        this.ignoreFriends = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-friends")).description("Doesn't draw tracers to friends.")).defaultValue(false)).build());
        this.showInvis = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-invisible")).description("Shows invisible entities.")).defaultValue(true)).build());
        this.style = this.sgAppearance.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("style")).description("What display mode should be used")).defaultValue(TracerStyle.Lines)).build());
        this.target = this.sgAppearance.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("target")).description("What part of the entity to target.")).defaultValue(Target.Body)).visible(() -> this.style.get() == TracerStyle.Lines)).build());
        this.stem = this.sgAppearance.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("stem")).description("Draw a line through the center of the tracer target.")).defaultValue(true)).visible(() -> this.style.get() == TracerStyle.Lines)).build());
        this.maxDist = this.sgAppearance.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("max-distance")).description("Maximum distance for tracers to show.")).defaultValue(256)).min(0).sliderMax(256).build());
        this.distanceOffscreen = this.sgAppearance.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("distance-offscreen")).description("Offscreen's distance from center.")).defaultValue(200)).min(0).sliderMax(500).visible(() -> this.style.get() == TracerStyle.Offscreen)).build());
        this.sizeOffscreen = this.sgAppearance.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("size-offscreen")).description("Offscreen's size.")).defaultValue(10)).min(2).sliderMax(50).visible(() -> this.style.get() == TracerStyle.Offscreen)).build());
        this.blinkOffscreen = this.sgAppearance.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("blink-offscreen")).description("Make offscreen Blink.")).defaultValue(true)).visible(() -> this.style.get() == TracerStyle.Offscreen)).build());
        this.blinkOffscreenSpeed = this.sgAppearance.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("blink-offscreen-speed")).description("Offscreen's blink speed.")).defaultValue(4.0).min(1.0).sliderMax(15.0).visible(() -> this.style.get() == TracerStyle.Offscreen && this.blinkOffscreen.get() != false)).build());
        this.distance = this.sgColors.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("distance-colors")).description("Changes the color of tracers depending on distance.")).defaultValue(false)).build());
        this.friendOverride = this.sgColors.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-friend-colors")).description("Whether or not to override the distance color of friends with the friend color.")).defaultValue(true)).visible(() -> this.distance.get() != false && this.ignoreFriends.get() == false)).build());
        this.playersColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("players-colors")).description("The player's color.")).defaultValue(new SettingColor(205, 205, 205, 127)).visible(() -> this.distance.get() == false)).build());
        this.animalsColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("animals-color")).description("The animal's color.")).defaultValue(new SettingColor(145, 255, 145, 127)).visible(() -> this.distance.get() == false)).build());
        this.waterAnimalsColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("water-animals-color")).description("The water animal's color.")).defaultValue(new SettingColor(145, 145, 255, 127)).visible(() -> this.distance.get() == false)).build());
        this.monstersColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("monsters-color")).description("The monster's color.")).defaultValue(new SettingColor(255, 145, 145, 127)).visible(() -> this.distance.get() == false)).build());
        this.ambientColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("ambient-color")).description("The ambient color.")).defaultValue(new SettingColor(75, 75, 75, 127)).visible(() -> this.distance.get() == false)).build());
        this.miscColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("misc-color")).description("The misc color.")).defaultValue(new SettingColor(145, 145, 145, 127)).visible(() -> this.distance.get() == false)).build());
        this.initTimer = Instant.now();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean shouldBeIgnored(Entity entity) {
        if (!PlayerUtils.isWithin(entity, (double)this.maxDist.get().intValue())) return true;
        if (!Modules.get().isActive(Freecam.class)) {
            if (entity == this.mc.player) return true;
        }
        if (!this.entities.get().contains(entity.getType())) return true;
        if (this.ignoreSelf.get().booleanValue()) {
            if (entity == this.mc.player) return true;
        }
        if (this.ignoreFriends.get().booleanValue() && entity instanceof Player) {
            Player player = (Player)entity;
            if (Friends.get().isFriend(player)) return true;
        }
        if (!this.showInvis.get().booleanValue()) {
            if (entity.isInvisible()) return true;
        }
        if (EntityUtils.isInRenderDistance(entity)) return false;
        return true;
    }

    /*
     * Enabled aggressive block sorting
     */
    private Color getEntityColor(Entity entity) {
        Color color;
        if (this.distance.get().booleanValue()) {
            if (this.friendOverride.get().booleanValue() && entity instanceof Player) {
                Player player = (Player)entity;
                if (Friends.get().isFriend(player)) {
                    color = Config.get().friendColor.get();
                    return new Color(color);
                }
            }
            color = EntityUtils.getColorFromDistance(entity);
            return new Color(color);
        }
        if (entity instanceof Player) {
            Player player = (Player)entity;
            color = PlayerUtils.getPlayerColor(player, this.playersColor.get());
            return new Color(color);
        }
        color = switch (entity.getType().getCategory()) {
            case MobCategory.CREATURE -> this.animalsColor.get();
            case MobCategory.WATER_AMBIENT, MobCategory.WATER_CREATURE, MobCategory.UNDERGROUND_WATER_CREATURE, MobCategory.AXOLOTLS -> this.waterAnimalsColor.get();
            case MobCategory.MONSTER -> this.monstersColor.get();
            case MobCategory.AMBIENT -> this.ambientColor.get();
            default -> this.miscColor.get();
        };
        return new Color(color);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.mc.options.hideGui || this.style.get() == TracerStyle.Offscreen) {
            return;
        }
        this.count = 0;
        for (Entity entity : this.mc.level.entitiesForRendering()) {
            if (this.shouldBeIgnored(entity)) continue;
            Color color = this.getEntityColor(entity);
            double x = entity.xo + (entity.getX() - entity.xo) * (double)event.tickDelta;
            double y = entity.yo + (entity.getY() - entity.yo) * (double)event.tickDelta;
            double z = entity.zo + (entity.getZ() - entity.zo) * (double)event.tickDelta;
            double height = entity.getBoundingBox().maxY - entity.getBoundingBox().minY;
            if (this.target.get() == Target.Head) {
                y += height;
            } else if (this.target.get() == Target.Body) {
                y += height / 2.0;
            }
            event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, x, y, z, color);
            if (this.stem.get().booleanValue()) {
                event.renderer.line(x, entity.getY(), z, x, entity.getY() + height, z, color);
            }
            ++this.count;
        }
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (this.mc.options.hideGui || this.style.get() != TracerStyle.Offscreen) {
            return;
        }
        this.count = 0;
        Renderer2D.COLOR.begin();
        for (Entity entity : this.mc.level.entitiesForRendering()) {
            if (this.shouldBeIgnored(entity)) continue;
            Color color = this.getEntityColor(entity);
            if (this.blinkOffscreen.get().booleanValue()) {
                color.a = (int)((float)color.a * this.getAlpha());
            }
            Vec2 screenCenter = new Vec2((float)this.mc.getWindow().getWidth() / 2.0f, (float)this.mc.getWindow().getHeight() / 2.0f);
            Vector3d projection = new Vector3d(entity.xo, entity.yo, entity.zo);
            boolean projSucceeded = NametagUtils.to2D(projection, 1.0, false, false);
            if (projSucceeded && projection.x > 0.0 && projection.x < (double)this.mc.getWindow().getWidth() && projection.y > 0.0 && projection.y < (double)this.mc.getWindow().getHeight()) continue;
            projection = new Vector3d(entity.xo, entity.yo, entity.zo);
            NametagUtils.to2D(projection, 1.0, false, true);
            Vector2f angle = this.vectorAngles(new Vector3d((double)screenCenter.x - projection.x, (double)screenCenter.y - projection.y, 0.0));
            angle.y += 180.0f;
            float angleYawRad = (float)Math.toRadians(angle.y);
            Vector2f newPoint = new Vector2f(screenCenter.x + (float)this.distanceOffscreen.get().intValue() * (float)Math.cos(angleYawRad), screenCenter.y + (float)this.distanceOffscreen.get().intValue() * (float)Math.sin(angleYawRad));
            Vector2f[] trianglePoints = new Vector2f[]{new Vector2f(newPoint.x - (float)this.sizeOffscreen.get().intValue(), newPoint.y - (float)this.sizeOffscreen.get().intValue()), new Vector2f(newPoint.x + (float)this.sizeOffscreen.get().intValue() * 0.73205f, newPoint.y), new Vector2f(newPoint.x - (float)this.sizeOffscreen.get().intValue(), newPoint.y + (float)this.sizeOffscreen.get().intValue())};
            this.rotateTriangle(trianglePoints, angle.y);
            Renderer2D.COLOR.triangle(trianglePoints[2].x, trianglePoints[2].y, trianglePoints[1].x, trianglePoints[1].y, trianglePoints[0].x, trianglePoints[0].y, color);
            ++this.count;
        }
        Renderer2D.COLOR.render();
    }

    private void rotateTriangle(Vector2f[] points, float ang) {
        Vector2f triangleCenter = new Vector2f(0.0f, 0.0f);
        triangleCenter.add((Vector2fc)points[0]).add((Vector2fc)points[1]).add((Vector2fc)points[2]).div(3.0f);
        float theta = (float)Math.toRadians(ang);
        float cos = (float)Math.cos(theta);
        float sin = (float)Math.sin(theta);
        for (int i = 0; i < 3; ++i) {
            Vector2f point = new Vector2f(points[i].x, points[i].y).sub((Vector2fc)triangleCenter);
            Vector2f newPoint = new Vector2f(point.x * cos - point.y * sin, point.x * sin + point.y * cos);
            newPoint.add((Vector2fc)triangleCenter);
            points[i] = newPoint;
        }
    }

    private Vector2f vectorAngles(Vector3d forward) {
        float pitch;
        float yaw;
        if (forward.x == 0.0 && forward.y == 0.0) {
            yaw = 0.0f;
            pitch = forward.z > 0.0 ? 270.0f : 90.0f;
        } else {
            float tmp;
            yaw = (float)(Math.atan2(forward.y, forward.x) * 180.0 / Math.PI);
            if (yaw < 0.0f) {
                yaw += 360.0f;
            }
            if ((pitch = (float)(Math.atan2(-forward.z, tmp = (float)Math.sqrt(forward.x * forward.x + forward.y * forward.y)) * 180.0 / Math.PI)) < 0.0f) {
                pitch += 360.0f;
            }
        }
        return new Vector2f(pitch, yaw);
    }

    private float getAlpha() {
        double speed = this.blinkOffscreenSpeed.get() / 4.0;
        double duration = (double)Math.abs(Duration.between(Instant.now(), this.initTimer).toMillis()) * speed;
        return (float)Math.abs(duration % 1000.0 - 500.0) / 500.0f;
    }

    @Override
    public String getInfoString() {
        return Integer.toString(this.count);
    }

    public static enum TracerStyle {
        Lines,
        Offscreen;

    }
}
