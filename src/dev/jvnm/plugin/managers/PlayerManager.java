package dev.jvnm.plugin.managers;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.utils.SafeTeleportUtil;

public class PlayerManager {
   public static void teleport(Player player, Vector3i vector) {
      teleport(player, vector, null, 0.0F);
   }

   public static void teleport(Player player, Vector3i vector, String targetWorldName) {
      teleport(player, vector, targetWorldName, 0.0F);
   }

   public static void teleport(Player player, Vector3i vector, String targetWorldName, float facingYaw) {
      World targetWorld = resolveTargetWorld(player, targetWorldName);
      if (targetWorld != null) {
         teleportToWorld(player, vector, targetWorld, facingYaw, true);
      }
   }

   public static void teleportExact(Player player, Vector3i vector, String targetWorldName, float facingYaw) {
      World targetWorld = resolveTargetWorld(player, targetWorldName);
      if (targetWorld != null) {
         teleportToWorld(player, vector, targetWorld, facingYaw, false);
      }
   }

   private static World resolveTargetWorld(Player player, String targetWorldName) {
      if (player.getReference() == null) {
         return null;
      } else {
         World targetWorld = null;
         if (targetWorldName != null && !targetWorldName.isEmpty()) {
            targetWorld = Universe.get().getWorld(targetWorldName);
            if (targetWorld == null) {
               System.out.println("[Waystone] Warning: Target world '" + targetWorldName + "' not found, falling back to current world");
            }
         }

         if (targetWorld == null) {
            targetWorld = player.getWorld();
         }

         return targetWorld;
      }
   }

   private static void teleportToWorld(Player player, Vector3i vector, World targetWorld, float facingYaw, boolean useSafeTeleport) {
      if (targetWorld != null && player.getReference() != null) {
         Store<EntityStore> store = player.getReference().getStore();
         if (store != null) {
            Vector3i targetLocation = useSafeTeleport ? SafeTeleportUtil.findSafeLocation(targetWorld, vector) : vector;
            Vector3d teleportPosition = new Vector3d(targetLocation.getX() + 0.5, targetLocation.getY() + 0.1, targetLocation.getZ() + 0.5);
            float yawRadians = (float)Math.toRadians(facingYaw);
            Teleport teleport = new Teleport(targetWorld, teleportPosition, new Vector3f(0.0F, yawRadians, 0.0F));
            store.addComponent(player.getReference(), Teleport.getComponentType(), teleport);
         }
      }
   }
}
