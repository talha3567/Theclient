package meteordevelopment.meteorclient.systems.modules.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class BookBot
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;
    private final Setting<RandomType> randomType;
    private final Setting<Integer> pages;
    private final Setting<Integer> characters;
    private final Setting<Integer> delay;
    private final Setting<Boolean> sign;
    private final Setting<String> name;
    private final Setting<Boolean> count;
    private final Setting<Boolean> wordWrap;
    private File file;
    private final PointerBuffer filters;
    private int delayTimer;
    private int bookCount;
    private Random random;

    public BookBot() {
        super(Categories.Misc, "book-bot", "Automatically writes in books.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("What kind of text to write.")).defaultValue(Mode.Random)).build());
        this.randomType = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("random-type")).description("What kind of random to use.")).defaultValue(RandomType.Utf8)).visible(() -> this.mode.get() == Mode.Random)).build());
        this.pages = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("pages")).description("The number of pages to write per book.")).defaultValue(50)).range(1, 100).sliderRange(1, 100).visible(() -> this.mode.get() != Mode.File && this.randomType.get() != RandomType.PaperMC)).build());
        this.characters = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("characters")).description("How many characters to write per page.")).defaultValue(128)).range(1, 1024).sliderRange(1, 1024).visible(() -> this.mode.get() == Mode.Random && this.randomType.get() != RandomType.PaperMC)).build());
        this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("The amount of delay between writing books.")).defaultValue(20)).min(1).sliderRange(1, 200).build());
        this.sign = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sign")).description("Whether to sign the book.")).defaultValue(true)).build());
        this.name = this.sgGeneral.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("name")).description("The name you want to give your books.")).defaultValue("Meteor on Crack!")).visible(this.sign::get)).build());
        this.count = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("append-count")).description("Whether to append the number of the book to the title.")).defaultValue(true)).visible(this.sign::get)).build());
        this.wordWrap = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("word-wrap")).description("Prevents words from being cut in the middle of lines.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.File)).build());
        this.file = new File(MeteorClient.FOLDER, "bookbot.txt");
        if (!this.file.exists()) {
            this.file = null;
        }
        this.filters = BufferUtils.createPointerBuffer((int)1);
        ByteBuffer txtFilter = MemoryUtil.memASCII((CharSequence)"*.txt");
        this.filters.put(txtFilter);
        this.filters.rewind();
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WHorizontalList list = theme.horizontalList();
        WButton selectFile = list.add(theme.button("Select File")).widget();
        WLabel fileName = list.add(theme.label(this.file != null && this.file.exists() ? this.file.getName() : "No file selected.")).widget();
        selectFile.action = () -> {
            String path = TinyFileDialogs.tinyfd_openFileDialog((CharSequence)"Select File", (CharSequence)new File(MeteorClient.FOLDER, "bookbot.txt").getAbsolutePath(), (PointerBuffer)this.filters, null, (boolean)false);
            if (path != null) {
                this.file = new File(path);
                fileName.set(this.file.getName());
            }
        };
        return list;
    }

    @Override
    public void onActivate() {
        if (!(this.file != null && this.file.exists() || this.mode.get() != Mode.File)) {
            this.info("No file selected, please select a file in the GUI.", new Object[0]);
            this.toggle();
            return;
        }
        this.random = new Random();
        this.delayTimer = this.delay.get();
        this.bookCount = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Predicate<ItemStack> bookPredicate = i -> {
            WritableBookContent component = (WritableBookContent)i.get(DataComponents.WRITABLE_BOOK_CONTENT);
            return i.getItem() == Items.WRITABLE_BOOK && (component == null || component.pages().isEmpty());
        };
        FindItemResult writableBook = InvUtils.find(bookPredicate);
        if (!writableBook.found()) {
            this.toggle();
            return;
        }
        if (!InvUtils.testInMainHand(bookPredicate)) {
            InvUtils.move().from(writableBook.slot()).toHotbar(this.mc.player.getInventory().getSelectedSlot());
            return;
        }
        if (this.delayTimer > 0) {
            --this.delayTimer;
            return;
        }
        this.delayTimer = this.delay.get();
        if (this.mode.get() == Mode.Random) {
            switch (this.randomType.get().ordinal()) {
                case 0: {
                    this.writeBook(this.random.ints(33, 128).filter(i -> !Character.isWhitespace(i) && i != 13 && i != 10).iterator());
                    break;
                }
                case 1: {
                    this.writeBook(this.random.ints(33, 55296).filter(i -> !Character.isWhitespace(i) && i != 13 && i != 10).iterator());
                    break;
                }
                case 2: {
                    this.writePaperMcBook();
                }
            }
        } else if (this.mode.get() == Mode.File) {
            if (!(this.file != null && this.file.exists() || this.mode.get() != Mode.File)) {
                this.info("No file selected, please select a file in the GUI.", new Object[0]);
                this.toggle();
                return;
            }
            if (this.file.length() == 0L) {
                MutableComponent message = Component.literal((String)"");
                message.append((Component)Component.literal((String)"The bookbot file is empty! ").withStyle(ChatFormatting.RED));
                message.append((Component)Component.literal((String)"Click here to edit it.").setStyle(Style.EMPTY.applyFormats(new ChatFormatting[]{ChatFormatting.UNDERLINE, ChatFormatting.RED}).withClickEvent((ClickEvent)new ClickEvent.OpenFile(this.file.getAbsolutePath()))));
                this.info((Component)message);
                this.toggle();
                return;
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(this.file));){
                String line;
                StringBuilder file = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    file.append(line).append('\n');
                }
                reader.close();
                this.writeBook(file.toString().chars().iterator());
            }
            catch (IOException iOException) {
                this.error("Failed to read the file.", new Object[0]);
            }
        }
    }

    private void writeBook(PrimitiveIterator.OfInt chars) {
        int maxPages;
        ArrayList<String> pages = new ArrayList<String>();
        ArrayList<Filterable<Component>> filteredPages = new ArrayList<Filterable<Component>>();
        int n = maxPages = this.mode.get() == Mode.File ? 100 : this.pages.get();
        if (this.wordWrap.get().booleanValue() && this.mode.get() == Mode.File) {
            StringBuilder text = new StringBuilder();
            while (chars.hasNext()) {
                text.appendCodePoint(chars.nextInt());
            }
            List wrappedLines = this.mc.font.splitIgnoringLanguage((FormattedText)Component.literal((String)text.toString()), 114);
            this.processLinesToPages(wrappedLines, pages, filteredPages, maxPages);
        } else {
            StringBuilder page = new StringBuilder();
            for (int pageIndex = 0; pageIndex != maxPages; ++pageIndex) {
                for (int i = 0; i < this.characters.get() && chars.hasNext(); ++i) {
                    page.appendCodePoint(chars.nextInt());
                }
                if (page.isEmpty()) continue;
                String builtPage = page.toString();
                filteredPages.add((Filterable<Component>)Filterable.passThrough((Object)Component.nullToEmpty((String)builtPage)));
                pages.add(builtPage);
                page.setLength(0);
            }
        }
        this.createBook(pages, filteredPages);
    }

    private void writePaperMcBook() {
        ArrayList<String> pages = new ArrayList<String>();
        ArrayList<Filterable<Component>> filteredPages = new ArrayList<Filterable<Component>>();
        StringBuilder page = new StringBuilder();
        PrimitiveIterator.OfInt oneByte = this.random.ints(33, 128).iterator();
        PrimitiveIterator.OfInt twoBytes = this.random.ints(128, 2048).iterator();
        PrimitiveIterator.OfInt threeBytes = this.random.ints(2048, 55296).iterator();
        for (int pageIndex = 0; pageIndex < 100; ++pageIndex) {
            if (pageIndex < 50) {
                page.appendCodePoint(threeBytes.nextInt());
                for (i = 1; i < 1024; ++i) {
                    page.appendCodePoint(oneByte.nextInt());
                }
            } else if (pageIndex == 50) {
                for (i = 0; i < 110; ++i) {
                    page.appendCodePoint(threeBytes.nextInt());
                }
                page.appendCodePoint(twoBytes.nextInt());
                for (i = 0; i < 913; ++i) {
                    page.appendCodePoint(oneByte.nextInt());
                }
            } else {
                for (i = 0; i < 1024; ++i) {
                    page.appendCodePoint(threeBytes.nextInt());
                }
            }
            String builtPage = page.toString();
            filteredPages.add((Filterable<Component>)Filterable.passThrough((Object)Component.nullToEmpty((String)builtPage)));
            pages.add(builtPage);
            page.setLength(0);
        }
        this.createBook(pages, filteredPages);
    }

    private void processLinesToPages(List<FormattedText> lines, ArrayList<String> pages, ArrayList<Filterable<Component>> filteredPages, int maxPages) {
        int pageIndex = 0;
        int lineIndex = 0;
        StringBuilder currentPage = new StringBuilder();
        for (FormattedText line : lines) {
            String lineText = line.getString();
            if (!currentPage.isEmpty()) {
                currentPage.append('\n');
            }
            currentPage.append(lineText);
            if (++lineIndex != 14) continue;
            filteredPages.add((Filterable<Component>)Filterable.passThrough((Object)Component.nullToEmpty((String)currentPage.toString())));
            pages.add(currentPage.toString());
            currentPage.setLength(0);
            lineIndex = 0;
            if (++pageIndex != maxPages) continue;
            break;
        }
        if (!currentPage.isEmpty() && pageIndex < maxPages) {
            filteredPages.add((Filterable<Component>)Filterable.passThrough((Object)Component.nullToEmpty((String)currentPage.toString())));
            pages.add(currentPage.toString());
        }
    }

    private void createBook(ArrayList<String> pages, ArrayList<Filterable<Component>> filteredPages) {
        Object title = this.name.get();
        if (this.count.get().booleanValue() && this.bookCount != 0) {
            title = (String)title + " #" + this.bookCount;
        }
        this.mc.player.getMainHandItem().set(DataComponents.WRITTEN_BOOK_CONTENT, (Object)new WrittenBookContent(Filterable.passThrough((Object)title), this.mc.player.getGameProfile().name(), 0, filteredPages, true));
        this.mc.player.connection.send((Packet)new ServerboundEditBookPacket(this.mc.player.getInventory().getSelectedSlot(), pages, this.sign.get() != false ? Optional.of(title) : Optional.empty()));
        ++this.bookCount;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        if (this.file != null && this.file.exists()) {
            tag.putString("file", this.file.getAbsolutePath());
        }
        return tag;
    }

    @Override
    public Module fromTag(CompoundTag tag) {
        if (tag.contains("file")) {
            this.file = new File(tag.getStringOr("file", ""));
        }
        return super.fromTag(tag);
    }

    public static enum Mode {
        File,
        Random;

    }

    public static enum RandomType {
        Ascii,
        Utf8,
        PaperMC;

    }
}
