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
import dev.jvnm.plugin.service.EconomyService;
import java.math.BigDecimal;
import javax.annotation.Nonnull;

public class WaystoneCurrencyInfoCommand extends AbstractPlayerCommand {
   private final ConfigurationService configurationService;
   private final PermissionService permissionService;
   private final EconomyService economyService;

   public WaystoneCurrencyInfoCommand(ConfigurationService configurationService, PermissionService permissionService, EconomyService economyService) {
      super("info", "Show current currency pricing");
      this.configurationService = configurationService;
      this.permissionService = permissionService;
      this.economyService = economyService;
   }

   protected void execute(
      @Nonnull CommandContext commandContext,
      @Nonnull Store<EntityStore> store,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nonnull World world
   ) {
      StringBuilder response = new StringBuilder();
      response.append("=== Currency Pricing Info ===\n");
      if (this.economyService.isAvailable()) {
         response.append("VaultUnlocked: Available\n");
      } else {
         response.append("VaultUnlocked: Not available\n");
         response.append("(Install VaultUnlocked to enable currency pricing)\n");
      }

      BigDecimal price = this.configurationService.getCurrencyPrice();
      String currencyType = this.configurationService.getCurrencyType();
      if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
         String formatted = this.economyService.format(price, currencyType);
         response.append("Current price: ").append(formatted);
         if (currencyType != null && !currencyType.isEmpty()) {
            response.append(" (").append(currencyType).append(")");
         }

         response.append("\n");
      } else {
         response.append("Currency price: Not set\n");
         response.append("(Using item-based pricing if configured)\n");
      }

      playerRef.sendMessage(Message.raw(response.toString()));
   }
}
