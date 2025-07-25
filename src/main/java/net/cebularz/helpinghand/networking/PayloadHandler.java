package net.cebularz.helpinghand.networking;

import net.cebularz.helpinghand.common.entity.mercenary.BaseMercenary;
import net.cebularz.helpinghand.networking.clientbound.ClientPayloadHandler;
import net.cebularz.helpinghand.networking.clientbound.ReputationSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PayloadHandler
{

    public static void syncReputation(ServerPlayer player, UUID playerUUID, int reputationLevel)
    {
        ReputationSyncPacket packet = new ReputationSyncPacket(playerUUID, reputationLevel);
        PacketDistributor.sendToPlayer(player, packet);
    }

    @SubscribeEvent
    public static void registerPacketPayloads(final RegisterPayloadHandlersEvent event)
    {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(ReputationSyncPacket.TYPE, ReputationSyncPacket.STREAM_CODEC,
                ClientPayloadHandler::handleReputationSync);
    }
}
