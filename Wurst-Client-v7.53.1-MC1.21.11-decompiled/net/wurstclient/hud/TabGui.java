package net.wurstclient.hud;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.wurstclient.Category;
import net.wurstclient.Feature;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.events.KeyPressListener;
import net.wurstclient.hacks.TooManyHaxHack;
import net.wurstclient.other_features.TabGuiOtf;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.RenderUtils;
import org.joml.Matrix3x2fStack;

public final class TabGui
implements KeyPressListener {
    private static final WurstClient WURST = WurstClient.INSTANCE;
    private static final class_310 MC = WurstClient.MC;
    private final ArrayList<Tab> tabs = new ArrayList();
    private final TabGuiOtf tabGuiOtf;
    private int width;
    private int height;
    private int selected;
    private boolean tabOpened;

    public TabGui() {
        this.tabGuiOtf = WurstClient.INSTANCE.getOtfs().tabGuiOtf;
        WURST.getEventManager().add(KeyPressListener.class, this);
        LinkedHashMap<Category, Tab> tabMap = new LinkedHashMap<Category, Tab>();
        for (Category category : Category.values()) {
            tabMap.put(category, new Tab(category.getName()));
        }
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.addAll(WURST.getHax().getAllHax());
        features.addAll(WURST.getCmds().getAllCmds());
        features.addAll(WURST.getOtfs().getAllOtfs());
        for (Feature feature : features) {
            if (feature.getCategory() == null) continue;
            ((Tab)tabMap.get((Object)feature.getCategory())).add(feature);
        }
        this.tabs.addAll(tabMap.values());
        this.tabs.forEach(Tab::updateSize);
        this.updateSize();
    }

    private void updateSize() {
        this.width = 64;
        for (Tab tab : this.tabs) {
            int tabWidth = TabGui.MC.field_1772.method_1727(tab.name) + 10;
            if (tabWidth <= this.width) continue;
            this.width = tabWidth;
        }
        this.height = this.tabs.size() * 10;
    }

    @Override
    public void onKeyPress(KeyPressListener.KeyPressEvent event) {
        if (event.getAction() != 1) {
            return;
        }
        if (this.tabGuiOtf.isHidden()) {
            return;
        }
        if (this.tabOpened) {
            switch (event.getKeyCode()) {
                case 263: {
                    this.tabOpened = false;
                    break;
                }
                default: {
                    this.tabs.get(this.selected).onKeyPress(event.getKeyCode());
                    break;
                }
            }
        } else {
            switch (event.getKeyCode()) {
                case 264: {
                    if (this.selected < this.tabs.size() - 1) {
                        ++this.selected;
                        break;
                    }
                    this.selected = 0;
                    break;
                }
                case 265: {
                    if (this.selected > 0) {
                        --this.selected;
                        break;
                    }
                    this.selected = this.tabs.size() - 1;
                    break;
                }
                case 262: {
                    this.tabOpened = true;
                }
            }
        }
    }

    public void render(class_332 context, float partialTicks) {
        if (this.tabGuiOtf.isHidden()) {
            return;
        }
        Matrix3x2fStack matrixStack = context.method_51448();
        matrixStack.pushMatrix();
        matrixStack.translate(2.0f, 23.0f);
        context.field_59826.method_71067();
        this.drawBox(context, 0, 0, this.width, this.height);
        context.method_44379(0, 0, this.width, this.height);
        int textY = 1;
        int txtColor = WURST.getGui().getTxtColor();
        class_327 tr = TabGui.MC.field_1772;
        context.field_59826.method_71067();
        for (int i = 0; i < this.tabs.size(); ++i) {
            Object tabName = this.tabs.get((int)i).name;
            if (i == this.selected) {
                tabName = (this.tabOpened ? "<" : ">") + (String)tabName;
            }
            context.method_51433(tr, (String)tabName, 2, textY, txtColor, false);
            textY += 10;
        }
        context.method_44380();
        if (this.tabOpened) {
            Tab tab = this.tabs.get(this.selected);
            matrixStack.pushMatrix();
            matrixStack.translate((float)(this.width + 2), 0.0f);
            this.drawBox(context, 0, 0, tab.width, tab.height);
            context.method_44379(0, 0, tab.width, tab.height);
            int tabTextY = 1;
            context.field_59826.method_71067();
            for (int i = 0; i < tab.features.size(); ++i) {
                Feature feature = tab.features.get(i);
                Object fName = feature.getName();
                if (feature.isEnabled()) {
                    fName = "\u00a7a" + (String)fName + "\u00a7r";
                }
                if (i == tab.selected) {
                    fName = ">" + (String)fName;
                }
                context.method_51433(tr, (String)fName, 2, tabTextY, txtColor, false);
                tabTextY += 10;
            }
            context.method_44380();
            matrixStack.popMatrix();
        }
        matrixStack.popMatrix();
    }

    private void drawBox(class_332 context, int x1, int y1, int x2, int y2) {
        ClickGui gui = WURST.getGui();
        int bgColor = RenderUtils.toIntColor(gui.getBgColor(), gui.getOpacity());
        context.method_25294(x1, y1, x2, y2, bgColor);
        RenderUtils.drawBoxShadow2D(context, x1, y1, x2, y2);
    }

    private static final class Tab {
        private final String name;
        private final ArrayList<Feature> features = new ArrayList();
        private int width;
        private int height;
        private int selected;

        public Tab(String name) {
            this.name = name;
        }

        public void updateSize() {
            this.width = 64;
            for (Feature feature : this.features) {
                int fWidth = TabGui.MC.field_1772.method_1727(feature.getName()) + 10;
                if (fWidth <= this.width) continue;
                this.width = fWidth;
            }
            this.height = this.features.size() * 10;
        }

        public void onKeyPress(int keyCode) {
            switch (keyCode) {
                case 264: {
                    if (this.selected < this.features.size() - 1) {
                        ++this.selected;
                        break;
                    }
                    this.selected = 0;
                    break;
                }
                case 265: {
                    if (this.selected > 0) {
                        --this.selected;
                        break;
                    }
                    this.selected = this.features.size() - 1;
                    break;
                }
                case 257: {
                    this.onEnter();
                }
            }
        }

        private void onEnter() {
            Feature feature = this.features.get(this.selected);
            TooManyHaxHack tooManyHax = TabGui.WURST.getHax().tooManyHaxHack;
            if (tooManyHax.isEnabled() && tooManyHax.isBlocked(feature)) {
                ChatUtils.error(feature.getName() + " is blocked by TooManyHax.");
                return;
            }
            feature.doPrimaryAction();
        }

        public void add(Feature feature) {
            this.features.add(feature);
        }
    }
}
