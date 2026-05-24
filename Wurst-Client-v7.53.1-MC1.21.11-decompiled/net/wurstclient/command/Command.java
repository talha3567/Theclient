package net.wurstclient.command;

import java.util.Objects;
import net.wurstclient.Category;
import net.wurstclient.Feature;
import net.wurstclient.command.CmdException;
import net.wurstclient.util.ChatUtils;

public abstract class Command
extends Feature {
    private final String name;
    private final String description;
    private final String[] syntax;
    private Category category;

    public Command(String name, String description, String ... syntax) {
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        Objects.requireNonNull(syntax);
        if (syntax.length > 0) {
            syntax[0] = "Syntax: " + syntax[0];
        }
        this.syntax = syntax;
        if (name.contains(" ")) {
            throw new IllegalArgumentException("Feature name must not contain spaces: " + name);
        }
    }

    public abstract void call(String[] var1) throws CmdException;

    @Override
    public final String getName() {
        return "." + this.name;
    }

    @Override
    public String getPrimaryAction() {
        return "";
    }

    @Override
    public final String getDescription() {
        Object description = this.description;
        if (this.syntax.length > 0) {
            description = (String)description + "\n";
        }
        for (String line : this.syntax) {
            description = (String)description + "\n" + line;
        }
        return description;
    }

    public final String[] getSyntax() {
        return this.syntax;
    }

    public final void printHelp() {
        for (String line : this.description.split("\n")) {
            ChatUtils.message(line);
        }
        for (String line : this.syntax) {
            ChatUtils.message(line);
        }
    }

    @Override
    public final Category getCategory() {
        return this.category;
    }

    protected final void setCategory(Category category) {
        this.category = category;
    }
}
