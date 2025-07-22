package net.cebularz.helpinghand.common.entity.mercenary.ai;

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