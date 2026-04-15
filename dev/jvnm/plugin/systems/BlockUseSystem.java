package dev.jvnm.plugin.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent.Post;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.model.Waystone;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.ConfigurationService;
import dev.jvnm.plugin.service.DiscoveryService;
import dev.jvnm.plugin.service.WaystoneService;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockUseSystem extends EntityEventSystem<EntityStore, Post> {
   private final WaystoneService waystoneService;
   private final DiscoveryService discoveryService;
   private final ConfigurationService configurationService;
   private final PermissionService permissionService;

   public BlockUseSystem(
      WaystoneService waystoneService, DiscoveryService discoveryService, ConfigurationService configurationService, PermissionService permissionService
   ) {
      super(Post.class);
      this.waystoneService = waystoneService;
      this.discoveryService = discoveryService;
      this.configurationService = configurationService;
      this.permissionService = permissionService;
   }

   public void handle(
      int i,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull Post useBlockEvent
   ) {
      Ref<EntityStore> ref = useBlockEvent.getContext().getEntity();
      Player player = (Player)store.getComponent(ref, Player.getComponentType());
      if (player != null) {
         World world = player.getWorld();
         if (world != null) {
            String blockKey = useBlockEvent.getBlockType().getId();
            if (blockKey.contains("Furniture_Ancient_Waystone")) {
               if (!this.permissionService.hasPermission(player.getUuid(), "hytale.command.waystone.use")) {
                  player.sendMessage(Message.raw("You do not have permission to use waystones."));
                  return;
               }

               Waystone waystone = this.waystoneService.getWaystoneAt(useBlockEvent.getTargetBlock());
               String playerUuid = player.getUuid().toString();
               if (waystone == null) {
                  String worldName = world != null ? world.getName() : "default";
                  waystone = this.waystoneService.getOrCreate(useBlockEvent.getTargetBlock(), null, true, worldName);
                  player.sendMessage(Message.raw("You discovered a new Waystone: " + waystone.getName()));
                  this.discoveryService.discoverWaystone(playerUuid, waystone.getName());
               } else {
                  boolean discoveryEnabled = this.configurationService.get("discovery_enabled", false);
                  if (discoveryEnabled && !this.discoveryService.hasDiscovered(playerUuid, waystone.getName()) && !playerUuid.equals(waystone.getOwnerUuid())) {
                     this.discoveryService.discoverWaystone(playerUuid, waystone.getName());
                     player.sendMessage(Message.raw("You have discovered: " + waystone.getName()));
                  }
               }

               boolean isOp = this.permissionService.isOp(player.getUuid());
               if (!this.waystoneService.isVisibleTo(waystone, playerUuid) && !isOp) {
                  player.sendMessage(Message.raw("You do not have permission to use this waystone."));
               } else {
                  this.waystoneService.openWaystoneListPage(store, player.getReference(), waystone.getName(), var0 -> {});
               }
            }
         }
      }
   }

   @Nullable
   public Query<EntityStore> getQuery() {
      return Query.any();
   }
}
