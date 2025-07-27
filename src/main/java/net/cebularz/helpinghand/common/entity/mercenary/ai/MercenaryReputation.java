package net.cebularz.helpinghand.common.entity.mercenary.ai;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MercenaryReputation implements INBTSerializable<CompoundTag> {
    private final Map<UUID, Integer> playerReputations = new HashMap<>();

    // Constant Fields (not to be touched, or can be if ur a coder ig)
    public static final int MIN_REPUTATION = -100;
    public static final int MAX_REPUTATION = 100;
    public static final int NEUTRAL_REPUTATION = 0;

    public static final int HIRE_REPUTATION_GAIN = 10;
    public static final int SUCCESSFUL_CONTRACT_BONUS = 5;
    public static final int ATTACK_REPUTATION_LOSS = -25;
    public static final int KILL_REPUTATION_LOSS = -50;

    public static final int HOSTILE_THRESHOLD = -50;
    public static final int UNFRIENDLY_THRESHOLD = -75;
    public static final int NEUTRAL_THRESHOLD = 0;
    public static final int FRIENDLY_THRESHOLD = 50;
    public static final int HONORED_THRESHOLD = 75;
    public static final int REVERED_THRESHOLD = 100;

    public int getReputation(UUID playerId) {
        return playerReputations.getOrDefault(playerId, NEUTRAL_REPUTATION);
    }

    public void setReputation(UUID playerId, int reputation) {
        reputation = Mth.clamp(reputation, MIN_REPUTATION, MAX_REPUTATION);
        if (reputation == NEUTRAL_REPUTATION) {
            playerReputations.remove(playerId);
        } else {
            playerReputations.put(playerId, reputation);
        }
    }

    public void addReputation(UUID playerId, int amount) {
        int currentRep = getReputation(playerId);
        setReputation(playerId, currentRep + amount);
    }

    public ReputationLevel getReputationLevel(UUID playerId) {
        int rep = getReputation(playerId);

        if (rep >= REVERED_THRESHOLD) return ReputationLevel.REVERED;
        if (rep >= HONORED_THRESHOLD) return ReputationLevel.HONORED;
        if (rep >= FRIENDLY_THRESHOLD) return ReputationLevel.FRIENDLY;
        if (rep >= NEUTRAL_THRESHOLD) return ReputationLevel.NEUTRAL;
        if (rep >= UNFRIENDLY_THRESHOLD) return ReputationLevel.UNFRIENDLY;
        return ReputationLevel.HOSTILE;
    }

    public double getPriceMultiplier(UUID playerId) {
        return getReputationLevel(playerId).getPriceMultiplier();
    }

    public boolean canHire(UUID playerId) {
        return getReputationLevel(playerId).canHire();
    }

    public boolean isHostile(UUID playerId) {
        return getReputationLevel(playerId) == ReputationLevel.HOSTILE;
    }

    public boolean shouldAttackOnSight(UUID playerId) {
        return getReputation(playerId) <= HOSTILE_THRESHOLD;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        tag.putInt("count", playerReputations.size());

        int index = 0;
        for (Map.Entry<UUID, Integer> entry : playerReputations.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("playerId", entry.getKey());
            entryTag.putInt("reputation", entry.getValue());
            tag.put("entry_" + index, entryTag);
            index++;
        }

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        playerReputations.clear();

        if (!tag.contains("count")) {
            deserializeOldFormat(tag);
            return;
        }

        int count = tag.getInt("count");

        for (int i = 0; i < count; i++) {
            String key = "entry_" + i;
            if (tag.contains(key)) {
                CompoundTag entryTag = tag.getCompound(key);
                try {
                    UUID playerId = entryTag.getUUID("playerId");
                    int reputation = entryTag.getInt("reputation");
                    playerReputations.put(playerId, reputation);
                } catch (Exception e) {
                    System.err.println("Failed to deserialize reputation entry " + i + ": " + e.getMessage());
                }
            }
        }
    }

    private void deserializeOldFormat(CompoundTag tag) {
        if (tag.contains("reputations")) {
            CompoundTag reputations = tag.getCompound("reputations");

            for (String key : reputations.getAllKeys()) {
                try {
                    UUID playerId = UUID.fromString(key);
                    int reputation = reputations.getInt(key);
                    playerReputations.put(playerId, reputation);
                } catch (IllegalArgumentException e) {
                    System.err.println("Skipping invalid UUID in reputation data: " + key);
                }
            }
        }
    }

    public static ReputationLevel getReputationLevelFromValue(int reputation) {
        if (reputation >= REVERED_THRESHOLD) return ReputationLevel.REVERED;
        if (reputation >= HONORED_THRESHOLD) return ReputationLevel.HONORED;
        if (reputation >= FRIENDLY_THRESHOLD) return ReputationLevel.FRIENDLY;
        if (reputation >= NEUTRAL_THRESHOLD) return ReputationLevel.NEUTRAL;
        if (reputation >= UNFRIENDLY_THRESHOLD) return ReputationLevel.UNFRIENDLY;
        if (reputation >= HOSTILE_THRESHOLD) return ReputationLevel.HOSTILE;
        return ReputationLevel.UNFRIENDLY;
    }

    public static double getPriceMultiplierFromValue(int reputation) {
        ReputationLevel level = getReputationLevelFromValue(reputation);
        return 0;
    }

    public static boolean canHireFromValue(int reputation) {
        return false;
    }

    public static boolean shouldAttackOnSightFromValue(int reputation) {
        return reputation <= HOSTILE_THRESHOLD;
    }

    public enum ReputationLevel {
        HOSTILE("Hostile", 2.0, false, 0xFF5555),
        UNFRIENDLY("Unfriendly", 1.5, false, 0xFFAA00),
        NEUTRAL("Neutral", 1.0, true, 0xFFFFFF),
        FRIENDLY("Friendly", 0.9, true, 0x55FF55),
        HONORED("Honored", 0.75, true, 0x5555FF),
        REVERED("Revered", 0.5, true, 0xFF55FF);

        private final String displayName;
        private final double priceMultiplier;
        private final boolean canHire;
        private final int color;

        ReputationLevel(String displayName, double priceMultiplier, boolean canHire, int color) {
            this.displayName = displayName;
            this.priceMultiplier = priceMultiplier;
            this.canHire = canHire;
            this.color = color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public double getPriceMultiplier() {
            return priceMultiplier;
        }

        public boolean canHire() {
            return canHire;
        }

        public int getColor() {
            return color;
        }
    }
}