package net.cebularz.helpinghand.datagen;

import net.cebularz.helpinghand.api.loaders.NamesJsonLoader;
import net.cebularz.helpinghand.common.CommonClass;
import net.cebularz.helpinghand.datagen.custom.NamesProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModNamesProvider extends NamesProvider {
    public ModNamesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void registerNames() {
        addNames(CommonClass.path( "general_names"), List.of(
                new NamesJsonLoader.NameData(1, "Jhon"),
                new NamesJsonLoader.NameData(1, "Lenny")
        ));
    }
}