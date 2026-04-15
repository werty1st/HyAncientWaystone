package dev.jvnm.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.managers.PlayerManager;
import dev.jvnm.plugin.model.Waystone;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.WaystoneService;
import javax.annotation.Nonnull;

@SuppressWarnings("removal")
public class WaystoneWarpCommand extends AbstractPlayerCommand {
   private final WaystoneService waystoneService;
   private final PermissionService permissionService;
   private final RequiredArg<Integer> xArg = this.withRequiredArg("x", "X Coord", ArgTypes.INTEGER);
   private final RequiredArg<Integer> yArg = this.withRequiredArg("y", "Y Coord", ArgTypes.INTEGER);
   private final RequiredArg<Integer> zArg = this.withRequiredArg("z", "Z Coord", ArgTypes.INTEGER);
   private final OptionalArg<String> flagArg = this.withOptionalArg("flag", "Flag", ArgTypes.STRING);

   public WaystoneWarpCommand(WaystoneService waystoneService, PermissionService permissionService) {
      super("warp", "Teleport to a waystone by coordinates");
      this.setPermissionGroup(GameMode.Adventure);
      this.waystoneService = waystoneService;
      this.permissionService = permissionService;
   }

   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      if (!this.permissionService.hasPermission(playerRef.getUuid(), "hytale.command.waystone.use")) {
         playerRef.sendMessage(Message.raw("You do not have permission to use waystones."));
      } else {
         Integer x = (Integer)this.xArg.get(context);
         Integer y = (Integer)this.yArg.get(context);
         Integer z = (Integer)this.zArg.get(context);
         String flag = (String)this.flagArg.get(context);
         if (flag == null) {
            String rawInput = context.getInputString();
            if (rawInput != null && rawInput.contains("use_warpberry")) {
               flag = "use_warpberry";
            }
         }

         if (x != null && y != null && z != null) {
            Vector3i pos = new Vector3i(x, y, z);
            Waystone target = this.waystoneService.getWaystoneAt(pos);
            if (target == null) {
               playerRef.sendMessage(Message.raw("No waystone found at " + x + ", " + y + ", " + z));
            } else {
               String playerUuid = playerRef.getUuid().toString();
               boolean isOp = this.permissionService.isOp(playerRef.getUuid());
               if (!this.waystoneService.isVisibleTo(target, playerUuid) && !isOp) {
                  playerRef.sendMessage(Message.raw("You cannot teleport to this waystone."));
               } else {
                  Player player = (Player)store.getComponent(ref, Player.getComponentType());
                  if (player != null) {
                     if ("use_warpberry".equals(flag)) {
                        CombinedItemContainer container = player.getInventory().getCombinedStorageFirst();
                        ItemStackTransaction tx = container.removeItemStack(new ItemStack("Plant_Fruit_Warpberry", 1));
                        if (!tx.succeeded()) {
                           playerRef.sendMessage(Message.raw("You need a Warpberry to use this marker!"));
                        } else {
                           playerRef.sendMessage(Message.raw("Warpberry consumed."));
                           PlayerManager.teleport(player, target.getPosition(), target.getWorldName());
                           this.waystoneService.playTeleportEffects(player, playerRef, target.getName(), name -> {});
                        }
                     } else {
                        this.waystoneService.warpPlayerToWaystone(player, playerRef, target, name -> {});
                     }
                  }
               }
            }
         } else {
            playerRef.sendMessage(Message.raw("Usage: /waystone warp <x> <y> <z> [use_warpberry]"));
         }
      }
   }
}
