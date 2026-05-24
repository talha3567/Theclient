package meteordevelopment.meteorclient.gui.tabs.builtin;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WConfirmedButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WConfirmedMinus;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.profiles.Profile;
import meteordevelopment.meteorclient.systems.profiles.Profiles;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.render.prompts.OkPrompt;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class ProfilesTab
extends Tab {
    private static final PointerBuffer filters = BufferUtils.createPointerBuffer((int)1);

    public ProfilesTab() {
        super("Profiles");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new ProfilesScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof ProfilesScreen;
    }

    static {
        ByteBuffer pngFilter = MemoryUtil.memASCII((CharSequence)"*.nbt");
        filters.put(pngFilter);
        filters.rewind();
    }

    private static class ProfilesScreen
    extends WindowTabScreen {
        public ProfilesScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
        }

        @Override
        public void initWidgets() {
            WTable table = this.add(this.theme.table()).expandX().minWidth(400.0).widget();
            this.initTable(table);
            this.add(this.theme.horizontalSeparator()).expandX();
            WHorizontalList l = this.add(this.theme.horizontalList()).expandX().widget();
            WButton create = l.add(this.theme.button("Create")).expandX().widget();
            create.tooltip = "Create new profile";
            create.action = () -> MeteorClient.mc.setScreen((Screen)new EditProfileScreen(this.theme, null, this::reload));
            WButton importBtn = l.add(this.theme.button("Import")).expandX().widget();
            importBtn.tooltip = "Import profile";
            importBtn.action = () -> {
                try {
                    Profile imported = this.importProfile();
                    if (imported != null) {
                        MeteorClient.LOG.info("Successfully imported profile '{}'.", (Object)imported.name.get());
                    }
                    this.reload();
                }
                catch (IOException e) {
                    MeteorClient.LOG.error("Error importing profile", e);
                    ((OkPrompt)((OkPrompt)((OkPrompt)((OkPrompt)OkPrompt.create().title("Failure importing profile")).message("There was an error importing the profile.")).message("Error: %d", e.getMessage())).dontShowAgainCheckboxVisible(false)).show();
                }
            };
        }

        private void initTable(WTable table) {
            table.clear();
            if (Profiles.get().isEmpty()) {
                return;
            }
            for (Profile profile : Profiles.get()) {
                table.add(this.theme.label(profile.name.get())).expandCellX();
                WConfirmedButton save = this.theme.confirmedButton("Save", "Confirm");
                save.action = profile::save;
                table.add(save).right();
                WButton load = table.add(this.theme.button("Load")).widget();
                load.action = profile::load;
                WButton export = table.add(this.theme.button("Export")).widget();
                export.action = () -> MeteorClient.mc.setScreen((Screen)new ExportProfileScreen(this.theme, profile));
                WButton edit = table.add(this.theme.button(GuiRenderer.EDIT)).widget();
                edit.action = () -> MeteorClient.mc.setScreen((Screen)new EditProfileScreen(this.theme, profile, this::reload));
                WConfirmedMinus remove = table.add(this.theme.confirmedMinus()).widget();
                remove.action = () -> {
                    Profiles.get().remove(profile);
                    this.reload();
                };
                table.row();
            }
        }

        private Profile importProfile() throws IOException {
            String file = TinyFileDialogs.tinyfd_openFileDialog((CharSequence)"Select profile to import", null, (PointerBuffer)filters, null, (boolean)false);
            if (file == null) {
                return null;
            }
            File profileFile = new File(file);
            CompoundTag nbt = NbtIo.read((Path)profileFile.toPath());
            Profile p = new Profile();
            p.name.set(nbt.getStringOr("name", profileFile.getName()));
            p.getFile().mkdirs();
            nbt.remove("name");
            for (Map.Entry entry : nbt.entrySet()) {
                String filename;
                switch (filename = (String)entry.getKey()) {
                    case "hud.nbt": {
                        p.hud.set(true);
                        break;
                    }
                    case "macros.nbt": {
                        p.macros.set(true);
                        break;
                    }
                    case "modules.nbt": {
                        p.modules.set(true);
                        break;
                    }
                    default: {
                        if (!filename.endsWith(".nbt")) break;
                        p.waypoints.set(true);
                    }
                }
                File f = new File(p.getFile(), filename);
                NbtIo.writeUnnamedTagWithFallback((Tag)((Tag)entry.getValue()), (DataOutput)new DataOutputStream(new FileOutputStream(f)));
            }
            Profiles.get().getAll().add(p);
            Profiles.get().save();
            return p;
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(Profiles.get());
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(Profiles.get());
        }
    }

    private static class ExportProfileScreen
    extends WindowScreen {
        private final Profile profile;

        public ExportProfileScreen(GuiTheme theme, Profile profile) {
            super(theme, "Export Profile");
            this.profile = profile;
        }

        @Override
        public void initWidgets() {
            this.add(this.theme.label("Select which profile settings to export."));
            WContainer settingsContainer = this.add(this.theme.verticalList()).expandX().minWidth(400.0).widget();
            settingsContainer.add(this.theme.horizontalSeparator()).expandX().widget();
            WCheckbox hud = this.addBool(settingsContainer, this.profile.settings.get("hud", Boolean.class));
            WCheckbox macros = this.addBool(settingsContainer, this.profile.settings.get("macros", Boolean.class));
            WCheckbox modules = this.addBool(settingsContainer, this.profile.settings.get("modules", Boolean.class));
            WCheckbox waypoints = this.addBool(settingsContainer, this.profile.settings.get("waypoints", Boolean.class));
            this.add(this.theme.horizontalSeparator()).expandX().widget();
            WButton export = this.add(this.theme.button("Export profile")).expandX().widget();
            export.action = () -> {
                this.exportProfile(this.profile, hud.checked, macros.checked, modules.checked, waypoints.checked);
                this.onClose();
            };
        }

        private WCheckbox addBool(WContainer container, Setting<Boolean> setting) {
            WHorizontalList boolList = container.add(this.theme.horizontalList()).expandX().widget();
            boolList.add(this.theme.label((String)setting.title)).widget().tooltip = setting.description;
            WCheckbox c = this.theme.checkbox(setting.get());
            boolList.add(c).expandCellX().right();
            return c;
        }

        private void exportProfile(Profile profile, boolean hud, boolean macros, boolean modules, boolean waypoints) {
            String path = TinyFileDialogs.tinyfd_saveFileDialog((CharSequence)"Save profile", (CharSequence)profile.name.get(), (PointerBuffer)filters, null);
            if (path == null) {
                return;
            }
            Path p = Path.of((String)(path.endsWith(".nbt") ? path : path + ".nbt"), new String[0]);
            CompoundTag nbt = new CompoundTag();
            nbt.putString("name", profile.name.get());
            try {
                for (File f : profile.getFile().listFiles()) {
                    if (f.getName().equals("hud.nbt") && hud || f.getName().equals("macros.nbt") && macros || f.getName().equals("modules.nbt") && modules) {
                        nbt.put(f.getName(), (Tag)NbtIo.read((Path)f.toPath()));
                        continue;
                    }
                    if (!f.getName().endsWith(".nbt") || !waypoints) continue;
                    nbt.put(f.getName(), (Tag)NbtIo.read((Path)f.toPath()));
                }
                NbtIo.write((CompoundTag)nbt, (Path)p);
            }
            catch (IOException e) {
                MeteorClient.LOG.error("Error serialising profile {} to a file", (Object)profile.name.get(), (Object)e);
                ((OkPrompt)((OkPrompt)((OkPrompt)((OkPrompt)OkPrompt.create().title("Failure exporting profile")).message("There was an error serialising or exporting the profile %d.", profile.name.get())).message("Error: %d", e.getMessage())).dontShowAgainCheckboxVisible(false)).show();
            }
        }
    }

    private static class EditProfileScreen
    extends WindowScreen {
        private WContainer settingsContainer;
        private final Profile profile;
        private final boolean isNew;
        private final Runnable action;

        public EditProfileScreen(GuiTheme theme, Profile profile, Runnable action) {
            super(theme, profile == null ? "New Profile" : "Edit Profile");
            this.isNew = profile == null;
            this.profile = this.isNew ? new Profile() : profile;
            this.action = action;
        }

        @Override
        public void initWidgets() {
            this.settingsContainer = this.add(this.theme.verticalList()).expandX().minWidth(400.0).widget();
            this.settingsContainer.add(this.theme.settings(this.profile.settings)).expandX();
            this.add(this.theme.horizontalSeparator()).expandX();
            WButton save = this.add(this.theme.button(this.isNew ? "Create" : "Save")).expandX().widget();
            this.enterAction = save.action = () -> {
                if (this.profile.name.get().isEmpty()) {
                    return;
                }
                if (this.isNew) {
                    for (Profile p : Profiles.get()) {
                        if (!this.profile.equals(p)) continue;
                        return;
                    }
                }
                ArrayList<String> valid = new ArrayList<String>();
                for (String address : this.profile.loadOnJoin.get()) {
                    if (!Utils.resolveAddress(address)) continue;
                    valid.add(address);
                }
                this.profile.loadOnJoin.set(valid);
                if (this.isNew) {
                    Profiles.get().add(this.profile);
                } else {
                    Profiles.get().save();
                }
                this.onClose();
            };
        }

        public void tick() {
            super.tick();
            this.profile.settings.tick(this.settingsContainer, this.theme);
        }

        @Override
        protected void onClosed() {
            if (this.action != null) {
                this.action.run();
            }
        }
    }
}
