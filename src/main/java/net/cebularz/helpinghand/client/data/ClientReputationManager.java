package net.cebularz.helpinghand.client.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side manager for storing mercenary reputation data
 * This allows the client to access reputation information for UI display
 */
public class ClientReputationManager {
    private static final Map<UUID, Integer> mercenaryReputations = new HashMap<>();

    /**
     * Update reputation for a specific mercenary
     */
    public static void updateReputation(UUID mercenaryUUID, int reputation) {
        mercenaryReputations.put(mercenaryUUID, reputation);
    }

    /**
     * Update all reputation data (used when joining world or changing dimensions)
     */
    public static void updateAllReputations(Map<UUID, Integer> reputations) {
        mercenaryReputations.clear();
        mercenaryReputations.putAll(reputations);
    }

    /**
     * Get reputation with a specific mercenary
     */
    public static int getReputation(UUID mercenaryUUID) {
        return mercenaryReputations.getOrDefault(mercenaryUUID, 0);
    }

    /**
     * Check if we have reputation data for a mercenary
     */
    public static boolean hasReputationData(UUID mercenaryUUID) {
        return mercenaryReputations.containsKey(mercenaryUUID);
    }

    /**
     * Get reputation level string for display
     */
    public static String getReputationDescription(UUID mercenaryUUID) {
        int reputation = getReputation(mercenaryUUID);
        String level = getReputationLevelName(reputation);
        return level + " (" + reputation + ")";
    }

    /**
     * Get reputation level name based on reputation value
     */
    public static String getReputationLevelName(int reputation) {
        if (reputation >= 75) return "§bExcellent";
        if (reputation >= 50) return "§2Good";
        if (reputation >= 25) return "§aFriendly";
        if (reputation >= -10) return "§7Neutral";
        if (reputation >= -25) return "§6Unfriendly";
        if (reputation >= -50) return "§cHostile";
        return "§4Hated";
    }

    /**
     * Get price multiplier based on reputation
     */
    public static float getPriceMultiplier(UUID mercenaryUUID) {
        int reputation = getReputation(mercenaryUUID);
        if (reputation >= 75) return 0.5f;
        if (reputation >= 50) return 0.75f;
        if (reputation >= 25) return 0.9f;
        if (reputation >= -10) return 1.0f;
        if (reputation >= -25) return 1.25f;
        if (reputation >= -50) return 1.5f;
        return 2.0f;
    }

    /**
     * Check if mercenary can be hired based on reputation
     */
    public static boolean canBeHired(UUID mercenaryUUID) {
        return getReputation(mercenaryUUID) >= -25;
    }

    /**
     * Clear all reputation data (useful for disconnect)
     */
    public static void clearAll() {
        mercenaryReputations.clear();
    }
}