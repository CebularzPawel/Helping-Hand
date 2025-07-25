package net.cebularz.helpinghand.common.events;

import net.cebularz.helpinghand.Constants;
import net.cebularz.helpinghand.common.capabilities.mercenary.CapabilityInstances;
import net.cebularz.helpinghand.common.data.reputation.MercenaryReputation;
import net.cebularz.helpinghand.common.entity.mercenary.BaseMercenary;
import net.cebularz.helpinghand.core.ModAttachments;
import net.cebularz.helpinghand.core.ModEntity;
import net.cebularz.helpinghand.networking.PayloadHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class ModEvents
{
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(
                CapabilityInstances.MERCENARY_REPUTATION,
                BaseMercenary.class,
                (entity, context) -> entity.getCapability()
        );
    }

    @SubscribeEvent
    public static void onEntityDamage(LivingDamageEvent.Post event)
    {
        if(event.getEntity() instanceof BaseMercenary mercenary)
        {
            if(mercenary.getLastAttacker() instanceof Player player)
            {
                int reputationLoss = mercenary.isHired() && player.getUUID().equals(mercenary.getOwnerUUID())
                        ? MercenaryReputation.HURT_REPUTATION_LOSS * 2
                        : MercenaryReputation.HURT_REPUTATION_LOSS;

                mercenary.decreaseReputationWith(player, Math.abs(reputationLoss));
            }
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event)
    {
        if(event.getEntity() instanceof BaseMercenary mercenary)
        {
            if(event.getSource().getEntity() instanceof Player player)
            {
                mercenary.decreaseReputationWith(player, Math.abs(MercenaryReputation.KILL_REPUTATION_LOSS));
                spreadReputationToNearbyMercenaries(mercenary, player, MercenaryReputation.KILL_REPUTATION_LOSS / 2);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PayloadHandler.syncAllMercenaryReputations(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PayloadHandler.syncAllMercenaryReputations(serverPlayer);
        }
    }

    private static void spreadReputationToNearbyMercenaries(BaseMercenary sourceMercenary, Player player, int reputationChange) {
        sourceMercenary.level().getEntitiesOfClass(BaseMercenary.class,
                        sourceMercenary.getBoundingBox().inflate(32.0))
                .stream()
                .filter(merc -> merc != sourceMercenary)
                .filter(merc -> merc.getMercenaryType() == sourceMercenary.getMercenaryType())
                .forEach(merc -> {
                    if (reputationChange > 0) {
                        merc.increaseReputationWith(player, reputationChange);
                    } else {
                        merc.decreaseReputationWith(player, Math.abs(reputationChange));
                    }
                });
    }
}
