package net.cebularz.helpinghand.common.capabilities.mercenary;

import net.cebularz.helpinghand.common.CommonClass;
import net.cebularz.helpinghand.common.capabilities.impl.IMercenaryReputation;
import net.neoforged.neoforge.capabilities.EntityCapability;

public class CapabilityInstances
{
    public static final EntityCapability<IMercenaryReputation, Void> MERCENARY_REPUTATION =
            EntityCapability.createVoid(
                    CommonClass.path("mercenary_reputation"),
                    IMercenaryReputation.class
            );
}
