package net.wurstclient.altmanager.screens;

import net.minecraft.class_2561;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.wurstclient.altmanager.LoginException;
import net.wurstclient.altmanager.LoginManager;
import net.wurstclient.altmanager.MicrosoftLoginManager;
import net.wurstclient.altmanager.screens.AltEditorScreen;

public final class DirectLoginScreen
extends AltEditorScreen {
    public DirectLoginScreen(class_437 prevScreen) {
        super(prevScreen, (class_2561)class_2561.method_43470((String)"Direct Login"));
    }

    @Override
    protected String getDoneButtonText() {
        return this.getPassword().isEmpty() ? "Change Cracked Name" : "Login with Password";
    }

    @Override
    protected void pressDoneButton() {
        String nameOrEmail = this.getNameOrEmail();
        String password = this.getPassword();
        if (password.isEmpty()) {
            LoginManager.changeCrackedName(nameOrEmail);
        } else {
            try {
                MicrosoftLoginManager.login(nameOrEmail, password);
            }
            catch (LoginException e) {
                this.message = "\u00a7c\u00a7lMicrosoft:\u00a7c " + e.getMessage();
                this.doErrorEffect();
                return;
            }
        }
        this.message = "";
        this.field_22787.method_1507((class_437)new class_442());
    }
}
