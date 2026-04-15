package dev.jvnm.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.model.Network;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.NetworkService;
import javax.annotation.Nonnull;

public class WaystoneNetworkDeleteCommand extends AbstractPlayerCommand {
   private final NetworkService networkService;
   private final PermissionService permissionService;
   RequiredArg<String> nameArg = this.withRequiredArg("name", "Network name", ArgTypes.STRING);

   public WaystoneNetworkDeleteCommand(NetworkService networkService, PermissionService permissionService) {
      super("delete", "Delete a network");
      this.networkService = networkService;
      this.permissionService = permissionService;
   }

   protected void execute(
      @Nonnull CommandContext commandContext,
      @Nonnull Store<EntityStore> store,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nonnull World world
   ) {
      String name = (String)this.nameArg.get(commandContext);
      if (name != null && !name.isEmpty()) {
         Network network = this.networkService.getNetwork(name.toLowerCase());
         if (network == null) {
            playerRef.sendMessage(Message.raw("Network not found: " + name));
         } else {
            boolean isOwner = playerRef.getUuid().toString().equals(network.getOwnerUuid());
            boolean hasManagePerm = this.permissionService.hasPermission(playerRef.getUuid(), "hytale.command.waystone.network.manage");
            if (!isOwner && !hasManagePerm) {
               playerRef.sendMessage(Message.raw("You don't have permission to delete this network."));
            } else {
               if (this.networkService.deleteNetwork(name.toLowerCase())) {
                  playerRef.sendMessage(Message.raw("Network '" + name + "' deleted."));
               } else {
                  playerRef.sendMessage(Message.raw("Cannot delete this network (global and personal networks cannot be deleted)."));
               }
            }
         }
      } else {
         playerRef.sendMessage(Message.raw("Usage: /waystone network delete <name>"));
      }
   }
}
