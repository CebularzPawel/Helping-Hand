package net.cebularz.helpinghand.api.loaders;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class NamesJsonLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private final List<NameData> loadedNames = new ArrayList<>();
    public static final NamesJsonLoader INSTANCE  = new NamesJsonLoader();
    public record NameData(int priority, String value)
    {
        public JsonObject toJsonObject()
        {
            JsonObject object = new JsonObject();
            object.addProperty("priority",priority);
            object.addProperty("value",value);
            return object;
        }

        public static class Builder
        {
            public int priority;
            public String name;

            public Builder(int priority, String name){
                this.priority = priority;
                this.name = name;
            }

            public void setPriority(int priority) {
                this.priority = priority;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }


    public NamesJsonLoader() {
        super(GSON, "names");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        loadedNames.clear();

        map.forEach((resourceLocation, jsonElement) -> {
            try {
                Type listType = new TypeToken<List<NameData>>(){}.getType();
                List<NameData> names = GSON.fromJson(jsonElement.toString(), listType);
                loadedNames.addAll(names);
            } catch (Exception e) {
                System.err.println("Error loading names from " + resourceLocation + ": " + e.getMessage());
            }
        });
    }

    public List<NameData> getAllNames() {
        return new ArrayList<>(loadedNames);
    }

    public List<String> getNamesByPriority(int priority) {
        return loadedNames.stream()
                .filter(nameData -> nameData.priority() == priority)
                .map(NameData::value)
                .toList();
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new NamesJsonLoader());
    }
}