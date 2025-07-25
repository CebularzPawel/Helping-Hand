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
    public final Map<ItemStack, Integer> priceTimeMap = new HashMap<>();
    public MercenaryHireSystem(BaseMercenary mercenary) {
        this.mercenary = mercenary;
        addTimeForItem();
    }

    public boolean canPlayerHire(Player player) {
        return ReputationManager.canHire(mercenary.level(), player);
    }

    public void hireMercenary(Player player, ItemStack hiringItem) {
        MercenaryContract contract = new MercenaryContract(player, getTimeForItems(hiringItem));
        mercenary.setContract(contract);
        mercenary.setOwner(player);

        ReputationManager.onMercenaryHired(mercenary.level(), player);

        player.sendSystemMessage(Component.literal("§aMercenary hired for 5 minutes!"));
    }

    public int getTimeForItems(ItemStack item)
    {
        return priceTimeMap.get(item);
    }

    public void addTimeForItem()
    {
        addItemForItem(new ItemStack(Items.DIAMOND),200);
        addItemForItem(new ItemStack(Items.NETHERITE_INGOT),500);
    }

    public void addItemForItem(ItemStack stack, int timeToAdd)
    {
        priceTimeMap.put(stack,timeToAdd);
    }
}