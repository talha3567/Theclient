package net.wurstclient.hacks.templatetool.states;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2680;
import net.minecraft.class_5250;
import net.wurstclient.hacks.TemplateToolHack;
import net.wurstclient.hacks.templatetool.TemplateToolState;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.json.JsonUtils;

public final class SavingFileState
extends TemplateToolState {
    @Override
    protected String getMessage(TemplateToolHack hack) {
        return "Saving file...";
    }

    @Override
    public void onEnter(TemplateToolHack hack) {
        JsonObject json = hack.areBlockTypesEnabled() ? this.createV2Json(hack) : this.createV1Json(hack);
        try (PrintWriter save = new PrintWriter(new FileWriter(hack.getFile()));){
            save.print(JsonUtils.GSON.toJson(json));
        }
        catch (IOException e) {
            e.printStackTrace();
            ChatUtils.error("File could not be saved.");
            hack.setEnabled(false);
            return;
        }
        class_5250 message = class_2561.method_43470((String)"Saved template as ");
        class_2558.class_10607 event = new class_2558.class_10607(hack.getFile().getParentFile().getAbsolutePath());
        class_5250 link = class_2561.method_43470((String)hack.getFile().getName()).method_27694(arg_0 -> SavingFileState.lambda$onEnter$0((class_2558)event, arg_0));
        message.method_10852((class_2561)link);
        ChatUtils.component((class_2561)message);
        hack.setEnabled(false);
    }

    private JsonObject createV2Json(TemplateToolHack hack) {
        JsonObject json = new JsonObject();
        json.addProperty("version", 2);
        class_2350 front = SavingFileState.MC.field_1724.method_5735();
        class_2338 origin = hack.getOriginPos();
        JsonArray jsonBlocks = new JsonArray();
        for (class_2338 pos : hack.getSortedBlocks()) {
            class_2680 state = hack.getNonEmptyBlocks().get(pos);
            if (state == null) {
                throw new IllegalStateException("Block at " + String.valueOf(pos) + " exists in sortedBlocks but not in nonEmptyBlocks.");
            }
            JsonObject jsonBlock = new JsonObject();
            jsonBlock.addProperty("block", BlockUtils.getName(state.method_26204()));
            JsonArray jsonPos = new JsonArray();
            pos = this.toTemplatePos(pos, origin, front);
            jsonPos.add(pos.method_10263());
            jsonPos.add(pos.method_10264());
            jsonPos.add(pos.method_10260());
            jsonBlock.add("pos", jsonPos);
            jsonBlocks.add(jsonBlock);
        }
        json.add("blocks", jsonBlocks);
        return json;
    }

    private JsonObject createV1Json(TemplateToolHack hack) {
        JsonObject json = new JsonObject();
        json.addProperty("version", 1);
        class_2350 front = SavingFileState.MC.field_1724.method_5735();
        class_2338 origin = hack.getOriginPos();
        JsonArray jsonBlocks = new JsonArray();
        for (class_2338 pos : hack.getSortedBlocks()) {
            JsonArray jsonPos = new JsonArray();
            pos = this.toTemplatePos(pos, origin, front);
            jsonPos.add(pos.method_10263());
            jsonPos.add(pos.method_10264());
            jsonPos.add(pos.method_10260());
            jsonBlocks.add(jsonPos);
        }
        json.add("blocks", jsonBlocks);
        return json;
    }

    private class_2338 toTemplatePos(class_2338 pos, class_2338 origin, class_2350 front) {
        class_2338 translated = pos.method_10059((class_2382)origin);
        class_2350 left = front.method_10160();
        int leftDist = translated.method_10263() * left.method_10148() + translated.method_10260() * left.method_10165();
        int upDist = translated.method_10264();
        int frontDist = translated.method_10263() * front.method_10148() + translated.method_10260() * front.method_10165();
        return new class_2338(leftDist, upDist, frontDist);
    }

    private static /* synthetic */ class_2583 lambda$onEnter$0(class_2558 event, class_2583 s) {
        return s.method_30938(Boolean.valueOf(true)).method_10958(event);
    }
}
