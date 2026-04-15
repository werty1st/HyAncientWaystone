package dev.jvnm.plugin.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.ConfigurationService;

public class WaystonePriceCommand extends AbstractCommandCollection {
   public WaystonePriceCommand(ConfigurationService configurationService, PermissionService permissionService) {
      super("price", "Manage waystone prices");
      this.addSubCommand(new WaystonePriceAddCommand(configurationService, permissionService));
      this.addSubCommand(new WaystonePriceDeleteCommand(configurationService, permissionService));
      this.addSubCommand(new WaystonePriceClearCommand(configurationService, permissionService));
      this.addSubCommand(new WaystonePriceListCommand(configurationService, permissionService));
   }
}
