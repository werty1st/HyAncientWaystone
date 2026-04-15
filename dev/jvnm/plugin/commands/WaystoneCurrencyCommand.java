package dev.jvnm.plugin.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.ConfigurationService;
import dev.jvnm.plugin.service.EconomyService;

public class WaystoneCurrencyCommand extends AbstractCommandCollection {
   public WaystoneCurrencyCommand(ConfigurationService configurationService, PermissionService permissionService, EconomyService economyService) {
      super("currency", "Manage currency-based waystone pricing");
      this.addSubCommand(new WaystoneCurrencySetCommand(configurationService, permissionService, economyService));
      this.addSubCommand(new WaystoneCurrencyRemoveCommand(configurationService, permissionService));
      this.addSubCommand(new WaystoneCurrencyInfoCommand(configurationService, permissionService, economyService));
   }
}
