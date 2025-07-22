package net.cebularz.helpinghand.common.data.reputation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.cebularz.helpinghand.HelpingHandConfig;
import net.cebularz.helpinghand.utils.ExtraCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public class ReputationData {

    private final UUID playerID;
    private int currentReputation;

    public ReputationData(UUID playerID, int currentReputation) {
        this.playerID = playerID;
        this.setCurrentReputation(currentReputation);
    }

    public UUID getPlayerID() {
        return playerID;
    }

    public int getCurrentReputation() {
        return currentReputation;
    }

    public void setCurrentReputation(int value) {
        this.currentReputation = Math.max(0, Math.min(HelpingHandConfig.MAX_MERCENARY_REPUTATION.get(), value));
    }

    public void increase(int amount) {
        setCurrentReputation(currentReputation + amount);
    }

    public void decrease(int amount) {
        setCurrentReputation(currentReputation - amount);
    }

    public static final Codec<ReputationData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ExtraCodecs.CODEC_UUID.fieldOf("playerID").forGetter(ReputationData::getPlayerID),
            Codec.INT.fieldOf("currentReputation").forGetter(ReputationData::getCurrentReputation)
    ).apply(inst, ReputationData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReputationData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, ReputationData data) {
            buf.writeUUID(data.getPlayerID());
            buf.writeInt(data.getCurrentReputation());
        }

        @Override
        public ReputationData decode(RegistryFriendlyByteBuf buf) {
            return new ReputationData(buf.readUUID(), buf.readInt());
        }
    };
}
