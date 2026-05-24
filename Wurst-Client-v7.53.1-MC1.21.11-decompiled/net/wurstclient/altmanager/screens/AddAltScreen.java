package net.wurstclient.altmanager.screens;

import net.minecraft.class_2561;
import net.minecraft.class_437;
import net.wurstclient.altmanager.AltManager;
import net.wurstclient.altmanager.CrackedAlt;
import net.wurstclient.altmanager.MojangAlt;
import net.wurstclient.altmanager.screens.AltEditorScreen;

public final class AddAltScreen
extends AltEditorScreen {
    private final AltManager altManager;

    public AddAltScreen(class_437 prevScreen, AltManager altManager) {
        super(prevScreen, (class_2561)class_2561.method_43470((String)"New Alt"));
        this.altManager = altManager;
    }

    @Override
    protected String getDoneButtonText() {
        return this.getPassword().isEmpty() ? "Add Cracked Alt" : "Add Premium Alt";
    }

    @Override
    protected void pressDoneButton() {
        String nameOrEmail = this.getNameOrEmail();
        String password = this.getPassword();
        if (password.isEmpty()) {
            this.altManager.add(new CrackedAlt(nameOrEmail));
        } else {
            this.altManager.add(new MojangAlt(nameOrEmail, password));
        }
        this.field_22787.method_1507(this.prevScreen);
    }
}
