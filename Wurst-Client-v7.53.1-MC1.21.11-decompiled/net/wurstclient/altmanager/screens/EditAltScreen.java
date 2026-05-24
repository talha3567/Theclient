package net.wurstclient.altmanager.screens;

import net.minecraft.class_2561;
import net.minecraft.class_437;
import net.wurstclient.altmanager.Alt;
import net.wurstclient.altmanager.AltManager;
import net.wurstclient.altmanager.MojangAlt;
import net.wurstclient.altmanager.screens.AltEditorScreen;

public final class EditAltScreen
extends AltEditorScreen {
    private final AltManager altManager;
    private Alt editedAlt;

    public EditAltScreen(class_437 prevScreen, AltManager altManager, Alt editedAlt) {
        super(prevScreen, (class_2561)class_2561.method_43470((String)"Edit Alt"));
        this.altManager = altManager;
        this.editedAlt = editedAlt;
    }

    @Override
    protected String getDefaultNameOrEmail() {
        return this.editedAlt instanceof MojangAlt ? ((MojangAlt)this.editedAlt).getEmail() : this.editedAlt.getName();
    }

    @Override
    protected String getDefaultPassword() {
        return this.editedAlt instanceof MojangAlt ? ((MojangAlt)this.editedAlt).getPassword() : "";
    }

    @Override
    protected String getDoneButtonText() {
        return "Save";
    }

    @Override
    protected void pressDoneButton() {
        this.altManager.edit(this.editedAlt, this.getNameOrEmail(), this.getPassword());
        this.field_22787.method_1507(this.prevScreen);
    }
}
