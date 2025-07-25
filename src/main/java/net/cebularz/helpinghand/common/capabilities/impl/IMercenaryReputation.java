package net.cebularz.helpinghand.common.capabilities.impl;

import java.util.UUID;

public interface IMercenaryReputation
{
    int getCurrentReputation(UUID targetedUUID);

    void increaseReputation(UUID targeted, int toIncrease);
    void decreaseReputation(UUID targeted, int toDecrease);
}
