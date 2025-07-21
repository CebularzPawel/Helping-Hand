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

    /**
     * Call this every tick to countdown the contract
     */
    public void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    /**
     * Check if the contract has expired
     */
    public boolean isExpired() {
        return remainingTicks <= 0;
    }

    /**
     * Get the UUID of the player who hired this mercenary
     */
    public UUID getHirer() {
        return hirerUUID;
    }

    /**
     * Get remaining time in seconds
     */
    public int getRemainingTime() {
        return Math.max(0, remainingTicks / 20);
    }

    /**
     * Get remaining time in ticks
     */
    public int getRemainingTicks() {
        return Math.max(0, remainingTicks);
    }

    /**
     * Get total duration in seconds
     */
    public int getTotalDuration() {
        return totalDurationTicks / 20;
    }

    /**
     * Get the time when this contract was created (system time)
     */
    public long getHiredTime() {
        return hiredTime;
    }

    /**
     * Get the progress of the contract (0.0 = just started, 1.0 = expired)
     */
    public float getProgress() {
        if (totalDurationTicks <= 0) return 1.0f;
        return 1.0f - (float) remainingTicks / totalDurationTicks;
    }

    /**
     * Add time to the contract
     * @param additionalTicks ticks to add
     */
    public void extendContract(int additionalTicks) {
        this.remainingTicks += additionalTicks;
    }

    /**
     * Check if a player is the owner of this contract
     */
    public boolean isOwner(Player player) {
        return player != null && hirerUUID.equals(player.getUUID());
    }

    /**
     * Check if a UUID is the owner of this contract
     */
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