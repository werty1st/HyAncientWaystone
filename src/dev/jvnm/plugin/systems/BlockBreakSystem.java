package dev.jvnm.plugin.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.model.Waystone;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.WaystoneService;
import javax.annotation.Nonnull;

@SuppressWarnings("removal")
public class BlockBreakSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {
   private final WaystoneService waystoneService;
   private final PermissionService permissionService;

   public BlockBreakSystem(WaystoneService waystoneService, PermissionService permissionService) {
      super(BreakBlockEvent.class);
      this.waystoneService = waystoneService;
      this.permissionService = permissionService;
   }

   public void handle(
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull BreakBlockEvent event
   ) {
      if (!event.isCancelled()) {
         Player player = (Player)archetypeChunk.getComponent(index, Player.getComponentType());
         PlayerRef playerRef = (PlayerRef)archetypeChunk.getComponent(index, PlayerRef.getComponentType());

         if (player != null) {
            String blockKey = event.getBlockType().getId();
            if (blockKey != null && blockKey.contains("Furniture_Ancient_Waystone")) {
               if (!this.permissionService.hasPermission(player.getUuid(), "hytale.command.waystone.break")) {
                  event.setCancelled(true);
                  playerRef.sendMessage(Message.raw("You do not have permission to break waystones."));
                  return;
               }

               Waystone waystone = this.waystoneService.getWaystoneAt(event.getTargetBlock());
               if (waystone != null) {
                  boolean isOwner = player.getUuid().toString().equals(waystone.getOwnerUuid());
                  boolean isOp = this.permissionService.isOp(player.getUuid());
                  if (!isOwner && !isOp) {
                     event.setCancelled(true);
                     playerRef.sendMessage(Message.raw("You do not have permission to break this waystone."));
                     return;
                  }
               }

               this.waystoneService.removeWaystone(event.getTargetBlock());
            }
         }
      }
   }

   @Nonnull
   public Query<EntityStore> getQuery() {
      return Query.any();
   }
}
