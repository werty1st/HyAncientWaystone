package dev.jvnm.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.model.Waystone;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.WaystoneService;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class WaystoneCleanupCommand extends AbstractPlayerCommand {
   private final WaystoneService waystoneService;
   private final PermissionService permissionService;

   public WaystoneCleanupCommand(WaystoneService waystoneService, PermissionService permissionService) {
      super("cleanup", "Remove waystones from non-existent worlds");
      this.waystoneService = waystoneService;
      this.permissionService = permissionService;
   }

   protected void execute(
      @Nonnull CommandContext commandContext,
      @Nonnull Store<EntityStore> store,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nonnull World world
   ) {
      if (!this.permissionService.hasPermission(playerRef.getUuid(), "hytale.command.waystone.cmd.cleanup")) {
         playerRef.sendMessage(Message.raw("You do not have permission to use this command."));
      } else {
         List<Waystone> allWaystones = this.waystoneService.getAllWaystones("");
         List<Waystone> toRemove = new ArrayList<>();

         for (Waystone waystone : allWaystones) {
            String worldName = waystone.getWorldName();
            if (worldName != null && !worldName.isEmpty()) {
               World targetWorld = Universe.get().getWorld(worldName);
               if (targetWorld == null) {
                  toRemove.add(waystone);
               }
            }
         }

         if (toRemove.isEmpty()) {
            playerRef.sendMessage(Message.raw("No waystones found in non-existent worlds."));
         } else {
            int removedCount = 0;

            for (Waystone waystonex : toRemove) {
               this.waystoneService.removeWaystone(waystonex.getPosition());
               removedCount++;
            }

            playerRef.sendMessage(Message.raw("Cleaned up " + removedCount + " waystone(s) from non-existent worlds."));
         }
      }
   }
}
