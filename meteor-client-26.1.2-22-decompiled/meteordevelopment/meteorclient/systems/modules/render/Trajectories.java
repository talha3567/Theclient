package meteordevelopment.meteorclient.systems.modules.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.simulator.ProjectileEntitySimulator;
import meteordevelopment.meteorclient.utils.entity.simulator.SimulationStep;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.WindChargeItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class Trajectories
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<List<Item>> items;
    private final Setting<Boolean> otherPlayers;
    private final Setting<Boolean> firedProjectiles;
    private final Setting<Boolean> ignoreWitherSkulls;
    private final Setting<Boolean> accurate;
    public final Setting<Integer> simulationSteps;
    private final Setting<Integer> ignoreFirstTicks;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<Boolean> renderPositionBox;
    private final Setting<Double> positionBoxSize;
    private final Setting<SettingColor> positionSideColor;
    private final Setting<SettingColor> positionLineColor;
    private final ProjectileEntitySimulator simulator;
    private final Pool<Vector3d> vec3s;
    private final List<Path> paths;
    private static final double MULTISHOT_OFFSET = Math.toRadians(10.0);

    public Trajectories() {
        super(Categories.Render, "trajectories", "Predicts the trajectory of throwable items.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.items = this.sgGeneral.add(((ItemListSetting.Builder)((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("items")).description("Items to display trajectories for.")).defaultValue(this.getDefaultItems())).filter(this::itemFilter).build());
        this.otherPlayers = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("other-players")).description("Calculates trajectories for other players.")).defaultValue(true)).build());
        this.firedProjectiles = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fired-projectiles")).description("Calculates trajectories for already fired projectiles.")).defaultValue(false)).build());
        this.ignoreWitherSkulls = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-wither-skulls")).description("Whether to ignore fired wither skulls.")).defaultValue(false)).visible(this.firedProjectiles::get)).build());
        this.accurate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("accurate")).description("Whether or not to calculate more accurate.")).defaultValue(false)).build());
        this.simulationSteps = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("simulation-steps")).description("How many steps to simulate projectiles. Zero for no limit")).defaultValue(500)).sliderMax(5000).build());
        this.ignoreFirstTicks = this.sgRender.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("ignore-rendering-first-ticks")).description("Ignores rendering the first given ticks, to make the rest of the path more visible.")).defaultValue(3)).min(0).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color.")).defaultValue(new SettingColor(255, 150, 0, 35)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color.")).defaultValue(new SettingColor(255, 150, 0)).build());
        this.renderPositionBox = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-position-boxes")).description("Renders the actual position the projectile will be at each tick along it's trajectory.")).defaultValue(false)).build());
        this.positionBoxSize = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("position-box-size")).description("The size of the box drawn at the simulated positions.")).defaultValue(0.02).sliderRange(0.01, 0.1).visible(this.renderPositionBox::get)).build());
        this.positionSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("position-side-color")).description("The side color.")).defaultValue(new SettingColor(255, 150, 0, 35)).visible(this.renderPositionBox::get)).build());
        this.positionLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("position-line-color")).description("The line color.")).defaultValue(new SettingColor(255, 150, 0)).visible(this.renderPositionBox::get)).build());
        this.simulator = new ProjectileEntitySimulator();
        this.vec3s = new Pool<Vector3d>(Vector3d::new);
        this.paths = new ArrayList<Path>();
    }

    private boolean itemFilter(Item item) {
        return item instanceof ProjectileWeaponItem || item instanceof FishingRodItem || item instanceof TridentItem || item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderpearlItem || item instanceof ExperienceBottleItem || item instanceof ThrowablePotionItem || item instanceof WindChargeItem;
    }

    private List<Item> getDefaultItems() {
        ArrayList<Item> items = new ArrayList<Item>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (!this.itemFilter(item)) continue;
            items.add(item);
        }
        return items;
    }

    private Path getEmptyPath() {
        for (Path path : this.paths) {
            if (!path.points.isEmpty()) continue;
            return path;
        }
        Path path = new Path(this);
        this.paths.add(path);
        return path;
    }

    private void calculatePath(Player player, float tickDelta) {
        for (Path path : this.paths) {
            path.clear();
        }
        ItemStack itemStack = player.getMainHandItem();
        if (!this.items.get().contains(itemStack.getItem())) {
            itemStack = player.getOffhandItem();
            if (!this.items.get().contains(itemStack.getItem())) {
                return;
            }
        }
        if (!this.simulator.set((Entity)player, itemStack, 0.0, this.accurate.get(), tickDelta)) {
            return;
        }
        Path p = this.getEmptyPath().calculate();
        if (player == this.mc.player) {
            p.ignoreFirstTicks();
        }
        if (itemStack.getItem() instanceof CrossbowItem && Utils.hasEnchantment(itemStack, (ResourceKey<Enchantment>)Enchantments.MULTISHOT)) {
            if (!this.simulator.set((Entity)player, itemStack, MULTISHOT_OFFSET, this.accurate.get(), tickDelta)) {
                return;
            }
            p = this.getEmptyPath().calculate();
            if (player == this.mc.player) {
                p.ignoreFirstTicks();
            }
            if (!this.simulator.set((Entity)player, itemStack, -MULTISHOT_OFFSET, this.accurate.get(), tickDelta)) {
                return;
            }
            p = this.getEmptyPath().calculate();
            if (player == this.mc.player) {
                p.ignoreFirstTicks();
            }
        }
    }

    private void calculateFiredPath(Entity entity, double tickDelta) {
        for (Path path : this.paths) {
            path.clear();
        }
        if (!this.simulator.set(entity)) {
            return;
        }
        this.getEmptyPath().setStart(entity, tickDelta).calculate();
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        float tickDelta = this.mc.level.tickRateManager().isFrozen() ? 1.0f : event.tickDelta;
        for (Player player : this.mc.level.players()) {
            if (!this.otherPlayers.get().booleanValue() && player != this.mc.player) continue;
            this.calculatePath(player, tickDelta);
            for (Path path : this.paths) {
                path.render(event);
            }
        }
        if (this.firedProjectiles.get().booleanValue()) {
            for (Entity entity : this.mc.level.entitiesForRendering()) {
                if (!(entity instanceof Projectile) || this.ignoreWitherSkulls.get().booleanValue() && entity instanceof WitherSkull) continue;
                if (entity instanceof ThrownTrident) {
                    ThrownTrident trident = (ThrownTrident)entity;
                    if (trident.noPhysics) continue;
                }
                this.calculateFiredPath(entity, tickDelta);
                for (Path path : this.paths) {
                    path.render(event);
                }
            }
        }
    }

    private class Path {
        private final List<Vector3d> points;
        private boolean hitQuad;
        private boolean hitQuadHorizontal;
        private double hitQuadX1;
        private double hitQuadY1;
        private double hitQuadZ1;
        private double hitQuadX2;
        private double hitQuadY2;
        private double hitQuadZ2;
        private final List<Entity> collidingEntities;
        public Vector3d lastPoint;
        private int start;
        final /* synthetic */ Trajectories this$0;

        private Path(Trajectories trajectories) {
            Trajectories trajectories2 = trajectories;
            Objects.requireNonNull(trajectories2);
            this.this$0 = trajectories2;
            this.points = new ArrayList<Vector3d>();
            this.collidingEntities = new ArrayList<Entity>();
        }

        public void clear() {
            this.this$0.vec3s.freeAll(this.points);
            this.points.clear();
            this.hitQuad = false;
            this.collidingEntities.clear();
            this.lastPoint = null;
            this.start = 0;
        }

        public Path calculate() {
            this.addPoint();
            for (int i = 0; i < (this.this$0.simulationSteps.get() > 0 ? this.this$0.simulationSteps.get() : Integer.MAX_VALUE); ++i) {
                SimulationStep result = this.this$0.simulator.tick();
                this.processHitResults(result);
                if (result.shouldStop) break;
                this.addPoint();
            }
            return this;
        }

        public Path setStart(Entity entity, double tickDelta) {
            this.lastPoint = new Vector3d(Mth.lerp((double)tickDelta, (double)entity.xOld, (double)entity.getX()), Mth.lerp((double)tickDelta, (double)entity.yOld, (double)entity.getY()), Mth.lerp((double)tickDelta, (double)entity.zOld, (double)entity.getZ()));
            return this;
        }

        private void addPoint() {
            this.points.add(this.this$0.vec3s.get().set((Vector3dc)this.this$0.simulator.pos));
        }

        private void processHitResults(SimulationStep step) {
            for (int i = 0; i < step.hitResults.length; ++i) {
                HitResult result = step.hitResults[i];
                if (result.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult r = (BlockHitResult)result;
                    this.hitQuad = true;
                    this.hitQuadX1 = r.getLocation().x;
                    this.hitQuadY1 = r.getLocation().y;
                    this.hitQuadZ1 = r.getLocation().z;
                    this.hitQuadX2 = r.getLocation().x;
                    this.hitQuadY2 = r.getLocation().y;
                    this.hitQuadZ2 = r.getLocation().z;
                    if (r.getDirection() == Direction.UP || r.getDirection() == Direction.DOWN) {
                        this.hitQuadHorizontal = true;
                        this.hitQuadX1 -= 0.25;
                        this.hitQuadZ1 -= 0.25;
                        this.hitQuadX2 += 0.25;
                        this.hitQuadZ2 += 0.25;
                    } else if (r.getDirection() == Direction.NORTH || r.getDirection() == Direction.SOUTH) {
                        this.hitQuadHorizontal = false;
                        this.hitQuadX1 -= 0.25;
                        this.hitQuadY1 -= 0.25;
                        this.hitQuadX2 += 0.25;
                        this.hitQuadY2 += 0.25;
                    } else {
                        this.hitQuadHorizontal = false;
                        this.hitQuadZ1 -= 0.25;
                        this.hitQuadY1 -= 0.25;
                        this.hitQuadZ2 += 0.25;
                        this.hitQuadY2 += 0.25;
                    }
                    this.points.add(Utils.set(this.this$0.vec3s.get(), result.getLocation()));
                    continue;
                }
                if (result.getType() != HitResult.Type.ENTITY) continue;
                Entity entity = ((EntityHitResult)result).getEntity();
                this.collidingEntities.add(entity);
                if (!step.shouldStop || i != step.hitResults.length - 1) continue;
                this.points.add(Utils.set(this.this$0.vec3s.get(), result.getLocation()));
            }
        }

        public void ignoreFirstTicks() {
            this.start = this.points.size() <= this.this$0.ignoreFirstTicks.get() ? 0 : this.this$0.ignoreFirstTicks.get();
        }

        public void render(Render3DEvent event) {
            for (int i = this.start; i < this.points.size(); ++i) {
                Vector3d point = this.points.get(i);
                if (this.lastPoint != null) {
                    event.renderer.line(this.lastPoint.x, this.lastPoint.y, this.lastPoint.z, point.x, point.y, point.z, this.this$0.lineColor.get());
                    if (this.this$0.renderPositionBox.get().booleanValue()) {
                        event.renderer.box(point.x - this.this$0.positionBoxSize.get(), point.y - this.this$0.positionBoxSize.get(), point.z - this.this$0.positionBoxSize.get(), point.x + this.this$0.positionBoxSize.get(), point.y + this.this$0.positionBoxSize.get(), point.z + this.this$0.positionBoxSize.get(), this.this$0.positionSideColor.get(), this.this$0.positionLineColor.get(), this.this$0.shapeMode.get(), 0);
                    }
                }
                this.lastPoint = point;
            }
            if (this.hitQuad) {
                if (this.hitQuadHorizontal) {
                    event.renderer.sideHorizontal(this.hitQuadX1, this.hitQuadY1, this.hitQuadZ1, this.hitQuadX1 + 0.5, this.hitQuadZ1 + 0.5, this.this$0.sideColor.get(), this.this$0.lineColor.get(), this.this$0.shapeMode.get());
                } else {
                    event.renderer.sideVertical(this.hitQuadX1, this.hitQuadY1, this.hitQuadZ1, this.hitQuadX2, this.hitQuadY2, this.hitQuadZ2, this.this$0.sideColor.get(), this.this$0.lineColor.get(), this.this$0.shapeMode.get());
                }
            }
            for (Entity collidingEntity : this.collidingEntities) {
                double x = (collidingEntity.getX() - collidingEntity.xOld) * (double)event.tickDelta;
                double y = (collidingEntity.getY() - collidingEntity.yOld) * (double)event.tickDelta;
                double z = (collidingEntity.getZ() - collidingEntity.zOld) * (double)event.tickDelta;
                AABB box = collidingEntity.getBoundingBox();
                event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, this.this$0.sideColor.get(), this.this$0.lineColor.get(), this.this$0.shapeMode.get(), 0);
            }
        }
    }
}
