package dev.jvnm.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
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
import dev.jvnm.plugin.model.Network;
import dev.jvnm.plugin.model.PriceItem;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.NetworkService;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class WaystoneNetworkSettingsCommand extends AbstractPlayerCommand {
   public WaystoneNetworkSettingsCommand(NetworkService networkService, PermissionService permissionService) {
      super("settings", "Configure network-specific settings");
      this.setPermissionGroup(GameMode.Adventure);
      this.addSubCommand(new WaystoneNetworkSettingsCommand.DiscoveryCommand(networkService, permissionService));
      this.addSubCommand(new WaystoneNetworkSettingsCommand.CooldownCommand(networkService, permissionService));
      this.addSubCommand(new WaystoneNetworkSettingsCommand.SeeGlobalCommand(networkService, permissionService));
      this.addSubCommand(new WaystoneNetworkSettingsCommand.PriceAddCommand(networkService, permissionService));
      this.addSubCommand(new WaystoneNetworkSettingsCommand.PriceDeleteCommand(networkService, permissionService));
      this.addSubCommand(new WaystoneNetworkSettingsCommand.PriceClearCommand(networkService, permissionService));
      this.addSubCommand(new WaystoneNetworkSettingsCommand.PriceListCommand(networkService, permissionService));
      this.addSubCommand(new WaystoneNetworkSettingsCommand.SecretCommand(networkService, permissionService));
   }

   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      playerRef.sendMessage(Message.raw("Usage: /waystone network settings (discovery|cooldown|seeglobal|priceadd|priceclear|pricelist) ..."));
   }

   private static boolean checkPermission(PlayerRef playerRef, Network network, PermissionService permissionService) {
      boolean isOwner = playerRef.getUuid().toString().equals(network.getOwnerUuid());
      boolean hasManagePerm = permissionService.hasPermission(playerRef.getUuid(), "hytale.command.waystone.network.manage");
      if (!isOwner && !hasManagePerm) {
         playerRef.sendMessage(Message.raw("You don't have permission to configure this network."));
         return false;
      } else {
         return true;
      }
   }

   public static class CooldownCommand extends AbstractPlayerCommand {
      private final NetworkService networkService;
      private final PermissionService permissionService;
      private final RequiredArg<String> networkArg;
      private final RequiredArg<Integer> secondsArg;

      public CooldownCommand(NetworkService networkService, PermissionService permissionService) {
         super("cooldown", "Set network cooldown");
         this.setPermissionGroup(GameMode.Adventure);
         this.networkService = networkService;
         this.permissionService = permissionService;
         this.networkArg = this.withRequiredArg("network", "Network name", ArgTypes.STRING);
         this.secondsArg = this.withRequiredArg("seconds", "Cooldown in seconds (0=use global)", ArgTypes.INTEGER);
      }

      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         String networkName = (String)this.networkArg.get(context);
         int seconds = (Integer)this.secondsArg.get(context);
         Network network = this.networkService.getNetwork(networkName.toLowerCase());
         if (network == null) {
            playerRef.sendMessage(Message.raw("Network not found: " + networkName));
         } else if (WaystoneNetworkSettingsCommand.checkPermission(playerRef, network, this.permissionService)) {
            network.setCooldown(seconds);
            this.networkService.updateNetwork(network);
            if (seconds <= 0) {
               playerRef.sendMessage(Message.raw("Cooldown for " + network.getDisplayName() + " now uses global setting"));
            } else {
               playerRef.sendMessage(Message.raw("Cooldown for " + network.getDisplayName() + " set to: " + seconds + " seconds"));
            }
         }
      }
   }

   public static class DiscoveryCommand extends AbstractPlayerCommand {
      private final NetworkService networkService;
      private final PermissionService permissionService;
      private final RequiredArg<String> networkArg;
      private final RequiredArg<String> valueArg;

      public DiscoveryCommand(NetworkService networkService, PermissionService permissionService) {
         super("discovery", "Set network discovery mode");
         this.setPermissionGroup(GameMode.Adventure);
         this.networkService = networkService;
         this.permissionService = permissionService;
         this.networkArg = this.withRequiredArg("network", "Network name", ArgTypes.STRING);
         this.valueArg = this.withRequiredArg("value", "true/false/inherit", ArgTypes.STRING);
      }

      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         String networkName = (String)this.networkArg.get(context);
         String value = (String)this.valueArg.get(context);
         Network network = this.networkService.getNetwork(networkName.toLowerCase());
         if (network == null) {
            playerRef.sendMessage(Message.raw("Network not found: " + networkName));
         } else if (WaystoneNetworkSettingsCommand.checkPermission(playerRef, network, this.permissionService)) {
            Boolean enabled = null;
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on") || value.equals("1")) {
               enabled = true;
            } else if (!value.equalsIgnoreCase("false") && !value.equalsIgnoreCase("off") && !value.equals("0")) {
               if (!value.equalsIgnoreCase("inherit") && !value.equalsIgnoreCase("default") && !value.equalsIgnoreCase("reset")) {
                  playerRef.sendMessage(Message.raw("Invalid value. Use true, false, or inherit."));
                  return;
               }

               enabled = null;
            } else {
               enabled = false;
            }

            network.setDiscoveryEnabled(enabled);
            this.networkService.updateNetwork(network);
            String status = enabled == null ? "inherit (Global)" : (enabled ? "enabled" : "disabled");
            playerRef.sendMessage(Message.raw("Discovery for " + network.getDisplayName() + " set to: " + status));
         }
      }
   }

   public static class PriceAddCommand extends AbstractPlayerCommand {
      private final NetworkService networkService;
      private final PermissionService permissionService;
      private final RequiredArg<String> networkArg;
      private final RequiredArg<Integer> amountArg;

      public PriceAddCommand(NetworkService networkService, PermissionService permissionService) {
         super("priceadd", "Add held item as teleport cost");
         this.setPermissionGroup(GameMode.Adventure);
         this.networkService = networkService;
         this.permissionService = permissionService;
         this.networkArg = this.withRequiredArg("network", "Network name", ArgTypes.STRING);
         this.amountArg = this.withRequiredArg("amount", "Number of items required", ArgTypes.INTEGER);
      }

      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         String networkName = (String)this.networkArg.get(context);
         int amount = (Integer)this.amountArg.get(context);
         Network network = this.networkService.getNetwork(networkName.toLowerCase());
         if (network == null) {
            playerRef.sendMessage(Message.raw("Network not found: " + networkName));
         } else if (WaystoneNetworkSettingsCommand.checkPermission(playerRef, network, this.permissionService)) {
            if (amount <= 0) {
               playerRef.sendMessage(Message.raw("Amount must be greater than 0."));
            } else {
               Player player = (Player)store.getComponent(ref, Player.getComponentType());
               if (player != null) {
                  ItemStack heldItem = player.getInventory().getHotbar().getItemStack(player.getInventory().getActiveHotbarSlot());
                  if (heldItem != null && !heldItem.getItemId().isEmpty()) {
                     String itemId = heldItem.getItemId();
                     List<PriceItem> costs = new ArrayList<>(network.getCosts());
                     costs.removeIf(existing -> existing.getItemId().equals(itemId));
                     costs.add(new PriceItem(itemId, amount));
                     network.setCosts(costs);
                     this.networkService.updateNetwork(network);
                     playerRef.sendMessage(Message.raw("Added " + amount + "x " + itemId + " to " + network.getDisplayName() + " teleport cost."));
                  } else {
                     playerRef.sendMessage(Message.raw("You must hold an item to add as a price cost."));
                  }
               }
            }
         }
      }
   }

   public static class PriceClearCommand extends AbstractPlayerCommand {
      private final NetworkService networkService;
      private final PermissionService permissionService;
      private final RequiredArg<String> networkArg;

      public PriceClearCommand(NetworkService networkService, PermissionService permissionService) {
         super("priceclear", "Clear all teleport costs");
         this.setPermissionGroup(GameMode.Adventure);
         this.networkService = networkService;
         this.permissionService = permissionService;
         this.networkArg = this.withRequiredArg("network", "Network name", ArgTypes.STRING);
      }

      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         String networkName = (String)this.networkArg.get(context);
         Network network = this.networkService.getNetwork(networkName.toLowerCase());
         if (network == null) {
            playerRef.sendMessage(Message.raw("Network not found: " + networkName));
         } else if (WaystoneNetworkSettingsCommand.checkPermission(playerRef, network, this.permissionService)) {
            network.setCosts(new ArrayList<>());
            this.networkService.updateNetwork(network);
            playerRef.sendMessage(Message.raw("Cleared all teleport costs for " + network.getDisplayName() + "."));
         }
      }
   }

   public static class PriceDeleteCommand extends AbstractPlayerCommand {
      private final NetworkService networkService;
      private final PermissionService permissionService;
      private final RequiredArg<String> networkArg;

      public PriceDeleteCommand(NetworkService networkService, PermissionService permissionService) {
         super("pricedelete", "Remove held item from teleport costs");
         this.setPermissionGroup(GameMode.Adventure);
         this.networkService = networkService;
         this.permissionService = permissionService;
         this.networkArg = this.withRequiredArg("network", "Network name", ArgTypes.STRING);
      }

      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         String networkName = (String)this.networkArg.get(context);
         Network network = this.networkService.getNetwork(networkName.toLowerCase());
         if (network == null) {
            playerRef.sendMessage(Message.raw("Network not found: " + networkName));
         } else if (WaystoneNetworkSettingsCommand.checkPermission(playerRef, network, this.permissionService)) {
            Player player = (Player)store.getComponent(ref, Player.getComponentType());
            if (player != null) {
               ItemStack heldItem = player.getInventory().getHotbar().getItemStack(player.getInventory().getActiveHotbarSlot());
               if (heldItem != null && !heldItem.getItemId().isEmpty() && !heldItem.getItemId().equals("hytale:items/air")) {
                  String itemId = heldItem.getItemId();
                  List<PriceItem> currentPrices = new ArrayList<>(network.getCosts());
                  boolean removed = currentPrices.removeIf(p -> p.getItemId().equals(itemId));
                  if (removed) {
                     network.setCosts(currentPrices);
                     this.networkService.updateNetwork(network);
                     playerRef.sendMessage(Message.raw("Removed " + itemId + " from " + network.getDisplayName() + " price list."));
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

   public static class PriceListCommand extends AbstractPlayerCommand {
      private final NetworkService networkService;
      private final PermissionService permissionService;
      private final RequiredArg<String> networkArg;

      public PriceListCommand(NetworkService networkService, PermissionService permissionService) {
         super("pricelist", "List teleport costs");
         this.setPermissionGroup(GameMode.Adventure);
         this.networkService = networkService;
         this.permissionService = permissionService;
         this.networkArg = this.withRequiredArg("network", "Network name", ArgTypes.STRING);
      }

      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         String networkName = (String)this.networkArg.get(context);
         Network network = this.networkService.getNetwork(networkName.toLowerCase());
         if (network == null) {
            playerRef.sendMessage(Message.raw("Network not found: " + networkName));
         } else if (WaystoneNetworkSettingsCommand.checkPermission(playerRef, network, this.permissionService)) {
            List<PriceItem> costs = network.getCosts();
            if (costs.isEmpty()) {
               playerRef.sendMessage(Message.raw(network.getDisplayName() + " has no custom costs (uses global)."));
            } else {
               playerRef.sendMessage(Message.raw("--- " + network.getDisplayName() + " Teleport Costs ---"));

               for (PriceItem cost : costs) {
                  playerRef.sendMessage(Message.raw(" - " + cost.getAmount() + "x " + cost.getItemId()));
               }
            }
         }
      }
   }

   public static class SecretCommand extends AbstractPlayerCommand {
      private final NetworkService networkService;
      private final PermissionService permissionService;
      private final RequiredArg<String> networkArg;
      private final RequiredArg<String> valueArg;

      public SecretCommand(NetworkService networkService, PermissionService permissionService) {
         super("secret", "Set network secrecy (only members see it)");
         this.setPermissionGroup(GameMode.Adventure);
         this.networkService = networkService;
         this.permissionService = permissionService;
         this.networkArg = this.withRequiredArg("network", "Network name", ArgTypes.STRING);
         this.valueArg = this.withRequiredArg("isSecret", "true/false", ArgTypes.STRING);
      }

      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         String networkName = (String)this.networkArg.get(context);
         String value = (String)this.valueArg.get(context);
         Network network = this.networkService.getNetwork(networkName.toLowerCase());
         if (network == null) {
            playerRef.sendMessage(Message.raw("Network not found: " + networkName));
         } else if (WaystoneNetworkSettingsCommand.checkPermission(playerRef, network, this.permissionService)) {
            boolean isSecret = value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on") || value.equals("1");
            network.setSecret(isSecret);
            this.networkService.updateNetwork(network);
            playerRef.sendMessage(Message.raw("Secret mode for " + network.getDisplayName() + " set to: " + isSecret));
         }
      }
   }

   public static class SeeGlobalCommand extends AbstractPlayerCommand {
      private final NetworkService networkService;
      private final PermissionService permissionService;
      private final RequiredArg<String> networkArg;
      private final RequiredArg<String> valueArg;

      public SeeGlobalCommand(NetworkService networkService, PermissionService permissionService) {
         super("seeglobal", "Set whether network can see global waystones");
         this.setPermissionGroup(GameMode.Adventure);
         this.networkService = networkService;
         this.permissionService = permissionService;
         this.networkArg = this.withRequiredArg("network", "Network name", ArgTypes.STRING);
         this.valueArg = this.withRequiredArg("enabled", "true/false", ArgTypes.STRING);
      }

      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         String networkName = (String)this.networkArg.get(context);
         String value = (String)this.valueArg.get(context);
         Network network = this.networkService.getNetwork(networkName.toLowerCase());
         if (network == null) {
            playerRef.sendMessage(Message.raw("Network not found: " + networkName));
         } else if (WaystoneNetworkSettingsCommand.checkPermission(playerRef, network, this.permissionService)) {
            boolean enabled = value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on") || value.equals("1");
            network.setSeeGlobal(enabled);
            this.networkService.updateNetwork(network);
            playerRef.sendMessage(Message.raw("See Global for " + network.getDisplayName() + " set to: " + (enabled ? "enabled" : "disabled")));
         }
      }
   }
}
