package net.cebularz.helpinghand.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.cebularz.helpinghand.HelpingHandConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ReputationData {

    private int currentReputation;

    public ReputationData(int currentReputation) {
        this.currentReputation = currentReputation;
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
            Codec.INT.fieldOf("currentReputation").forGetter(ReputationData::getCurrentReputation)
    ).apply(inst, ReputationData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReputationData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, ReputationData data) {
            buf.writeInt(data.getCurrentReputation());
        }

        @Override
        public ReputationData decode(RegistryFriendlyByteBuf buf) {
            return new ReputationData(buf.readInt());
        }
    };
}
