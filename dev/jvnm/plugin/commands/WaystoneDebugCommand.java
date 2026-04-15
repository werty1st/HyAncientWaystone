package dev.jvnm.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.model.Network;
import dev.jvnm.plugin.model.Waystone;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.ConfigurationService;
import dev.jvnm.plugin.service.DiscoveryService;
import dev.jvnm.plugin.service.NetworkService;
import dev.jvnm.plugin.service.WaystoneService;
import javax.annotation.Nonnull;

public class WaystoneDebugCommand extends AbstractPlayerCommand {
   private final NetworkService networkService;
   private final WaystoneService waystoneService;
   private final PermissionService permissionService;
   private final DiscoveryService discoveryService;
   private final ConfigurationService configurationService;

   public WaystoneDebugCommand(
      NetworkService networkService,
      WaystoneService waystoneService,
      PermissionService permissionService,
      DiscoveryService discoveryService,
      ConfigurationService configurationService
   ) {
      super("debug", "Debug permission and visibility info");
      this.setPermissionGroup(GameMode.Adventure);
      this.networkService = networkService;
      this.waystoneService = waystoneService;
      this.permissionService = permissionService;
      this.discoveryService = discoveryService;
      this.configurationService = configurationService;
   }

   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      String uuid = playerRef.getUuid().toString();
      boolean isOp = this.permissionService.isOp(playerRef.getUuid());
      boolean networksEnabled = this.networkService.isEnabled();
      boolean globalDiscovery = this.configurationService.get("discovery_enabled", false);
      playerRef.sendMessage(Message.raw("--- Debug Info ---"));
      playerRef.sendMessage(Message.raw("UUID: " + uuid));
      playerRef.sendMessage(Message.raw("OP: " + isOp));
      playerRef.sendMessage(Message.raw("Networks Enabled: " + networksEnabled));
      playerRef.sendMessage(Message.raw("Global Discovery: " + globalDiscovery));
      Network testNet = this.networkService.getNetwork("test");
      if (testNet != null) {
         playerRef.sendMessage(Message.raw("- Network 'test': Found"));
         playerRef.sendMessage(Message.raw("  Is Secret: " + testNet.isSecret()));
         playerRef.sendMessage(Message.raw("  Owner: " + testNet.getOwnerUuid()));
         playerRef.sendMessage(Message.raw("  Is Owner: " + uuid.equals(testNet.getOwnerUuid())));
         playerRef.sendMessage(Message.raw("  Is Member: " + testNet.isMember(uuid)));
         boolean visible = isOp || uuid.equals(testNet.getOwnerUuid()) || testNet.isMember(uuid) || !testNet.isSecret();
         playerRef.sendMessage(Message.raw("  Can See Network (Dropdown): " + visible));
         Boolean netDiscovery = testNet.getDiscoveryEnabled();
         String netDesc = netDiscovery == null ? "Inherit" : (netDiscovery ? "True" : "False");
         boolean effectiveParams = netDiscovery != null ? netDiscovery : globalDiscovery;
         playerRef.sendMessage(Message.raw("  Discovery Setting: " + netDesc + " -> Effective: " + effectiveParams));
      } else {
         playerRef.sendMessage(Message.raw("- Network 'test': NOT FOUND"));
      }

      for (Waystone w : this.waystoneService.getAllWaystones("")) {
         if ("test".equals(w.getNetworkId())) {
            playerRef.sendMessage(Message.raw("- Waystone '" + w.getName() + "' (Net: test)"));
            boolean discovered = this.discoveryService.hasDiscovered(uuid, w.getName());
            playerRef.sendMessage(Message.raw("  Has Discovered: " + discovered));
            boolean secretPass = true;
            if (testNet != null && testNet.isSecret()) {
               boolean canSee = isOp || uuid.equals(testNet.getOwnerUuid()) || testNet.isMember(uuid);
               if (!canSee) {
                  secretPass = false;
               }
            }

            playerRef.sendMessage(Message.raw("  Secret Check Pass: " + secretPass));
            boolean discoveryPass = true;
            boolean effectiveDiscovery = testNet != null && testNet.getDiscoveryEnabled() != null ? testNet.getDiscoveryEnabled() : globalDiscovery;
            if (effectiveDiscovery && !discovered && !isOp) {
               discoveryPass = false;
            }

            playerRef.sendMessage(Message.raw("  Discovery Check Pass: " + discoveryPass));
         }
      }
   }
}
