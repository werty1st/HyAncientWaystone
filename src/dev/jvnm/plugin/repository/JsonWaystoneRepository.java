package dev.jvnm.plugin.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.math.vector.Vector3i;
import dev.jvnm.plugin.model.Waystone;
import dev.jvnm.plugin.utils.FileUtil;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonWaystoneRepository implements WaystoneRepository {
   private static final Logger logger = Logger.getLogger(JsonWaystoneRepository.class.getName());
   private static final String DIR_WAYSTONES = "waystones";
   private static final String FILE_WAYSTONES_JSON = "waystones.json";
   private static final Gson gson = new GsonBuilder()
      .setPrettyPrinting()
      .registerTypeAdapter(Waystone.class, new JsonWaystoneRepository.WaystoneDeserializer())
      .create();
   private final Path dataDirectory;
   private final Map<Vector3i, Waystone> waystonesByPosition = new ConcurrentHashMap<>();

   public JsonWaystoneRepository(Path dataDirectory) {
      this.dataDirectory = dataDirectory;
      this.load();
   }

   @Override
   public void load() {
      Path waystoneFile = this.dataDirectory.resolve("waystones").resolve("waystones.json");
      if (Files.exists(waystoneFile)) {
         try (Reader reader = Files.newBufferedReader(waystoneFile)) {
            this.waystonesByPosition.clear();
            Type listType = (new TypeToken<ArrayList<Waystone>>() {
               {
                  Objects.requireNonNull(JsonWaystoneRepository.this);
               }
            }).getType();
            List<Waystone> loadedData = (List<Waystone>)gson.fromJson(reader, listType);
            if (loadedData != null) {
               for (Waystone w : loadedData) {
                  this.waystonesByPosition.put(w.getPosition(), w);
               }
            }
         } catch (IOException var9) {
            logger.log(Level.SEVERE, "Error loading waystones", (Throwable)var9);
         }
      }
   }

   @Override
   public void save() {
      Path waystoneDirectory = this.dataDirectory.resolve("waystones");
      Path waystoneFile = waystoneDirectory.resolve("waystones.json");
      List<Waystone> snapshot = new ArrayList<>(this.waystonesByPosition.values());
      String json = gson.toJson(snapshot);
      FileUtil.writeAsync(waystoneFile, json);
   }

   @Override
   public List<Waystone> findAll() {
      return new ArrayList<>(this.waystonesByPosition.values());
   }

   @Override
   public Optional<Waystone> findByPosition(Vector3i position) {
      return Optional.ofNullable(this.waystonesByPosition.get(position));
   }

   @Override
   public Optional<Waystone> findByName(String name) {
      return this.waystonesByPosition.values().stream().filter(w -> w.getName().equalsIgnoreCase(name)).findFirst();
   }

   @Override
   public void add(Waystone waystone) {
      this.waystonesByPosition.put(waystone.getPosition(), waystone);
      this.save();
   }

   @Override
   public void remove(Waystone waystone) {
      this.waystonesByPosition.remove(waystone.getPosition());
      this.save();
   }

   @Override
   public void update(Waystone waystone) {
      this.waystonesByPosition.put(waystone.getPosition(), waystone);
      this.save();
   }

   @Override
   public List<Waystone> findByOwner(String ownerUuid) {
      return this.waystonesByPosition.values().stream().filter(w -> ownerUuid.equals(w.getOwnerUuid())).toList();
   }

   @Override
   public List<Waystone> findByNetwork(String networkId) {
      return this.waystonesByPosition.values().stream().filter(w -> networkId.equals(w.getNetworkId())).toList();
   }

   private static class WaystoneDeserializer implements JsonDeserializer<Waystone> {
      public Waystone deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
         JsonObject obj = json.getAsJsonObject();
         Vector3i position = (Vector3i)context.deserialize(obj.get("position"), Vector3i.class);
         String name = obj.has("name") ? obj.get("name").getAsString() : "";
         String ownerUuid = obj.has("ownerUuid") && !obj.get("ownerUuid").isJsonNull() ? obj.get("ownerUuid").getAsString() : null;
         boolean isPublic = !obj.has("isPublic") || obj.get("isPublic").getAsBoolean();
         String worldName = obj.has("worldName") ? obj.get("worldName").getAsString() : "default";
         String networkId = obj.has("networkId") ? obj.get("networkId").getAsString() : "global";
         float facingYaw = obj.has("facingYaw") ? obj.get("facingYaw").getAsFloat() : 0.0F;
         Vector3i customTeleportPos = obj.has("customTeleportPos") && !obj.get("customTeleportPos").isJsonNull()
            ? (Vector3i)context.deserialize(obj.get("customTeleportPos"), Vector3i.class)
            : null;
         return new Waystone(position, name, ownerUuid, isPublic, worldName, networkId, facingYaw, customTeleportPos);
      }
   }
}
