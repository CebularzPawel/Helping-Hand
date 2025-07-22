package net.cebularz.helpinghand.common.data.reputation;

import net.cebularz.helpinghand.networking.clientbound.ReputationSyncPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReputationManager {

    private final Map<UUID, ReputationData> reputationMap = new HashMap<>();
    private final ServerLevel level;

    public ReputationManager(ServerLevel level) {
        this.level = level;
    }

    public ReputationData getOrCreate(UUID playerID) {
        return reputationMap.computeIfAbsent(playerID, id -> new ReputationData(id, 0));
    }

    public void setReputation(UUID playerID, int reputation) {
        getOrCreate(playerID).setCurrentReputation(reputation);
        syncToClient(playerID);
    }

    public void increaseReputation(UUID playerID, int amount) {
        getOrCreate(playerID).increase(amount);
        syncToClient(playerID);
    }

    public void decreaseReputation(UUID playerID, int amount) {
        getOrCreate(playerID).decrease(amount);
        syncToClient(playerID);
    }

    public int getReputation(UUID playerID) {
        return getOrCreate(playerID).getCurrentReputation();
    }

    public Map<UUID, ReputationData> getAllReputations() {
        return reputationMap;
    }

    private void syncToClient(UUID playerID) {
        if (level != null) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerID);
            if (player != null) {
                int reputation = getReputation(playerID);
                ReputationSyncPacket packet = new ReputationSyncPacket(playerID, reputation);
                PacketDistributor.sendToPlayer(player, packet);
            }
        }
    }

    // Method to sync reputation to a specific player (useful for login)
    public void syncToPlayer(ServerPlayer player) {
        int reputation = getReputation(player.getUUID());
        ReputationSyncPacket packet = new ReputationSyncPacket(player.getUUID(), reputation);
        PacketDistributor.sendToPlayer(player, packet);
    }

    // Method to sync all reputations to a player (admin/debugging)
    public void syncAllToPlayer(ServerPlayer player) {
        for (Map.Entry<UUID, ReputationData> entry : reputationMap.entrySet()) {
            ReputationSyncPacket packet = new ReputationSyncPacket(
                    entry.getKey(),
                    entry.getValue().getCurrentReputation()
            );
            PacketDistributor.sendToPlayer(player, packet);
        }
    }
}