package net.wurstclient.hacks.templatetool.states;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import net.minecraft.class_11907;
import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_4286;
import net.minecraft.class_437;
import net.minecraft.class_5244;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.TemplateToolHack;
import net.wurstclient.hacks.templatetool.TemplateToolState;
import net.wurstclient.hacks.templatetool.states.SavingFileState;

public final class ChooseNameState
extends TemplateToolState {
    @Override
    public void onEnter(TemplateToolHack hack) {
        MC.method_1507((class_437)new ChooseNameScreen(hack));
    }

    @Override
    public void onExit(TemplateToolHack hack) {
        MC.method_1507(null);
    }

    @Override
    protected String getMessage(TemplateToolHack hack) {
        File file = hack.getFile();
        if (file != null && file.exists()) {
            return "WARNING: This file already exists.";
        }
        return "Choose a name for this template.";
    }

    public static final class ChooseNameScreen
    extends class_437 {
        private static final WurstClient WURST = WurstClient.INSTANCE;
        private final TemplateToolHack hack;
        private class_342 nameField;
        private class_4286 includeTypesBox;
        private class_4185 doneButton;
        private class_4185 cancelButton;

        public ChooseNameScreen(TemplateToolHack hack) {
            super(class_5244.field_39003);
            this.hack = hack;
        }

        public void method_25426() {
            class_327 tr = this.field_22787.field_1772;
            int middleX = this.field_22789 / 2;
            int middleY = this.field_22790 / 2;
            this.nameField = new class_342(tr, middleX - 99, middleY + 16, 198, 16, (class_2561)class_2561.method_43473());
            this.nameField.method_1858(false);
            this.nameField.method_1880(32);
            this.nameField.method_25365(true);
            this.nameField.method_1868(-1);
            this.method_25429((class_364)this.nameField);
            this.method_25395((class_364)this.nameField);
            this.includeTypesBox = class_4286.method_54787((class_2561)class_2561.method_43470((String)"Include block types"), (class_327)tr).method_54789(middleX - 99, middleY + 32).method_54794(true).method_54788();
            this.method_37063((class_364)this.includeTypesBox);
            this.doneButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Done"), b -> this.done()).method_46434(middleX - 75, middleY + 56, 150, 20).method_46431();
            this.method_37063((class_364)this.doneButton);
            this.cancelButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Cancel"), b -> this.cancel()).method_46434(middleX - 50, middleY + 80, 100, 15).method_46431();
            this.method_37063((class_364)this.cancelButton);
        }

        private void done() {
            if (this.hack.getFile() == null) {
                return;
            }
            this.hack.setBlockTypesEnabled(this.includeTypesBox.method_20372());
            this.hack.setState(new SavingFileState());
        }

        private void cancel() {
            this.hack.setEnabled(false);
        }

        public void method_25393() {
            if (this.nameField.method_1882().isEmpty()) {
                this.hack.setFile(null);
            } else {
                try {
                    Path folder = ChooseNameScreen.WURST.getHax().autoBuildHack.getFolder();
                    Path file = folder.resolve(this.nameField.method_1882() + ".json");
                    this.hack.setFile(file.toFile());
                }
                catch (InvalidPathException e) {
                    this.hack.setFile(null);
                }
            }
            this.doneButton.field_22763 = this.hack.getFile() != null;
        }

        public boolean method_25404(class_11908 context) {
            switch (context.comp_4795()) {
                case 256: {
                    this.cancelButton.method_25306((class_11907)context);
                    break;
                }
                case 257: {
                    this.doneButton.method_25306((class_11907)context);
                }
            }
            return super.method_25404(context);
        }

        public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
            super.method_25394(context, mouseX, mouseY, partialTicks);
            int middleX = this.field_22789 / 2;
            int middleY = this.field_22790 / 2;
            int x1 = middleX - 100;
            int y1 = middleY + 15;
            int x2 = middleX + 100;
            int y2 = middleY + 26;
            context.method_25294(x1, y1, x2, y2, Integer.MIN_VALUE);
            this.nameField.method_25394(context, mouseX, mouseY, partialTicks);
        }

        public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        }

        public boolean method_25421() {
            return false;
        }

        public boolean method_25422() {
            return false;
        }
    }
}
