package meteordevelopment.meteorclient.utils.notebot;

import java.util.HashMap;
import java.util.Map;
import meteordevelopment.meteorclient.utils.notebot.instrumentdetect.InstrumentDetectFunction;
import meteordevelopment.meteorclient.utils.notebot.song.Note;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;

public class NotebotUtils {
    public static Note getNoteFromNoteBlock(BlockState noteBlock, BlockPos blockPos, NotebotMode mode, InstrumentDetectFunction instrumentDetectFunction) {
        NoteBlockInstrument instrument = null;
        int level = (Integer)noteBlock.getValue((Property)NoteBlock.NOTE);
        if (mode == NotebotMode.ExactInstruments) {
            instrument = instrumentDetectFunction.detectInstrument(noteBlock, blockPos);
        }
        return new Note(instrument, level);
    }

    public static enum NotebotMode {
        AnyInstrument,
        ExactInstruments;

    }

    public static enum OptionalInstrument {
        None(null),
        Harp(NoteBlockInstrument.HARP),
        Basedrum(NoteBlockInstrument.BASEDRUM),
        Snare(NoteBlockInstrument.SNARE),
        Hat(NoteBlockInstrument.HAT),
        Bass(NoteBlockInstrument.BASS),
        Flute(NoteBlockInstrument.FLUTE),
        Bell(NoteBlockInstrument.BELL),
        Guitar(NoteBlockInstrument.GUITAR),
        Chime(NoteBlockInstrument.CHIME),
        Xylophone(NoteBlockInstrument.XYLOPHONE),
        IronXylophone(NoteBlockInstrument.IRON_XYLOPHONE),
        CowBell(NoteBlockInstrument.COW_BELL),
        Didgeridoo(NoteBlockInstrument.DIDGERIDOO),
        Bit(NoteBlockInstrument.BIT),
        Banjo(NoteBlockInstrument.BANJO),
        Pling(NoteBlockInstrument.PLING);

        public static final Map<NoteBlockInstrument, OptionalInstrument> BY_MINECRAFT_INSTRUMENT;
        private final NoteBlockInstrument minecraftInstrument;

        private OptionalInstrument(NoteBlockInstrument minecraftInstrument) {
            this.minecraftInstrument = minecraftInstrument;
        }

        public NoteBlockInstrument toMinecraftInstrument() {
            return this.minecraftInstrument;
        }

        public static OptionalInstrument fromMinecraftInstrument(NoteBlockInstrument instrument) {
            if (instrument != null) {
                return BY_MINECRAFT_INSTRUMENT.get(instrument);
            }
            return null;
        }

        static {
            BY_MINECRAFT_INSTRUMENT = new HashMap<NoteBlockInstrument, OptionalInstrument>();
            for (OptionalInstrument optionalInstrument : OptionalInstrument.values()) {
                BY_MINECRAFT_INSTRUMENT.put(optionalInstrument.minecraftInstrument, optionalInstrument);
            }
        }
    }
}
