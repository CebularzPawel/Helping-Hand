/**
 * Code Under MIT Licence
 * **/
package net.cebularz.helpinghand.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.cebularz.helpinghand.HelpingHandConfig;
import net.cebularz.helpinghand.api.loaders.NamesJsonLoader;
import net.cebularz.helpinghand.api.screens.NameBoxWidget;
import net.cebularz.helpinghand.common.CommonClass;
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
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MercenaryScreen extends AbstractContainerScreen<MercenaryMenu> {
    public static final ResourceLocation TEXTURE = CommonClass.path("textures/gui/mercenary/mercenary_gui.png");
    public static final ResourceLocation ICONS = CommonClass.path("textures/gui/mercenary/icons/icons.png");

    private final Minecraft minecraft;
    private NameBoxWidget nameBoxWidget;

    private float lastHealth = 0f;
    private float lastReputation = 0f;
    private double damageValue;
    private double rangeValue;
    private UUID currentReputationUUID = null;
    private int currentReputation = 0;

    public MercenaryScreen(MercenaryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    protected void init() {
        this.imageWidth = 275;
        this.imageHeight = 165;
        super.init();

        updateNameBoxPosition();
        this.addRenderableWidget(nameBoxWidget);
        setValues();

        if (menu.getAssociatedEntity() instanceof BaseMercenary mercenary) {
            updateReputationState(mercenary);
        }
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String currentText = nameBoxWidget != null ? nameBoxWidget.getValue() : "";
        boolean wasFocused = nameBoxWidget != null && nameBoxWidget.isFocused();

        super.resize(minecraft, width, height);

        if (nameBoxWidget != null) {
            nameBoxWidget.setValue(currentText);
            if (wasFocused) nameBoxWidget.setFocused(true);
        }
    }

    private void setValues() {
        if (menu.getAssociatedEntity() instanceof BaseMercenary mercenary) {
            damageValue = mercenary.getAttributeValue(Attributes.ATTACK_DAMAGE);
            rangeValue = mercenary.isRanged() ? mercenary.getAttributeValue(Attributes.FOLLOW_RANGE)
                    : damageValue;
        }
    }

    private void updateNameBoxPosition() {
        int centerX = (width - imageWidth) / 2;
        int centerY = (height - imageHeight) / 2;

        if (nameBoxWidget == null) {
            nameBoxWidget = isFocused() ? new NameBoxWidget(
                    font, centerX + 7, centerY + 142,
                    83, 15, Component.empty(),
                    TEXTURE, 0, 165, 512, 256
            ) : new NameBoxWidget(
                    font, centerX + 7, centerY + 142,
                    83, 15, Component.empty(),
                    TEXTURE, 0, 0, 512, 256
            );
            if (menu.getAssociatedEntity() != null && menu.getAssociatedEntity().hasCustomName()) {
                nameBoxWidget.setValue(menu.getAssociatedEntity().getCustomName().getString());
            }
        } else {
            nameBoxWidget.setPosition(centerX + 7, centerY + 142);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 && nameBoxWidget.isFocused()) {
            nameBoxWidget.setFocused(false);
            var entity = menu.getAssociatedEntity();
            if (entity != null) {
                String name = nameBoxWidget.getValue();
                entity.setCustomName(name.isEmpty() ? null : Component.literal(name));
                entity.setCustomNameVisible(!name.isEmpty());
            }
            return true;
        }
        return nameBoxWidget.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return nameBoxWidget.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (nameBoxWidget.mouseClicked(mouseX, mouseY, button)) {
            setFocused(nameBoxWidget);
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
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 512, 256);

        if (!(menu.getAssociatedEntity() instanceof BaseMercenary mercenary)) return;

        renderLerpedHealthBar(guiGraphics, x, y, partialTick);
        renderLerpedReputationBar(guiGraphics, x, y, partialTick);

        guiGraphics.blit(ICONS, x + 16, y + 89, 0, 0, 16, 16, 16, 48);
        guiGraphics.blit(ICONS, x + 16, y + 119, 0, 16, 16, 16, 16, 48);
        guiGraphics.blit(ICONS, x + 16, y + 149, 0, 32, 16, 16, 16, 48);

        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, x + 24, y + 9, x + 73, y + 74, 21, 0.35f, mouseX, mouseY, mercenary);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!(menu.getAssociatedEntity() instanceof BaseMercenary mercenary)) return;

        lastHealth = mercenary.getHealth();
        updateReputationState(mercenary);

        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        renderStatTooltips(guiGraphics, mouseX, mouseY);
        renderRemainingTimeToolTip(guiGraphics, mouseX, mouseY, mercenary);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        if (menu.getAssociatedEntity() instanceof BaseMercenary mercenary) {
            String name = (nameBoxWidget != null && !nameBoxWidget.getValue().isEmpty()) ? nameBoxWidget.getValue() : getRandomName();
            String displayText = name + " the " + mercenary.type;

            if (mercenary.isHired()) {
                MercenaryContract contract = mercenary.getCurrentContract();
                if (contract != null) {
                    displayText += " (" + formatTime(contract.getRemainingTime()) + ")";
                }
            }

            renderStatValues(guiGraphics, mercenary);
            renderTimeRemaining(guiGraphics, mercenary);
            guiGraphics.drawString(font, displayText, 110, 6, 0x404040, false);
        }
    }

    private void renderRemainingTimeToolTip(GuiGraphics guiGraphics, int mouseX, int mouseY, BaseMercenary mercenary) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        if (!mercenary.isHired()) return;
        MercenaryContract contract = mercenary.getCurrentContract();
        if (contract == null) return;

        if (mouseX >= x + 210 && mouseX <= x + 225 && mouseY >= y + 34 && mouseY <= y + 48) {
            guiGraphics.blit(TEXTURE, x + 211, y + 34, 288, 32, imageWidth, imageHeight, 512, 256);
            guiGraphics.renderTooltip(font, Component.literal(GuiUtility.formatTicksToTime(contract.getRemainingTicks())), mouseX, mouseY);
        }
    }

    private void renderLerpedHealthBar(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        if (!(menu.getAssociatedEntity() instanceof BaseMercenary mercenary)) return;
        float ratio = Mth.clamp(Mth.lerp(partialTick, lastHealth, mercenary.getHealth()) / mercenary.getMaxHealth(), 0f, 1f);
        int filledHeight = Math.round(ratio * 61);
        guiGraphics.blit(TEXTURE, x + 11, y + 11 + (61 - filledHeight), 96, 170 + (61 - filledHeight), 6, filledHeight, 512, 256);
    }

    private void renderLerpedReputationBar(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        float ratio = Mth.clamp(Mth.lerp(partialTick, lastReputation, currentReputation) / HelpingHandConfig.MAX_MERCENARY_REPUTATION.get(), 0f, 1f);
        int filledHeight = Math.round(ratio * 61);
        guiGraphics.blit(TEXTURE, x + 82, y + 11 + (61 - filledHeight), 114, 170 + (61 - filledHeight), 5, filledHeight, 512, 256);
    }

    private void renderStatTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!(menu.getAssociatedEntity() instanceof BaseMercenary mercenary)) return;
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        if (mouseX >= x + 16 && mouseX <= x + 32 && mouseY >= y + 89 && mouseY <= y + 105)
            guiGraphics.renderTooltip(font, Component.literal(String.format("Attack Damage: %.1f", damageValue)), mouseX, mouseY);
        else if (mouseX >= x + 16 && mouseX <= x + 32 && mouseY >= y + 119 && mouseY <= y + 135)
            guiGraphics.renderTooltip(font, Component.literal(String.format(mercenary.isRanged() ? "Attack Range: %.1f" : "Melee Damage: %.1f", rangeValue)), mouseX, mouseY);
        else if (mouseX >= x + 16 && mouseX <= x + 32 && mouseY >= y + 149 && mouseY <= y + 165)
            guiGraphics.renderTooltip(font, Component.literal(String.format("Health: %.1f/%.1f", mercenary.getHealth(), mercenary.getMaxHealth())), mouseX, mouseY);
    }

    private void renderStatValues(GuiGraphics guiGraphics, BaseMercenary mercenary) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.drawString(font, String.format("%.1f", damageValue), 38, 93, 0x404040, false);
        guiGraphics.drawString(font, String.format("%.1f", rangeValue), 38, 113, 0x404040, false);
        guiGraphics.drawString(font, String.format("%.1f/%.1f", mercenary.getHealth(), mercenary.getMaxHealth()), 38, 133, 0x404040, false);
    }

    private void renderTimeRemaining(GuiGraphics guiGraphics, BaseMercenary mercenary) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        if (!mercenary.isHired()) return;
        MercenaryContract contract = mercenary.getCurrentContract();
        if (contract == null) return;

        guiGraphics.drawString(font, GuiUtility.formatTicksToTime(contract.getRemainingTicks()), x + 195, y + 34, 0x404040, false);
    }

    private void updateReputationState(BaseMercenary mercenary) {
        UUID targetUUID = getReputationTargetUUID(mercenary);
        int newReputation = mercenary.getData(ModAttachments.REPUTATION.get()).getReputationWith(targetUUID);

        if (!targetUUID.equals(currentReputationUUID) || newReputation != currentReputation) {
            lastReputation = currentReputation;
            currentReputationUUID = targetUUID;
            currentReputation = newReputation;
        }
    }

    private UUID getReputationTargetUUID(BaseMercenary mercenary) {
        if (mercenary.isHired() && mercenary.getOwnerUUID() != null) {
            return mercenary.getOwnerUUID();
        }
        var nearestPlayer = mercenary.level().getNearestPlayer(TargetingConditions.forNonCombat(), mercenary);
        return nearestPlayer != null ? nearestPlayer.getUUID() : new UUID(0, 0);
    }

    private String getRandomName() {
        List<NamesJsonLoader.NameData> allNames = NamesJsonLoader.INSTANCE.getAllNames();
        return allNames.isEmpty() ? "Unknown" : allNames.get(new Random().nextInt(allNames.size())).value();
    }

    private String formatTime(int seconds) {
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }
}
