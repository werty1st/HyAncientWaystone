package dev.jvnm.plugin.service;

import dev.jvnm.plugin.model.DiscoveryData;
import dev.jvnm.plugin.repository.DiscoveryRepository;
import java.util.Collections;
import java.util.Set;

public class DiscoveryService {
   private final DiscoveryRepository discoveryRepository;
   private final ConfigurationService configurationService;

   public DiscoveryService(DiscoveryRepository discoveryRepository, ConfigurationService configurationService) {
      this.discoveryRepository = discoveryRepository;
      this.configurationService = configurationService;
   }

   public boolean isEnabled() {
      return this.configurationService.get("discovery", false);
   }

   public void discoverWaystone(String playerUuid, String waystoneName) {
      DiscoveryData data = this.discoveryRepository.findByPlayer(playerUuid).orElse(new DiscoveryData(playerUuid));
      data.addDiscovery(waystoneName);
      this.discoveryRepository.saveDiscoveryData(data);
   }

   public boolean hasDiscovered(String playerUuid, String waystoneName) {
      return this.discoveryRepository.findByPlayer(playerUuid).map(data -> data.hasDiscovered(waystoneName)).orElse(false);
   }

   public Set<String> getDiscoveredWaystones(String playerUuid) {
      return this.discoveryRepository.findByPlayer(playerUuid).map(DiscoveryData::getDiscoveredWaystones).orElse(Collections.emptySet());
   }

   public void removeWaystoneFromAllDiscoveries(String waystoneName) {
   }
}
