package net.cebularz.helpinghand.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.cebularz.helpinghand.api.screens.NameBoxWidget;
import net.cebularz.helpinghand.common.CommonClass;
import net.cebularz.helpinghand.common.entity.mercenary.BaseMercenary;
import net.cebularz.helpinghand.common.entity.mercenary.ai.MercenaryContract;
import net.cebularz.helpinghand.common.menu.MercenaryMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MercenaryScreen extends AbstractContainerScreen<MercenaryMenu> {

    public static final ResourceLocation TEXTURE = CommonClass.path("textures/gui/mercenary/mercenary_gui.png");
    private final Minecraft minecraft;
    private final NameBoxWidget nameBoxWidget;

    public static final int FULL_WIDTH = 512;
    public static final int FULL_HEIGHT = 256;

    private int left;
    private int right;
    private int top;
    private int bottom;

    public MercenaryScreen(MercenaryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        minecraft = Minecraft.getInstance();
        this.nameBoxWidget = new NameBoxWidget(this.minecraft.font,7,142,83,15,Component.empty(),TEXTURE,0,165,83,15,15);
    }

    @Override
    protected void init() {
        super.init();
        this.imageWidth = 275;
        this.imageHeight = 165;
        left = width / 2 - FULL_WIDTH / 2;
        top = height / 2 - FULL_HEIGHT / 2;
        right = width / 2 + FULL_WIDTH / 2;
        bottom = height / 2 + FULL_HEIGHT / 2;
        nameBoxWidget.setX(this.leftPos + 7);
        nameBoxWidget.setY(this.topPos + 114);
        this.addRenderableWidget(nameBoxWidget);
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 && this.nameBoxWidget.isFocused()) {
            this.nameBoxWidget.setFocused(false);
            return true;
        }

        if (this.nameBoxWidget.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.nameBoxWidget.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (nameBoxWidget.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(nameBoxWidget);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        if (!(menu.getAssociatedEntity() instanceof BaseMercenary mercenary)) return;

        InventoryScreen.renderEntityInInventoryFollowsMouse(
                guiGraphics,
                0,
                0,
                this.leftPos + 36,
                this.topPos + 60,
                30,
                0.35f,
                (float)(this.leftPos + 36) - mouseX,
                (float)(this.topPos + 60 - 50) - mouseY,
                mercenary
        );
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics,mouseX,mouseY,partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics,mouseX,mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        if (menu.getAssociatedEntity() instanceof BaseMercenary mercenary) {
            String displayText = nameBoxWidget.getValue();

            if (mercenary.isHired()) {
                MercenaryContract contract = mercenary.getCurrentContract();
                if (contract != null) {
                    int remainingSeconds = contract.getRemainingTime();
                    displayText += " (" + formatTime(remainingSeconds) + ")";
                }
            }

            guiGraphics.drawString(this.font, displayText, 95, 37, 0x404040, false);
        }
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }
}