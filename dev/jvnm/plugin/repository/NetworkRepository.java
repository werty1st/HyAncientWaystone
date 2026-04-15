package dev.jvnm.plugin.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import dev.jvnm.plugin.model.Network;
import dev.jvnm.plugin.model.PriceItem;
import dev.jvnm.plugin.utils.FileUtil;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkRepository {
   private static final String NETWORKS_FILE = "networks.json";
   private final Path storagePath;
   private final Gson gson;
   private final Map<String, Network> networks;

   public NetworkRepository(Path dataFolder) {
      this.storagePath = dataFolder.resolve("networks.json");
      this.gson = new GsonBuilder().setPrettyPrinting().create();
      this.networks = new ConcurrentHashMap<>();
      this.load();
   }

   public Network getNetwork(String id) {
      return this.networks.get(id);
   }

   public List<Network> getAllNetworks() {
      return new ArrayList<>(this.networks.values());
   }

   public List<Network> getCustomNetworks() {
      List<Network> result = new ArrayList<>();

      for (Network network : this.networks.values()) {
         if (!network.isGlobal() && !network.isPersonal()) {
            result.add(network);
         }
      }

      return result;
   }

   public Network getPersonalNetwork(String playerUuid) {
      return this.networks.get("personal:" + playerUuid);
   }

   public void saveNetwork(Network network) {
      this.networks.put(network.getId(), network);
      this.save();
   }

   public boolean deleteNetwork(String id) {
      if ("global".equals(id)) {
         return false;
      } else {
         Network removed = this.networks.remove(id);
         if (removed != null) {
            this.save();
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean exists(String id) {
      return this.networks.containsKey(id);
   }

   public Network getOrCreateGlobal() {
      Network global = this.networks.get("global");
      if (global == null) {
         global = Network.createGlobal();
         this.saveNetwork(global);
      }

      return global;
   }

   public Network getOrCreatePersonal(String playerUuid, String playerName) {
      String id = "personal:" + playerUuid;
      Network personal = this.networks.get(id);
      if (personal == null) {
         personal = Network.createPersonal(playerUuid, playerName);
         this.saveNetwork(personal);
      }

      return personal;
   }

   private void load() {
      if (!Files.exists(this.storagePath)) {
         this.networks.put("global", Network.createGlobal());
         this.save();
      } else {
         try (Reader reader = Files.newBufferedReader(this.storagePath)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject networksObj = root.getAsJsonObject("networks");
            if (networksObj != null) {
               for (Entry<String, JsonElement> entry : networksObj.entrySet()) {
                  Network network = this.parseNetwork(entry.getKey(), entry.getValue().getAsJsonObject());
                  this.networks.put(network.getId(), network);
               }
            }

            if (!this.networks.containsKey("global")) {
               this.networks.put("global", Network.createGlobal());
            } else {
               Network global = this.networks.get("global");
               if (global.getDiscoveryEnabled() != null && !global.getDiscoveryEnabled()) {
                  global.setDiscoveryEnabled(null);
               }
            }
         } catch (Exception var9) {
            System.err.println("Failed to load networks: " + var9.getMessage());
            if (!this.networks.containsKey("global")) {
               this.networks.put("global", Network.createGlobal());
            }
         }
      }
   }

   private Network parseNetwork(String id, JsonObject json) {
      String displayName = json.has("displayName") ? json.get("displayName").getAsString() : id;
      String ownerUuid = json.has("ownerUuid") && !json.get("ownerUuid").isJsonNull() ? json.get("ownerUuid").getAsString() : null;
      Boolean discoveryEnabled = null;
      if (json.has("discovery") && !json.get("discovery").isJsonNull()) {
         discoveryEnabled = json.get("discovery").getAsBoolean();
      }

      int cooldown = json.has("cooldown") ? json.get("cooldown").getAsInt() : 0;
      boolean seeGlobal = !json.has("seeGlobal") || json.get("seeGlobal").getAsBoolean();
      List<PriceItem> costs = new ArrayList<>();
      if (json.has("costs") && json.get("costs").isJsonArray()) {
         Type listType = (new TypeToken<List<PriceItem>>() {
            {
               Objects.requireNonNull(NetworkRepository.this);
            }
         }).getType();
         costs = (List<PriceItem>)this.gson.fromJson(json.get("costs"), listType);
      }

      Boolean isSecret = !json.has("isSecret") || json.get("isSecret").getAsBoolean();
      List<String> members = new ArrayList<>();
      if (json.has("members") && json.get("members").isJsonArray()) {
         Type listType = (new TypeToken<List<String>>() {
            {
               Objects.requireNonNull(NetworkRepository.this);
            }
         }).getType();
         members = (List<String>)this.gson.fromJson(json.get("members"), listType);
      }

      return new Network(id, displayName, ownerUuid, discoveryEnabled, cooldown, seeGlobal, costs, isSecret, members);
   }

   private void save() {
      JsonObject root = new JsonObject();
      JsonObject networksObj = new JsonObject();

      for (Network network : this.networks.values()) {
         JsonObject json = new JsonObject();
         json.addProperty("displayName", network.getDisplayName());
         if (network.getOwnerUuid() != null) {
            json.addProperty("ownerUuid", network.getOwnerUuid());
         }

         if (network.getDiscoveryEnabled() != null) {
            json.addProperty("discovery", network.getDiscoveryEnabled());
         }

         json.addProperty("cooldown", network.getCooldown());
         json.addProperty("seeGlobal", network.canSeeGlobal());
         json.add("costs", this.gson.toJsonTree(network.getCosts()));
         json.addProperty("isSecret", network.isSecret());
         json.add("members", this.gson.toJsonTree(network.getMembers()));
         networksObj.add(network.getId(), json);
      }

      root.add("networks", networksObj);
      String jsonString = this.gson.toJson(root);
      FileUtil.writeAsync(this.storagePath, jsonString);
   }
}
