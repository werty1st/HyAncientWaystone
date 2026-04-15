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

public class WaystoneCurrencyRemoveCommand extends AbstractPlayerCommand {
   private final ConfigurationService configurationService;
   private final PermissionService permissionService;

   public WaystoneCurrencyRemoveCommand(ConfigurationService configurationService, PermissionService permissionService) {
      super("remove", "Remove currency pricing (revert to item-based)");
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
      if (!this.permissionService.hasPermission(playerRef.getUuid(), "hytale.command.waystone.cmd.price")) {
         playerRef.sendMessage(Message.raw("You don't have permission to modify currency prices."));
      } else if (!this.configurationService.hasCurrencyPrice()) {
         playerRef.sendMessage(Message.raw("No currency price is currently set."));
      } else {
         this.configurationService.clearCurrencyPrice();
         playerRef.sendMessage(Message.raw("Currency pricing removed. Item-based pricing will be used if configured."));
      }
   }
}
