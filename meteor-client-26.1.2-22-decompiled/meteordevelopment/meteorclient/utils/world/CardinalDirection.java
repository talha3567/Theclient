package meteordevelopment.meteorclient.utils.world;

import net.minecraft.core.Direction;

public enum CardinalDirection {
    North,
    East,
    South,
    West;


    public Direction toDirection() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Direction.NORTH;
            case 1 -> Direction.EAST;
            case 2 -> Direction.SOUTH;
            case 3 -> Direction.WEST;
        };
    }

    public static CardinalDirection fromDirection(Direction direction) {
        return switch (direction) {
            default -> throw new MatchException(null, null);
            case Direction.NORTH -> North;
            case Direction.SOUTH -> South;
            case Direction.WEST -> East;
            case Direction.EAST -> West;
            case Direction.DOWN, Direction.UP -> null;
        };
    }
}
