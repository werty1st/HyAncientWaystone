package dev.jvnm.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.model.Network;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.NetworkService;
import java.util.List;
import javax.annotation.Nonnull;

public class WaystoneNetworkListCommand extends AbstractPlayerCommand {
   private final NetworkService networkService;

   public WaystoneNetworkListCommand(NetworkService networkService, PermissionService permissionService) {
      super("list", "List all networks");
      this.networkService = networkService;
   }

   protected void execute(
      @Nonnull CommandContext commandContext,
      @Nonnull Store<EntityStore> store,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nonnull World world
   ) {
      List<Network> networks = this.networkService.getAllNetworks();
      playerRef.sendMessage(Message.raw("--- Waystone Networks ---"));

      for (Network network : networks) {
         String status = network.isGlobal() ? " [Global]" : (network.isPersonal() ? " [Personal]" : "");
         playerRef.sendMessage(Message.raw(" - " + network.getDisplayName() + status));
      }

      playerRef.sendMessage(Message.raw("Total: " + networks.size() + " networks"));
   }
}
