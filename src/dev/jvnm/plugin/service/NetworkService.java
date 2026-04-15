package dev.jvnm.plugin.service;

import dev.jvnm.plugin.model.Network;
import dev.jvnm.plugin.repository.NetworkRepository;
import java.util.ArrayList;
import java.util.List;

public class NetworkService {
   private final NetworkRepository networkRepository;
   private final ConfigurationService configurationService;

   public NetworkService(NetworkRepository networkRepository, ConfigurationService configurationService) {
      this.networkRepository = networkRepository;
      this.configurationService = configurationService;
   }

   public boolean isEnabled() {
      return this.configurationService.get("networks_enabled", false);
   }

   public void setEnabled(boolean enabled) {
      this.configurationService.set("networks_enabled", enabled);
   }

   public Network getNetwork(String id) {
      return this.networkRepository.getNetwork(id);
   }

   public Network getGlobalNetwork() {
      return this.networkRepository.getOrCreateGlobal();
   }

   public Network getOrCreatePersonalNetwork(String playerUuid, String playerName) {
      return this.networkRepository.getOrCreatePersonal(playerUuid, playerName);
   }

   public Network getPersonalNetwork(String playerUuid) {
      return this.networkRepository.getPersonalNetwork(playerUuid);
   }

   public List<Network> getAllNetworks() {
      return this.networkRepository.getAllNetworks();
   }

   public List<Network> getCustomNetworks() {
      return this.networkRepository.getCustomNetworks();
   }

   public Network createNetwork(String id, String displayName, String ownerUuid) {
      if (this.networkRepository.exists(id)) {
         return null;
      } else {
         Network network = new Network(id, displayName, ownerUuid);
         this.networkRepository.saveNetwork(network);
         return network;
      }
   }

   public void updateNetwork(Network network) {
      this.networkRepository.saveNetwork(network);
   }

   public boolean deleteNetwork(String id) {
      return !"global".equals(id) && !Network.isPersonalNetwork(id) ? this.networkRepository.deleteNetwork(id) : false;
   }

   public boolean networkExists(String id) {
      return this.networkRepository.exists(id);
   }

   public String getEffectiveNetworkId(String networkId) {
      if (!this.isEnabled()) {
         return "global";
      } else {
         return networkId != null ? networkId : "global";
      }
   }

   public List<Network> getNetworksForPlayer(String playerUuid, boolean isOp) {
      List<Network> result = new ArrayList<>();
      result.add(this.getGlobalNetwork());
      Network personal = this.getPersonalNetwork(playerUuid);
      if (personal != null) {
         result.add(personal);
      }

      for (Network network : this.getCustomNetworks()) {
         if (isOp || playerUuid.equals(network.getOwnerUuid()) || network.isMember(playerUuid) || !network.isSecret()) {
            result.add(network);
         }
      }

      return result;
   }

   public void addMember(Network network, String playerUuid) {
      network.addMember(playerUuid);
      this.updateNetwork(network);
   }

   public void removeMember(Network network, String playerUuid) {
      network.removeMember(playerUuid);
      this.updateNetwork(network);
   }

   public boolean isMember(Network network, String playerUuid) {
      return network.isMember(playerUuid);
   }
}
