package net.cebularz.helpinghand.core;

import net.cebularz.helpinghand.Constants;
import net.cebularz.helpinghand.common.entity.mercenary.ai.MercenaryReputation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments
{
    public static final DeferredRegister<AttachmentType<?>> REGISTER = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MOD_ID
    );

    public static final Supplier<AttachmentType<MercenaryReputation>> MERCENARY_REPUTATION =
            REGISTER.register("mercenary_reputation", () -> AttachmentType.serializable(MercenaryReputation::new).build());
}
