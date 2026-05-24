package meteordevelopment.meteorclient.systems.modules.world;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;

public class AutoNametag
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<Double> range;
    private final Setting<SortPriority> priority;
    private final Setting<Boolean> renametag;
    private final Setting<Boolean> rotate;
    private final Object2IntMap<Entity> entityCooldowns;
    private Entity target;
    private boolean offHand;

    public AutoNametag() {
        super(Categories.World, "auto-nametag", "Automatically uses nametags on entities without a nametag. WILL nametag ALL entities in the specified distance.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Which entities to nametag.")).build());
        this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("The maximum range an entity can be to be nametagged.")).defaultValue(5.0).min(0.0).sliderMax(6.0).build());
        this.priority = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("priority")).description("Priority sort")).defaultValue(SortPriority.LowestDistance)).build());
        this.renametag = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("renametag")).description("Allows already nametagged entities to be renamed.")).defaultValue(true)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Automatically faces towards the mob being nametagged.")).defaultValue(true)).build());
        this.entityCooldowns = new Object2IntOpenHashMap();
    }

    @Override
    public void onDeactivate() {
        this.entityCooldowns.clear();
    }

    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        FindItemResult findNametag = InvUtils.findInHotbar(Items.NAME_TAG);
        if (!findNametag.found()) {
            this.error("No Nametag in Hotbar", new Object[0]);
            this.toggle();
            return;
        }
        this.target = TargetUtils.get(entity -> {
            if (!PlayerUtils.isWithin(entity, (double)this.range.get())) {
                return false;
            }
            if (!this.entities.get().contains(entity.getType())) {
                return false;
            }
            if (entity.hasCustomName() && (!this.renametag.get().booleanValue() || entity.getCustomName().equals((Object)this.mc.player.getInventory().getItem(findNametag.slot()).getDisplayName()))) {
                return false;
            }
            return this.entityCooldowns.getInt(entity) <= 0;
        }, this.priority.get());
        if (this.target == null) {
            return;
        }
        InvUtils.swap(findNametag.slot(), true);
        this.offHand = findNametag.isOffhand();
        if (this.rotate.get().booleanValue()) {
            Rotations.rotate(Rotations.getYaw(this.target), Rotations.getPitch(this.target), -100, this::interact);
        } else {
            this.interact();
        }
    }

    @EventHandler
    private void onTickPost(TickEvent.Post event) {
        ObjectIterator it = this.entityCooldowns.keySet().iterator();
        while (it.hasNext()) {
            Entity entity = (Entity)it.next();
            int cooldown = this.entityCooldowns.getInt((Object)entity) - 1;
            if (cooldown <= 0) {
                it.remove();
                continue;
            }
            this.entityCooldowns.put((Object)entity, cooldown);
        }
    }

    private void interact() {
        InteractionHand hand = this.offHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        EntityHitResult location = new EntityHitResult(this.target, this.target.getBoundingBox().getCenter());
        this.mc.gameMode.interact((Player)this.mc.player, this.target, location, hand);
        InvUtils.swapBack();
        this.entityCooldowns.put((Object)this.target, 20);
    }
}
