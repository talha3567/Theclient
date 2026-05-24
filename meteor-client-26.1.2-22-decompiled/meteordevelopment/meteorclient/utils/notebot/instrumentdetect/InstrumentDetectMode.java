package meteordevelopment.meteorclient.utils.notebot.instrumentdetect;

import meteordevelopment.meteorclient.utils.notebot.instrumentdetect.InstrumentDetectFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;

public enum InstrumentDetectMode {
    BlockState((noteBlock, blockPos) -> (NoteBlockInstrument)noteBlock.getValue((Property)NoteBlock.INSTRUMENT)),
    BelowBlock((blockState, blockPos) -> Minecraft.getInstance().level.getBlockState(blockPos.below()).instrument());

    private final InstrumentDetectFunction instrumentDetectFunction;

    private InstrumentDetectMode(InstrumentDetectFunction instrumentDetectFunction) {
        this.instrumentDetectFunction = instrumentDetectFunction;
    }

    public InstrumentDetectFunction getInstrumentDetectFunction() {
        return this.instrumentDetectFunction;
    }
}
