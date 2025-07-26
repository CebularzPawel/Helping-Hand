package net.cebularz.helpinghand;

import net.cebularz.helpinghand.api.loaders.NamesJsonLoader;
import net.cebularz.helpinghand.core.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.concurrent.CompletableFuture;

@Mod(Constants.MOD_ID)
public class HelpingHand {

    public HelpingHand(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, HelpingHandConfig.SPEC);

        //Registrars
        ModItems.REGISTER.register(modEventBus);
        ModBlocks.REGISTER.register(modEventBus);
        ModCreativeTabs.REGISTER.register(modEventBus);
        ModEntity.REGISTER.register(modEventBus);
        ModMenus.REGISTER.register(modEventBus);
        ModAttachments.REGISTER.register(modEventBus);

        modEventBus.addListener(this::onGatherData);
        NeoForge.EVENT_BUS.addListener(this::registerReloadListener);
    }

    public void registerReloadListener(AddReloadListenerEvent event) {
       event.addListener(NamesJsonLoader.INSTANCE);
    }

    public void onGatherData (GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> completableFuture = event.getLookupProvider();

    }
}
