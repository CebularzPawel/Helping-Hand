package net.cebularz.helpinghand.common.menu;

import net.cebularz.helpinghand.core.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
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

    public MercenaryMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf buf)
    {
        this(containerId, inv, new SimpleContainer(1) ,inv.player.level().getEntity(buf.readInt()));
    }

    protected MercenaryMenu(int containerId, Inventory inv, Container container ,Entity associatedEntity) {
        super(ModMenus.MERCENARY_MENU.get(), containerId);
        this.associatedEntity = associatedEntity;
        this.level = inv.player.level();
        this.container = container;
        checkContainerSize(container, 1);
        container.startOpen(inv.player);

        addPlayerInventorySlots(105,83,inv);
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
        return this.container.stillValid(player);
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
