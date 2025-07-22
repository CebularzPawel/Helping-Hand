package net.cebularz.helpinghand.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.cebularz.helpinghand.HelpingHandConfig;
import net.cebularz.helpinghand.api.loaders.NamesJsonLoader;
import net.cebularz.helpinghand.api.screens.NameBoxWidget;
import net.cebularz.helpinghand.client.data.ClientReputationManager;
import net.cebularz.helpinghand.common.CommonClass;
import net.cebularz.helpinghand.common.data.reputation.ReputationData;
import net.cebularz.helpinghand.common.entity.mercenary.BaseMercenary;
import net.cebularz.helpinghand.common.entity.mercenary.ai.MercenaryContract;
import net.cebularz.helpinghand.common.menu.MercenaryMenu;
import net.cebularz.helpinghand.core.ModAttachments;
import net.cebularz.helpinghand.utils.GuiUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Random;

public class MercenaryScreen extends AbstractContainerScreen<MercenaryMenu> {

    public static final ResourceLocation TEXTURE = CommonClass.path("textures/gui/mercenary/mercenary_gui.png");
    public static final ResourceLocation ICONS = CommonClass.path("textures/gui/mercenary/icons/icons.png");

    private final Minecraft minecraft;
    private NameBoxWidget nameBoxWidget;
    private float lastHealth = 0f;
    private float lastReputation = 0f;
    private double damageValue;
    private double rangeValue;
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
        setValues();
    }

    private void setValues()
    {
        if(menu.getAssociatedEntity() instanceof BaseMercenary mercenary){
            damageValue = mercenary.getAttributeValue(Attributes.ATTACK_DAMAGE);
            rangeValue = mercenary.isRanged() ? mercenary.getAttributeValue(Attributes.FOLLOW_RANGE) : mercenary.getAttributeValue(Attributes.ATTACK_DAMAGE);
        }
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

        guiGraphics.blit(ICONS, x + 16, y + 89, 0, 0, 16, 16, 16, 48);

        guiGraphics.blit(ICONS, x + 16, y + 119, 0, 16, 16, 16, 16, 48);

        guiGraphics.blit(ICONS, x + 16, y + 149, 0, 32, 16, 16, 16, 48);

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
        if (!(menu.getAssociatedEntity() instanceof BaseMercenary mercenary)) return;
        lastHealth = mercenary.getHealth();
        lastReputation = mercenary.getData(ModAttachments.REPUTATION).getCurrentReputation();
        this.renderBackground(guiGraphics,mouseX,mouseY,partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics,mouseX,mouseY);

        renderStatTooltips(guiGraphics, mouseX, mouseY);
        renderRemainingTimeToolTip(guiGraphics,mouseX,mouseY,mercenary);
    }

    private void renderRemainingTimeToolTip(GuiGraphics guiGraphics, int mouseX, int mouseY, BaseMercenary mercenary) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        if (!mercenary.isHired()) return;

        MercenaryContract contract = mercenary.getCurrentContract();
        if (contract == null) return;

        if (mouseX >= x + 210 && mouseX <= x + 225 && mouseY >= y + 34 && mouseY <= y + 48) {
            guiGraphics.blit(TEXTURE, x + 211, y + 34, 288, 32, imageWidth, imageHeight, 512, 256);
            guiGraphics.renderTooltip(this.font,
                    Component.literal(GuiUtility.formatTicksToTime(contract.getRemainingTicks())),
                    mouseX, mouseY);
        }
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
        int playerReputation = ClientReputationManager.getReputation(Minecraft.getInstance().player.getUUID());
        float rep = Mth.lerp(partialTick, lastReputation, playerReputation);
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

    private void renderStatTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!(menu.getAssociatedEntity() instanceof BaseMercenary mercenary)) return;

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        if (mouseX >= x + 16 && mouseX <= x + 32 && mouseY >= y + 89 && mouseY <= y + 105) {
            String damageText = String.format("Attack Damage: %.1f", damageValue);
            guiGraphics.renderTooltip(this.font, Component.literal(damageText), mouseX, mouseY);
        }

        else if (mouseX >= x + 16 && mouseX <= x + 32 && mouseY >= y + 119 && mouseY <= y + 135) {
            String rangeText;
            if (mercenary.isRanged()) {
                rangeText = String.format("Attack Range: %.1f", rangeValue);
            } else {
                rangeText = String.format("Melee Damage: %.1f", rangeValue);
            }
            guiGraphics.renderTooltip(this.font, Component.literal(rangeText), mouseX, mouseY);
        }

        else if (mouseX >= x + 16 && mouseX <= x + 32 && mouseY >= y + 149 && mouseY <= y + 165) {
            String healthText = String.format("Health: %.1f/%.1f", mercenary.getHealth(), mercenary.getMaxHealth());
            guiGraphics.renderTooltip(this.font, Component.literal(healthText), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        if (menu.getAssociatedEntity() instanceof BaseMercenary mercenary) {

            String name = nameBoxWidget != null && !nameBoxWidget.getValue().isEmpty()
                    ? nameBoxWidget.getValue()
                    : getRandomName();

            String displayText = name + " the " + mercenary.type;


            if (mercenary.isHired()) {
                MercenaryContract contract = mercenary.getCurrentContract();
                if (contract != null) {
                    int remainingSeconds = contract.getRemainingTime();
                    displayText += " (" + formatTime(remainingSeconds) + ")";
                }
            }
            renderStatValues(guiGraphics, mercenary);
            renderTimeRemaining(guiGraphics,mercenary);
            guiGraphics.drawString(this.font, displayText, 110, 6, 0x404040, false);
        }
    }

    private void renderStatValues(GuiGraphics guiGraphics, BaseMercenary mercenary) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int mouseX = (int) minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth();
        int mouseY = (int) minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight();

        boolean isDamageHovered = mouseX >= x + 16 && mouseX <= x + 32 && mouseY >= y + 89 && mouseY <= y + 105;
        boolean isRangeHovered = mouseX >= x + 16 && mouseX <= x + 32 && mouseY >= y + 119 && mouseY <= y + 135;
        boolean isHealthHovered = mouseX >= x + 16 && mouseX <= x + 32 && mouseY >= y + 149 && mouseY <= y + 165;

        String damageText = String.format("%.1f", damageValue);
        guiGraphics.drawString(this.font, damageText, 38, 93, 0x404040, false);
        if (isDamageHovered) {
            int textWidth = this.font.width(damageText);
            guiGraphics.fill(38, 102, 38 + textWidth, 103, 0x404040);
        }

        String rangeText = String.format("%.1f", rangeValue);
        guiGraphics.drawString(this.font, rangeText, 38, 113, 0x404040, false);
        if (isRangeHovered) {
            int textWidth = this.font.width(rangeText);
            guiGraphics.fill(38, 132, 38 + textWidth, 123, 0x404040);
        }

        String healthText = String.format("%.1f/%.1f", mercenary.getHealth(), mercenary.getMaxHealth());
        guiGraphics.drawString(this.font, healthText, 38, 133, 0x404040, false);
        if (isHealthHovered) {
            int textWidth = this.font.width(healthText);
            guiGraphics.fill(38, 162, 38 + textWidth, 143, 0x404040);
        }
    }

    private void renderTimeRemaining(GuiGraphics guiGraphics, BaseMercenary mercenary) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        if (!mercenary.isHired()) return;

        MercenaryContract contract = mercenary.getCurrentContract();
        if (contract == null) return;

        String toShowText = GuiUtility.formatTicksToTime(contract.getRemainingTicks());
        guiGraphics.drawString(this.font, toShowText, x + 195, y + 34, 0x404040, false);
    }

    private String getRandomName() {
        List<NamesJsonLoader.NameData> allNames = NamesJsonLoader.INSTANCE.getAllNames();
        return allNames.isEmpty() ? "Unknown" : allNames.get(new Random().nextInt(allNames.size())).value();
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }
}