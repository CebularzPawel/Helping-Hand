package net.cebularz.helpinghand.common.entity.mercenary.ai;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class MercenaryContract {
    private final UUID hirerUUID;
    private final long hiredTime;
    private final int totalDurationTicks;
    private int remainingTicks;

    public MercenaryContract(Player hirer, int durationTicks) {
        this.hirerUUID = hirer.getUUID();
        this.hiredTime = System.currentTimeMillis();
        this.totalDurationTicks = durationTicks;
        this.remainingTicks = durationTicks;
    }

    private MercenaryContract(UUID hirerUUID, long hiredTime, int totalDurationTicks, int remainingTicks) {
        this.hirerUUID = hirerUUID;
        this.hiredTime = hiredTime;
        this.totalDurationTicks = totalDurationTicks;
        this.remainingTicks = remainingTicks;
    }

    public void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    public boolean isExpired() {
        return remainingTicks <= 0;
    }

    public UUID getHirer() {
        return hirerUUID;
    }

    public int getRemainingTime() {
        return Math.max(0, remainingTicks / 20);
    }

    public int getRemainingTicks() {
        return Math.max(0, remainingTicks);
    }

    public int getTotalDuration() {
        return totalDurationTicks / 20;
    }

    public long getHiredTime() {
        return hiredTime;
    }

    public float getProgress() {
        if (totalDurationTicks <= 0) return 1.0f;
        return 1.0f - (float) remainingTicks / totalDurationTicks;
    }

    public void extendContract(int additionalTicks) {
        this.remainingTicks += additionalTicks;
    }

    public boolean isOwner(Player player) {
        return player != null && hirerUUID.equals(player.getUUID());
    }

    public boolean isOwner(UUID uuid) {
        return hirerUUID.equals(uuid);
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("hirerUUID", hirerUUID);
        tag.putLong("hiredTime", hiredTime);
        tag.putInt("totalDurationTicks", totalDurationTicks);
        tag.putInt("remainingTicks", remainingTicks);
        return tag;
    }

    public static MercenaryContract fromNBT(CompoundTag tag) {
        if (!tag.hasUUID("hirerUUID")) {
            throw new IllegalArgumentException("Contract NBT missing hirerUUID");
        }

        return new MercenaryContract(
                tag.getUUID("hirerUUID"),
                tag.getLong("hiredTime"),
                tag.getInt("totalDurationTicks"),
                tag.getInt("remainingTicks")
        );
    }

    @Override
    public String toString() {
        return "MercenaryContract{" +
                "hirer=" + hirerUUID +
                ", remainingTicks=" + remainingTicks +
                ", totalDuration=" + totalDurationTicks +
                ", progress=" + String.format("%.1f%%", getProgress() * 100) +
                '}';
    }
}
