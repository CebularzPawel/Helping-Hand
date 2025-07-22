package net.cebularz.helpinghand.client.data;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientReputationManager {
    private static final Map<UUID, Integer> clientReputationCache = new HashMap<>();

    public static void updateReputation(UUID playerID, int reputation) {
        clientReputationCache.put(playerID, reputation);
    }

    public static int getReputation(UUID playerID) {
        return clientReputationCache.getOrDefault(playerID, 0);
    }

    public static void clearCache() {
        clientReputationCache.clear();
    }
}