package net.cebularz.helpinghand.common.entity.mercenary.ai;

import net.cebularz.helpinghand.common.entity.mercenary.BaseMercenary;
import net.cebularz.helpinghand.common.entity.util.ReputationManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class MercenaryHireSystem {
    private final BaseMercenary mercenary;
    public final Map<Item, Integer> priceTimeMap = new HashMap<>();

    public MercenaryHireSystem(BaseMercenary mercenary) {
        this.mercenary = mercenary;
        addTimeForItem();
    }

    public boolean canPlayerHire(Player player) {
        return true;
    }

    public void hireMercenary(Player player, ItemStack hiringItem) {
        int timeToAdd = getTimeForItems(hiringItem);

        MercenaryContract currentContract = mercenary.getCurrentContract();

        if (currentContract != null && !currentContract.isExpired()) {
            currentContract.extendContract(timeToAdd);

            int totalRemainingSeconds = currentContract.getRemainingTime();
            player.sendSystemMessage(Component.literal(
                    "§aMercenary contract extended! Time remaining: " + formatTime(totalRemainingSeconds)
            ));
        } else {
            MercenaryContract contract = new MercenaryContract(player, timeToAdd);
            mercenary.setContract(contract);
            mercenary.setOwner(player);

            int durationSeconds = timeToAdd / 20;
            player.sendSystemMessage(Component.literal(
                    "§aMercenary hired for " + formatTime(durationSeconds) + "!"
            ));
        }

        ReputationManager.onMercenaryHired(mercenary.level(), player);
    }

    public int getTimeForItems(ItemStack item) {
        return priceTimeMap.getOrDefault(item.getItem(), 0);
    }

    public void addTimeForItem() {
        addItemForItem(Items.DIAMOND, 200);
        addItemForItem(Items.NETHERITE_INGOT, 500);
        addItemForItem(Items.EMERALD, 300);
        addItemForItem(Items.GOLD_INGOT, 100);
    }

    public void addItemForItem(Item item, int timeToAdd) {
        priceTimeMap.put(item, timeToAdd);
    }

    private String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + " seconds";
        } else {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return minutes + ":" + String.format("%02d", remainingSeconds) + " minutes";
        }
    }

    public boolean canExtendContract() {
        MercenaryContract contract = mercenary.getCurrentContract();
        return contract != null && !contract.isExpired();
    }

    public int getRemainingContractTime() {
        MercenaryContract contract = mercenary.getCurrentContract();
        if (contract != null && !contract.isExpired()) {
            return contract.getRemainingTime();
        }
        return 0;
    }
}