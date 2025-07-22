package net.cebularz.helpinghand.core;

import net.cebularz.helpinghand.Constants;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModBlocks
{
    public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(Constants.MOD_ID);

    public static <I extends Block> DeferredBlock<I> registerBlock(String name,Function<BlockBehaviour.Properties,I> blockFunc)
    {
        return registerBlock(name,blockFunc,true);
    }

    public static <I extends Block> DeferredBlock<I> registerBlock(String name, Function<BlockBehaviour.Properties,I> blockFunc, boolean putInTab)
    {
        DeferredBlock<I> toRet = REGISTER.registerBlock(name,blockFunc);
        ModItems.registerItem(name,properties -> new BlockItem(toRet.get(),properties),false);
        if(putInTab){
            ModCreativeTabs.BLOCK_LIST.add((DeferredBlock<Block>) toRet);
        }
        return toRet;
    }
}
