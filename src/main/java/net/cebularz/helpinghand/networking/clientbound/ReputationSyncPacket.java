package net.cebularz.helpinghand.networking.clientbound;

import com.mojang.serialization.Codec;
import net.cebularz.helpinghand.common.CommonClass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Map;
import java.util.UUID;

public record ReputationSyncPacket(UUID playerID, int reputationLevel) implements CustomPacketPayload {
    public static final Type<ReputationSyncPacket> TYPE = new Type<>(CommonClass.path("serverboundSyncReputationPacket"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReputationSyncPacket> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ReputationSyncPacket>() {
        @Override
        public ReputationSyncPacket decode(RegistryFriendlyByteBuf buf) {
            return new ReputationSyncPacket(buf.readUUID(),buf.readInt());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ReputationSyncPacket packet) {
            buf.writeUUID(packet.playerID);
            buf.writeInt(packet.reputationLevel);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
