package net.cebularz.helpinghand.common.entity.util;

import net.cebularz.helpinghand.common.entity.mercenary.BaseMercenary;
import net.cebularz.helpinghand.common.entity.mercenary.ai.MercenaryReputation;
import net.cebularz.helpinghand.core.ModAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

public class ReputationManager {

    public static void onMercenaryHired(Level level, Player player) {
        if (level.isClientSide()) return;

        MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION.get());
        reputation.addReputation(player.getUUID(), MercenaryReputation.HIRE_REPUTATION_GAIN);

    }

    public static void onContractCompleted(Level level, Player player) {
        if (level.isClientSide()) return;

        MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION.get());
        reputation.addReputation(player.getUUID(), MercenaryReputation.SUCCESSFUL_CONTRACT_BONUS);

    }

    public static void onMercenaryAttacked(Level level, Player player, BaseMercenary mercenary) {
        if (level.isClientSide()) return;

        MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION.get());
        reputation.addReputation(player.getUUID(), MercenaryReputation.ATTACK_REPUTATION_LOSS);

        alertNearbyMercenaries(level, mercenary, player);

    }

    public static void onMercenaryKilled(Level level, Player player, BaseMercenary mercenary) {
        if (level.isClientSide()) return;

        MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION.get());
        reputation.addReputation(player.getUUID(), MercenaryReputation.KILL_REPUTATION_LOSS);

        alertNearbyMercenaries(level, mercenary, player);

    }

    public static MercenaryReputation.ReputationLevel getReputationLevel(Level level, Player player) {
        MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION.get());
        return reputation.getReputationLevel(player.getUUID());
    }

    public static int getReputation(Level level, Player player) {
        MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION.get());
        return reputation.getReputation(player.getUUID());
    }

    public static boolean canHire(Level level, Player player) {
        MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION.get());
        return reputation.canHire(player.getUUID());
    }

    public static double getPriceMultiplier(Level level, Player player) {
        MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION.get());
        return reputation.getPriceMultiplier(player.getUUID());
    }

    public static boolean shouldBeHostile(Level level, Player player) {
        MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION.get());
        return reputation.shouldAttackOnSight(player.getUUID());
    }

    private static void alertNearbyMercenaries(Level level, BaseMercenary attackedMercenary, Player attacker) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        double alertRadius = 16.0;
        AABB searchArea = attackedMercenary.getBoundingBox().inflate(alertRadius);

        List<BaseMercenary> nearbyMercenaries = serverLevel.getEntitiesOfClass(
                BaseMercenary.class,
                searchArea,
                mercenary -> mercenary != attackedMercenary && mercenary.isAlive()
        );

        for (BaseMercenary mercenary : nearbyMercenaries) {
            mercenary.setLastHurtByMob(attacker);
            mercenary.startPersistentAngerTimer();
            mercenary.setPersistentAngerTarget(attacker.getUUID());
        }
    }

    public static Component getReputationDisplay(Level level, Player player) {
        MercenaryReputation.ReputationLevel repLevel = getReputationLevel(level, player);
        int reputation = getReputation(level, player);

        String levelColor = String.format("§%x", repLevel.getColor());

        return Component.literal(String.format("§7Reputation: §f%d §7(%s%s§7)",
                reputation, levelColor, repLevel.getDisplayName()));
    }

    public static void debugAttachment(Level level, Player player) {
        if (level.isClientSide()) return;

        try {
            MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION);

            if (reputation != null) {
                int rep = reputation.getReputation(player.getUUID());
                MercenaryReputation.ReputationLevel repLevel = reputation.getReputationLevel(player.getUUID());

                player.sendSystemMessage(Component.literal("§aAttachment working! Your reputation: " + rep + " (" + repLevel.getDisplayName() + ")"));

                CompoundTag serialized = reputation.serializeNBT(level.registryAccess());
                player.sendSystemMessage(Component.literal("§7Serialized data: " + serialized.toString()));

            } else {
                player.sendSystemMessage(Component.literal("§cAttachment is null!"));
            }
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("§cError accessing attachment: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    public static void testReputationSystem(Level level, Player player) {
        if (level.isClientSide()) return;

        player.sendSystemMessage(Component.literal("§7Testing reputation system..."));

        int initialRep = getReputation(level, player);
        player.sendSystemMessage(Component.literal("§7Initial reputation: " + initialRep));

        MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION);
        reputation.addReputation(player.getUUID(), 50);

        int newRep = getReputation(level, player);
        player.sendSystemMessage(Component.literal("§aNew reputation: " + newRep + " (added 50)"));

        double multiplier = getPriceMultiplier(level, player);
        player.sendSystemMessage(Component.literal("§7Price multiplier: " + multiplier));
    }
}