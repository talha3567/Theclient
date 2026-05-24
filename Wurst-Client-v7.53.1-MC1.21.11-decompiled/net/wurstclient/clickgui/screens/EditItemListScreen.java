package net.wurstclient.clickgui.screens;

import java.util.List;
import java.util.Objects;
import net.minecraft.class_11907;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1935;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_350;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_410;
import net.minecraft.class_4185;
import net.minecraft.class_4280;
import net.minecraft.class_437;
import net.minecraft.class_7923;
import net.wurstclient.settings.ItemListSetting;
import net.wurstclient.util.ItemUtils;
import net.wurstclient.util.RenderUtils;
import org.joml.Matrix3x2fStack;

public final class EditItemListScreen
extends class_437 {
    private final class_437 prevScreen;
    private final ItemListSetting itemList;
    private ListGui listGui;
    private class_342 itemNameField;
    private class_4185 addButton;
    private class_4185 removeButton;
    private class_4185 doneButton;
    private class_1792 itemToAdd;

    public EditItemListScreen(class_437 prevScreen, ItemListSetting itemList) {
        super((class_2561)class_2561.method_43470((String)""));
        this.prevScreen = prevScreen;
        this.itemList = itemList;
    }

    public void method_25426() {
        this.listGui = new ListGui(this.field_22787, this, this.itemList.getItemNames());
        this.method_25429((class_364)this.listGui);
        this.itemNameField = new class_342(this.field_22787.field_1772, this.field_22789 / 2 - 152, this.field_22790 - 56, 150, 20, (class_2561)class_2561.method_43470((String)""));
        this.method_25429((class_364)this.itemNameField);
        this.itemNameField.method_1880(256);
        this.addButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Add"), b -> {
            this.itemList.add(this.itemToAdd);
            this.field_22787.method_1507((class_437)this);
        }).method_46434(this.field_22789 / 2 - 2, this.field_22790 - 56, 30, 20).method_46431();
        this.method_37063((class_364)this.addButton);
        this.removeButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Remove Selected"), b -> {
            this.itemList.remove(this.itemList.getItemNames().indexOf(this.listGui.getSelectedBlockName()));
            this.field_22787.method_1507((class_437)this);
        }).method_46434(this.field_22789 / 2 + 52, this.field_22790 - 56, 100, 20).method_46431();
        this.method_37063((class_364)this.removeButton);
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Reset to Defaults"), b -> this.field_22787.method_1507((class_437)new class_410(b2 -> {
            if (b2) {
                this.itemList.resetToDefaults();
            }
            this.field_22787.method_1507((class_437)this);
        }, (class_2561)class_2561.method_43470((String)"Reset to Defaults"), (class_2561)class_2561.method_43470((String)"Are you sure?")))).method_46434(this.field_22789 - 108, 8, 100, 20).method_46431());
        this.doneButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Done"), b -> this.field_22787.method_1507(this.prevScreen)).method_46434(this.field_22789 / 2 - 100, this.field_22790 - 28, 200, 20).method_46431();
        this.method_37063((class_364)this.doneButton);
    }

    public boolean method_25402(class_11909 context, boolean doubleClick) {
        this.itemNameField.method_25402(context, doubleClick);
        return super.method_25402(context, doubleClick);
    }

    public boolean method_25404(class_11908 context) {
        switch (context.comp_4795()) {
            case 257: {
                if (!this.addButton.field_22763) break;
                this.addButton.method_25306((class_11907)context);
                break;
            }
            case 261: {
                if (this.itemNameField.method_25370()) break;
                this.removeButton.method_25306((class_11907)context);
                break;
            }
            case 256: {
                this.doneButton.method_25306((class_11907)context);
                break;
            }
        }
        return super.method_25404(context);
    }

    public void method_25393() {
        String nameOrId = this.itemNameField.method_1882().toLowerCase();
        this.itemToAdd = ItemUtils.getItemFromNameOrID(nameOrId);
        this.addButton.field_22763 = this.itemToAdd != null;
        this.removeButton.field_22763 = this.listGui.method_25334() != null;
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        Matrix3x2fStack matrixStack = context.method_51448();
        this.listGui.method_25394(context, mouseX, mouseY, partialTicks);
        context.method_25300(this.field_22787.field_1772, this.itemList.getName() + " (" + this.itemList.getItemNames().size() + ")", this.field_22789 / 2, 12, -1);
        matrixStack.pushMatrix();
        this.itemNameField.method_25394(context, mouseX, mouseY, partialTicks);
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
        }
        context.field_59826.method_71067();
        matrixStack.pushMatrix();
        matrixStack.translate((float)(-64 + this.field_22789 / 2 - 152), 0.0f);
        if (this.itemNameField.method_1882().isEmpty() && !this.itemNameField.method_25370()) {
            context.method_25303(this.field_22787.field_1772, "item name or ID", 68, this.field_22790 - 50, -8355712);
        }
        int border = this.itemNameField.method_25370() ? -1 : -6250336;
        int black = -16777216;
        context.method_25294(48, this.field_22790 - 56, 64, this.field_22790 - 36, border);
        context.method_25294(49, this.field_22790 - 55, 65, this.field_22790 - 37, black);
        context.method_25294(214, this.field_22790 - 56, 244, this.field_22790 - 55, border);
        context.method_25294(214, this.field_22790 - 37, 244, this.field_22790 - 36, border);
        context.method_25294(244, this.field_22790 - 56, 246, this.field_22790 - 36, border);
        context.method_25294(213, this.field_22790 - 55, 243, this.field_22790 - 52, black);
        context.method_25294(213, this.field_22790 - 40, 243, this.field_22790 - 37, black);
        context.method_25294(213, this.field_22790 - 55, 216, this.field_22790 - 37, black);
        context.method_25294(242, this.field_22790 - 55, 245, this.field_22790 - 37, black);
        matrixStack.popMatrix();
        RenderUtils.drawItem(context, this.itemToAdd == null ? class_1799.field_8037 : new class_1799((class_1935)this.itemToAdd), this.field_22789 / 2 - 164, this.field_22790 - 52, false);
        matrixStack.popMatrix();
    }

    public boolean method_25421() {
        return false;
    }

    public boolean method_25422() {
        return false;
    }

    private final class ListGui
    extends class_4280<Entry> {
        public ListGui(class_310 minecraft, EditItemListScreen screen, List<String> list) {
            super(minecraft, screen.field_22789, screen.field_22790 - 96, 36, 30);
            list.stream().map(x$0 -> new Entry((String)x$0)).forEach(x$0 -> this.method_25321((class_350.class_351)x$0));
        }

        public String getSelectedBlockName() {
            Entry selected = (Entry)this.method_25334();
            return selected != null ? selected.itemName : null;
        }
    }

    private final class Entry
    extends class_4280.class_4281<Entry> {
        private final String itemName;

        public Entry(String itemName) {
            this.itemName = Objects.requireNonNull(itemName);
        }

        public class_2561 method_37006() {
            class_1792 item = (class_1792)class_7923.field_41178.method_63535(class_2960.method_60654((String)this.itemName));
            class_1799 stack = new class_1799((class_1935)item);
            return class_2561.method_43469((String)"narrator.select", (Object[])new Object[]{"Item " + this.getDisplayName(stack) + ", " + this.itemName + ", " + this.getIdText(item)});
        }

        public void method_25343(class_332 context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int x = this.method_73380();
            int y = this.method_73382();
            class_1792 item = (class_1792)class_7923.field_41178.method_63535(class_2960.method_60654((String)this.itemName));
            class_1799 stack = new class_1799((class_1935)item);
            class_327 tr = ((EditItemListScreen)EditItemListScreen.this).field_22787.field_1772;
            RenderUtils.drawItem(context, stack, x + 1, y + 1, true);
            context.method_51433(tr, this.getDisplayName(stack), x + 28, y, -986896, false);
            context.method_51433(tr, this.itemName, x + 28, y + 9, -6250336, false);
            context.method_51433(tr, this.getIdText(item), x + 28, y + 18, -6250336, false);
        }

        private String getDisplayName(class_1799 stack) {
            return stack.method_7960() ? "\u00a7ounknown item\u00a7r" : stack.method_7964().getString();
        }

        private String getIdText(class_1792 item) {
            return "ID: " + class_7923.field_41178.method_10206((Object)item);
        }
    }
}
