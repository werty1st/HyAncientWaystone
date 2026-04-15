package dev.jvnm.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.ProfileServiceClient.PublicGameProfile;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.FriendService;
import java.util.Map;
import javax.annotation.Nonnull;

public class WaystoneFriendCommand extends AbstractPlayerCommand {
   public WaystoneFriendCommand(FriendService friendService, PermissionService permissionService) {
      super("friend", "Manage friends");
      this.setPermissionGroup(GameMode.Adventure);
      this.addSubCommand(new WaystoneFriendCommand.AddFriendCommand(friendService, permissionService));
      this.addSubCommand(new WaystoneFriendCommand.RemoveFriendCommand(friendService, permissionService));
      this.addSubCommand(new WaystoneFriendCommand.ListFriendsCommand(friendService, permissionService));
   }

   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
   }

   public static class AddFriendCommand extends AbstractPlayerCommand {
      private final FriendService friendService;
      private final PermissionService permissionService;
      private final RequiredArg<PublicGameProfile> playerArg;

      public AddFriendCommand(FriendService friendService, PermissionService permissionService) {
         super("add", "Add a friend");
         this.setPermissionGroup(GameMode.Adventure);
         this.friendService = friendService;
         this.permissionService = permissionService;
         this.playerArg = this.withRequiredArg("player", "Player Name", ArgTypes.GAME_PROFILE_LOOKUP);
      }

      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         if (!this.permissionService.hasPermission(playerRef.getUuid(), "hytale.command.waystone.friends")) {
            playerRef.sendMessage(Message.raw("You do not have permission to manage friends."));
         } else {
            PublicGameProfile profile = (PublicGameProfile)this.playerArg.get(context);
            if (profile == null) {
               playerRef.sendMessage(Message.raw("Could not find player."));
            } else {
               this.friendService.addFriend(playerRef.getUuid().toString(), profile.getUuid().toString(), profile.getUsername());
               playerRef.sendMessage(Message.raw("Added friend: " + profile.getUsername()));
            }
         }
      }
   }

   public static class ListFriendsCommand extends AbstractPlayerCommand {
      private final FriendService friendService;
      private final PermissionService permissionService;

      public ListFriendsCommand(FriendService friendService, PermissionService permissionService) {
         super("list", "List your friends");
         this.setPermissionGroup(GameMode.Adventure);
         this.friendService = friendService;
         this.permissionService = permissionService;
      }

      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         if (!this.permissionService.hasPermission(playerRef.getUuid(), "hytale.command.waystone.friends")) {
            playerRef.sendMessage(Message.raw("You do not have permission to manage friends."));
         } else {
            Map<String, String> friends = this.friendService.getFriends(playerRef.getUuid().toString());
            if (friends.isEmpty()) {
               playerRef.sendMessage(Message.raw("You have no friends added."));
            } else {
               playerRef.sendMessage(Message.raw("Friends: " + String.join(", ", friends.values())));
            }
         }
      }
   }

   public static class RemoveFriendCommand extends AbstractPlayerCommand {
      private final FriendService friendService;
      private final PermissionService permissionService;
      private final RequiredArg<PublicGameProfile> playerArg;

      public RemoveFriendCommand(FriendService friendService, PermissionService permissionService) {
         super("remove", "Remove a friend");
         this.setPermissionGroup(GameMode.Adventure);
         this.friendService = friendService;
         this.permissionService = permissionService;
         this.playerArg = this.withRequiredArg("player", "Player Name", ArgTypes.GAME_PROFILE_LOOKUP);
      }

      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         if (!this.permissionService.hasPermission(playerRef.getUuid(), "hytale.command.waystone.friends")) {
            playerRef.sendMessage(Message.raw("You do not have permission to manage friends."));
         } else {
            PublicGameProfile profile = (PublicGameProfile)this.playerArg.get(context);
            if (profile == null) {
               playerRef.sendMessage(Message.raw("Could not find player."));
            } else {
               this.friendService.removeFriend(playerRef.getUuid().toString(), profile.getUuid().toString());
               playerRef.sendMessage(Message.raw("Removed friend: " + profile.getUsername()));
            }
         }
      }
   }
}
