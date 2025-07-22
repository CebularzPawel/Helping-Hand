package net.cebularz.helpinghand.common.events;

import net.cebularz.helpinghand.Constants;
import net.cebularz.helpinghand.client.data.ClientReputationManager;
import net.cebularz.helpinghand.common.data.reputation.ReputationManager;
import net.cebularz.helpinghand.common.data.reputation.WorldReputationManager;
import net.cebularz.helpinghand.common.entity.mercenary.BaseMercenary;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {

    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        if (victim instanceof BaseMercenary mercenary) {
            Entity attacker = event.getEntity().getLastAttacker();

            if (attacker instanceof Player player && mercenary.isOwnedBy(player)
                    && victim.level() instanceof ServerLevel serverLevel) {

                int toDec;
                if (mercenary.isHired()) {
                    toDec = (int) Mth.clamp(mercenary.getLastDamageSource().getFoodExhaustion(), 5, 14);
                } else {
                    toDec = (int) Mth.clamp(mercenary.getLastDamageSource().getFoodExhaustion(), 3, 11);
                }

                ReputationManager reputationManager = getReputationManager(mercenary);
                if (reputationManager != null) {
                    reputationManager.decreaseReputation(player.getUUID(), toDec);
                    WorldReputationManager.get(serverLevel).markDirty();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ServerLevel level = serverPlayer.serverLevel();
            WorldReputationManager worldManager = WorldReputationManager.get(level);
            worldManager.onPlayerJoin(serverPlayer);
        }
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            ClientReputationManager.clearCache();
        }
    }

    private static ReputationManager getReputationManager(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            WorldReputationManager worldManager = WorldReputationManager.get(serverLevel);
            return worldManager.getReputationManager();
        }
        return null;
    }
}