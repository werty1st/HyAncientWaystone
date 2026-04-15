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

public class WaystonePlayerLimitCommand extends AbstractPlayerCommand {
   private final ConfigurationService configurationService;
   private final PermissionService permissionService;
   RequiredArg<Integer> amountArg = this.withRequiredArg("amount", "The maximum number of waystones per player (-1 for unlimited)", ArgTypes.INTEGER);

   public WaystonePlayerLimitCommand(ConfigurationService configurationService, PermissionService permissionService) {
      super("playerlimit", "Set maximum amount of waystones each player can place");
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
      if (!this.permissionService.hasPermission(playerRef.getUuid(), "hytale.command.waystone.cmd.playerlimit")) {
         playerRef.sendMessage(Message.raw("You do not have permission to use this command."));
      } else {
         Integer amount = (Integer)this.amountArg.get(commandContext);
         if (amount < -1) {
            playerRef.sendMessage(Message.raw("Amount must be -1 (unlimited) or a non-negative number"));
         } else {
            this.configurationService.set("playerlimit", amount);
            this.configurationService.save();
            if (amount < 0) {
               playerRef.sendMessage(Message.raw("Player waystone limit disabled (unlimited)"));
            } else {
               playerRef.sendMessage(Message.raw("Player waystone limit set to " + amount));
            }
         }
      }
   }
}
