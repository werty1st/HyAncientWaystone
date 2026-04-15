package dev.jvnm.plugin.utils;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import java.util.logging.Level;

public class SafeTeleportUtil {
   private static final HytaleLogger LOGGER = HytaleLogger.get("SafeTeleportUtil");

   public static Vector3i findSafeLocation(World world, Vector3i targetPos) {
      if (world != null && targetPos != null) {
         int[][] offsets = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

         for (int[] offset : offsets) {
            int cx = targetPos.getX() + offset[0];
            int cy = targetPos.getY();
            int cz = targetPos.getZ() + offset[1];
            if (isSpotSafe(world, cx, cy, cz)) {
               Vector3i safePos = new Vector3i(cx, cy, cz);
               LOGGER.at(Level.FINE).log("Found safe spot at %s", safePos);
               return safePos;
            }
         }

         long chunkIndex = ChunkUtil.indexChunkFromBlock(targetPos.getX(), targetPos.getZ());
         WorldChunk worldChunk = world.getChunkIfInMemory(chunkIndex);
         if (worldChunk != null) {
            int x = MathUtil.floor(targetPos.getX());
            int z = MathUtil.floor(targetPos.getZ());
            int topHeight = worldChunk.getHeight(x, z);
            if (topHeight > targetPos.getY()) {
               Vector3i topPos = new Vector3i(targetPos.getX(), topHeight + 2, targetPos.getZ());
               LOGGER.at(Level.FINE).log("Waystone obscured/buried. Teleporting to top surface: %s", topPos);
               return topPos;
            }
         }

         return new Vector3i(targetPos.getX() + 1, targetPos.getY() + 1, targetPos.getZ());
      } else {
         return targetPos;
      }
   }

   private static boolean isSpotSafe(World world, int x, int y, int z) {
      return isAir(world, x, y, z) && isAir(world, x, y + 1, z);
   }

   private static boolean isAir(World world, int x, int y, int z) {
      try {
         BlockType block = world.getBlockType(x, y, z);
         LOGGER.at(Level.FINEST).log("Block at (%d, %d, %d): %s", x, y, z, block != null ? block.getId() : "null");
         return block == null || "Empty".equals(block.getId());
      } catch (Exception var5) {
         LOGGER.at(Level.WARNING).log("Error checking block at (%d, %d, %d): %s", x, y, z, var5.getMessage());
         return false;
      }
   }
}
