package dev.jvnm.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.NetworkService;
import javax.annotation.Nonnull;

public class WaystoneNetworkToggleCommand extends AbstractPlayerCommand {
   private final NetworkService networkService;
   private final PermissionService permissionService;

   public WaystoneNetworkToggleCommand(NetworkService networkService, PermissionService permissionService) {
      super("toggle", "Toggle networks on/off (OP only)");
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
      if (!this.permissionService.isOp(playerRef.getUuid())) {
         playerRef.sendMessage(Message.raw("You must be an OP to toggle networks."));
      } else {
         boolean currentState = this.networkService.isEnabled();
         boolean newState = !currentState;
         this.networkService.setEnabled(newState);
         if (newState) {
            playerRef.sendMessage(Message.raw("Networks ENABLED. Waystones are now grouped by network."));
         } else {
            playerRef.sendMessage(Message.raw("Networks DISABLED. All waystones share a global network."));
         }
      }
   }
}
