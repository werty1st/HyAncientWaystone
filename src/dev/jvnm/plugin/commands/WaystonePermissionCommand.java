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
import javax.annotation.Nonnull;

public class WaystonePermissionCommand extends AbstractPlayerCommand {
   private final PermissionService permissionService;

   public WaystonePermissionCommand(PermissionService permissionService) {
      super("permission", "Toggle the waystone permission system on/off");
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
         playerRef.sendMessage(Message.raw("You must be an OP to toggle the permission system."));
      } else {
         boolean currentState = this.permissionService.isEnabled();
         boolean newState = !currentState;
         this.permissionService.setEnabled(newState);
         if (newState) {
            playerRef.sendMessage(Message.raw("Permission system ENABLED. Players now need permissions to use waystones."));
            playerRef.sendMessage(Message.raw("Use /perm to manage waystone permissions."));
         } else {
            playerRef.sendMessage(Message.raw("Permission system DISABLED. Everyone can use waystones freely."));
         }
      }
   }
}
