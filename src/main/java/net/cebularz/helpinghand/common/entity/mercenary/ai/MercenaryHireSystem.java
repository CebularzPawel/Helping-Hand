package net.cebularz.helpinghand.common.entity.mercenary.ai;

import net.cebularz.helpinghand.common.entity.mercenary.BaseMercenary;
import net.cebularz.helpinghand.common.menu.MercenaryMenu;
import net.cebularz.helpinghand.core.ModMenus;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MercenaryHireSystem
{
    public final BaseMercenary mercenary;

    public MercenaryHireSystem(BaseMercenary mercenary) {
        this.mercenary = mercenary;
    }

    public InteractionResult handlePlayerInteraction(Player player, ItemStack heldItem) {
        if (mercenary.isHired()) {

            return handleHiredMercenaryInteraction(player);
        } else {
            return handleHireAttempt(player, heldItem);
        }
    }

    private InteractionResult handleHireAttempt(Player player, ItemStack payment) {
        HirePrice price = getPriceForType();

        if (price.canAffordAll(player)) {
            price.consumeAllPayment(player);
            MercenaryContract contract = new MercenaryContract(player, 6000); // 5 minutes
            mercenary.setContract(contract);

            player.sendSystemMessage(Component.literal("Mercenary hired for 5 minutes!"));
            return InteractionResult.SUCCESS;
        } else {
            player.sendSystemMessage(Component.literal("Price: " + price.getDescription()));
            return InteractionResult.PASS;
        }
    }

    private InteractionResult handleHiredMercenaryInteraction(Player player) {
        MercenaryContract contract = mercenary.getCurrentContract();
        if (contract != null && contract.getHirer().equals(player.getUUID())) {
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    public HirePrice getPriceForType() {
        return switch (mercenary.getMercenaryType()) {
            case ELF -> HirePrice.builder()
                    .add(Items.EMERALD, 5)
                    .add(Items.DIAMOND, 4)
                    .add(Items.CHICKEN, 2)
                    .build();

            case HUMAN -> HirePrice.builder()
                    .add(Items.GOLD_INGOT, 8)
                    .add(Items.IRON_INGOT, 15)
                    .add(Items.BREAD, 10)
                    .build();

            case UNDEAD -> HirePrice.builder()
                    .add(Items.BONE, 20)
                    .add(Items.ROTTEN_FLESH, 15)
                    .add(Items.SOUL_SAND, 5)
                    .add(Items.WITHER_SKELETON_SKULL, 1)
                    .build();

            case DWARFS -> HirePrice.builder()
                    .add(Items.DIAMOND, 3)
                    .add(Items.IRON_PICKAXE, 2)
                    .add(Items.PAPER, 5)
                    .add(Items.GOLDEN_APPLE, 1)
                    .add(Items.COAL, 32)
                    .build();

            case NONE -> HirePrice.builder()
                    .add(Items.STICK, 1)
                    .build();

            default -> HirePrice.builder()
                    .add(Items.IRON_INGOT, 5)
                    .build();
        };
    }

    public record HirePrice(Map<Item, Integer> itemRequirements) {

        public HirePrice(Item item, int amount) {
            this(Map.of(item, amount));
        }

        public HirePrice(List<Item> items, int amount) {
            this(items.stream().collect(Collectors.toMap(item -> item, item -> amount)));
        }

        public String getDescription() {
            if (itemRequirements.isEmpty()) {
                return "Free";
            }

            return itemRequirements.entrySet().stream()
                    .map(entry -> entry.getValue() + "x " + entry.getKey().getDescriptionId())
                    .collect(Collectors.joining(" + "));
        }

        public String getOrDescription() {
            if (itemRequirements.isEmpty()) {
                return "Free";
            }

            return itemRequirements.entrySet().stream()
                    .map(entry -> entry.getValue() + "x " + entry.getKey().getDescriptionId())
                    .collect(Collectors.joining(" OR "));
        }

        public boolean canAffordAll(Player player) {
            return itemRequirements.entrySet().stream()
                    .allMatch(entry -> player.getInventory().countItem(entry.getKey()) >= entry.getValue());
        }

        public boolean canAffordAny(Player player, ItemStack heldItem) {
            Integer requiredAmount = itemRequirements.get(heldItem.getItem());
            return requiredAmount != null && heldItem.getCount() >= requiredAmount;
        }

        public boolean canAffordWithStack(ItemStack stack) {
            Integer requiredAmount = itemRequirements.get(stack.getItem());
            return requiredAmount != null && stack.getCount() >= requiredAmount;
        }

        public boolean consumeAllPayment(Player player) {
            if (!canAffordAll(player)) {
                return false;
            }

            itemRequirements.forEach((item, amount) -> {
                player.getInventory().clearOrCountMatchingItems(stack ->
                        stack.getItem() == item, amount,player.getInventory());
            });
            return true;
        }

        public boolean consumeHeldPayment(Player player, ItemStack heldItem) {
            if (!canAffordWithStack(heldItem)) {
                return false;
            }

            Integer requiredAmount = itemRequirements.get(heldItem.getItem());
            heldItem.shrink(requiredAmount);
            return true;
        }

        public Optional<Map.Entry<Item, Integer>> getCheapestOption() {
            return itemRequirements.entrySet().stream()
                    .min(Map.Entry.comparingByValue());
        }

        public static class Builder {
            private final Map<Item, Integer> requirements = new HashMap<>();

            public Builder add(Item item, int amount) {
                requirements.put(item, amount);
                return this;
            }

            public Builder add(Item item) {
                return add(item, 1);
            }

            public HirePrice build() {
                return new HirePrice(Map.copyOf(requirements));
            }
        }

        public static Builder builder() {
            return new Builder();
        }
    }
}
