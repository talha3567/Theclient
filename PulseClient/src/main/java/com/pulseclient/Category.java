package com.pulseclient;

public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    RENDER("Render"),
    WORLD("World"),
    MISC("Misc");

    public final String name;

    Category(String name) {
        this.name = name;
    }
}
