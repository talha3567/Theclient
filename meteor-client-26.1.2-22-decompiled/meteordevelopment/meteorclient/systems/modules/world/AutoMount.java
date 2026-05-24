package meteordevelopment.meteorclient.systems.modules.world;

import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.EntityHitResult;

public class AutoMount
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> checkSaddle;
    private final Setting<Boolean> rotate;
    private final Setting<Set<EntityType<?>>> entities;

    public AutoMount() {
        super(Categories.World, "auto-mount", "Automatically mounts entities.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.checkSaddle = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("check-saddle")).description("Checks if the entity contains a saddle before mounting.")).defaultValue(false)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Faces the entity you mount.")).defaultValue(true)).build());
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Rideable entities.")).filter(EntityUtils::isRideable).build());
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.player.isPassenger()) {
            return;
        }
        if (this.mc.player.isShiftKeyDown()) {
            return;
        }
        if (this.mc.player.getMainHandItem().getItem() instanceof SpawnEggItem) {
            return;
        }
        for (Entity entity : this.mc.level.entitiesForRendering()) {
            if (!this.entities.get().contains(entity.getType()) || !PlayerUtils.isWithin(entity, 4.0) || (entity instanceof Pig || entity instanceof SkeletonHorse || entity instanceof Strider || entity instanceof ZombieHorse) && !((Mob)entity).isSaddled()) continue;
            if (!(entity instanceof Llama) && entity instanceof Mob) {
                Mob mobEntity = (Mob)entity;
                if (this.checkSaddle.get().booleanValue() && !mobEntity.isSaddled()) continue;
            }
            this.interact(entity, this.rotate.get());
            return;
        }
    }

    private void interact(Entity entity, boolean rotate) {
        if (rotate) {
            Rotations.rotate(Rotations.getYaw(entity), Rotations.getPitch(entity), -100, () -> this.interact(entity));
        } else {
            this.interact(entity);
        }
    }

    private void interact(Entity entity) {
        EntityHitResult location = new EntityHitResult(entity, entity.getBoundingBox().getCenter());
        this.mc.gameMode.interact((Player)this.mc.player, entity, location, InteractionHand.MAIN_HAND);
    }
}
