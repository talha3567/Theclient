package com.pulseclient.gui;

import com.pulseclient.Category;
import com.pulseclient.Module;
import com.pulseclient.PulseClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.stream.Collectors;

public class ClickGuiScreen extends Screen {
    public ClickGuiScreen() {
        super(Component.literal("PulseClient ClickGUI"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int x = 20;
        for (Category category : Category.values()) {
            renderCategory(graphics, category, x, 20, mouseX, mouseY);
            x += 110;
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderCategory(GuiGraphics graphics, Category category, int x, int y, int mouseX, int mouseY) {
        // Draw category header
        graphics.fill(x, y, x + 100, y + 15, 0xFF202020);
        graphics.drawString(this.font, category.name, x + 5, y + 4, 0xFFFFFFFF);

        // Draw modules
        List<Module> modules = PulseClient.INSTANCE.moduleManager.getModules().stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());

        int currentY = y + 15;
        for (Module module : modules) {
            int color = module.isEnabled() ? 0xFF30CC30 : 0xFF404040;
            graphics.fill(x, currentY, x + 100, currentY + 12, color);
            graphics.drawString(this.font, module.getName(), x + 5, currentY + 2, 0xFFFFFFFF);
            currentY += 12;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = 20;
        for (Category category : Category.values()) {
            List<Module> modules = PulseClient.INSTANCE.moduleManager.getModules().stream()
                    .filter(m -> m.getCategory() == category)
                    .collect(Collectors.toList());

            int currentY = 35; // y + 15
            for (Module module : modules) {
                if (mouseX >= x && mouseX <= x + 100 && mouseY >= currentY && mouseY <= currentY + 12) {
                    module.toggle();
                    return true;
                }
                currentY += 12;
            }
            x += 110;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
