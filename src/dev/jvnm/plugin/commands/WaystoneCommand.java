package dev.jvnm.plugin.commands;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.ConfigurationService;
import dev.jvnm.plugin.service.DiscoveryService;
import dev.jvnm.plugin.service.EconomyService;
import dev.jvnm.plugin.service.FriendService;
import dev.jvnm.plugin.service.NetworkService;
import dev.jvnm.plugin.service.WaystoneService;

public class WaystoneCommand extends AbstractCommandCollection {
   public WaystoneCommand(
      ConfigurationService configurationService,
      FriendService friendService,
      WaystoneService waystoneService,
      PermissionService permissionService,
      NetworkService networkService,
      DiscoveryService discoveryService,
      EconomyService economyService
   ) {
      super("waystone", "Waystone setup commands");
      this.setPermissionGroup(GameMode.Adventure);
      this.addSubCommand(new WaystoneLimitCommand(configurationService, permissionService));
      this.addSubCommand(new WaystonePlayerLimitCommand(configurationService, permissionService));
      this.addSubCommand(new WaystonePriceCommand(configurationService, permissionService));
      this.addSubCommand(new WaystoneCurrencyCommand(configurationService, permissionService, economyService));
      this.addSubCommand(new WaystoneCooldownCommand(configurationService, permissionService));
      this.addSubCommand(new WaystoneFriendCommand(friendService, permissionService));
      this.addSubCommand(new WaystoneDiscoveryCommand(configurationService, permissionService));
      this.addSubCommand(new WaystoneCustomTeleportCommand(configurationService, permissionService));
      this.addSubCommand(new WaystoneCleanupCommand(waystoneService, permissionService));
      this.addSubCommand(new WaystonePermissionCommand(permissionService));
      this.addSubCommand(new WaystoneNetworkCommand(networkService, permissionService));
      this.addSubCommand(new WaystoneDebugCommand(networkService, waystoneService, permissionService, discoveryService, configurationService));
      this.addSubCommand(new WaystoneMapMarkerCommand(configurationService, permissionService));
      this.addSubCommand(new WaystoneWarpCommand(waystoneService, permissionService));
   }
}
