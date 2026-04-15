package dev.jvnm.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
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

public class WaystonePriceAddCommand extends AbstractPlayerCommand {
   private final ConfigurationService configurationService;
   private final PermissionService permissionService;
   RequiredArg<Integer> quantityArg = this.withRequiredArg("qty", "Quantity", ArgTypes.INTEGER);

   public WaystonePriceAddCommand(ConfigurationService configurationService, PermissionService permissionService) {
      super("add", "Add held item to waystone price. Usage: /waystone price add <qty>");
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
         playerRef.sendMessage(Message.raw("Cannot modify item prices while currency pricing is active. Use /waystone currency remove first."));
      } else {
         Integer qty = (Integer)this.quantityArg.get(commandContext);
         if (qty == null) {
            playerRef.sendMessage(Message.raw("Usage: /waystone price add <quantity>"));
         } else if (qty <= 0) {
            playerRef.sendMessage(Message.raw("Quantity must be positive."));
         } else {
            Player player = (Player)store.getComponent(ref, Player.getComponentType());
            if (player == null) {
               playerRef.sendMessage(Message.raw("Could not access player inventory."));
            } else {
               ItemStack heldItem = player.getInventory().getHotbar().getItemStack(player.getInventory().getActiveHotbarSlot());
               if (heldItem != null && !heldItem.getItemId().isEmpty()) {
                  String itemId = heldItem.getItemId();
                  List<PriceItem> currentPrices = new ArrayList<>(this.configurationService.getPriceItems());
                  currentPrices.removeIf(p -> p.getItemId().equals(itemId));
                  if (currentPrices.size() >= 4) {
                     playerRef.sendMessage(Message.raw("Limit reached: You can only have a maximum of 4 different price items."));
                  } else {
                     currentPrices.add(new PriceItem(itemId, qty));
                     this.configurationService.setPriceItems(currentPrices);
                     playerRef.sendMessage(Message.raw("Added/Updated price: " + qty + "x " + itemId));
                     StringBuilder message = new StringBuilder("Current Waystone Prices: ");

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
                  playerRef.sendMessage(Message.raw("You must hold an item to add it to the price."));
               }
            }
         }
      }
   }
}
