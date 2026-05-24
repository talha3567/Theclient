package net.wurstclient.ai;

import net.minecraft.class_1656;
import net.minecraft.class_310;
import net.wurstclient.WurstClient;
import net.wurstclient.hack.HackList;

public record PlayerAbilities(boolean invulnerable, boolean creativeFlying, boolean flying, boolean immuneToFallDamage, boolean noWaterSlowdown, boolean jesus, boolean spider) {
    private static final WurstClient WURST = WurstClient.INSTANCE;
    private static final class_310 MC = WurstClient.MC;

    public static PlayerAbilities get() {
        HackList hax = WURST.getHax();
        class_1656 mcAbilities = PlayerAbilities.MC.field_1724.method_31549();
        boolean invulnerable = mcAbilities.field_7480 || mcAbilities.field_7477;
        boolean creativeFlying = mcAbilities.field_7479;
        boolean flying = creativeFlying || hax.flightHack.isEnabled();
        boolean immuneToFallDamage = invulnerable || hax.noFallHack.isEnabled();
        boolean noWaterSlowdown = hax.antiWaterPushHack.isPreventingSlowdown();
        boolean jesus = hax.jesusHack.isEnabled();
        boolean spider = hax.spiderHack.isEnabled();
        return new PlayerAbilities(invulnerable, creativeFlying, flying, immuneToFallDamage, noWaterSlowdown, jesus, spider);
    }
}
