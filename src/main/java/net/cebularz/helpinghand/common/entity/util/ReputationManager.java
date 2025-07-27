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

public class ReputationManager {

    public static void onMercenaryHired(Level level, Player player) {
        if (level.isClientSide()) return;

        MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION.get());
        reputation.addReputation(player.getUUID(), MercenaryReputation.HIRE_REPUTATION_GAIN);
        updateNearbyMercenariesReputation(level, player);
    }

    public static void onContractCompleted(Level level, Player player) {
        if (level.isClientSide()) return;

        MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION.get());
        reputation.addReputation(player.getUUID(), MercenaryReputation.SUCCESSFUL_CONTRACT_BONUS);
        updateNearbyMercenariesReputation(level, player);
    }

    public static void onMercenaryAttacked(Level level, Player player, BaseMercenary mercenary) {
        if (level.isClientSide()) return;

        MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION.get());
        reputation.addReputation(player.getUUID(), MercenaryReputation.ATTACK_REPUTATION_LOSS);

        alertNearbyMercenaries(level, mercenary, player);
        updateNearbyMercenariesReputation(level, player);
    }

    public static void onMercenaryKilled(Level level, Player player, BaseMercenary mercenary) {
        if (level.isClientSide()) return;

        MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION.get());
        reputation.addReputation(player.getUUID(), MercenaryReputation.KILL_REPUTATION_LOSS);

        alertNearbyMercenaries(level, mercenary, player);
        updateNearbyMercenariesReputation(level, player);
    }

    public static int getReputation(Level level, Player player) {
        return getReputation(level, player, null);
    }

    public static int getReputation(Level level, Player player, BaseMercenary contextMercenary) {
        if (level.isClientSide() && contextMercenary != null) {
            return contextMercenary.getPlayerReputation();
        } else if (!level.isClientSide()) {
            MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION.get());
            return reputation.getReputation(player.getUUID());
        } else {
            return 0;
        }
    }

    public static MercenaryReputation.ReputationLevel getReputationLevel(Level level, Player player) {
        if (level.isClientSide()) {
            int rep = 0;
            return MercenaryReputation.getReputationLevelFromValue(rep);
        }

        MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION.get());
        return reputation.getReputationLevel(player.getUUID());
    }

    public static MercenaryReputation.ReputationLevel getReputationLevel(Level level, Player player, BaseMercenary contextMercenary) {
        int rep = getReputation(level, player, contextMercenary);
        return MercenaryReputation.getReputationLevelFromValue(rep);
    }

    public static boolean canHire(Level level, Player player) {
        return canHire(level, player, null);
    }

    public static boolean canHire(Level level, Player player, BaseMercenary contextMercenary) {
        return true;
    }

    public static double getPriceMultiplier(Level level, Player player) {
        return getPriceMultiplier(level, player, null);
    }

    public static double getPriceMultiplier(Level level, Player player, BaseMercenary contextMercenary) {
        if (level.isClientSide() && contextMercenary != null) {
            int rep = contextMercenary.getPlayerReputation();
            return MercenaryReputation.getPriceMultiplierFromValue(rep);
        } else if (!level.isClientSide()) {
            MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION.get());
            return reputation.getPriceMultiplier(player.getUUID());
        }
        return 1.0;
    }

    public static boolean shouldBeHostile(Level level, Player player) {
        return shouldBeHostile(level, player, null);
    }

    public static boolean shouldBeHostile(Level level, Player player, BaseMercenary contextMercenary) {
        if (level.isClientSide() && contextMercenary != null) {
            int rep = contextMercenary.getPlayerReputation();
            return rep <= MercenaryReputation.HOSTILE_THRESHOLD;
        } else if (!level.isClientSide()) {
            MercenaryReputation reputation = level.getData(ModAttachments.MERCENARY_REPUTATION.get());
            return reputation.shouldAttackOnSight(player.getUUID());
        }
        return false;
    }

    private static void updateNearbyMercenariesReputation(Level level, Player player) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return;

        double updateRadius = 32.0;
        AABB searchArea = player.getBoundingBox().inflate(updateRadius);

        List<BaseMercenary> nearbyMercenaries = serverLevel.getEntitiesOfClass(
                BaseMercenary.class,
                searchArea,
                BaseMercenary::isAlive
        );

        int newReputation = getReputation(level, player);
        for (BaseMercenary mercenary : nearbyMercenaries) {
            mercenary.setPlayerReputation(newReputation);
        }
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
        return getReputationDisplay(level, player, null);
    }

    public static Component getReputationDisplay(Level level, Player player, BaseMercenary contextMercenary) {
        MercenaryReputation.ReputationLevel repLevel = getReputationLevel(level, player, contextMercenary);
        int reputation = getReputation(level, player, contextMercenary);

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

        updateNearbyMercenariesReputation(level, player);
    }
}
