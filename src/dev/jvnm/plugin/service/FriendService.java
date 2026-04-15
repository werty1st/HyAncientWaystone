package dev.jvnm.plugin.service;

import dev.jvnm.plugin.model.FriendData;
import dev.jvnm.plugin.repository.FriendRepository;
import java.util.Collections;
import java.util.Map;

public class FriendService {
   private final FriendRepository friendRepository;

   public FriendService(FriendRepository friendRepository) {
      this.friendRepository = friendRepository;
   }

   public void addFriend(String playerUuid, String friendUuid, String friendName) {
      FriendData data = this.friendRepository.findByPlayer(playerUuid).orElse(new FriendData(playerUuid));
      data.addFriend(friendUuid, friendName);
      this.friendRepository.saveFriendData(data);
   }

   public void removeFriend(String playerUuid, String friendUuid) {
      this.friendRepository.findByPlayer(playerUuid).ifPresent(data -> {
         data.removeFriend(friendUuid);
         this.friendRepository.saveFriendData(data);
      });
   }

   public Map<String, String> getFriends(String playerUuid) {
      return this.friendRepository.findByPlayer(playerUuid).map(FriendData::getFriends).orElse(Collections.emptyMap());
   }

   public boolean isFriend(String playerUuid, String potentialFriendUuid) {
      return this.friendRepository.findByPlayer(playerUuid).map(data -> data.isFriend(potentialFriendUuid)).orElse(false);
   }
}
