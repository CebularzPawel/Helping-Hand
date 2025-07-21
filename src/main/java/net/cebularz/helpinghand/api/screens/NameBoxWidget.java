package net.cebularz.helpinghand.api.screens;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A custom EditBox that uses a texture background for active and inactive states.
 */
public class NameBoxWidget extends EditBox {

    private final ResourceLocation texture;
    private final int textureU;
    private final int textureV;
    private final int textureWidth;
    private final int textureHeight;
    private final int regionHeight;

    public NameBoxWidget(Font font, int x, int y, int width, int height,
                         Component message, ResourceLocation texture,
                         int textureU, int textureV,
                         int textureWidth, int textureHeight,
                         int regionHeight) {
        super(font, x, y, width, height, message);
        this.texture = texture;
        this.textureU = textureU;
        this.textureV = textureV;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.regionHeight = regionHeight;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderTexturedBackground(guiGraphics);
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderTexturedBackground(GuiGraphics guiGraphics) {
        int vOffset = this.isFocused() ? regionHeight : 0;

        guiGraphics.blit(
                texture,
                this.getX(), this.getY(),
                textureU, textureV + vOffset,
                this.getWidth(), this.getHeight(),
                textureWidth, textureHeight
        );
    }
}
