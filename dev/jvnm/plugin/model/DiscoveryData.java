package dev.jvnm.plugin.model;

import java.util.HashSet;
import java.util.Set;

public class DiscoveryData {
   private final String playerUuid;
   private Set<String> discoveredWaystones;

   public DiscoveryData(String playerUuid) {
      this.playerUuid = playerUuid;
      this.discoveredWaystones = new HashSet<>();
   }

   public String getPlayerUuid() {
      return this.playerUuid;
   }

   public Set<String> getDiscoveredWaystones() {
      if (this.discoveredWaystones == null) {
         this.discoveredWaystones = new HashSet<>();
      }

      return this.discoveredWaystones;
   }

   public void addDiscovery(String waystoneName) {
      this.getDiscoveredWaystones().add(waystoneName);
   }

   public boolean hasDiscovered(String waystoneName) {
      return this.getDiscoveredWaystones().contains(waystoneName);
   }

   public void removeDiscovery(String waystoneName) {
      this.getDiscoveredWaystones().remove(waystoneName);
   }
}
