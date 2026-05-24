package meteordevelopment.meteorclient.events.entity.player;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

public class InteractItemEvent {
    private static final InteractItemEvent INSTANCE = new InteractItemEvent();
    public InteractionHand hand;
    public InteractionResult toReturn;

    public static InteractItemEvent get(InteractionHand hand) {
        InteractItemEvent.INSTANCE.hand = hand;
        InteractItemEvent.INSTANCE.toReturn = null;
        return INSTANCE;
    }
}
