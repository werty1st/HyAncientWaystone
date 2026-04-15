package dev.jvnm.plugin.repository;

import dev.jvnm.plugin.model.FriendData;
import java.util.Optional;

public interface FriendRepository {
   void load();

   void save();

   Optional<FriendData> findByPlayer(String var1);

   void saveFriendData(FriendData var1);
}
