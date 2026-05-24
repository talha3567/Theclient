package net.wurstclient;

import com.google.gson.JsonArray;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeSet;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.wurstclient.WurstClient;
import net.wurstclient.commands.FriendsCmd;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;

public class FriendsList {
    private final TreeSet<String> friends = new TreeSet();
    private Path path;

    public FriendsList(Path path) {
        this.path = path;
    }

    public void addAndSave(String name) {
        this.friends.add(name);
        this.save();
    }

    public void removeAndSave(String name) {
        this.friends.remove(name);
        this.save();
    }

    public void removeAllAndSave() {
        this.friends.clear();
        this.save();
    }

    public void middleClick(class_1297 entity) {
        if (entity == null || !(entity instanceof class_1657)) {
            return;
        }
        FriendsCmd friendsCmd = WurstClient.INSTANCE.getCmds().friendsCmd;
        CheckboxSetting middleClickFriends = friendsCmd.getMiddleClickFriends();
        if (!middleClickFriends.isChecked()) {
            return;
        }
        String name = entity.method_5477().getString();
        if (this.contains(name)) {
            this.removeAndSave(name);
        } else {
            this.addAndSave(name);
        }
    }

    public boolean contains(String name) {
        return this.friends.contains(name);
    }

    public boolean isFriend(class_1297 entity) {
        return entity != null && this.contains(entity.method_5477().getString());
    }

    public ArrayList<String> toList() {
        return new ArrayList<String>(this.friends);
    }

    public void load() {
        try {
            this.friends.clear();
            this.friends.addAll(JsonUtils.parseFileToArray(this.path).getAllStrings());
        }
        catch (NoSuchFileException noSuchFileException) {
        }
        catch (IOException | JsonException e) {
            System.out.println("Couldn't load " + String.valueOf(this.path.getFileName()));
            e.printStackTrace();
        }
        this.save();
    }

    private void save() {
        try {
            JsonUtils.toJson(this.createJson(), this.path);
        }
        catch (IOException | JsonException e) {
            System.out.println("Couldn't save " + String.valueOf(this.path.getFileName()));
            e.printStackTrace();
        }
    }

    private JsonArray createJson() {
        JsonArray json = new JsonArray();
        this.friends.forEach(json::add);
        return json;
    }
}
