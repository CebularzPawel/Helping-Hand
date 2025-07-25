package net.cebularz.helpinghand.common.entity.mercenary.ai;

import net.cebularz.helpinghand.common.entity.mercenary.BaseMercenary;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

public class MercenaryHireSystem {
    private final BaseMercenary mercenary;

    public MercenaryHireSystem(BaseMercenary mercenary) {
        this.mercenary = mercenary;
    }

    public void hireMercenary(Player player) {
        MercenaryContract contract = new MercenaryContract(player, 6000);
        mercenary.setContract(contract);
        mercenary.setOwner(player);
        player.sendSystemMessage(Component.literal("§aMercenary hired for 5 minutes!"));
    }

    public HirePrice getPriceForType() {
        return switch (mercenary.getMercenaryType()) {
            case ELF, HUMAN, UNDEAD, DWARFS, NONE -> new HirePrice(Items.DIAMOND, 5);
            default -> new HirePrice(Items.DIAMOND, 3);
        };
    }

    public record HirePrice(Item item, int amount) {
        public String getDescription() {
            return amount + "x " + item.getDescription().getString();
        }

        public String getAffordableItems(Player player) {
            int has = player.getInventory().countItem(item);
            String color = has >= amount ? "§a" : "§c";
            return color + has + "/" + amount + " " + item.getDescription().getString();
        }

        public boolean canAffordAll(Player player) {
            return player.getInventory().countItem(item) >= amount;
        }

        public boolean canAffordWithStack(ItemStack stack) {
            return stack.getItem() == item && stack.getCount() >= amount;
        }

        public boolean consumeAllPayment(Player player) {
            int remaining = amount;
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && stack.getItem() == item) {
                    int toConsume = Math.min(stack.getCount(), remaining);
                    stack.shrink(toConsume);
                    remaining -= toConsume;
                    if (remaining <= 0) break;
                }
            }
            return remaining <= 0;
        }

        public boolean consumeHeldPayment(Player player, ItemStack heldItem) {
            if (canAffordWithStack(heldItem)) {
                heldItem.shrink(amount);
                return true;
            }
            return false;
        }
    }
}