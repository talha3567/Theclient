package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.NotebotSongArgumentType;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.Notebot;
import meteordevelopment.meteorclient.utils.notebot.song.Note;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

public class NotebotCommand
extends Command {
    private static final SimpleCommandExceptionType INVALID_SONG = new SimpleCommandExceptionType((Message)Component.literal((String)"Invalid song."));
    private static final DynamicCommandExceptionType INVALID_PATH = new DynamicCommandExceptionType(object -> Component.literal((String)"'%s' is not a valid path.".formatted(object)));
    int ticks = -1;
    private final Map<Integer, List<Note>> song = new HashMap<Integer, List<Note>>();

    public NotebotCommand() {
        super("notebot", "Allows you load notebot files", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(NotebotCommand.literal("help").executes(commandContext -> {
            Util.getPlatform().openUri("https://github.com/MeteorDevelopment/meteor-client/wiki/Notebot-Guide");
            return 1;
        }));
        builder.then(NotebotCommand.literal("status").executes(commandContext -> {
            Notebot notebot = Modules.get().get(Notebot.class);
            this.info(notebot.getStatus(), new Object[0]);
            return 1;
        }));
        builder.then(NotebotCommand.literal("pause").executes(commandContext -> {
            Notebot notebot = Modules.get().get(Notebot.class);
            notebot.pause();
            return 1;
        }));
        builder.then(NotebotCommand.literal("resume").executes(commandContext -> {
            Notebot notebot = Modules.get().get(Notebot.class);
            notebot.pause();
            return 1;
        }));
        builder.then(NotebotCommand.literal("stop").executes(commandContext -> {
            Notebot notebot = Modules.get().get(Notebot.class);
            notebot.stop();
            return 1;
        }));
        builder.then(NotebotCommand.literal("randomsong").executes(commandContext -> {
            Notebot notebot = Modules.get().get(Notebot.class);
            notebot.playRandomSong();
            return 1;
        }));
        builder.then(NotebotCommand.literal("play").then(NotebotCommand.argument("song", NotebotSongArgumentType.create()).executes(ctx -> {
            Notebot notebot = Modules.get().get(Notebot.class);
            Path songPath = (Path)ctx.getArgument("song", Path.class);
            if (songPath == null || !songPath.toFile().exists()) {
                throw INVALID_SONG.create();
            }
            notebot.loadSong(songPath.toFile());
            return 1;
        })));
        builder.then(NotebotCommand.literal("preview").then(NotebotCommand.argument("song", NotebotSongArgumentType.create()).executes(ctx -> {
            Notebot notebot = Modules.get().get(Notebot.class);
            Path songPath = (Path)ctx.getArgument("song", Path.class);
            if (songPath == null || !songPath.toFile().exists()) {
                throw INVALID_SONG.create();
            }
            notebot.previewSong(songPath.toFile());
            return 1;
        })));
        builder.then(NotebotCommand.literal("record").then(NotebotCommand.literal("start").executes(commandContext -> {
            this.ticks = -1;
            this.song.clear();
            MeteorClient.EVENT_BUS.subscribe(this);
            this.info("Recording started", new Object[0]);
            return 1;
        })));
        builder.then(NotebotCommand.literal("record").then(NotebotCommand.literal("cancel").executes(commandContext -> {
            MeteorClient.EVENT_BUS.unsubscribe(this);
            this.info("Recording cancelled", new Object[0]);
            return 1;
        })));
        builder.then(NotebotCommand.literal("record").then(NotebotCommand.literal("save").then(NotebotCommand.argument("name", StringArgumentType.greedyString()).executes(ctx -> {
            String name = (String)ctx.getArgument("name", String.class);
            if (name == null || name.isEmpty()) {
                throw INVALID_PATH.create((Object)name);
            }
            Path notebotFolder = MeteorClient.FOLDER.toPath().resolve("notebot");
            Path path = notebotFolder.resolve(String.format("%s.txt", name)).normalize();
            if (!path.startsWith(notebotFolder)) {
                throw INVALID_PATH.create((Object)path);
            }
            this.saveRecording(path);
            return 1;
        }))));
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.ticks == -1) {
            return;
        }
        ++this.ticks;
    }

    @EventHandler
    private void onReadPacket(PacketEvent.Receive event) {
        ClientboundSoundPacket sound;
        Packet<?> packet = event.packet;
        if (packet instanceof ClientboundSoundPacket && ((SoundEvent)(sound = (ClientboundSoundPacket)packet).getSound().value()).location().getPath().contains("note_block")) {
            if (this.ticks == -1) {
                this.ticks = 0;
            }
            List notes = this.song.computeIfAbsent(this.ticks, n -> new ArrayList());
            Note note = this.getNote(sound);
            if (note != null) {
                notes.add(note);
            }
        }
    }

    private void saveRecording(Path path) {
        if (this.song.isEmpty()) {
            MeteorClient.EVENT_BUS.unsubscribe(this);
            return;
        }
        try {
            MeteorClient.EVENT_BUS.unsubscribe(this);
            FileWriter file = new FileWriter(path.toFile());
            for (Map.Entry<Integer, List<Note>> entry : this.song.entrySet()) {
                int tick = entry.getKey();
                List<Note> notes = entry.getValue();
                for (Note note : notes) {
                    NoteBlockInstrument instrument = note.getInstrument();
                    int noteLevel = note.getNoteLevel();
                    file.write(String.format("%d:%d:%d\n", tick, noteLevel, instrument.ordinal()));
                }
            }
            file.close();
            this.info("Song saved.", new Object[0]);
        }
        catch (IOException iOException) {
            this.info("Couldn't create the file.", new Object[0]);
            MeteorClient.EVENT_BUS.unsubscribe(this);
        }
    }

    private Note getNote(ClientboundSoundPacket soundPacket) {
        float pitch = soundPacket.getPitch();
        int noteLevel = -1;
        for (int n = 0; n < 25; ++n) {
            if (!((double)((float)Math.pow(2.0, (double)(n - 12) / 12.0)) - 0.01 < (double)pitch) || !((double)((float)Math.pow(2.0, (double)(n - 12) / 12.0)) + 0.01 > (double)pitch)) continue;
            noteLevel = n;
            break;
        }
        if (noteLevel == -1) {
            this.error("Error while bruteforcing a note level! Sound: " + String.valueOf(soundPacket.getSound().value()) + " Pitch: " + pitch, new Object[0]);
            return null;
        }
        NoteBlockInstrument instrument = this.getInstrumentFromSound((SoundEvent)soundPacket.getSound().value());
        if (instrument == null) {
            this.error("Can't find the instrument from sound! Sound: " + String.valueOf(soundPacket.getSound().value()), new Object[0]);
            return null;
        }
        return new Note(instrument, noteLevel);
    }

    private NoteBlockInstrument getInstrumentFromSound(SoundEvent sound) {
        String path = sound.location().getPath();
        if (path.contains("harp")) {
            return NoteBlockInstrument.HARP;
        }
        if (path.contains("basedrum")) {
            return NoteBlockInstrument.BASEDRUM;
        }
        if (path.contains("snare")) {
            return NoteBlockInstrument.SNARE;
        }
        if (path.contains("hat")) {
            return NoteBlockInstrument.HAT;
        }
        if (path.contains("bass")) {
            return NoteBlockInstrument.BASS;
        }
        if (path.contains("flute")) {
            return NoteBlockInstrument.FLUTE;
        }
        if (path.contains("bell")) {
            return NoteBlockInstrument.BELL;
        }
        if (path.contains("guitar")) {
            return NoteBlockInstrument.GUITAR;
        }
        if (path.contains("chime")) {
            return NoteBlockInstrument.CHIME;
        }
        if (path.contains("xylophone")) {
            return NoteBlockInstrument.XYLOPHONE;
        }
        if (path.contains("iron_xylophone")) {
            return NoteBlockInstrument.IRON_XYLOPHONE;
        }
        if (path.contains("cow_bell")) {
            return NoteBlockInstrument.COW_BELL;
        }
        if (path.contains("didgeridoo")) {
            return NoteBlockInstrument.DIDGERIDOO;
        }
        if (path.contains("bit")) {
            return NoteBlockInstrument.BIT;
        }
        if (path.contains("banjo")) {
            return NoteBlockInstrument.BANJO;
        }
        if (path.contains("pling")) {
            return NoteBlockInstrument.PLING;
        }
        return null;
    }
}
