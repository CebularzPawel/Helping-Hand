package net.cebularz.helpinghand.client.renderer.entity;

import net.cebularz.helpinghand.client.model.TestEntityModel;
import net.cebularz.helpinghand.common.CommonClass;
import net.cebularz.helpinghand.common.entity.mercenary.TestMercenaryEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class TestEntityRenderer extends HumanoidMobRenderer<TestMercenaryEntity, TestEntityModel<TestMercenaryEntity>> {

    public TestEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new TestEntityModel<>(context.bakeLayer(TestEntityModel.LOCATION)), 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(TestMercenaryEntity testMercenaryEntity) {
        return CommonClass.path("textures/entity/test.png");
    }
}
