package dev.jvnm.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.ProfileServiceClient.PublicGameProfile;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.model.Network;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.NetworkService;
import java.util.List;
import javax.annotation.Nonnull;

public class WaystoneNetworkMemberCommand extends AbstractCommandCollection {
   public WaystoneNetworkMemberCommand(NetworkService networkService, PermissionService permissionService) {
      super("member", "Manage network members");
      this.setPermissionGroup(GameMode.Adventure);
      this.addSubCommand(new WaystoneNetworkMemberCommand.MemberAddCommand(networkService, permissionService));
      this.addSubCommand(new WaystoneNetworkMemberCommand.MemberRemoveCommand(networkService, permissionService));
      this.addSubCommand(new WaystoneNetworkMemberCommand.MemberListCommand(networkService, permissionService));
   }

   private static boolean checkPermission(PlayerRef playerRef, Network network, PermissionService permissionService) {
      boolean isOwner = playerRef.getUuid().toString().equals(network.getOwnerUuid());
      boolean hasManagePerm = permissionService.hasPermission(playerRef.getUuid(), "hytale.command.waystone.network.manage");
      if (!isOwner && !hasManagePerm) {
         playerRef.sendMessage(Message.raw("You don't have permission to manage members of this network."));
         return false;
      } else {
         return true;
      }
   }

   public static class MemberAddCommand extends AbstractPlayerCommand {
      private final NetworkService networkService;
      private final PermissionService permissionService;
      private final RequiredArg<String> networkArg;
      private final RequiredArg<PublicGameProfile> playerArg;

      public MemberAddCommand(NetworkService networkService, PermissionService permissionService) {
         super("add", "Add a member to the network");
         this.setPermissionGroup(GameMode.Adventure);
         this.networkService = networkService;
         this.permissionService = permissionService;
         this.networkArg = this.withRequiredArg("network", "Network name", ArgTypes.STRING);
         this.playerArg = this.withRequiredArg("player", "Player Name", ArgTypes.GAME_PROFILE_LOOKUP);
      }

      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         String networkName = (String)this.networkArg.get(context);
         PublicGameProfile profile = (PublicGameProfile)this.playerArg.get(context);
         if (profile == null) {
            playerRef.sendMessage(Message.raw("Could not find player."));
         } else {
            String playerUuid = profile.getUuid().toString();
            String playerName = profile.getUsername();
            Network network = this.networkService.getNetwork(networkName.toLowerCase());
            if (network == null) {
               playerRef.sendMessage(Message.raw("Network not found: " + networkName));
            } else if (WaystoneNetworkMemberCommand.checkPermission(playerRef, network, this.permissionService)) {
               if (network.isMember(playerUuid)) {
                  playerRef.sendMessage(Message.raw(playerName + " is already a member of " + network.getDisplayName()));
               } else {
                  this.networkService.addMember(network, playerUuid);
                  playerRef.sendMessage(Message.raw("Added " + playerName + " to " + network.getDisplayName()));
               }
            }
         }
      }
   }

   public static class MemberListCommand extends AbstractPlayerCommand {
      private final NetworkService networkService;
      private final PermissionService permissionService;
      private final RequiredArg<String> networkArg;

      public MemberListCommand(NetworkService networkService, PermissionService permissionService) {
         super("list", "List members of the network");
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
         } else if (!playerRef.getUuid().toString().equals(network.getOwnerUuid())
            && !this.permissionService.hasPermission(playerRef.getUuid(), "hytale.command.waystone.network.manage")
            && !network.isMember(playerRef.getUuid().toString())) {
            playerRef.sendMessage(Message.raw("You don't have permission to view members of this network."));
         } else {
            List<String> members = network.getMembers();
            if (members.isEmpty()) {
               playerRef.sendMessage(Message.raw(network.getDisplayName() + " has no members."));
            } else {
               playerRef.sendMessage(Message.raw("--- " + network.getDisplayName() + " Members ---"));

               for (String member : members) {
                  playerRef.sendMessage(Message.raw("- " + member));
               }
            }
         }
      }
   }

   public static class MemberRemoveCommand extends AbstractPlayerCommand {
      private final NetworkService networkService;
      private final PermissionService permissionService;
      private final RequiredArg<String> networkArg;
      private final RequiredArg<PublicGameProfile> playerArg;

      public MemberRemoveCommand(NetworkService networkService, PermissionService permissionService) {
         super("remove", "Remove a member from the network");
         this.setPermissionGroup(GameMode.Adventure);
         this.networkService = networkService;
         this.permissionService = permissionService;
         this.networkArg = this.withRequiredArg("network", "Network name", ArgTypes.STRING);
         this.playerArg = this.withRequiredArg("player", "Player Name", ArgTypes.GAME_PROFILE_LOOKUP);
      }

      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         String networkName = (String)this.networkArg.get(context);
         PublicGameProfile profile = (PublicGameProfile)this.playerArg.get(context);
         if (profile == null) {
            playerRef.sendMessage(Message.raw("Could not find player."));
         } else {
            String playerUuid = profile.getUuid().toString();
            String playerName = profile.getUsername();
            Network network = this.networkService.getNetwork(networkName.toLowerCase());
            if (network == null) {
               playerRef.sendMessage(Message.raw("Network not found: " + networkName));
            } else if (WaystoneNetworkMemberCommand.checkPermission(playerRef, network, this.permissionService)) {
               if (!network.isMember(playerUuid)) {
                  playerRef.sendMessage(Message.raw(playerName + " is not a member of " + network.getDisplayName()));
               } else {
                  this.networkService.removeMember(network, playerUuid);
                  playerRef.sendMessage(Message.raw("Removed " + playerName + " from " + network.getDisplayName()));
               }
            }
         }
      }
   }
}
