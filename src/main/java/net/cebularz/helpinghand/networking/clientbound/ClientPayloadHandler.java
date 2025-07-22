package net.cebularz.helpinghand.networking.clientbound;

import net.cebularz.helpinghand.client.data.ClientReputationManager;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleReputationSync(ReputationSyncPacket packet, IPayloadContext context)
    {
        context.enqueueWork(()->{
            ClientReputationManager.updateReputation(packet.playerID(),packet.reputationLevel());
        });
    }
}
