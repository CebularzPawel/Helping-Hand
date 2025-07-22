package net.cebularz.helpinghand.networking;

import net.cebularz.helpinghand.networking.clientbound.ClientPayloadHandler;
import net.cebularz.helpinghand.networking.clientbound.ReputationSyncPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PayloadHandler
{

    @SubscribeEvent
    public static void registerPacketPayloads(final RegisterPayloadHandlersEvent event)
    {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(ReputationSyncPacket.TYPE, ReputationSyncPacket.STREAM_CODEC, ClientPayloadHandler::handleReputationSync);
    }
}
