package dev.jvnm.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.ConfigurationService;
import dev.jvnm.plugin.service.EconomyService;
import java.math.BigDecimal;
import javax.annotation.Nonnull;

public class WaystoneCurrencySetCommand extends AbstractPlayerCommand {
   private final ConfigurationService configurationService;
   private final PermissionService permissionService;
   private final EconomyService economyService;
   RequiredArg<Double> amountArg = this.withRequiredArg("amount", "Currency amount", ArgTypes.DOUBLE);
   OptionalArg<String> currencyArg = this.withOptionalArg("currency", "Currency type", ArgTypes.STRING);

   public WaystoneCurrencySetCommand(ConfigurationService configurationService, PermissionService permissionService, EconomyService economyService) {
      super("set", "Set the currency price for teleportation");
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
      if (!this.permissionService.hasPermission(playerRef.getUuid(), "hytale.command.waystone.cmd.price")) {
         playerRef.sendMessage(Message.raw("You don't have permission to set currency prices."));
      } else if (!this.economyService.isAvailable()) {
         playerRef.sendMessage(Message.raw("VaultUnlocked is not installed. Currency pricing requires VaultUnlocked."));
      } else {
         Double amountDouble = (Double)this.amountArg.get(commandContext);
         if (amountDouble != null && !(amountDouble <= 0.0)) {
            BigDecimal amount = BigDecimal.valueOf(amountDouble);
            String currencyType = (String)this.currencyArg.get(commandContext);
            this.configurationService.setCurrencyPrice(amount, currencyType);
            String formatted = this.economyService.format(amount, currencyType);
            if (currencyType != null && !currencyType.isEmpty()) {
               playerRef.sendMessage(Message.raw("Currency price set to " + formatted + " (" + currencyType + ")"));
            } else {
               playerRef.sendMessage(Message.raw("Currency price set to " + formatted));
            }
         } else {
            playerRef.sendMessage(Message.raw("Please specify a positive amount."));
         }
      }
   }
}
