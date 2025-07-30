package net.cebularz.helpinghand.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.cebularz.helpinghand.client.model.TestEntityModel;
import net.cebularz.helpinghand.common.CommonClass;
import net.cebularz.helpinghand.common.entity.mercenary.TestMercenaryEntity;
import net.cebularz.helpinghand.utils.GuiUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.ClientHooks;
import org.joml.Matrix4f;

public class TestEntityRenderer extends HumanoidMobRenderer<TestMercenaryEntity, TestEntityModel<TestMercenaryEntity>> {

    public TestEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new TestEntityModel<>(context.bakeLayer(TestEntityModel.LOCATION)), 0.6F);
    }


    @Override
    protected void renderNameTag(TestMercenaryEntity entity, Component nameComponent, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float partialTick) {
        double distanceSqr = this.entityRenderDispatcher.distanceToSqr(entity);

        // Only render if close enough
        if (ClientHooks.isNameplateInRenderDistance(entity, distanceSqr)) {
            poseStack.pushPose();

            // Base offset above entity's head
            double yOffset = entity.getBbHeight() + 0.5;
            poseStack.translate(0.0D, yOffset, 0.0D);

            // Make text face camera
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

            // Flip Y for proper text orientation
            poseStack.scale(0.025F, -0.025F, 0.025F);

            Font font = this.getFont();

            // --- Render Name Tag (optional) ---
            if (this.shouldShowName(entity)) {
                Component name = nameComponent;
                float nameX = -font.width(name) / 2f;

                Matrix4f matrix = poseStack.last().pose();

                float alpha = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
                int backgroundColor = ((int)(alpha * 255.0F) << 24);

                boolean isVisibleThroughWalls = !entity.isDiscrete();

                // Shadow text
                font.drawInBatch(name, nameX, 0, 553648127, false, matrix, buffer,
                        isVisibleThroughWalls ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, backgroundColor, packedLight);

                // Bright text
                if (isVisibleThroughWalls) {
                    font.drawInBatch(name, nameX, 0, 0xFFFFFF, false, matrix, buffer,
                            Font.DisplayMode.NORMAL, 0, packedLight);
                }
            }

            // --- Render Custom Label Below Name ---
            if (entity.isHired()) {
                String timerText = "⏰ " + GuiUtility.formatTicksToTime(entity.getContractRemainingTicks());
                float timerX = -font.width(timerText) / 2f;
                float yGap = -10; // Pixels below name tag

                Matrix4f matrix = poseStack.last().pose();

                font.drawInBatch(timerText, timerX, yGap, 0xFFFFFF, false, matrix, buffer,
                        Font.DisplayMode.NORMAL, 0, packedLight);
            }

            poseStack.popPose();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(TestMercenaryEntity testMercenaryEntity) {
        return CommonClass.path("textures/entity/test.png");
    }
}
