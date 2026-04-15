package dev.jvnm.plugin.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.jvnm.plugin.model.DiscoveryData;
import dev.jvnm.plugin.utils.FileUtil;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonDiscoveryRepository implements DiscoveryRepository {
   private static final Logger logger = Logger.getLogger(JsonDiscoveryRepository.class.getName());
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final Path filePath;
   private final Map<String, DiscoveryData> discoveryMap = new ConcurrentHashMap<>();

   public JsonDiscoveryRepository(Path dataDirectory) {
      this.filePath = dataDirectory.resolve("discoveries.json");
      this.load();
   }

   @Override
   public void load() {
      if (Files.exists(this.filePath)) {
         try (Reader reader = Files.newBufferedReader(this.filePath)) {
            Type type = (new TypeToken<Map<String, DiscoveryData>>() {
               {
                  Objects.requireNonNull(JsonDiscoveryRepository.this);
               }
            }).getType();
            Map<String, DiscoveryData> loaded = (Map<String, DiscoveryData>)GSON.fromJson(reader, type);
            if (loaded != null) {
               this.discoveryMap.clear();
               this.discoveryMap.putAll(loaded);
            }
         } catch (IOException var6) {
            logger.log(Level.SEVERE, "Failed to load discovery data", (Throwable)var6);
         }
      }
   }

   @Override
   public void save() {
      Map<String, DiscoveryData> snapshot = new HashMap<>(this.discoveryMap);
      String json = GSON.toJson(snapshot);
      FileUtil.writeAsync(this.filePath, json);
   }

   @Override
   public Optional<DiscoveryData> findByPlayer(String playerUuid) {
      return Optional.ofNullable(this.discoveryMap.get(playerUuid));
   }

   @Override
   public void saveDiscoveryData(DiscoveryData discoveryData) {
      this.discoveryMap.put(discoveryData.getPlayerUuid(), discoveryData);
      this.save();
   }
}
