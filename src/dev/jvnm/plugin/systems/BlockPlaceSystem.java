package dev.jvnm.plugin.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.model.Waystone;
import dev.jvnm.plugin.pages.WaystoneSettingsPage;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.ConfigurationService;
import dev.jvnm.plugin.service.NetworkService;
import dev.jvnm.plugin.service.WaystoneService;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class BlockPlaceSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
   private final WaystoneService waystoneService;
   private final ConfigurationService configurationService;
   private final PermissionService permissionService;
   private final NetworkService networkService;

   public BlockPlaceSystem(
      WaystoneService waystoneService, ConfigurationService configurationService, PermissionService permissionService, NetworkService networkService
   ) {
      super(PlaceBlockEvent.class);
      this.waystoneService = waystoneService;
      this.configurationService = configurationService;
      this.permissionService = permissionService;
      this.networkService = networkService;
   }

   public void handle(
      int index,
      ArchetypeChunk<EntityStore> chunk,
      @NonNullDecl Store<EntityStore> store,
      @NonNullDecl CommandBuffer<EntityStore> buffer,
      @NonNullDecl PlaceBlockEvent event
   ) {
      Player player = (Player)chunk.getComponent(index, Player.getComponentType());
      PlayerRef playerRef = (PlayerRef)chunk.getComponent(index, PlayerRef.getComponentType());
      if (player != null && playerRef != null) {
         assert event.getItemInHand() != null;

         String blockKey = event.getItemInHand().getBlockKey();
         if (blockKey != null && blockKey.contains("Furniture_Ancient_Waystone")) {
            if (!this.permissionService.hasPermission(playerRef.getUuid(), "hytale.command.waystone.place")) {
               playerRef.sendMessage(Message.raw("You do not have permission to place waystones."));
               event.setCancelled(true);
               return;
            }

            String playerUuid = playerRef.getUuid().toString();
            boolean isOp = this.permissionService.isOp(playerRef.getUuid());
            int playerLimit = this.configurationService.getInt("playerlimit");
            if (playerLimit >= 0 && !isOp) {
               int playerWaystoneCount = this.waystoneService.getWaystonesByOwner(playerUuid).size();
               if (playerWaystoneCount >= playerLimit) {
                  playerRef.sendMessage(Message.raw("You have reached your personal limit of " + playerLimit + " waystone(s)."));
                  event.setCancelled(true);
                  return;
               }
            }

            int waystoneCount = this.waystoneService.getAllWaystones("").size();
            int limit = this.configurationService.getInt("limit");
            if (limit >= 0 && waystoneCount >= limit && !isOp) {
               playerRef.sendMessage(
                  Message.raw("The maximum number of waystones (" + limit + ") has been reached. Remove an existing waystone to place a new one.")
               );
               event.setCancelled(true);
               return;
            }

            String worldName = player.getWorld() != null ? player.getWorld().getName() : "default";
            Waystone waystone = this.waystoneService.getOrCreate(event.getTargetBlock(), playerUuid, true, worldName);
            Ref<EntityStore> entityPlayerRef = player.getReference();

            assert entityPlayerRef != null;

            player.getPageManager()
               .openCustomPage(
                  entityPlayerRef,
                  store,
                  new WaystoneSettingsPage(playerRef, waystone, this.waystoneService, this.networkService, this.configurationService, isOp)
               );
         }
      }
   }

   public Query<EntityStore> getQuery() {
      return Query.any();
   }
}
