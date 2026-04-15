package dev.jvnm.plugin.repository;

import com.hypixel.hytale.math.vector.Vector3i;
import dev.jvnm.plugin.model.Waystone;
import java.util.List;
import java.util.Optional;

public interface WaystoneRepository {
   void load();

   void save();

   List<Waystone> findAll();

   Optional<Waystone> findByPosition(Vector3i var1);

   Optional<Waystone> findByName(String var1);

   void add(Waystone var1);

   void remove(Waystone var1);

   void update(Waystone var1);

   List<Waystone> findByOwner(String var1);

   List<Waystone> findByNetwork(String var1);
}
