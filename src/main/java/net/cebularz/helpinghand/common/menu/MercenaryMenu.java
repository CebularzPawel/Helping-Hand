package net.cebularz.helpinghand.common.menu;

import net.cebularz.helpinghand.common.entity.mercenary.BaseMercenary;
import net.cebularz.helpinghand.common.entity.mercenary.ai.MercenaryContract;
import net.cebularz.helpinghand.common.entity.mercenary.ai.MercenaryHireSystem;
import net.cebularz.helpinghand.common.entity.util.ReputationManager;
import net.cebularz.helpinghand.core.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MercenaryMenu extends AbstractContainerMenu {

    private @NotNull final Entity associatedEntity;
    private final Container container;
    private @NotNull final Level level;
    private final Player player;
    private boolean processingSlotChange = false;

    public MercenaryMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf buf)
    {
        this(containerId, inv, new SimpleContainer(1) ,inv.player.level().getEntity(buf.readInt()));
    }

    public MercenaryMenu(int containerId, Inventory inv, Container container, Entity associatedEntity) {
        super(ModMenus.MERCENARY_MENU.get(), containerId);
        this.associatedEntity = associatedEntity;
        this.level = inv.player.level();
        this.container = container;
        this.player = inv.player;
        checkContainerSize(container, 1);
        container.startOpen(inv.player);
        this.addSlot(new PaymentSlot(container,0,141,34));
        addPlayerInventorySlots(105,83,inv);
    }

    private class PaymentSlot extends Slot {
        public PaymentSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            checkHireConditions();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (!(associatedEntity instanceof BaseMercenary mercenary)) return false;
            if (stack.isEmpty()) return false;

            MercenaryHireSystem hireSystem = mercenary.getHireSystem();
            return hireSystem.getTimeForItems(stack) > 0;
        }
    }

    private void checkHireConditions() {
        if (!(associatedEntity instanceof BaseMercenary mercenary)) return;
        if (level.isClientSide) return;
        if (processingSlotChange) return;

        ItemStack slotItem = container.getItem(0);
        if (slotItem.isEmpty()) return;

        MercenaryHireSystem hireSystem = mercenary.getHireSystem();

        if (hireSystem.getTimeForItems(slotItem) <= 0) return;

        boolean canProcess = false;

        if (!mercenary.isHired()) {
            canProcess = hireSystem.canPlayerHire(player);
        } else {
            MercenaryContract contract = mercenary.getCurrentContract();
            canProcess = contract != null && !contract.isExpired() && contract.isOwner(player);
        }

        if (canProcess) {
            processingSlotChange = true;
            try {
                ItemStack itemToProcess = slotItem.copy();
                itemToProcess.setCount(1);

                slotItem.shrink(1);
                hireSystem.hireMercenary(player, itemToProcess);
                container.setChanged();
            } finally {
                processingSlotChange = false;
            }
        }
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!this.level.isClientSide) {
            ItemStack remainingItem = this.container.getItem(0);
            if (!remainingItem.isEmpty()) {
                player.getInventory().placeItemBackInInventory(remainingItem);
                this.container.setItem(0, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if(slot.hasItem())
        {
            ItemStack slotStack = slot.getItem();
            stack = slotStack.copy();
            if(slotIndex < this.container.getContainerSize())
            {
                if(!this.moveItemStackTo(slotStack, this.container.getContainerSize(), this.slots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(!this.moveItemStackTo(slotStack, 0, 1, false))
            {
                return ItemStack.EMPTY;
            }
            if(slotStack.isEmpty())
            {
                slot.setByPlayer(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }
        }
        return stack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player) && associatedEntity.isAlive() &&
                associatedEntity.distanceToSqr(player) < 64.0; // Add distance check
    }

    protected void addPlayerInventorySlots(int x, int y, Inventory playerInventory)
    {
        for(int j = 0; j < 3; j++)
        {
            for(int i = 0; i < 9; i++)
            {
                int slotIndex = i + j * 9 + 9;
                int slotX = x + i * 18;
                int slotY = y + j * 18;
                this.addSlot(new Slot(playerInventory, slotIndex, slotX, slotY));
            }
        }
        for(int i = 0; i < 9; i++)
        {
            int slotX = x + i * 18;
            int slotY = y + 58;
            this.addSlot(new Slot(playerInventory, i, slotX, slotY));
        }
    }

    public @NotNull Entity getAssociatedEntity() {
        return associatedEntity;
    }

    public @NotNull Level getLevel() {
        return level;
    }
}