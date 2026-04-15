package dev.jvnm.plugin.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FriendData {
   private final String playerUuid;
   private Map<String, String> friends = new HashMap<>();

   public FriendData(String playerUuid) {
      this.playerUuid = playerUuid;
      this.friends = new HashMap<>();
   }

   public String getPlayerUuid() {
      return this.playerUuid;
   }

   public Map<String, String> getFriends() {
      if (this.friends == null) {
         this.friends = new HashMap<>();
      }

      return this.friends;
   }

   public Set<String> getFriendUuids() {
      return this.getFriends().keySet();
   }

   public void addFriend(String friendUuid, String friendName) {
      this.getFriends().put(friendUuid, friendName);
   }

   public void removeFriend(String friendUuid) {
      this.getFriends().remove(friendUuid);
   }

   public boolean isFriend(String friendUuid) {
      return this.getFriends().containsKey(friendUuid);
   }
}
