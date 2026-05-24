package meteordevelopment.meteorclient.utils.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.DirectionAccessor;
import meteordevelopment.meteorclient.mixin.EntitySectionAccessor;
import meteordevelopment.meteorclient.mixin.EntitySectionStorageAccessor;
import meteordevelopment.meteorclient.mixin.LevelAccessor;
import meteordevelopment.meteorclient.mixin.LevelEntityGetterAdapterAccessor;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

public class EntityUtils {
    private static final BlockPos.MutableBlockPos testPos = new BlockPos.MutableBlockPos();

    private EntityUtils() {
    }

    public static boolean isAttackable(EntityType<?> type) {
        return type != EntityType.AREA_EFFECT_CLOUD && type != EntityType.ARROW && type != EntityType.FALLING_BLOCK && type != EntityType.FIREWORK_ROCKET && type != EntityType.ITEM && type != EntityType.LLAMA_SPIT && type != EntityType.SPECTRAL_ARROW && type != EntityType.ENDER_PEARL && type != EntityType.EXPERIENCE_BOTTLE && type != EntityType.SPLASH_POTION && type != EntityType.LINGERING_POTION && type != EntityType.TRIDENT && type != EntityType.LIGHTNING_BOLT && type != EntityType.FISHING_BOBBER && type != EntityType.EXPERIENCE_ORB && type != EntityType.EGG;
    }

    public static boolean isRideable(EntityType<?> type) {
        return type == EntityType.PIG || type == EntityType.STRIDER || type == EntityType.HORSE || type == EntityType.DONKEY || type == EntityType.MULE || type == EntityType.SKELETON_HORSE || type == EntityType.ZOMBIE_HORSE || type == EntityType.LLAMA || type == EntityType.TRADER_LLAMA || type == EntityType.CAMEL || type == EntityType.CAMEL_HUSK || type == EntityType.MINECART || type == EntityType.OAK_BOAT || type == EntityType.SPRUCE_BOAT || type == EntityType.BIRCH_BOAT || type == EntityType.JUNGLE_BOAT || type == EntityType.ACACIA_BOAT || type == EntityType.CHERRY_BOAT || type == EntityType.DARK_OAK_BOAT || type == EntityType.PALE_OAK_BOAT || type == EntityType.MANGROVE_BOAT || type == EntityType.BAMBOO_RAFT || type == EntityType.ACACIA_CHEST_BOAT || type == EntityType.BIRCH_CHEST_BOAT || type == EntityType.CHERRY_CHEST_BOAT || type == EntityType.DARK_OAK_CHEST_BOAT || type == EntityType.JUNGLE_CHEST_BOAT || type == EntityType.MANGROVE_CHEST_BOAT || type == EntityType.OAK_CHEST_BOAT || type == EntityType.PALE_OAK_CHEST_BOAT || type == EntityType.SPRUCE_CHEST_BOAT || type == EntityType.BAMBOO_CHEST_RAFT || type == EntityType.NAUTILUS || type == EntityType.ZOMBIE_NAUTILUS || type == EntityType.HAPPY_GHAST;
    }

    public static float getTotalHealth(LivingEntity target) {
        return target.getHealth() + target.getAbsorptionAmount();
    }

    public static int getPing(Player player) {
        if (MeteorClient.mc.getConnection() == null) {
            return 0;
        }
        PlayerInfo playerListEntry = MeteorClient.mc.getConnection().getPlayerInfo(player.getUUID());
        if (playerListEntry == null) {
            return 0;
        }
        return playerListEntry.getLatency();
    }

    public static GameType getGameMode(Player player) {
        if (player == null) {
            return null;
        }
        PlayerInfo playerListEntry = MeteorClient.mc.getConnection().getPlayerInfo(player.getUUID());
        if (playerListEntry == null) {
            return null;
        }
        return playerListEntry.getGameMode();
    }

    public static boolean isAboveWater(Entity entity) {
        BlockState state;
        BlockPos.MutableBlockPos blockPos = entity.blockPosition().mutable();
        int bottom = MeteorClient.mc.level.getMinY();
        while (blockPos.getY() > bottom && !(state = MeteorClient.mc.level.getBlockState((BlockPos)blockPos)).blocksMotion()) {
            Fluid fluid = state.getFluidState().getType();
            if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
                return true;
            }
            blockPos.move(0, -1, 0);
        }
        return false;
    }

    public static boolean isInCobweb(Entity entity) {
        return MeteorClient.mc.level.getBlockStatesIfLoaded(entity.getBoundingBox()).anyMatch(state -> state.is((Object)Blocks.COBWEB));
    }

    public static boolean isInRenderDistance(Entity entity) {
        if (entity == null) {
            return false;
        }
        return EntityUtils.isInRenderDistance(entity.getX(), entity.getZ());
    }

    public static boolean isInRenderDistance(BlockEntity entity) {
        if (entity == null) {
            return false;
        }
        return EntityUtils.isInRenderDistance(entity.getBlockPos().getX(), entity.getBlockPos().getZ());
    }

    public static boolean isInRenderDistance(BlockPos pos) {
        if (pos == null) {
            return false;
        }
        return EntityUtils.isInRenderDistance(pos.getX(), pos.getZ());
    }

    public static boolean isInRenderDistance(double posX, double posZ) {
        double x = Math.abs(MeteorClient.mc.gameRenderer.getMainCamera().position().x - posX);
        double z = Math.abs(MeteorClient.mc.gameRenderer.getMainCamera().position().z - posZ);
        double d = ((Integer)MeteorClient.mc.options.renderDistance().get() + 1) * 16;
        return x < d && z < d;
    }

    public static BlockPos getCityBlock(Player player) {
        if (player == null) {
            return null;
        }
        double bestDistanceSquared = 36.0;
        Direction bestDirection = null;
        for (Direction direction : DirectionAccessor.meteor$getHorizontal()) {
            double testDistanceSquared;
            testPos.set((Vec3i)player.blockPosition().relative(direction));
            Block block = MeteorClient.mc.level.getBlockState((BlockPos)testPos).getBlock();
            if (block != Blocks.OBSIDIAN && block != Blocks.NETHERITE_BLOCK && block != Blocks.CRYING_OBSIDIAN && block != Blocks.RESPAWN_ANCHOR && block != Blocks.ANCIENT_DEBRIS || !((testDistanceSquared = PlayerUtils.squaredDistanceTo((BlockPos)testPos)) < bestDistanceSquared)) continue;
            bestDistanceSquared = testDistanceSquared;
            bestDirection = direction;
        }
        if (bestDirection == null) {
            return null;
        }
        return player.blockPosition().relative(bestDirection);
    }

    public static String getName(Entity entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof Player) {
            return entity.getName().getString();
        }
        return entity.getType().getDescription().getString();
    }

    public static Color getColorFromDistance(Entity entity) {
        int g;
        int r;
        Color distanceColor = new Color(255, 255, 255);
        double distance = PlayerUtils.distanceToCamera(entity);
        double percent = distance / 60.0;
        if (percent < 0.0 || percent > 1.0) {
            distanceColor.set(0, 255, 0, 255);
            return distanceColor;
        }
        if (percent < 0.5) {
            r = 255;
            g = (int)(255.0 * percent / 0.5);
        } else {
            g = 255;
            r = 255 - (int)(255.0 * (percent - 0.5) / 0.5);
        }
        distanceColor.set(r, g, 0, 255);
        return distanceColor;
    }

    public static Color getColorFromHealth(Entity entity, Color nonLivingEntityColor) {
        int g;
        int r;
        if (!(entity instanceof LivingEntity)) {
            return new Color(nonLivingEntityColor);
        }
        LivingEntity living = (LivingEntity)entity;
        float health = living.getHealth();
        float maxHealth = living.getMaxHealth();
        if (maxHealth <= 0.0f) {
            return new Color(nonLivingEntityColor);
        }
        double percent = health / maxHealth;
        if ((percent = Math.clamp(percent, 0.0, 1.0)) < 0.5) {
            r = 255;
            g = (int)(255.0 * (percent / 0.5));
        } else {
            g = 255;
            r = 255 - (int)(255.0 * ((percent - 0.5) / 0.5));
        }
        return new Color(r, g, 0, 255);
    }

    public static boolean intersectsWithEntity(AABB box, Predicate<Entity> predicate) {
        LevelEntityGetter<Entity> entityLookup = ((LevelAccessor)MeteorClient.mc.level).meteor$getEntityLookup();
        if (entityLookup instanceof LevelEntityGetterAdapter) {
            LevelEntityGetterAdapter simpleEntityLookup = (LevelEntityGetterAdapter)entityLookup;
            EntitySectionStorage cache = ((LevelEntityGetterAdapterAccessor)simpleEntityLookup).meteor$getSectionStorage();
            LongSortedSet trackedPositions = ((EntitySectionStorageAccessor)cache).meteor$getSectionIds();
            Long2ObjectMap trackingSections = ((EntitySectionStorageAccessor)cache).meteor$getSections();
            int i = SectionPos.posToSectionCoord((double)(box.minX - 2.0));
            int j = SectionPos.posToSectionCoord((double)(box.minY - 2.0));
            int k = SectionPos.posToSectionCoord((double)(box.minZ - 2.0));
            int l = SectionPos.posToSectionCoord((double)(box.maxX + 2.0));
            int m = SectionPos.posToSectionCoord((double)(box.maxY + 2.0));
            int n = SectionPos.posToSectionCoord((double)(box.maxZ + 2.0));
            for (int o = i; o <= l; ++o) {
                long p = SectionPos.asLong((int)o, (int)0, (int)0);
                long q = SectionPos.asLong((int)o, (int)-1, (int)-1);
                LongBidirectionalIterator longIterator = trackedPositions.subSet(p, q + 1L).iterator();
                while (longIterator.hasNext()) {
                    EntitySection entityTrackingSection;
                    long r = longIterator.nextLong();
                    int s = SectionPos.y((long)r);
                    int t = SectionPos.z((long)r);
                    if (s < j || s > m || t < k || t > n || (entityTrackingSection = (EntitySection)trackingSections.get(r)) == null || !entityTrackingSection.getStatus().isAccessible()) continue;
                    for (Entity entity2 : ((EntitySectionAccessor)entityTrackingSection).meteor$getStorage()) {
                        if (!entity2.getBoundingBox().intersects(box) || !predicate.test(entity2)) continue;
                        return true;
                    }
                }
            }
            return false;
        }
        AtomicBoolean found = new AtomicBoolean(false);
        entityLookup.get(box, entity -> {
            if (!found.get() && predicate.test((Entity)entity)) {
                found.set(true);
            }
        });
        return found.get();
    }

    public static EntityType<?> getGroup(Entity entity) {
        return entity.getType();
    }

    public static boolean isOnAir(Entity entity) {
        return entity.level().getBlockStates(entity.getBoundingBox().inflate(0.0625).expandTowards(0.0, -0.55, 0.0)).allMatch(BlockBehaviour.BlockStateBase::isAir);
    }
}
