package meteordevelopment.meteorclient.systems.modules.world;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;

public class AutoShearer
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> distance;
    private final Setting<Boolean> antiBreak;
    private final Setting<Boolean> rotate;
    private Entity entity;
    private InteractionHand hand;

    public AutoShearer() {
        super(Categories.World, "auto-shearer", "Automatically shears sheep.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.distance = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("distance")).description("The maximum distance the sheep have to be to be sheared.")).min(0.0).defaultValue(5.0).build());
        this.antiBreak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-break")).description("Prevents shears from being broken.")).defaultValue(false)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Automatically faces towards the animal being sheared.")).defaultValue(true)).build());
    }

    @Override
    public void onDeactivate() {
        this.entity = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        this.entity = null;
        for (Entity entity : this.mc.level.entitiesForRendering()) {
            Sheep sheep;
            if (!(entity instanceof Sheep) || (sheep = (Sheep)entity).isSheared() || sheep.isBaby() || !PlayerUtils.isWithin(entity, (double)this.distance.get())) continue;
            FindItemResult findShear = InvUtils.findInHotbar(itemStack -> itemStack.getItem() == Items.SHEARS && (this.antiBreak.get() == false || itemStack.getDamageValue() < itemStack.getMaxDamage() - 1));
            if (!InvUtils.swap(findShear.slot(), true)) {
                return;
            }
            this.hand = findShear.getHand();
            this.entity = entity;
            if (this.rotate.get().booleanValue()) {
                Rotations.rotate(Rotations.getYaw(entity), Rotations.getPitch(entity), -100, this::interact);
            } else {
                this.interact();
            }
            return;
        }
    }

    private void interact() {
        EntityHitResult location = new EntityHitResult(this.entity, this.entity.getBoundingBox().getCenter());
        this.mc.gameMode.interact((Player)this.mc.player, this.entity, location, this.hand);
        InvUtils.swapBack();
    }
}
