package dev.jvnm.plugin.repository;

import dev.jvnm.plugin.model.DiscoveryData;
import java.util.Optional;

public interface DiscoveryRepository {
   void load();

   void save();

   Optional<DiscoveryData> findByPlayer(String var1);

   void saveDiscoveryData(DiscoveryData var1);
}
