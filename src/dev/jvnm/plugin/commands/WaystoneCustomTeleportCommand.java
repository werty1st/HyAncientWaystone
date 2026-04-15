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
import dev.jvnm.plugin.service.ConfigurationService;
import javax.annotation.Nonnull;

public class WaystoneCustomTeleportCommand extends AbstractPlayerCommand {
   private final ConfigurationService configurationService;
   private final PermissionService permissionService;

   public WaystoneCustomTeleportCommand(ConfigurationService configurationService, PermissionService permissionService) {
      super("customtp", "Toggle custom teleport position UI");
      this.configurationService = configurationService;
      this.permissionService = permissionService;
   }

   protected void execute(
      @Nonnull CommandContext commandContext,
      @Nonnull Store<EntityStore> store,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nonnull World world
   ) {
      if (!this.permissionService.hasPermission(playerRef.getUuid(), "hytale.command.waystone.cmd.customtp")) {
         playerRef.sendMessage(Message.raw("You do not have permission to use this command."));
      } else {
         boolean currentValue = this.configurationService.get("custom_teleport_enabled", false);
         boolean newValue = !currentValue;
         this.configurationService.set("custom_teleport_enabled", newValue);
         this.configurationService.save();
         if (newValue) {
            playerRef.sendMessage(Message.raw("Custom teleport position UI: enabled"));
            playerRef.sendMessage(Message.raw("Players can now set custom X/Y/Z coordinates in waystone settings."));
         } else {
            playerRef.sendMessage(Message.raw("Custom teleport position UI: disabled"));
            playerRef.sendMessage(Message.raw("Custom position inputs are now hidden from waystone settings."));
         }
      }
   }
}
