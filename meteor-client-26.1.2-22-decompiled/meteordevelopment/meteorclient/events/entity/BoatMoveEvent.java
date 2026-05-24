package meteordevelopment.meteorclient.events.entity;

import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

public class BoatMoveEvent {
    private static final BoatMoveEvent INSTANCE = new BoatMoveEvent();
    public AbstractBoat boat;

    public static BoatMoveEvent get(AbstractBoat entity) {
        BoatMoveEvent.INSTANCE.boat = entity;
        return INSTANCE;
    }
}
