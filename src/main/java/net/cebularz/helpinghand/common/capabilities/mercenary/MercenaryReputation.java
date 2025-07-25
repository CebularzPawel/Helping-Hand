package net.cebularz.helpinghand.common.capabilities.mercenary;

import net.cebularz.helpinghand.common.capabilities.impl.IMercenaryReputation;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MercenaryReputation implements IMercenaryReputation, INBTSerializable<CompoundTag>
{
    private final Map<UUID, Integer> reputationMap = new HashMap<>();

    @Override
    public int getCurrentReputation(UUID targetedUUID) {
        return reputationMap.getOrDefault(targetedUUID, 0);
    }

    @Override
    public void increaseReputation(UUID targeted, int toIncrease) {
        int current = getCurrentReputation(targeted);
        reputationMap.put(targeted, current + toIncrease);
    }

    @Override
    public void decreaseReputation(UUID targeted, int toDecrease) {
        int current = getCurrentReputation(targeted);
        reputationMap.put(targeted, current - toDecrease);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        CompoundTag reputations = new CompoundTag();

        for (Map.Entry<UUID, Integer> entry : reputationMap.entrySet()) {
            reputations.putInt(entry.getKey().toString(), entry.getValue());
        }

        tag.put("reputations", reputations);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        reputationMap.clear();

        if (nbt.contains("reputations")) {
            CompoundTag reputations = nbt.getCompound("reputations");
            for (String key : reputations.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    int reputation = reputations.getInt(key);
                    reputationMap.put(uuid, reputation);
                } catch (IllegalArgumentException e) {
                }
            }
        }
    }
}