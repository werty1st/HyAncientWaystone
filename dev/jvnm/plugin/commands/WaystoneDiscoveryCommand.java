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

public class WaystoneDiscoveryCommand extends AbstractPlayerCommand {
   private final ConfigurationService configurationService;
   private final PermissionService permissionService;

   public WaystoneDiscoveryCommand(ConfigurationService configurationService, PermissionService permissionService) {
      super("discovery", "Toggle waystone discovery requirement");
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
      if (!this.permissionService.hasPermission(playerRef.getUuid(), "hytale.command.waystone.cmd.discovery")) {
         playerRef.sendMessage(Message.raw("You do not have permission to use this command."));
      } else {
         boolean currentValue = this.configurationService.get("discovery_enabled", false);
         boolean newValue = !currentValue;
         this.configurationService.set("discovery_enabled", newValue);
         this.configurationService.save();
         if (newValue) {
            playerRef.sendMessage(Message.raw("Waystone discovery system: enabled"));
            playerRef.sendMessage(Message.raw("Players must now discover public waystones before seeing them."));
         } else {
            playerRef.sendMessage(Message.raw("Waystone discovery system: disabled"));
            playerRef.sendMessage(Message.raw("All visible waystones are now shown without discovery."));
         }
      }
   }
}
