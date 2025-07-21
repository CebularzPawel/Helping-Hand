package net.cebularz.helpinghand.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.cebularz.helpinghand.client.model.TestEntityModel;
import net.cebularz.helpinghand.common.CommonClass;
import net.cebularz.helpinghand.common.entity.mercenary.TestMercenaryEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TestEntityRenderer extends HumanoidMobRenderer<TestMercenaryEntity, TestEntityModel<TestMercenaryEntity>> {

    public TestEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new TestEntityModel<>(context.bakeLayer(TestEntityModel.LOCATION)), 1.0F);
    }

    @Override
    protected void renderNameTag(TestMercenaryEntity entity, Component p_114499_, PoseStack p_114500_, MultiBufferSource p_114501_, int p_114502_, float p_316698_) {
        super.renderNameTag(entity, p_114499_, p_114500_, p_114501_, p_114502_, p_316698_);

    }

    @Override
    public ResourceLocation getTextureLocation(TestMercenaryEntity testMercenaryEntity) {
        return CommonClass.path("textures/entity/test.png");
    }
}
