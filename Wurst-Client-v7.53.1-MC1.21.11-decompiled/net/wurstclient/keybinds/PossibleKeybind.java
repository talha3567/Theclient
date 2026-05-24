package net.wurstclient.keybinds;

public final class PossibleKeybind {
    private final String command;
    private final String description;

    public PossibleKeybind(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public String getCommand() {
        return this.command;
    }

    public String getDescription() {
        return this.description;
    }
}
