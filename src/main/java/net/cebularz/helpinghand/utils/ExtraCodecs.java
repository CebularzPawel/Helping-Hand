package net.cebularz.helpinghand.utils;

import com.mojang.serialization.Codec;

import java.util.UUID;

public class ExtraCodecs
{
    public static final Codec<UUID> CODEC_UUID = Codec.STRING.xmap(UUID::fromString, UUID::toString);
}
