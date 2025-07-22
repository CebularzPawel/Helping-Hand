package net.cebularz.helpinghand.core;

import net.cebularz.helpinghand.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModCreativeTabs
{
    public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    public static final List<DeferredItem<Item>> ITEM_LIST = new ArrayList<>();
    public static final List<DeferredBlock<Block>> BLOCK_LIST = new ArrayList<>();

    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> ITEM_TAB = REGISTER
            .register("item_tab",()-> CreativeModeTab.builder()
                    .title(Component.translatable("tab.helpinghand.item"))
                    .icon(()-> ItemStack.EMPTY)
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .displayItems((itemDisplayParameters, output) -> ITEM_LIST.forEach(output::accept))
                    .build());

    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> BLOCK_TAB = REGISTER
            .register("block_tab",()-> CreativeModeTab.builder()
                    .title(Component.translatable("tab.helpinghand.block"))
                    .icon(()-> ItemStack.EMPTY)
                    .withTabsBefore(ITEM_TAB.getKey())
                    .displayItems((itemDisplayParameters, output) -> BLOCK_LIST.forEach(output::accept))
                    .build());
}
