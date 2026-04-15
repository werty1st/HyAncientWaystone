package dev.jvnm.plugin.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Network {
   public static final String GLOBAL_ID = "global";
   public static final String PERSONAL_PREFIX = "personal:";
   private final String id;
   private String displayName;
   private String ownerUuid;
   private Boolean discoveryEnabled;
   private int cooldown;
   private boolean seeGlobal;
   private List<PriceItem> costs;
   private Boolean isSecret;
   private List<String> members;

   public Network(String id, String displayName) {
      this(id, displayName, null);
   }

   public Network(String id, String displayName, String ownerUuid) {
      this.id = id;
      this.displayName = displayName;
      this.ownerUuid = ownerUuid;
      this.discoveryEnabled = null;
      this.cooldown = 0;
      this.seeGlobal = true;
      this.costs = new CopyOnWriteArrayList<>();
      this.isSecret = true;
      this.members = new CopyOnWriteArrayList<>();
   }

   public Network(
      String id,
      String displayName,
      String ownerUuid,
      Boolean discoveryEnabled,
      int cooldown,
      boolean seeGlobal,
      List<PriceItem> costs,
      Boolean isSecret,
      List<String> members
   ) {
      this.id = id;
      this.displayName = displayName;
      this.ownerUuid = ownerUuid;
      this.discoveryEnabled = discoveryEnabled;
      this.cooldown = cooldown;
      this.seeGlobal = seeGlobal;
      this.costs = costs != null ? new CopyOnWriteArrayList<>(costs) : new CopyOnWriteArrayList<>();
      this.isSecret = isSecret;
      this.members = members != null ? new CopyOnWriteArrayList<>(members) : new CopyOnWriteArrayList<>();
   }

   public static Network createGlobal() {
      return new Network("global", "Global", null, null, 0, true, new CopyOnWriteArrayList<>(), false, new CopyOnWriteArrayList<>());
   }

   public static Network createPersonal(String playerUuid, String playerName) {
      String id = "personal:" + playerUuid;
      return new Network(id, playerName + "'s Network", playerUuid, null, 0, true, new CopyOnWriteArrayList<>(), true, new CopyOnWriteArrayList<>());
   }

   public static boolean isPersonalNetwork(String networkId) {
      return networkId != null && networkId.startsWith("personal:");
   }

   public static String getOwnerFromPersonalId(String networkId) {
      return isPersonalNetwork(networkId) ? networkId.substring("personal:".length()) : null;
   }

   public String getId() {
      return this.id;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public void setDisplayName(String displayName) {
      this.displayName = displayName;
   }

   public String getOwnerUuid() {
      return this.ownerUuid;
   }

   public Boolean getDiscoveryEnabled() {
      return this.discoveryEnabled;
   }

   public boolean isDiscoveryEnabled() {
      return this.discoveryEnabled != null && this.discoveryEnabled;
   }

   public void setDiscoveryEnabled(Boolean discoveryEnabled) {
      this.discoveryEnabled = discoveryEnabled;
   }

   public int getCooldown() {
      return this.cooldown;
   }

   public void setCooldown(int cooldown) {
      this.cooldown = cooldown;
   }

   public boolean canSeeGlobal() {
      return this.seeGlobal;
   }

   public void setSeeGlobal(boolean seeGlobal) {
      this.seeGlobal = seeGlobal;
   }

   public List<PriceItem> getCosts() {
      return new ArrayList<>(this.costs);
   }

   public void setCosts(List<PriceItem> costs) {
      this.costs = costs != null ? new CopyOnWriteArrayList<>(costs) : new CopyOnWriteArrayList<>();
   }

   public boolean isSecret() {
      return this.isSecret != null ? this.isSecret : true;
   }

   public void setSecret(boolean secret) {
      this.isSecret = secret;
   }

   public List<String> getMembers() {
      return this.members != null ? new ArrayList<>(this.members) : new ArrayList<>();
   }

   public void setMembers(List<String> members) {
      this.members = members != null ? new CopyOnWriteArrayList<>(members) : new CopyOnWriteArrayList<>();
   }

   public void addMember(String playerUuid) {
      if (this.members == null) {
         this.members = new CopyOnWriteArrayList<>();
      }

      if (!this.members.contains(playerUuid)) {
         this.members.add(playerUuid);
      }
   }

   public void removeMember(String playerUuid) {
      if (this.members != null) {
         this.members.remove(playerUuid);
      }
   }

   public boolean isMember(String playerUuid) {
      return this.members != null && this.members.contains(playerUuid);
   }

   public boolean isGlobal() {
      return "global".equals(this.id);
   }

   public boolean isPersonal() {
      return isPersonalNetwork(this.id);
   }
}
