package net.cebularz.helpinghand.core;

import net.cebularz.helpinghand.Constants;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class ModItems
{
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(Constants.MOD_ID);

    public static final DeferredItem<Item> TEST_ENTITY_SPAWN_EGG = registerItem("test_entity_spawn_egg",
            properties -> new DeferredSpawnEggItem(ModEntity.TEST_ENTITY, 0x000F,0x0000D,properties));

    public static <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties,I> itemFunc)
    {
        return registerItem(name,itemFunc,true);
    }

    public static <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties,I> itemFunc, boolean putInTab)
    {
        DeferredItem<I> toRet = REGISTER.registerItem(name,itemFunc);
        if(putInTab){
            ModCreativeTabs.ITEM_LIST.add((DeferredItem<Item>) toRet);
        }
        return toRet;
    }
}
