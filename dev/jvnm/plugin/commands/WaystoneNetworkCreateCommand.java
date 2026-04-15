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

public class WaystoneNetworkCreateCommand extends AbstractPlayerCommand {
   private final NetworkService networkService;
   private final PermissionService permissionService;
   RequiredArg<String> nameArg = this.withRequiredArg("name", "Network name", ArgTypes.STRING);

   public WaystoneNetworkCreateCommand(NetworkService networkService, PermissionService permissionService) {
      super("create", "Create a new network");
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
      if (!this.permissionService.hasPermission(playerRef.getUuid(), "hytale.command.waystone.network.create")) {
         playerRef.sendMessage(Message.raw("You don't have permission to create networks."));
      } else {
         String name = (String)this.nameArg.get(commandContext);
         if (name == null || name.isEmpty()) {
            playerRef.sendMessage(Message.raw("Usage: /waystone network create <name>"));
         } else if (!name.matches("^[a-zA-Z0-9_]+$")) {
            playerRef.sendMessage(Message.raw("Network name must contain only letters, numbers, and underscores."));
         } else if (!name.equalsIgnoreCase("global") && !name.startsWith("personal:")) {
            if (this.networkService.networkExists(name.toLowerCase())) {
               playerRef.sendMessage(Message.raw("A network with that name already exists."));
            } else {
               Network network = this.networkService.createNetwork(name.toLowerCase(), name, playerRef.getUuid().toString());
               if (network != null) {
                  playerRef.sendMessage(Message.raw("Network '" + name + "' created!"));
               } else {
                  playerRef.sendMessage(Message.raw("Failed to create network."));
               }
            }
         } else {
            playerRef.sendMessage(Message.raw("That network name is reserved."));
         }
      }
   }
}
