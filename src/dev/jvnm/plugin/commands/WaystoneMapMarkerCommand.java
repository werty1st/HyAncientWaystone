package dev.jvnm.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.ConfigurationService;
import javax.annotation.Nonnull;

public class WaystoneMapMarkerCommand extends AbstractPlayerCommand {
   private final ConfigurationService configurationService;
   private final PermissionService permissionService;
   private final OptionalArg<String> stateArg = this.withOptionalArg("state", "enable/disable", ArgTypes.STRING);

   public WaystoneMapMarkerCommand(ConfigurationService configurationService, PermissionService permissionService) {
      super("mapMarker", "Enable or disable map markers");
      this.setPermissionGroup(GameMode.Adventure);
      this.configurationService = configurationService;
      this.permissionService = permissionService;
   }

   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      if (!this.permissionService.isOp(playerRef.getUuid())) {
         playerRef.sendMessage(Message.raw("You do not have permission to change waystone visualization settings."));
      } else {
         String state = (String)this.stateArg.get(context);
         boolean current = this.configurationService.get("map_markers_enabled", false);
         boolean newState;
         if (state == null) {
            newState = !current;
         } else if (!state.equalsIgnoreCase("true") && !state.equalsIgnoreCase("enable") && !state.equalsIgnoreCase("on")) {
            if (!state.equalsIgnoreCase("false") && !state.equalsIgnoreCase("disable") && !state.equalsIgnoreCase("off")) {
               playerRef.sendMessage(Message.raw("Usage: /waystone mapMarker [enable/disable]"));
               return;
            }

            newState = false;
         } else {
            newState = true;
         }

         this.configurationService.set("map_markers_enabled", newState);
         playerRef.sendMessage(Message.raw("Waystone Map Makers are now: " + (newState ? "ENABLED" : "DISABLED")));
      }
   }
}
