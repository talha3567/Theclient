package net.wurstclient.hacks.templatetool.states;

import net.minecraft.class_2338;
import net.wurstclient.hacks.TemplateToolHack;
import net.wurstclient.hacks.templatetool.SelectPositionState;
import net.wurstclient.hacks.templatetool.TemplateToolState;
import net.wurstclient.hacks.templatetool.states.CreatingTemplateState;

public final class SelectOriginState
extends SelectPositionState {
    @Override
    protected String getDefaultMessage() {
        return "Select the first block to be placed by AutoBuild.";
    }

    @Override
    protected class_2338 getSelectedPos(TemplateToolHack hack) {
        return hack.getOriginPos();
    }

    @Override
    protected void setSelectedPos(TemplateToolHack hack, class_2338 pos) {
        hack.setOriginPos(pos);
    }

    @Override
    protected TemplateToolState getNextState() {
        return new CreatingTemplateState();
    }
}
