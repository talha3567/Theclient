package meteordevelopment.meteorclient.commands.arguments;

import com.mojang.serialization.Codec;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.core.Direction;

public class DirectionArgumentType
extends StringRepresentableArgument<Direction> {
    private static final DirectionArgumentType INSTANCE = new DirectionArgumentType();

    private DirectionArgumentType() {
        super((Codec)Direction.CODEC, Direction::values);
    }

    public static DirectionArgumentType create() {
        return INSTANCE;
    }
}
