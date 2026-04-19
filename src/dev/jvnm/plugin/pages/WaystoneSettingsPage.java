package dev.jvnm.plugin.pages;

import org.joml.Vector3i;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.model.Network;
import dev.jvnm.plugin.model.Waystone;
import dev.jvnm.plugin.service.ConfigurationService;
import dev.jvnm.plugin.service.NetworkService;
import dev.jvnm.plugin.service.WaystoneService;
import java.util.List;
import javax.annotation.Nonnull;

@SuppressWarnings("removal")
public class WaystoneSettingsPage extends InteractiveCustomUIPage<WaystoneSettingsPage.WaystoneSettingsEventData> {
   private static final String EVENT_VISIBILITY = "@VisibilityInput";
   private static final String EVENT_NETWORK = "@NetworkInput";
   private static final String EVENT_ORIENTATION = "@OrientationInput";
   private static final String EVENT_NAME = "@NameInput";
   private static final String EVENT_CUSTOM_X = "@CustomX";
   private static final String EVENT_CUSTOM_Y = "@CustomY";
   private static final String EVENT_CUSTOM_Z = "@CustomZ";
   private static final String EVENT_SAVE = "@Save";
   private static final String EVENT_CANCEL = "Cancel";
   private static final String VALUE_PUBLIC = "public";
   private static final String VALUE_PRIVATE = "private";
   private static final String VALUE_NORTH = "0";
   private static final String VALUE_EAST = "270";
   private static final String VALUE_SOUTH = "180";
   private static final String VALUE_WEST = "90";
   private final Waystone waystone;
   private final WaystoneService waystoneService;
   private final NetworkService networkService;
   private final ConfigurationService configurationService;
   private String pendingVisibility;
   private String pendingNetworkId;
   private String pendingName;
   private String pendingOrientation;
   private String pendingCustomX;
   private String pendingCustomY;
   private String pendingCustomZ;
   private List<Network> availableNetworks;

   public WaystoneSettingsPage(
      @Nonnull PlayerRef playerRef,
      Waystone waystone,
      WaystoneService waystoneService,
      NetworkService networkService,
      ConfigurationService configurationService,
      boolean isOp
   ) {
      super(playerRef, CustomPageLifetime.CanDismiss, WaystoneSettingsPage.WaystoneSettingsEventData.CODEC);
      this.waystone = waystone;
      this.waystoneService = waystoneService;
      this.networkService = networkService;
      this.configurationService = configurationService;
      this.pendingVisibility = waystone.isPublic() ? "public" : "private";
      this.pendingNetworkId = waystone.getNetworkId();
      this.pendingName = waystone.getName();
      this.pendingOrientation = String.valueOf((int)waystone.getFacingYaw());
      if (waystone.hasCustomTeleportPos()) {
         Vector3i pos = waystone.getCustomTeleportPos();
         this.pendingCustomX = String.valueOf(pos.x());
         this.pendingCustomY = String.valueOf(pos.y());
         this.pendingCustomZ = String.valueOf(pos.z());
      } else {
         this.pendingCustomX = "";
         this.pendingCustomY = "";
         this.pendingCustomZ = "";
      }

      if (networkService != null && networkService.isEnabled()) {
         this.availableNetworks = networkService.getNetworksForPlayer(playerRef.getUuid().toString(), isOp);
      } else {
         this.availableNetworks = List.of();
      }
   }

   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("WaystoneSettingsPage.ui");
      commandBuilder.set("#NameInput.Value", this.pendingName);
      eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#NameInput", EventData.of("@NameInput", "#NameInput.Value"));
      DropdownEntryInfo[] visibilityEntries = new DropdownEntryInfo[]{
         new DropdownEntryInfo(LocalizableString.fromString("Public"), "public"), new DropdownEntryInfo(LocalizableString.fromString("Private"), "private")
      };
      commandBuilder.set("#Visibility #Input.Entries", visibilityEntries);
      commandBuilder.set("#Visibility #Input.Value", this.pendingVisibility);
      eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#Visibility #Input", EventData.of("@VisibilityInput", "#Visibility #Input.Value"));
      if (this.networkService != null && this.networkService.isEnabled() && !this.availableNetworks.isEmpty()) {
         commandBuilder.set("#NetworkSection.Visible", true);
         DropdownEntryInfo[] networkEntries = new DropdownEntryInfo[this.availableNetworks.size()];

         for (int i = 0; i < this.availableNetworks.size(); i++) {
            Network network = this.availableNetworks.get(i);
            networkEntries[i] = new DropdownEntryInfo(LocalizableString.fromString(network.getDisplayName()), network.getId());
         }

         commandBuilder.set("#NetworkSection #NetworkInput.Entries", networkEntries);
         commandBuilder.set("#NetworkSection #NetworkInput.Value", this.pendingNetworkId);
         eventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged, "#NetworkSection #NetworkInput", EventData.of("@NetworkInput", "#NetworkSection #NetworkInput.Value")
         );
      }

      DropdownEntryInfo[] orientationEntries = new DropdownEntryInfo[]{
         new DropdownEntryInfo(LocalizableString.fromString("South"), "180"),
         new DropdownEntryInfo(LocalizableString.fromString("West"), "90"),
         new DropdownEntryInfo(LocalizableString.fromString("North"), "0"),
         new DropdownEntryInfo(LocalizableString.fromString("East"), "270")
      };
      commandBuilder.set("#OrientationSection #OrientationInput.Entries", orientationEntries);
      commandBuilder.set("#OrientationSection #OrientationInput.Value", this.pendingOrientation);
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.ValueChanged,
         "#OrientationSection #OrientationInput",
         EventData.of("@OrientationInput", "#OrientationSection #OrientationInput.Value")
      );
      boolean customTeleportEnabled = this.configurationService != null && this.configurationService.get("custom_teleport_enabled", false);
      if (customTeleportEnabled) {
         commandBuilder.set("#CustomTeleportSection.Visible", true);
         commandBuilder.set("#CustomX.Value", this.pendingCustomX);
         commandBuilder.set("#CustomY.Value", this.pendingCustomY);
         commandBuilder.set("#CustomZ.Value", this.pendingCustomZ);
         eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#CustomX", EventData.of("@CustomX", "#CustomX.Value"));
         eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#CustomY", EventData.of("@CustomY", "#CustomY.Value"));
         eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#CustomZ", EventData.of("@CustomZ", "#CustomZ.Value"));
      }

      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SaveButton", EventData.of("@Save", "#NameInput.Value"));
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CancelButton", EventData.of("Cancel", "cancel"));
   }

   public void handleDataEvent(
      @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull WaystoneSettingsPage.WaystoneSettingsEventData eventData
   ) {
      Player player = (Player)store.getComponent(ref, Player.getComponentType());
      if (player != null) {
         if (eventData.getName() != null) {
            this.pendingName = eventData.getName();
            this.sendUpdate(null, false);
         } else if (eventData.getVisibility() != null) {
            this.pendingVisibility = eventData.getVisibility();
            this.sendUpdate(null, false);
         } else if (eventData.getNetwork() != null) {
            this.pendingNetworkId = eventData.getNetwork();
            this.sendUpdate(null, false);
         } else if (eventData.getOrientation() != null) {
            this.pendingOrientation = eventData.getOrientation();
            this.sendUpdate(null, false);
         } else if (eventData.getCustomX() != null) {
            this.pendingCustomX = eventData.getCustomX();
            this.sendUpdate(null, false);
         } else if (eventData.getCustomY() != null) {
            this.pendingCustomY = eventData.getCustomY();
            this.sendUpdate(null, false);
         } else if (eventData.getCustomZ() != null) {
            this.pendingCustomZ = eventData.getCustomZ();
            this.sendUpdate(null, false);
         } else {
            if (eventData.getSave() != null) {
               boolean isOp = PermissionsModule.get().getGroupsForUser(this.playerRef.getUuid()).contains("OP");
               boolean isOwner = player.getUuid().toString().equals(this.waystone.getOwnerUuid());
               if (!isOp && !isOwner) {
                  this.playerRef.sendMessage(Message.raw("You do not have permission to edit this waystone."));
                  return;
               }

               String newName = eventData.getSave();
               if (newName == null || newName.trim().isEmpty()) {
                  newName = this.pendingName;
               }

               if (newName == null || newName.trim().isEmpty()) {
                  newName = this.waystone.getName();
               }

               if (this.waystoneService.isNameTaken(newName.trim(), this.waystone)) {
                  UICommandBuilder commandBuilder = new UICommandBuilder();
                  commandBuilder.set("#Error.Visible", true);
                  commandBuilder.set("#Error.Text", "Name already taken!");
                  this.sendUpdate(commandBuilder, new UIEventBuilder(), false);
                  return;
               }

               boolean isPublic = "public".equals(this.pendingVisibility);
               this.waystoneService.updateWaystoneName(this.waystone.getPosition(), newName.trim());
               this.waystoneService.updateWaystoneVisibility(this.waystone.getPosition(), isPublic);
               if (this.networkService != null
                  && this.networkService.isEnabled()
                  && this.pendingNetworkId != null
                  && !this.pendingNetworkId.equals(this.waystone.getNetworkId())) {
                  boolean validNetwork = this.availableNetworks.stream().anyMatch(n -> n.getId().equals(this.pendingNetworkId));
                  if (validNetwork) {
                     this.waystoneService.updateWaystoneNetwork(this.waystone.getPosition(), this.pendingNetworkId);
                  }
               }

               try {
                  float newFacingYaw = Float.parseFloat(this.pendingOrientation);
                  this.waystoneService.updateWaystoneFacingYaw(this.waystone.getPosition(), newFacingYaw);
               } catch (NumberFormatException var10) {
               }

               Vector3i customPos = this.parseCustomPosition();
               this.waystoneService.updateWaystoneCustomTeleportPos(this.waystone.getPosition(), customPos);
               player.getPageManager().setPage(ref, store, Page.None);
            } else if (eventData.getCancel() != null) {
               player.getPageManager().setPage(ref, store, Page.None);
            }
         }
      }
   }

   private Vector3i parseCustomPosition() {
      if (this.pendingCustomX != null
         && !this.pendingCustomX.trim().isEmpty()
         && this.pendingCustomY != null
         && !this.pendingCustomY.trim().isEmpty()
         && this.pendingCustomZ != null
         && !this.pendingCustomZ.trim().isEmpty()) {
         try {
            int x = Integer.parseInt(this.pendingCustomX.trim());
            int y = Integer.parseInt(this.pendingCustomY.trim());
            int z = Integer.parseInt(this.pendingCustomZ.trim());
            return new Vector3i(x, y, z);
         } catch (NumberFormatException var4) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static class WaystoneSettingsEventData {
      @Nonnull
      public static final BuilderCodec<WaystoneSettingsPage.WaystoneSettingsEventData> CODEC = BuilderCodec
              .builder(WaystoneSettingsPage.WaystoneSettingsEventData.class, WaystoneSettingsPage.WaystoneSettingsEventData::new)
              .append(new KeyedCodec("@NameInput",        Codec.STRING), (entry, s) -> ((WaystoneSettingsEventData) entry).name        = (String) s, entry -> ((WaystoneSettingsEventData) entry).name)        .add()
              .append(new KeyedCodec("@VisibilityInput",  Codec.STRING), (entry, s) -> ((WaystoneSettingsEventData) entry).visibility  = (String) s, entry -> ((WaystoneSettingsEventData) entry).visibility)  .add()
              .append(new KeyedCodec("@NetworkInput",     Codec.STRING), (entry, s) -> ((WaystoneSettingsEventData) entry).network     = (String) s, entry -> ((WaystoneSettingsEventData) entry).network)     .add()
              .append(new KeyedCodec("@OrientationInput", Codec.STRING), (entry, s) -> ((WaystoneSettingsEventData) entry).orientation = (String) s, entry -> ((WaystoneSettingsEventData) entry).orientation) .add()
              .append(new KeyedCodec("@CustomX",          Codec.STRING), (entry, s) -> ((WaystoneSettingsEventData) entry).customX     = (String) s, entry -> ((WaystoneSettingsEventData) entry).customX)     .add()
              .append(new KeyedCodec("@CustomY",          Codec.STRING), (entry, s) -> ((WaystoneSettingsEventData) entry).customY     = (String) s, entry -> ((WaystoneSettingsEventData) entry).customY)     .add()
              .append(new KeyedCodec("@CustomZ",          Codec.STRING), (entry, s) -> ((WaystoneSettingsEventData) entry).customZ     = (String) s, entry -> ((WaystoneSettingsEventData) entry).customZ)     .add()
              .append(new KeyedCodec("@Save",             Codec.STRING), (entry, s) -> ((WaystoneSettingsEventData) entry).save        = (String) s, entry -> ((WaystoneSettingsEventData) entry).save)        .add()
              .append(new KeyedCodec("Cancel",            Codec.STRING), (entry, s) -> ((WaystoneSettingsEventData) entry).cancel      = (String) s, entry -> ((WaystoneSettingsEventData) entry).cancel)      .add()
              .build();
      private String name;
      private String visibility;
      private String network;
      private String orientation;
      private String customX;
      private String customY;
      private String customZ;
      private String save;
      private String cancel;

      public String getName() {
         return this.name;
      }

      public String getVisibility() {
         return this.visibility;
      }

      public String getNetwork() {
         return this.network;
      }

      public String getOrientation() {
         return this.orientation;
      }

      public String getCustomX() {
         return this.customX;
      }

      public String getCustomY() {
         return this.customY;
      }

      public String getCustomZ() {
         return this.customZ;
      }

      public String getSave() {
         return this.save;
      }

      public String getCancel() {
         return this.cancel;
      }
   }
}
