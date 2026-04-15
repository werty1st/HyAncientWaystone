package dev.jvnm.plugin;

import com.hypixel.hytale.logger.HytaleLogger.Api;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import dev.jvnm.plugin.commands.WaystoneCommand;
import dev.jvnm.plugin.interactions.ConsumeWarpberryInteraction;
import dev.jvnm.plugin.map.WaystoneMarkerProvider;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.repository.JsonCooldownRepository;
import dev.jvnm.plugin.repository.JsonDiscoveryRepository;
import dev.jvnm.plugin.repository.JsonFriendRepository;
import dev.jvnm.plugin.repository.JsonWaystoneRepository;
import dev.jvnm.plugin.repository.NetworkRepository;
import dev.jvnm.plugin.service.ConfigurationService;
import dev.jvnm.plugin.service.DiscoveryService;
import dev.jvnm.plugin.service.EconomyService;
import dev.jvnm.plugin.service.FriendService;
import dev.jvnm.plugin.service.NetworkService;
import dev.jvnm.plugin.service.WaystoneService;
import dev.jvnm.plugin.systems.BlockBreakSystem;
import dev.jvnm.plugin.systems.BlockPlaceSystem;
import dev.jvnm.plugin.systems.BlockUseSystem;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class AncientWaystone extends JavaPlugin {
   private static AncientWaystone instance;
   private WaystoneService waystoneService;
   private PermissionService permissionService;
   private NetworkService networkService;
   private EconomyService economyService;

   public static AncientWaystone getInstance() {
      return instance;
   }

   public static WaystoneService getWaystoneService() {
      return instance.waystoneService;
   }

   public static PermissionService getPermissionService() {
      return instance.permissionService;
   }

   public static NetworkService getNetworkService() {
      return instance.networkService;
   }

   public static EconomyService getEconomyService() {
      return instance.economyService;
   }

   public AncientWaystone(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
      ((Api)this.getLogger().atInfo()).log("AncientWaystone has been loaded!");
   }

   public void setup() {
      try {
         Path dataDirectory = this.getDataDirectory();
         ConfigurationService configurationService = new ConfigurationService(dataDirectory);
         JsonWaystoneRepository waystoneRepository = new JsonWaystoneRepository(dataDirectory);
         JsonCooldownRepository cooldownRepository = new JsonCooldownRepository(dataDirectory);
         JsonFriendRepository friendRepository = new JsonFriendRepository(dataDirectory);
         JsonDiscoveryRepository discoveryRepository = new JsonDiscoveryRepository(dataDirectory);
         waystoneRepository.load();
         cooldownRepository.load();
         friendRepository.load();
         discoveryRepository.load();
         FriendService friendService = new FriendService(friendRepository);
         DiscoveryService discoveryService = new DiscoveryService(discoveryRepository, configurationService);
         PermissionService permissionService = new PermissionService(configurationService);
         this.permissionService = permissionService;
         NetworkRepository networkRepository = new NetworkRepository(dataDirectory);
         NetworkService networkService = new NetworkService(networkRepository, configurationService);
         this.networkService = networkService;
         EconomyService economyService = new EconomyService();
         this.economyService = economyService;
         WaystoneService waystoneService = new WaystoneService(
            waystoneRepository, cooldownRepository, configurationService, friendService, discoveryService, economyService
         );
         waystoneService.setNetworkService(networkService);
         this.waystoneService = waystoneService;
         this.getEntityStoreRegistry().registerSystem(new BlockPlaceSystem(waystoneService, configurationService, permissionService, networkService));
         this.getEntityStoreRegistry().registerSystem(new BlockBreakSystem(waystoneService, permissionService));
         this.getEntityStoreRegistry().registerSystem(new BlockUseSystem(waystoneService, discoveryService, configurationService, permissionService));
         ((Api)this.getLogger().atInfo()).log("All ECS systems registered successfully!");
         this.getCodecRegistry(Interaction.CODEC).register("consume_warpberry", ConsumeWarpberryInteraction.class, ConsumeWarpberryInteraction.CODEC);
         ((Api)this.getLogger().atInfo()).log("Custom interaction registered successfully!");
         this.getCommandRegistry()
            .registerCommand(
               new WaystoneCommand(configurationService, friendService, waystoneService, permissionService, networkService, discoveryService, economyService)
            );
         this.getEventRegistry()
            .registerGlobal(AddWorldEvent.class, event -> event.getWorld().getWorldMapManager().addMarkerProvider("waystones", WaystoneMarkerProvider.INSTANCE));
         ((Api)this.getLogger().atInfo()).log("Waystone map markers registered successfully!");
      } catch (Exception var14) {
         ((Api)((Api)this.getLogger().atSevere()).withCause(var14)).log("ERROR during AncientWaystone setup!");
      }
   }
}
