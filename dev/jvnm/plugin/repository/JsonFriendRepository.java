package dev.jvnm.plugin.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.jvnm.plugin.model.FriendData;
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

public class JsonFriendRepository implements FriendRepository {
   private static final Logger logger = Logger.getLogger(JsonFriendRepository.class.getName());
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final Path filePath;
   private final Map<String, FriendData> friendsMap = new ConcurrentHashMap<>();

   public JsonFriendRepository(Path dataDirectory) {
      this.filePath = dataDirectory.resolve("friends.json");
      this.load();
   }

   @Override
   public void load() {
      if (Files.exists(this.filePath)) {
         try (Reader reader = Files.newBufferedReader(this.filePath)) {
            Type type = (new TypeToken<Map<String, FriendData>>() {
               {
                  Objects.requireNonNull(JsonFriendRepository.this);
               }
            }).getType();
            Map<String, FriendData> loaded = (Map<String, FriendData>)GSON.fromJson(reader, type);
            if (loaded != null) {
               this.friendsMap.clear();
               this.friendsMap.putAll(loaded);
            }
         } catch (IOException var6) {
            logger.log(Level.SEVERE, "Failed to load friends data", (Throwable)var6);
         }
      }
   }

   @Override
   public void save() {
      Map<String, FriendData> snapshot = new HashMap<>(this.friendsMap);
      String json = GSON.toJson(snapshot);
      FileUtil.writeAsync(this.filePath, json);
   }

   @Override
   public Optional<FriendData> findByPlayer(String playerUuid) {
      return Optional.ofNullable(this.friendsMap.get(playerUuid));
   }

   @Override
   public void saveFriendData(FriendData friendData) {
      this.friendsMap.put(friendData.getPlayerUuid(), friendData);
      this.save();
   }
}
