package net.cebularz.helpinghand.core;

import net.cebularz.helpinghand.Constants;
import net.cebularz.helpinghand.common.menu.MercenaryMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus
{
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.MENU, Constants.MOD_ID);

    public static final Supplier<MenuType<MercenaryMenu>> MERCENARY_MENU = REGISTER.register("mercenary_menu",
            ()-> IMenuTypeExtension.create(MercenaryMenu::new));
 }
