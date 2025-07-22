package net.cebularz.helpinghand.datagen.custom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import net.cebularz.helpinghand.api.loaders.NamesJsonLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class NamesProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> completableFuture;
    private final Map<ResourceLocation, List<NamesJsonLoader.NameData>> nameMap = new HashMap<>();

    public NamesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "names");
        this.completableFuture = completableFuture;
    }

    public void addName(ResourceLocation id, NamesJsonLoader.NameData data) {
        this.nameMap.computeIfAbsent(id, k -> new ArrayList<>()).add(data);
    }

    public void addNames(ResourceLocation id, List<NamesJsonLoader.NameData> dataList) {
        this.nameMap.put(id, new ArrayList<>(dataList));
    }

    protected abstract void registerNames();

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        return this.completableFuture.thenCompose(provider -> {
            this.nameMap.clear();
            this.registerNames();
            return CompletableFuture.allOf(this.nameMap.entrySet().stream().map(entry -> {
                ResourceLocation location = entry.getKey();
                List<NamesJsonLoader.NameData> dataList = entry.getValue();
                Path path = this.pathProvider.json(location);

                JsonArray jsonArray = new JsonArray();
                dataList.forEach(data -> jsonArray.add(data.toJsonObject()));

                return DataProvider.saveStable(pOutput, jsonArray, path);
            }).toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName() {
        return "Names Provider";
    }
}