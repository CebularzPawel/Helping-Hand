package net.cebularz.helpinghand;

import net.cebularz.helpinghand.client.model.TestEntityModel;
import net.cebularz.helpinghand.client.renderer.entity.TestEntityRenderer;
import net.cebularz.helpinghand.common.entity.mercenary.TestMercenaryEntity;
import net.cebularz.helpinghand.core.ModEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class HelpingHandClient {
    public HelpingHandClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
    }

    @SubscribeEvent
    public static void buildAttributes(EntityAttributeCreationEvent event){
        event.put(ModEntity.TEST_ENTITY.get(), TestMercenaryEntity.createAttribs().build());
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(TestEntityModel.LOCATION, () -> LayerDefinition.create(TestEntityModel.createMesh(CubeDeformation.NONE), 64, 64));
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(ModEntity.TEST_ENTITY.get(), TestEntityRenderer::new);
    }
}
