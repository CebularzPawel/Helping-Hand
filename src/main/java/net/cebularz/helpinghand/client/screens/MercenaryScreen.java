package net.cebularz.helpinghand.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.cebularz.helpinghand.HelpingHandConfig;
import net.cebularz.helpinghand.api.screens.NameBoxWidget;
import net.cebularz.helpinghand.common.CommonClass;
import net.cebularz.helpinghand.common.data.ReputationData;
import net.cebularz.helpinghand.common.entity.mercenary.BaseMercenary;
import net.cebularz.helpinghand.common.entity.mercenary.ai.MercenaryContract;
import net.cebularz.helpinghand.common.menu.MercenaryMenu;
import net.cebularz.helpinghand.core.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class MercenaryScreen extends AbstractContainerScreen<MercenaryMenu> {

    public static final ResourceLocation TEXTURE = CommonClass.path("textures/gui/mercenary/mercenary_gui.png");
    private final Minecraft minecraft;
    private NameBoxWidget nameBoxWidget;
    private float lastHealth = 0f;
    private float lastReputation = 0f;
    public MercenaryScreen(MercenaryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        minecraft = Minecraft.getInstance();
    }

    @Override
    protected void init() {
        this.imageWidth = 275;
        this.imageHeight = 165;
        super.init();
        updateNameBoxPosition();
        this.addRenderableWidget(nameBoxWidget);
    }
    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String currentText = nameBoxWidget != null ? nameBoxWidget.getValue() : "";
        boolean wasFocused = nameBoxWidget != null && nameBoxWidget.isFocused();

        super.resize(minecraft, width, height);

        if (nameBoxWidget != null) {
            nameBoxWidget.setValue(currentText);
            if (wasFocused) {
                nameBoxWidget.setFocused(true);
            }
        }
    }

    private void updateNameBoxPosition() {
        int centerX = (width - imageWidth) / 2;
        int centerY = (height - imageHeight) / 2;

        if (nameBoxWidget == null) {
            this.nameBoxWidget = new NameBoxWidget(
                    this.font,
                    centerX + 7, centerY + 142,
                    83, 15,
                    Component.empty(),
                    TEXTURE,
                    0, 165,
                    512, 256
            );
            if (menu.getAssociatedEntity() != null && menu.getAssociatedEntity().hasCustomName()) {
                String currentName = menu.getAssociatedEntity().getCustomName().getString();
                nameBoxWidget.setValue(currentName);
            }
        } else {
            nameBoxWidget.setPosition(centerX + 7, centerY + 142);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 && this.nameBoxWidget.isFocused()) { // Enter key
            this.nameBoxWidget.setFocused(false);
            if(menu.getAssociatedEntity() != null){
                String name = this.nameBoxWidget.getValue();
                if (!name.isEmpty()) {
                    menu.getAssociatedEntity().setCustomName(Component.literal(name));
                    menu.getAssociatedEntity().setCustomNameVisible(true);
                } else {
                    menu.getAssociatedEntity().setCustomName(null);
                    menu.getAssociatedEntity().setCustomNameVisible(false);
                }
            }
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

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight,512,256);

        if (!(menu.getAssociatedEntity() instanceof BaseMercenary mercenary)) return;

        renderLerpedHealthBar(guiGraphics, x,y, partialTick);
        renderLerpedReputationBar(guiGraphics, x,y, partialTick);

        InventoryScreen.renderEntityInInventoryFollowsMouse(
                guiGraphics,
                x+24,
                y+9,
                x + 73,
                y + 74,
                21,
                0.35f,
                mouseX,
                mouseY,
                mercenary
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (menu.getAssociatedEntity() instanceof BaseMercenary mercenary) {
            lastHealth = mercenary.getHealth();
            lastReputation = mercenary.getData(ModAttachments.REPUTATION).getCurrentReputation();
        }
        this.renderBackground(guiGraphics,mouseX,mouseY,partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics,mouseX,mouseY);
    }

    private void renderLerpedHealthBar(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        if (!(menu.getAssociatedEntity() instanceof BaseMercenary mercenary)) return;

        float health = Mth.lerp(partialTick, lastHealth, mercenary.getHealth());
        float max = mercenary.getMaxHealth();
        float ratio = Mth.clamp(health / max, 0f, 1f);

        int fullHeight = 61;
        int filledHeight = Math.round(ratio * fullHeight);

        guiGraphics.blit(
                TEXTURE,
                x + 11,
                y + 11 + (fullHeight - filledHeight),
                96,
                170 + (fullHeight - filledHeight),
                6,
                filledHeight,
                512,
                256
        );
    }

    private void renderLerpedReputationBar(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        if (!(menu.getAssociatedEntity() instanceof BaseMercenary mercenary)) return;
        ReputationData data = mercenary.getData(ModAttachments.REPUTATION);

        float rep = Mth.lerp(partialTick, lastReputation, data.getCurrentReputation());
        float maxRep = HelpingHandConfig.MAX_MERCENARY_REPUTATION.get();
        float ratio = Mth.clamp(rep / maxRep, 0f, 1f);

        int fullHeight = 61;
        int filledHeight = Math.round(ratio * fullHeight);

        guiGraphics.blit(
                TEXTURE,
                x + 82,
                y + 11 + (fullHeight - filledHeight),
                114,
                170 + (fullHeight - filledHeight),
                5,
                filledHeight,
                512,
                256
        );
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