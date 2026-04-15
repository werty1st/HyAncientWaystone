package dev.jvnm.plugin.map;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.worldmap.ContextMenuItem;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager.MarkerProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MapMarkerBuilder;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;
import dev.jvnm.plugin.AncientWaystone;
import dev.jvnm.plugin.model.Waystone;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;

@SuppressWarnings("removal")
public class WaystoneMarkerProvider implements MarkerProvider {
   public static final WaystoneMarkerProvider INSTANCE = new WaystoneMarkerProvider();
   private static final String MARKER_ICON = "Waystone.png";

   private WaystoneMarkerProvider() {
   }

   public void update(@Nonnull World world, @Nonnull Player player, @Nonnull MarkersCollector collector) {
      UUID playerUuid = player.getUuid();

      assert playerUuid != null;

      boolean isOp = PermissionsModule.get().getGroupsForUser(playerUuid).contains("OP");
      String worldName = world.getName();
      boolean markersEnabled = AncientWaystone.getWaystoneService().getConfigurationService().get("map_markers_enabled", false);
      List<Waystone> waystones;
      if (markersEnabled) {
         waystones = AncientWaystone.getWaystoneService().getAllVisibleWaystones(playerUuid.toString(), null, isOp);
      } else {
         waystones = Collections.emptyList();
      }

      for (Waystone waystone : waystones) {
         if (waystone.getWorldName() != null && worldName.equals(waystone.getWorldName())) {
            Vector3d position = new Vector3d(waystone.getPosition().getX() + 0.5, waystone.getPosition().getY(), waystone.getPosition().getZ() + 0.5);
            String markerId = "ws_mk_" + waystone.getName();
            Transform transform = new Transform(position);
            String command = "waystone warp "
               + waystone.getPosition().getX()
               + " "
               + waystone.getPosition().getY()
               + " "
               + waystone.getPosition().getZ()
               + " --flag=use_warpberry";
            MapMarker marker = new MapMarkerBuilder(markerId, "Waystone.png", transform)
               .withName(Message.raw(waystone.getName()))
               .withContextMenuItem(new ContextMenuItem("Use warpberry", command))
               .build();
            collector.add(marker);
         }
      }
   }
}
