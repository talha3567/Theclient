package meteordevelopment.meteorclient.events.entity.player;

import net.minecraft.world.level.material.FluidState;

public class CanWalkOnFluidEvent {
    private static final CanWalkOnFluidEvent INSTANCE = new CanWalkOnFluidEvent();
    public FluidState fluidState;
    public boolean walkOnFluid;

    public static CanWalkOnFluidEvent get(FluidState fluid) {
        CanWalkOnFluidEvent.INSTANCE.fluidState = fluid;
        CanWalkOnFluidEvent.INSTANCE.walkOnFluid = false;
        return INSTANCE;
    }
}
