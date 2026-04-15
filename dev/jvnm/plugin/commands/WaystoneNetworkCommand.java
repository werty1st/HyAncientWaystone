package dev.jvnm.plugin.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.NetworkService;

public class WaystoneNetworkCommand extends AbstractCommandCollection {
   public WaystoneNetworkCommand(NetworkService networkService, PermissionService permissionService) {
      super("network", "Manage waystone networks");
      this.addSubCommand(new WaystoneNetworkToggleCommand(networkService, permissionService));
      this.addSubCommand(new WaystoneNetworkCreateCommand(networkService, permissionService));
      this.addSubCommand(new WaystoneNetworkDeleteCommand(networkService, permissionService));
      this.addSubCommand(new WaystoneNetworkListCommand(networkService, permissionService));
      this.addSubCommand(new WaystoneNetworkInfoCommand(networkService, permissionService));
      this.addSubCommand(new WaystoneNetworkSettingsCommand(networkService, permissionService));
      this.addSubCommand(new WaystoneNetworkMemberCommand(networkService, permissionService));
   }
}
