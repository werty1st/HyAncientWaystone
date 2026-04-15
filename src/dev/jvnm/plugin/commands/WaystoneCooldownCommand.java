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
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.ConfigurationService;
import javax.annotation.Nonnull;

public class WaystoneCooldownCommand extends AbstractPlayerCommand {
   private final ConfigurationService configurationService;
   private final PermissionService permissionService;
   RequiredArg<Integer> secondsArg = this.withRequiredArg("seconds", "The cooldown duration in seconds", ArgTypes.INTEGER);

   public WaystoneCooldownCommand(ConfigurationService configurationService, PermissionService permissionService) {
      super("cooldown", "Set cooldown between teleportations in seconds");
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
      if (!this.permissionService.hasPermission(playerRef.getUuid(), "hytale.command.waystone.cmd.cooldown")) {
         playerRef.sendMessage(Message.raw("You do not have permission to use this command."));
      } else {
         Integer seconds = (Integer)this.secondsArg.get(commandContext);
         if (seconds < 0) {
            playerRef.sendMessage(Message.raw("Cooldown must be a non-negative integer"));
         } else {
            this.configurationService.set("cooldown", seconds);
            this.configurationService.save();
            playerRef.sendMessage(Message.raw("Waystone teleport cooldown set to " + seconds + " seconds"));
         }
      }
   }
}
