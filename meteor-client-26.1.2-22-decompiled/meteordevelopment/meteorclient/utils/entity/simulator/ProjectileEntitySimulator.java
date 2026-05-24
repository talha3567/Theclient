package meteordevelopment.meteorclient.utils.entity.simulator;

import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.CrossbowItemAccessor;
import meteordevelopment.meteorclient.mixin.ProjectileInGroundAccessor;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.NoSlow;
import meteordevelopment.meteorclient.systems.modules.movement.Sneak;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.simulator.SimulationStep;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.SpectralArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.WindChargeItem;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class ProjectileEntitySimulator {
    private final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
    private final Vec3 pos3d = new Vec3(0.0, 0.0, 0.0);
    private final Vec3 prevPos3d = new Vec3(0.0, 0.0, 0.0);
    public final Vector3d pos = new Vector3d();
    private final Vector3d velocity = new Vector3d();
    private Projectile simulatingEntity;
    private EntityDimensions dimensions;
    private int tickCount;
    private int pierceLevel;
    private double gravity;
    private float airDrag;
    private float waterDrag;
    private boolean isInWater;
    private static final MotionData EGG = new MotionData(1.5f, 0.0f, 0.03, 0.99f, 0.8f, EntityType.EGG);
    private static final MotionData ENDER_PEARL = new MotionData(1.5f, 0.0f, 0.03, 0.99f, 0.8f, EntityType.ENDER_PEARL);
    private static final MotionData SNOWBALL = new MotionData(1.5f, 0.0f, 0.03, 0.99f, 0.8f, EntityType.SNOWBALL);
    private static final MotionData EXPERIENCE_BOTTLE = new MotionData(0.7f, -20.0f, 0.07, 0.99f, 0.8f, EntityType.EXPERIENCE_BOTTLE);
    private static final MotionData LINGERING_POTION = new MotionData(0.5f, -20.0f, 0.05, 0.99f, 0.8f, EntityType.LINGERING_POTION);
    private static final MotionData SPLASH_POTION = new MotionData(0.5f, -20.0f, 0.05, 0.99f, 0.8f, EntityType.SPLASH_POTION);
    private static final MotionData EXPLOSIVE = new MotionData(0.0f, 0.0f, 0.0, 1.0f, 1.0f, null);
    private static final MotionData WIND_CHARGE = new MotionData(1.5f, 0.0f, 0.0, 1.0f, 1.0f, EntityType.WIND_CHARGE);
    private static final MotionData ARROW = new MotionData(0.0f, 0.0f, 0.05, 0.99f, 0.6f, EntityType.ARROW);
    private static final MotionData TRIDENT = new MotionData(2.5f, 0.0f, 0.05, 0.99f, 0.99f, EntityType.TRIDENT);
    private static final MotionData FIREWORK_ROCKET = new MotionData(0.0f, 0.0f, 0.0, 1.0f, 1.0f, EntityType.FIREWORK_ROCKET);
    private static final MotionData FISHING_BOBBER = new MotionData(0.0f, 0.0f, 0.03, 0.92f, 0.0f, EntityType.FISHING_BOBBER);
    private static final MotionData LLAMA_SPIT = new MotionData(1.5f, 0.0f, 0.06, 0.99f, 0.0f, EntityType.LLAMA_SPIT);

    public boolean set(Entity user, ItemStack itemStack, double angleOffset, boolean accurate, float tickDelta) {
        Item item;
        Item item2 = item = itemStack.getItem();
        Objects.requireNonNull(item2);
        Item item3 = item2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{BowItem.class, CrossbowItem.class, WindChargeItem.class, TridentItem.class, SnowballItem.class, EggItem.class, EnderpearlItem.class, ExperienceBottleItem.class, SplashPotionItem.class, LingeringPotionItem.class, FishingRodItem.class}, (Item)item3, n)) {
            case 0: {
                if (!(user instanceof LivingEntity)) {
                    return false;
                }
                LivingEntity livingEntity = (LivingEntity)user;
                float charge = BowItem.getPowerForTime((int)livingEntity.getTicksUsingItem());
                if ((double)charge <= 0.1) {
                    if (user == MeteorClient.mc.player) {
                        charge = 1.0f;
                    } else {
                        return false;
                    }
                }
                this.set(user, angleOffset, accurate, tickDelta, ARROW.withPower(charge * 3.0f));
                break;
            }
            case 1: {
                ChargedProjectiles projectilesComponent = (ChargedProjectiles)itemStack.get(DataComponents.CHARGED_PROJECTILES);
                if (projectilesComponent == null) {
                    return false;
                }
                float speed = CrossbowItemAccessor.meteor$getSpeed(projectilesComponent);
                if (projectilesComponent.contains(Items.FIREWORK_ROCKET)) {
                    this.set(user, angleOffset, accurate, tickDelta, FIREWORK_ROCKET.withPower(speed));
                } else {
                    this.set(user, angleOffset, accurate, tickDelta, ARROW.withPower(speed));
                }
                this.pierceLevel = projectilesComponent.contains(Items.FIREWORK_ROCKET) ? 0 : Utils.getEnchantmentLevel(itemStack, (ResourceKey<Enchantment>)Enchantments.PIERCING);
                break;
            }
            case 2: {
                this.set(user, angleOffset, accurate, tickDelta, WIND_CHARGE);
                break;
            }
            case 3: {
                this.set(user, angleOffset, accurate, tickDelta, TRIDENT);
                break;
            }
            case 4: {
                this.set(user, angleOffset, accurate, tickDelta, SNOWBALL);
                break;
            }
            case 5: {
                this.set(user, angleOffset, accurate, tickDelta, EGG);
                break;
            }
            case 6: {
                this.set(user, angleOffset, accurate, tickDelta, ENDER_PEARL);
                break;
            }
            case 7: {
                this.set(user, angleOffset, accurate, tickDelta, EXPERIENCE_BOTTLE);
                break;
            }
            case 8: {
                this.set(user, angleOffset, accurate, tickDelta, SPLASH_POTION);
                break;
            }
            case 9: {
                this.set(user, angleOffset, accurate, tickDelta, LINGERING_POTION);
                break;
            }
            case 10: {
                this.setFishingBobber(user, tickDelta, FISHING_BOBBER);
                break;
            }
            default: {
                return false;
            }
        }
        return true;
    }

    public void set(Entity user, double angleOffset, boolean accurate, float tickDelta, MotionData data) {
        double z;
        double y;
        double x;
        double pitch;
        double yaw;
        Pose pose = user.getPose();
        if (user == MeteorClient.mc.player && (Modules.get().get(NoSlow.class).airStrict() || Modules.get().get(Sneak.class).doPacket())) {
            pose = Pose.CROUCHING;
        }
        Utils.set(this.pos, user, tickDelta).add(0.0, (double)(user.getEyeHeight(pose) - 0.1f), 0.0);
        if (user == MeteorClient.mc.player && Rotations.rotating) {
            yaw = Rotations.serverYaw;
            pitch = Rotations.serverPitch;
        } else {
            yaw = user.getYRot(tickDelta);
            pitch = user.getXRot(tickDelta);
        }
        if (angleOffset == 0.0) {
            x = -Math.sin(yaw * 0.017453292) * Math.cos(pitch * 0.017453292);
            y = -Math.sin((pitch + (double)data.roll()) * 0.017453292);
            z = Math.cos(yaw * 0.017453292) * Math.cos(pitch * 0.017453292);
        } else {
            Vec3 oppositeRotationVec = user.getUpVector(1.0f);
            Quaterniond quaternion = new Quaterniond().setAngleAxis(angleOffset, oppositeRotationVec.x, oppositeRotationVec.y, oppositeRotationVec.z);
            Vec3 rotationVec = user.getViewVector(1.0f);
            Vector3d vector3d = new Vector3d(rotationVec.x, rotationVec.y, rotationVec.z);
            vector3d.rotate((Quaterniondc)quaternion);
            x = vector3d.x;
            y = vector3d.y;
            z = vector3d.z;
        }
        this.velocity.set(x, y, z).normalize().mul((double)data.power());
        if (accurate) {
            Vec3 vel = user.getKnownMovement();
            this.velocity.add(vel.x, user.onGround() ? 0.0 : vel.y, vel.z);
        }
        this.setSimulationData((Projectile)data.entity().create((Level)MeteorClient.mc.level, null), data);
    }

    public void setFishingBobber(Entity user, float tickDelta, MotionData data) {
        double pitch;
        double yaw;
        if (user == MeteorClient.mc.player && Rotations.rotating) {
            yaw = Rotations.serverYaw;
            pitch = Rotations.serverPitch;
        } else {
            yaw = user.getYRot(tickDelta);
            pitch = user.getXRot(tickDelta);
        }
        double h = Math.cos(-yaw * 0.01745329238474369 - 3.1415927410125732);
        double i = Math.sin(-yaw * 0.01745329238474369 - 3.1415927410125732);
        double j = -Math.cos(-pitch * 0.01745329238474369);
        double k = Math.sin(-pitch * 0.01745329238474369);
        Pose pose = user.getPose();
        if (user == MeteorClient.mc.player && (Modules.get().get(NoSlow.class).airStrict() || Modules.get().get(Sneak.class).doPacket())) {
            pose = Pose.CROUCHING;
        }
        Utils.set(this.pos, user, tickDelta).sub(i * 0.3, 0.0, h * 0.3).add(0.0, (double)user.getEyeHeight(pose), 0.0);
        this.velocity.set(-i, Mth.clamp((double)(-(k / j)), (double)-5.0, (double)5.0), -h);
        double l = this.velocity.length();
        this.velocity.mul(0.6 / l + 0.5, 0.6 / l + 0.5, 0.6 / l + 0.5);
        this.setSimulationData((Projectile)data.entity().create((Level)MeteorClient.mc.level, null), data);
    }

    public boolean set(Entity entity) {
        ProjectileInGroundAccessor ppe;
        if (entity instanceof ProjectileInGroundAccessor && (ppe = (ProjectileInGroundAccessor)entity).meteor$invokeIsInGround()) {
            return false;
        }
        Entity entity2 = entity;
        Objects.requireNonNull(entity2);
        Entity entity3 = entity2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Arrow.class, SpectralArrow.class, ThrownTrident.class, ThrownEnderpearl.class, Snowball.class, ThrownEgg.class, ThrownExperienceBottle.class, ThrownSplashPotion.class, ThrownLingeringPotion.class, AbstractWindCharge.class, AbstractHurtingProjectile.class, LlamaSpit.class}, (Entity)entity3, n)) {
            case 0: {
                Arrow e = (Arrow)entity3;
                this.set((Projectile)e, ARROW);
                break;
            }
            case 1: {
                SpectralArrow e = (SpectralArrow)entity3;
                this.set((Projectile)e, ARROW);
                break;
            }
            case 2: {
                ThrownTrident e = (ThrownTrident)entity3;
                this.set((Projectile)e, TRIDENT);
                break;
            }
            case 3: {
                ThrownEnderpearl e = (ThrownEnderpearl)entity3;
                this.set((Projectile)e, ENDER_PEARL);
                break;
            }
            case 4: {
                Snowball e = (Snowball)entity3;
                this.set((Projectile)e, SNOWBALL);
                break;
            }
            case 5: {
                ThrownEgg e = (ThrownEgg)entity3;
                this.set((Projectile)e, EGG);
                break;
            }
            case 6: {
                ThrownExperienceBottle e = (ThrownExperienceBottle)entity3;
                this.set((Projectile)e, EXPERIENCE_BOTTLE);
                break;
            }
            case 7: {
                ThrownSplashPotion e = (ThrownSplashPotion)entity3;
                this.set((Projectile)e, SPLASH_POTION);
                break;
            }
            case 8: {
                ThrownLingeringPotion e = (ThrownLingeringPotion)entity3;
                this.set((Projectile)e, LINGERING_POTION);
                break;
            }
            case 9: {
                AbstractWindCharge e = (AbstractWindCharge)entity3;
                this.set((Projectile)e, WIND_CHARGE);
                break;
            }
            case 10: {
                AbstractHurtingProjectile e = (AbstractHurtingProjectile)entity3;
                this.set((Projectile)e, EXPLOSIVE);
                break;
            }
            case 11: {
                LlamaSpit e = (LlamaSpit)entity3;
                this.set((Projectile)e, LLAMA_SPIT);
                break;
            }
            default: {
                return false;
            }
        }
        if (entity.isNoGravity()) {
            this.gravity = 0.0;
        }
        return true;
    }

    public void set(Projectile entity, MotionData data) {
        this.pos.set(entity.getX(), entity.getY(), entity.getZ());
        double speed = entity.getDeltaMovement().length();
        this.velocity.set(entity.getDeltaMovement().x, entity.getDeltaMovement().y, entity.getDeltaMovement().z).normalize().mul(speed);
        this.setSimulationData(entity, data);
    }

    private void setSimulationData(Projectile entity, MotionData data) {
        this.gravity = data.gravity();
        this.airDrag = data.airDrag();
        this.waterDrag = data.waterDrag();
        this.simulatingEntity = entity;
        this.dimensions = this.simulatingEntity.getDimensions(this.simulatingEntity.getPose());
        this.isInWater = this.simulatingEntity.isInWater();
        this.tickCount = this.simulatingEntity.tickCount;
        this.pierceLevel = 0;
    }

    public SimulationStep tick() {
        ++this.tickCount;
        ((IVec3)this.prevPos3d).meteor$set(this.pos);
        if (this.simulatingEntity instanceof ThrowableProjectile || this.simulatingEntity instanceof AbstractHurtingProjectile) {
            this.velocity.sub(0.0, this.gravity, 0.0);
            this.velocity.mul(this.isInWater ? (double)this.waterDrag : (double)this.airDrag);
            this.pos.add((Vector3dc)this.velocity);
            this.tickIsTouchingWater();
        } else if (this.simulatingEntity instanceof AbstractArrow || this.simulatingEntity instanceof LlamaSpit) {
            this.pos.add((Vector3dc)this.velocity);
            this.velocity.mul(this.isInWater ? (double)this.waterDrag : (double)this.airDrag);
            this.velocity.sub(0.0, this.gravity, 0.0);
            this.tickIsTouchingWater();
        } else if (this.simulatingEntity instanceof Projectile) {
            this.tickIsTouchingWater();
            this.velocity.sub(0.0, this.gravity, 0.0);
            this.pos.add((Vector3dc)this.velocity);
            this.velocity.mul(this.isInWater ? (double)this.waterDrag : (double)this.airDrag);
        }
        if (this.pos.y < (double)MeteorClient.mc.level.getMinY()) {
            return SimulationStep.MISS;
        }
        int chunkX = SectionPos.posToSectionCoord((double)this.pos.x);
        int chunkZ = SectionPos.posToSectionCoord((double)this.pos.z);
        if (!MeteorClient.mc.level.getChunkSource().hasChunk(chunkX, chunkZ)) {
            return SimulationStep.MISS;
        }
        ((IVec3)this.pos3d).meteor$set(this.pos);
        if (this.pos3d.equals((Object)this.prevPos3d)) {
            return SimulationStep.MISS;
        }
        return this.getCollision();
    }

    public void tickIsTouchingWater() {
        AABB box = this.dimensions.makeBoundingBox(this.pos.x, this.pos.y, this.pos.z).deflate(0.001);
        int minX = Mth.floor((double)box.minX);
        int maxX = Mth.ceil((double)box.maxX);
        int minY = Mth.floor((double)box.minY);
        int maxY = Mth.ceil((double)box.maxY);
        int minZ = Mth.floor((double)box.minZ);
        int maxZ = Mth.ceil((double)box.maxZ);
        for (int x = minX; x < maxX; ++x) {
            for (int y = minY; y < maxY; ++y) {
                for (int z = minZ; z < maxZ; ++z) {
                    double fluidY;
                    this.blockPos.set(x, y, z);
                    FluidState fluidState = MeteorClient.mc.level.getFluidState((BlockPos)this.blockPos);
                    if (!fluidState.is(FluidTags.WATER) || !((fluidY = (double)((float)y + fluidState.getHeight((BlockGetter)MeteorClient.mc.level, (BlockPos)this.blockPos))) >= box.minY)) continue;
                    this.isInWater = true;
                    return;
                }
            }
        }
        this.isInWater = false;
    }

    private SimulationStep getCollision() {
        EntityHitResult ehr;
        BlockHitResult blockCollision = MeteorClient.mc.level.clipIncludingBorder(new ClipContext(this.prevPos3d, this.pos3d, ClipContext.Block.COLLIDER, this.waterDrag == 0.0f ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, (Entity)this.simulatingEntity));
        if (blockCollision.getType() != HitResult.Type.MISS) {
            ((IVec3)this.pos3d).meteor$set(blockCollision.getLocation());
        }
        if (this.simulatingEntity instanceof AbstractArrow) {
            EntityHitResult result;
            boolean hit;
            Collection entityCollisions = ProjectileUtil.getManyEntityHitResult((Level)MeteorClient.mc.level, (Entity)this.simulatingEntity, (Vec3)this.prevPos3d, (Vec3)this.pos3d, (AABB)this.dimensions.makeBoundingBox(this.prevPos3d).expandTowards(this.velocity.x, this.velocity.y, this.velocity.z).inflate(1.0), entity -> !entity.isSpectator() && entity.isAlive() && entity.isPickable(), (float)this.getToleranceMargin(), (ClipContext.Block)ClipContext.Block.COLLIDER, (boolean)false);
            entityCollisions.removeIf(collision -> this.tickCount <= 1 && collision.getEntity() == MeteorClient.mc.player);
            if (entityCollisions.isEmpty()) {
                return new SimulationStep(this.hitOrDeflect((HitResult)blockCollision), new HitResult[]{blockCollision});
            }
            boolean stop = false;
            ArrayList<EntityHitResult> hits = new ArrayList<EntityHitResult>();
            Iterator iterator = entityCollisions.iterator();
            while (iterator.hasNext() && (hit = this.hitOrDeflect((HitResult)(result = (EntityHitResult)iterator.next())))) {
                hits.add(result);
                if (this.pierceLevel <= 0) {
                    stop = true;
                    break;
                }
                --this.pierceLevel;
            }
            return new SimulationStep(stop, (HitResult[])hits.toArray(HitResult[]::new));
        }
        EntityHitResult entityCollision = ProjectileUtil.getEntityHitResult((Level)MeteorClient.mc.level, (Entity)this.simulatingEntity, (Vec3)this.prevPos3d, (Vec3)this.pos3d, (AABB)this.dimensions.makeBoundingBox(this.prevPos3d).expandTowards(this.velocity.x, this.velocity.y, this.velocity.z).inflate(1.0), entity -> !entity.isSpectator() && entity.isAlive() && entity.isPickable(), (float)this.getToleranceMargin());
        if (entityCollision == null || this.tickCount <= 1 && entityCollision instanceof EntityHitResult && (ehr = entityCollision).getEntity() == MeteorClient.mc.player) {
            return new SimulationStep(this.hitOrDeflect((HitResult)blockCollision), new HitResult[]{blockCollision});
        }
        if (this.hitOrDeflect((HitResult)entityCollision)) {
            return new SimulationStep(true, new HitResult[]{entityCollision});
        }
        return new SimulationStep(false, new HitResult[0]);
    }

    private boolean hitOrDeflect(HitResult hitResult) {
        if (hitResult instanceof EntityHitResult) {
            LivingEntity livingEntity;
            EntityHitResult entityHitResult = (EntityHitResult)hitResult;
            Entity entity = entityHitResult.getEntity();
            Utils.set(this.pos, entityHitResult.getLocation());
            if (entity instanceof Breeze && !(this.simulatingEntity instanceof AbstractWindCharge) || entity.deflection(this.simulatingEntity) == ProjectileDeflection.REVERSE) {
                this.velocity.mul(-0.5);
                return false;
            }
            if (entity instanceof LivingEntity && (livingEntity = (LivingEntity)entity).isBlocking() && this.simulatingEntity instanceof AbstractArrow) {
                this.velocity.mul(-0.5).mul(0.2);
                return this.velocity.lengthSquared() < 1.0E-7;
            }
            return true;
        }
        if (hitResult instanceof BlockHitResult) {
            BlockHitResult bhr = (BlockHitResult)hitResult;
            Utils.set(this.pos, bhr.getLocation());
            if (this.simulatingEntity.shouldBounceOnWorldBorder() && bhr.isWorldBorderHit()) {
                this.velocity.mul(-0.5).mul(0.2);
                return false;
            }
            return bhr.getType() != HitResult.Type.MISS;
        }
        return false;
    }

    private float getToleranceMargin() {
        return Math.clamp((float)(this.tickCount - 2) / 20.0f, 0.0f, 0.3f);
    }

    public record MotionData(float power, float roll, double gravity, float airDrag, float waterDrag, EntityType<?> entity) {
        public MotionData withPower(float power) {
            return new MotionData(power, this.roll(), this.gravity(), this.airDrag(), this.waterDrag(), this.entity());
        }
    }
}
