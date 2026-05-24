package com.pulseclient.modules.movement;

import com.pulseclient.Category;
import com.pulseclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class PulseFlight extends Module {
    private double speed = 1.0;
    private boolean antiKick = true;
    private int antiKickTicks = 0;
    private Minecraft mc = Minecraft.getInstance();

    public PulseFlight() {
        super("PulseFlight", "Allows you to fly. Combined Meteor & Wurst logic.", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            mc.player.getAbilities().mayfly = true;
            mc.player.getAbilities().flying = true;
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null && !mc.player.isCreative()) {
            mc.player.getAbilities().mayfly = false;
            mc.player.getAbilities().flying = false;
        }
    }

    public void onTick() {
        if (!isEnabled() || mc.player == null) return;

        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlyingSpeed((float) speed / 20f);

        if (antiKick) {
            antiKickTicks++;
            if (antiKickTicks >= 40) {
                Vec3 motion = mc.player.getDeltaMovement();
                mc.player.setDeltaMovement(motion.x, -0.04, motion.z);
                antiKickTicks = 0;
            }
        }
    }
}
