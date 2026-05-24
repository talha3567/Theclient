package net.wurstclient.hacks;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2680;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.events.GUIRenderListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.templatetool.TemplateToolState;
import net.wurstclient.hacks.templatetool.states.SelectBoxStartState;
import net.wurstclient.util.RenderUtils;

public final class TemplateToolHack
extends Hack
implements UpdateListener,
RenderListener,
GUIRenderListener {
    private TemplateToolState state;
    private class_2338 startPos;
    private class_2338 endPos;
    private class_2338 originPos;
    private final LinkedHashMap<class_2338, class_2680> nonEmptyBlocks = new LinkedHashMap();
    private final LinkedHashSet<class_2338> sortedBlocks = new LinkedHashSet();
    private boolean blockTypesEnabled;
    private File file;

    public TemplateToolHack() {
        super("TemplateTool");
        this.setCategory(Category.BLOCKS);
    }

    @Override
    public void onEnable() {
        TemplateToolHack.WURST.getHax().autoBuildHack.setEnabled(false);
        TemplateToolHack.WURST.getHax().instaBuildHack.setEnabled(false);
        TemplateToolHack.WURST.getHax().bowAimbotHack.setEnabled(false);
        TemplateToolHack.WURST.getHax().excavatorHack.setEnabled(false);
        this.setState(new SelectBoxStartState());
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(RenderListener.class, this);
        EVENTS.add(GUIRenderListener.class, this);
    }

    @Override
    public void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        EVENTS.remove(GUIRenderListener.class, this);
        this.setState(null);
        this.startPos = null;
        this.endPos = null;
        this.originPos = null;
        this.nonEmptyBlocks.clear();
        this.sortedBlocks.clear();
        this.blockTypesEnabled = false;
        this.file = null;
    }

    @Override
    public void onUpdate() {
        if (this.state != null) {
            this.state.onUpdate(this);
        }
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        int black = Integer.MIN_VALUE;
        int green15 = 637599488;
        if (this.startPos != null && this.endPos != null) {
            class_238 bounds = class_238.method_54784((class_2338)this.startPos, (class_2338)this.endPos).method_1011(0.0625);
            RenderUtils.drawOutlinedBox(matrixStack, bounds, black, true);
        }
        if (this.originPos != null) {
            class_238 box = new class_238(this.originPos).method_1011(0.0625);
            RenderUtils.drawOutlinedBox(matrixStack, box, black, false);
            RenderUtils.drawSolidBox(matrixStack, box, green15, false);
        }
        if (this.state != null) {
            this.state.onRender(this, matrixStack, partialTicks);
        }
    }

    @Override
    public void onRenderGUI(class_332 context, float partialTicks) {
        if (this.state != null) {
            this.state.onRenderGUI(this, context, partialTicks);
        }
    }

    public void setState(TemplateToolState state) {
        if (this.state != null) {
            this.state.onExit(this);
        }
        this.state = state;
        if (state != null) {
            state.onEnter(this);
        }
    }

    public class_2338 getStartPos() {
        return this.startPos;
    }

    public void setStartPos(class_2338 pos) {
        this.startPos = pos;
    }

    public class_2338 getEndPos() {
        return this.endPos;
    }

    public void setEndPos(class_2338 pos) {
        this.endPos = pos;
    }

    public class_2338 getOriginPos() {
        return this.originPos;
    }

    public void setOriginPos(class_2338 pos) {
        this.originPos = pos;
    }

    public LinkedHashMap<class_2338, class_2680> getNonEmptyBlocks() {
        return this.nonEmptyBlocks;
    }

    public LinkedHashSet<class_2338> getSortedBlocks() {
        return this.sortedBlocks;
    }

    public boolean areBlockTypesEnabled() {
        return this.blockTypesEnabled;
    }

    public void setBlockTypesEnabled(boolean blockTypesEnabled) {
        this.blockTypesEnabled = blockTypesEnabled;
    }

    public File getFile() {
        return this.file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
