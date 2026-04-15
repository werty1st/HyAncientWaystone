package dev.jvnm.plugin.model;

public class PlayerCooldown {
   private final String playerName;
   private final long lastTeleportTime;

   public PlayerCooldown(String playerName, long lastTeleportTime) {
      this.playerName = playerName;
      this.lastTeleportTime = lastTeleportTime;
   }

   public String getPlayerName() {
      return this.playerName;
   }

   public long getLastTeleportTime() {
      return this.lastTeleportTime;
   }
}
