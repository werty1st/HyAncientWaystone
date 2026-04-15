package dev.jvnm.plugin.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.HytaleLogger.Api;
import dev.jvnm.plugin.model.PlayerCooldown;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JsonCooldownRepository implements CooldownRepository {
   private static final HytaleLogger logger = HytaleLogger.forEnclosingClass();
   private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private static final String DIR_WAYSTONES = "waystones";
   private static final String FILE_COOLDOWNS_JSON = "cooldowns.json";
   private final Path dataDirectory;
   private final List<PlayerCooldown> cooldowns = new ArrayList<>();

   public JsonCooldownRepository(Path dataDirectory) {
      this.dataDirectory = dataDirectory;
   }

   @Override
   public void load() {
      Path cooldownFile = this.dataDirectory.resolve("waystones").resolve("cooldowns.json");
      if (Files.exists(cooldownFile)) {
         try (Reader reader = new FileReader(cooldownFile.toFile())) {
            this.cooldowns.clear();
            Type listType = (new TypeToken<ArrayList<PlayerCooldown>>() {
               {
                  Objects.requireNonNull(JsonCooldownRepository.this);
               }
            }).getType();
            List<PlayerCooldown> loadedData = (List<PlayerCooldown>)gson.fromJson(reader, listType);
            if (loadedData != null) {
               this.cooldowns.addAll(loadedData);
            }
         } catch (IOException var7) {
            ((Api)logger.atSevere()).log("Error while loading cooldowns", var7);
         }
      }
   }

   @Override
   public void save() {
      Path waystoneDirectory = this.dataDirectory.resolve("waystones");
      Path cooldownFile = waystoneDirectory.resolve("cooldowns.json");

      try {
         if (!Files.exists(waystoneDirectory)) {
            Files.createDirectories(waystoneDirectory);
         }

         try (Writer writer = new FileWriter(cooldownFile.toFile())) {
            gson.toJson(this.cooldowns, writer);
         }
      } catch (IOException var8) {
         ((Api)logger.atSevere()).log("Error saving cooldowns", var8);
      }
   }

   @Override
   public Optional<Long> getCooldown(String playerName) {
      return this.cooldowns.stream().filter(c -> c.getPlayerName().equals(playerName)).findFirst().map(PlayerCooldown::getLastTeleportTime);
   }

   @Override
   public void setCooldown(String playerName, long timestamp) {
      this.cooldowns.removeIf(c -> c.getPlayerName().equals(playerName));
      this.cooldowns.add(new PlayerCooldown(playerName, timestamp));
      this.save();
   }
}
