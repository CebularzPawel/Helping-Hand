package net.cebularz.helpinghand.common;

import net.cebularz.helpinghand.Constants;
import net.minecraft.resources.ResourceLocation;

public class CommonClass
{
    public static ResourceLocation path (String path)
    {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,path);
    }
}
