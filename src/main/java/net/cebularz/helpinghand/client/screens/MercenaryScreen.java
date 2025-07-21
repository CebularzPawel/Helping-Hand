package net.cebularz.helpinghand.client.screens;

import net.cebularz.helpinghand.api.screens.NameBoxWidget;
import net.cebularz.helpinghand.common.CommonClass;
import net.cebularz.helpinghand.common.entity.mercenary.BaseMercenary;
import net.cebularz.helpinghand.common.menu.MercenaryMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MercenaryScreen extends AbstractContainerScreen<MercenaryMenu> {

    public static final ResourceLocation TEXTURE = CommonClass.path("textures/gui/mercenary/mercenary_gui.png");
    private final Minecraft minecraft;
    private final NameBoxWidget nameBoxWidget;

    public MercenaryScreen(MercenaryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        minecraft = Minecraft.getInstance();
        this.nameBoxWidget = new NameBoxWidget(this.minecraft.font,7,142,83,15,Component.empty(),TEXTURE,0,165,83,15,15);

    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        nameBoxWidget.setX(this.leftPos + 7);
        nameBoxWidget.setY(this.topPos + 142);
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
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0,0, this.imageWidth, this.imageHeight);
        if(!(menu.getAssociatedEntity() instanceof BaseMercenary mercenary)) return;
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics,x+24,y+9,x+73, y+74,20,0.25f,mouseX,mouseY,mercenary);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
    }
}
