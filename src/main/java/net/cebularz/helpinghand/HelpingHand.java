package net.cebularz.helpinghand;

import net.cebularz.helpinghand.core.ModEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Constants.MOD_ID)
public class HelpingHand {

    public HelpingHand(IEventBus modEventBus, ModContainer modContainer) {

        ModEntity.REGISTER.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, HelpingHandConfig.SPEC);
    }
}
