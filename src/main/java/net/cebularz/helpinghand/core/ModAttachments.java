package net.cebularz.helpinghand.core;

import net.cebularz.helpinghand.Constants;
import net.cebularz.helpinghand.common.data.reputation.ReputationData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments
{
    public static final DeferredRegister<AttachmentType<?>> REGISTER = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MOD_ID);

    public static final Supplier<AttachmentType<ReputationData>> REPUTATION = REGISTER.register("reputation",
            () -> AttachmentType.builder(() -> new ReputationData(null,0))
                    .serialize(ReputationData.CODEC)
                    .build()
    );
}
