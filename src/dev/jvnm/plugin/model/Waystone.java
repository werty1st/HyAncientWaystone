package dev.jvnm.plugin.model;


import org.joml.Vector3i;
import javax.annotation.Nullable;

public class Waystone {
   private final Vector3i position;
   private final String name;
   private final String ownerUuid;
   private final boolean isPublic;
   private final String worldName;
   private final String networkId;
   private final float facingYaw;
   @Nullable
   private final Vector3i customTeleportPos;

   public Waystone(Vector3i position, String name, String ownerUuid, boolean isPublic) {
      this(position, name, ownerUuid, isPublic, "default", "global", 0.0F, null);
   }

   public Waystone(Vector3i position, String name, String ownerUuid, boolean isPublic, String worldName) {
      this(position, name, ownerUuid, isPublic, worldName, "global", 0.0F, null);
   }

   public Waystone(Vector3i position, String name, String ownerUuid, boolean isPublic, String worldName, String networkId) {
      this(position, name, ownerUuid, isPublic, worldName, networkId, 0.0F, null);
   }

   public Waystone(Vector3i position, String name, String ownerUuid, boolean isPublic, String worldName, String networkId, float facingYaw) {
      this(position, name, ownerUuid, isPublic, worldName, networkId, facingYaw, null);
   }

   public Waystone(
      Vector3i position, String name, String ownerUuid, boolean isPublic, String worldName, String networkId, float facingYaw, Vector3i customTeleportPos
   ) {
      this.position = position;
      this.name = name;
      this.ownerUuid = ownerUuid;
      this.isPublic = isPublic;
      this.worldName = worldName != null ? worldName : "default";
      this.networkId = networkId != null ? networkId : "global";
      this.facingYaw = facingYaw;
      this.customTeleportPos = customTeleportPos;
   }

   public Vector3i getPosition() {
      return this.position;
   }

   public String getName() {
      return this.name;
   }

   public String getOwnerUuid() {
      return this.ownerUuid;
   }

   public boolean isPublic() {
      return this.isPublic;
   }

   public String getWorldName() {
      return this.worldName;
   }

   public String getNetworkId() {
      return this.networkId;
   }

   public float getFacingYaw() {
      return this.facingYaw;
   }

   public Vector3i getCustomTeleportPos() {
      return this.customTeleportPos;
   }

   public boolean hasCustomTeleportPos() {
      return this.customTeleportPos != null;
   }

   public Waystone withNetwork(String newNetworkId) {
      return new Waystone(this.position, this.name, this.ownerUuid, this.isPublic, this.worldName, newNetworkId, this.facingYaw, this.customTeleportPos);
   }

   public Waystone withName(String newName) {
      return new Waystone(this.position, newName, this.ownerUuid, this.isPublic, this.worldName, this.networkId, this.facingYaw, this.customTeleportPos);
   }

   public Waystone withVisibility(boolean newIsPublic) {
      return new Waystone(this.position, this.name, this.ownerUuid, newIsPublic, this.worldName, this.networkId, this.facingYaw, this.customTeleportPos);
   }

   public Waystone withFacingYaw(float newFacingYaw) {
      return new Waystone(this.position, this.name, this.ownerUuid, this.isPublic, this.worldName, this.networkId, newFacingYaw, this.customTeleportPos);
   }

   public Waystone withCustomTeleportPos(Vector3i newCustomTeleportPos) {
      return new Waystone(this.position, this.name, this.ownerUuid, this.isPublic, this.worldName, this.networkId, this.facingYaw, newCustomTeleportPos);
   }
}
