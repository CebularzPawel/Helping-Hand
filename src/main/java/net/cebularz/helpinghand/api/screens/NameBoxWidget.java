package net.cebularz.helpinghand.api.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A custom EditBox that uses a texture background for active and inactive states.
 */
public class NameBoxWidget extends EditBox {

    private final ResourceLocation texture;
    private final int u, v;
    private final int texWidth, texHeight;

    public NameBoxWidget(Font font, int x, int y, int width, int height,
                         Component message,
                         ResourceLocation texture,
                         int u, int v,
                         int texWidth, int texHeight) {
        super(font, x, y, width, height, message);
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
    }

    /**
     * Updates the position of this widget. Useful for handling screen resizes.
     */
    public void setPosition(int x, int y) {
        this.setX(x);
        this.setY(y);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderTexturedBackground(guiGraphics);
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderTexturedBackground(GuiGraphics guiGraphics) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(
                texture,
                this.getX(), this.getY(),
                u, v,
                this.getWidth(), this.getHeight(),
                texWidth, texHeight
        );
    }
}