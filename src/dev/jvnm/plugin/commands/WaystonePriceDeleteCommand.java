package dev.jvnm.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.model.PriceItem;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.ConfigurationService;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class WaystonePriceDeleteCommand extends AbstractPlayerCommand {
   private final ConfigurationService configurationService;
   private final PermissionService permissionService;

   public WaystonePriceDeleteCommand(ConfigurationService configurationService, PermissionService permissionService) {
      super("delete", "Remove held item from waystone price.");
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
      } else if (this.configurationService.hasCurrencyPrice()) {
         playerRef.sendMessage(Message.raw("Cannot delete item prices while currency pricing is active. Use /waystone currency remove first."));
      } else {
         Player player = (Player)store.getComponent(ref, Player.getComponentType());
         if (player == null) {
            playerRef.sendMessage(Message.raw("Could not access player inventory."));
         } else {
            ItemStack heldItem = player.getInventory().getHotbar().getItemStack(player.getInventory().getActiveHotbarSlot());
            if (heldItem != null && !heldItem.getItemId().isEmpty() && !heldItem.getItemId().equals("hytale:items/air")) {
               String itemId = heldItem.getItemId();
               List<PriceItem> currentPrices = new ArrayList<>(this.configurationService.getPriceItems());
               boolean removed = currentPrices.removeIf(p -> p.getItemId().equals(itemId));
               if (removed) {
                  this.configurationService.setPriceItems(currentPrices);
                  playerRef.sendMessage(Message.raw("Removed " + itemId + " from price list."));
                  if (currentPrices.isEmpty()) {
                     playerRef.sendMessage(Message.raw("Price list is now empty (Free teleportation)."));
                  } else {
                     StringBuilder message = new StringBuilder("Remaining Waystone Prices: ");

                     for (int i = 0; i < currentPrices.size(); i++) {
                        PriceItem item = currentPrices.get(i);
                        message.append(item.getAmount()).append("x ").append(item.getItemId());
                        if (i < currentPrices.size() - 1) {
                           message.append(", ");
                        }
                     }

                     playerRef.sendMessage(Message.raw(message.toString()));
                  }
               } else {
                  playerRef.sendMessage(Message.raw("Item " + itemId + " is not in the price list."));
               }
            } else {
               playerRef.sendMessage(Message.raw("You must hold the item you want to remove from the price list."));
            }
         }
      }
   }
}
