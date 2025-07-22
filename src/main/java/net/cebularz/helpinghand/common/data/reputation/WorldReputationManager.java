package net.cebularz.helpinghand.common.data.reputation;

import net.cebularz.helpinghand.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldReputationManager extends SavedData {

    private static final String DATA_NAME = Constants.MOD_ID + "_reputation_data";
    private final ReputationManager reputationManager;
    private final ServerLevel level;

    public WorldReputationManager(ServerLevel level) {
        super();
        this.level = level;
        this.reputationManager = new ReputationManager(level);
    }

    public static WorldReputationManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        () -> new WorldReputationManager(level),
                        (tag, provider) -> load(tag, provider, level),
                        null
                ),
                DATA_NAME
        );
    }

    public ReputationManager getReputationManager() {
        return reputationManager;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        ListTag reputationList = new ListTag();

        for (Map.Entry<UUID, ReputationData> entry : reputationManager.getAllReputations().entrySet()) {
            CompoundTag reputationTag = new CompoundTag();
            reputationTag.putUUID("playerID", entry.getKey());
            reputationTag.putInt("reputation", entry.getValue().getCurrentReputation());
            reputationList.add(reputationTag);
        }

        tag.put("reputations", reputationList);
        return tag;
    }

    public static WorldReputationManager load(CompoundTag tag, HolderLookup.Provider registries, ServerLevel level) {
        WorldReputationManager manager = new WorldReputationManager(level);

        if (tag.contains("reputations", Tag.TAG_LIST)) {
            ListTag reputationList = tag.getList("reputations", Tag.TAG_COMPOUND);

            for (int i = 0; i < reputationList.size(); i++) {
                CompoundTag reputationTag = reputationList.getCompound(i);
                UUID playerID = reputationTag.getUUID("playerID");
                int reputation = reputationTag.getInt("reputation");

                manager.reputationManager.getOrCreate(playerID).setCurrentReputation(reputation);
            }
        }

        return manager;
    }

    public void onPlayerJoin(ServerPlayer player) {
        reputationManager.syncToPlayer(player);
    }

    @Override
    public boolean isDirty() {
        return super.isDirty();
    }

    public void markDirty() {
        setDirty();
    }
}
