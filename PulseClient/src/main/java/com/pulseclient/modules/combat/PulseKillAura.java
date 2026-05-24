package com.pulseclient.modules.combat;

import com.pulseclient.Category;
import com.pulseclient.Module;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;

public class PulseKillAura extends Module {
    private double range = 4.5;
    private boolean rotate = true;
    private Minecraft mc = Minecraft.getInstance();

    public PulseKillAura() {
        super("PulseKillAura", "Attacks entities around you. Combined Meteor & Wurst logic.", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        // Initialization logic
    }

    public void onTick() {
        if (!isEnabled() || mc.player == null) return;

        List<Entity> targets = mc.level.getEntities().getAll().stream()
            .filter(this::isValidTarget)
            .sorted(Comparator.comparingDouble(e -> mc.player.distanceTo(e)))
            .collect(Collectors.toList());

        if (!targets.isEmpty()) {
            Entity target = targets.get(0);
            attack(target);
        }
    }

    private boolean isValidTarget(Entity entity) {
        if (!(entity instanceof LivingEntity) || entity == mc.player) return false;
        if (mc.player.distanceTo(entity) > range) return false;
        if (!entity.isAlive()) return false;
        if (entity instanceof Player && ((Player) entity).isCreative()) return false;
        return true;
    }

    private void attack(Entity target) {
        if (rotate) {
            // Rotation logic placeholder
        }
        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }
}
