package dev.jvnm.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.model.PriceItem;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.ConfigurationService;
import java.util.List;
import javax.annotation.Nonnull;

public class WaystonePriceListCommand extends AbstractPlayerCommand {
   private final ConfigurationService configurationService;
   private final PermissionService permissionService;

   public WaystonePriceListCommand(ConfigurationService configurationService, PermissionService permissionService) {
      super("list", "List current waystone prices.");
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
         playerRef.sendMessage(Message.raw("You do not have permission to use this command."));
      } else {
         List<PriceItem> prices = this.configurationService.getPriceItems();
         if (prices.isEmpty()) {
            playerRef.sendMessage(Message.raw("Price list is empty (Free teleportation)."));
         } else {
            StringBuilder message = new StringBuilder("Current Waystone Prices: ");

            for (int i = 0; i < prices.size(); i++) {
               PriceItem item = prices.get(i);
               message.append(item.getAmount()).append("x ").append(item.getItemId());
               if (i < prices.size() - 1) {
                  message.append(", ");
               }
            }

            playerRef.sendMessage(Message.raw(message.toString()));
         }
      }
   }
}
