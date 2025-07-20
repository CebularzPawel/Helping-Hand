package net.cebularz.helpinghand.common.entity.mercenary.ai;

import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class MercenaryContract
{
    private final UUID hirerUUID;
    private final long hiredTime;
    private final int durationTicks;
    private int remainingTicks;

    public MercenaryContract(Player hirer, int durationTicks) {
        this.hirerUUID = hirer.getUUID();
        this.hiredTime = System.currentTimeMillis();
        this.durationTicks = durationTicks;
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
        return remainingTicks / 20;
    }
}
