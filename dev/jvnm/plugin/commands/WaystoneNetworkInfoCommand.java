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

public class WaystoneNetworkInfoCommand extends AbstractPlayerCommand {
   private final NetworkService networkService;
   RequiredArg<String> nameArg = this.withRequiredArg("name", "Network name", ArgTypes.STRING);

   public WaystoneNetworkInfoCommand(NetworkService networkService, PermissionService permissionService) {
      super("info", "Show network info");
      this.networkService = networkService;
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
            playerRef.sendMessage(Message.raw("--- Network: " + network.getDisplayName() + " ---"));
            playerRef.sendMessage(Message.raw("ID: " + network.getId()));
            String discoveryStatus = network.getDiscoveryEnabled() == null
               ? "Inherit (Default)"
               : (network.getDiscoveryEnabled() ? "Force Enabled" : "Force Disabled");
            playerRef.sendMessage(Message.raw("Discovery: " + discoveryStatus));
            playerRef.sendMessage(Message.raw("Cooldown: " + (network.getCooldown() > 0 ? network.getCooldown() + "s" : "uses global")));
            playerRef.sendMessage(Message.raw("See Global: " + (network.canSeeGlobal() ? "yes" : "no")));
            playerRef.sendMessage(Message.raw("Secret: " + (network.isSecret() ? "Yes" : "No")));
            playerRef.sendMessage(Message.raw("Members: " + network.getMembers().size()));
            playerRef.sendMessage(Message.raw("Owner: " + (network.getOwnerUuid() != null ? network.getOwnerUuid() : "Server")));
            if (network.getCosts().isEmpty()) {
               playerRef.sendMessage(Message.raw("Costs: uses global"));
            } else {
               playerRef.sendMessage(Message.raw("Costs: " + network.getCosts().size() + " item(s)"));
            }
         }
      } else {
         playerRef.sendMessage(Message.raw("Usage: /waystone network info <name>"));
      }
   }
}
