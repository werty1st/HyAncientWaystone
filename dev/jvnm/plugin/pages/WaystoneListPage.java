package dev.jvnm.plugin.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec.Builder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.model.Network;
import dev.jvnm.plugin.model.PriceItem;
import dev.jvnm.plugin.model.Waystone;
import dev.jvnm.plugin.service.ConfigurationService;
import dev.jvnm.plugin.service.NetworkService;
import dev.jvnm.plugin.service.WaystoneService;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WaystoneListPage extends InteractiveCustomUIPage<WaystoneListPage.WaystoneListPageEventData> {
   private static final String UI_PAGE_FRAME = "WaystoneListPage.ui";
   private static final String UI_PRICE_CONTAINER = "WaystoneListPriceContainer.ui";
   private static final String UI_WARP_LIST_CONTAINER = "WaystoneListWarpList.ui";
   private static final String UI_PRICE = "WaystoneListPagePrice.ui";
   private static final String UI_PRICE_CURRENCY = "WaystoneListPriceCurrency.ui";
   private static final String UI_CURRENT = "WaystoneListPageCurrent.ui";
   private static final String UI_ENTRY_BUTTON = "WaystoneEntryButton.ui";
   private static final String ID_PRICE_CONTAINER = "#PriceContainer";
   private static final String ID_WARP_LIST = "#WarpList";
   private static final String ID_PRICE_TEXT = "#PriceText.Text";
   private static final String ID_ITEM_ICON = "#ItemIcon.ItemId";
   private static final String ID_QUANTITY_LABEL = "#QuantityLabel.Text";
   private static final String ID_CURRENT_TEXT = "#Current.Text";
   private static final String ID_NAME_TEXT = " #Name.Text";
   private static final String ID_WORLD_TEXT = " #World.Text";
   private static final String EVENT_WARP = "Warp";
   private static final String EVENT_VISIBILITY_FILTER = "@VisibilityFilter";
   private static final String EVENT_NETWORK_FILTER = "@NetworkFilter";
   private static final String EVENT_EDIT = "Edit";
   private static final String FILTER_ALL = "all";
   private static final String FILTER_PUBLIC = "public";
   private static final String FILTER_PRIVATE = "private";
   private static final String NETWORK_ALL = "all";
   @Nonnull
   private final Consumer<String> callback;
   private final List<Waystone> waystones;
   private final List<PriceItem> priceItems;
   private final String playerUuid;
   private final boolean isOp;
   private final WaystoneService waystoneService;
   private final NetworkService networkService;
   private final ConfigurationService configurationService;
   @Nonnull
   private final String currentWaystoneName;
   @Nullable
   private final BigDecimal currencyPrice;
   @Nullable
   private final String currencyType;
   private String visibilityFilter = "all";
   private String networkFilter = "all";
   private List<Network> availableNetworks;

   public WaystoneListPage(
      @Nonnull PlayerRef playerRef,
      String playerUuid,
      boolean isOp,
      String currentWaystoneName,
      List<Waystone> waystones,
      List<PriceItem> priceItems,
      @Nullable BigDecimal currencyPrice,
      @Nullable String currencyType,
      WaystoneService waystoneService,
      NetworkService networkService,
      ConfigurationService configurationService,
      Consumer<String> callback
   ) {
      super(playerRef, CustomPageLifetime.CanDismiss, WaystoneListPage.WaystoneListPageEventData.CODEC);
      this.callback = callback;
      this.playerUuid = playerUuid;
      this.isOp = isOp;
      this.waystones = waystones;
      this.priceItems = priceItems;
      this.currencyPrice = currencyPrice;
      this.currencyType = currencyType;
      this.waystoneService = waystoneService;
      this.networkService = networkService;
      this.configurationService = configurationService;
      this.currentWaystoneName = currentWaystoneName != null ? currentWaystoneName : "";
      if (networkService != null && networkService.isEnabled()) {
         this.availableNetworks = networkService.getNetworksForPlayer(playerUuid, isOp);
      } else {
         this.availableNetworks = List.of();
      }
   }

   private boolean canEdit(Waystone waystone) {
      return this.isOp || this.playerUuid.equals(waystone.getOwnerUuid());
   }

   private void buildWarpList(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
      commandBuilder.clear("#WarpList");
      ObjectArrayList<Waystone> displayList = new ObjectArrayList(this.waystones);
      String i = this.visibilityFilter;
      switch (i) {
         case "public":
            displayList.removeIf(w -> !w.isPublic());
            break;
         case "private":
            displayList.removeIf(w -> w.isPublic());
         case "all":
      }

      if (this.networkService != null && this.networkService.isEnabled() && !"all".equals(this.networkFilter)) {
         displayList.removeIf(w -> !this.networkFilter.equals(w.getNetworkId()));
      }

      if (displayList.isEmpty()) {
         commandBuilder.set("#NoWaystones.Visible", true);
      } else {
         commandBuilder.set("#NoWaystones.Visible", false);
         displayList.sort(Comparator.comparing(Waystone::getName));

         for (int i = 0; i < displayList.size(); i++) {
            String selector = "#WarpList[" + i + "]";
            Waystone data = (Waystone)displayList.get(i);
            commandBuilder.append("#WarpList", "WaystoneEntryButton.ui");
            commandBuilder.set(selector + " #Name.Text", data.getName());
            String coords = data.getPosition().x + ", " + data.getPosition().y + ", " + data.getPosition().z;
            commandBuilder.set(selector + " #World.Text", coords);
            String posString = data.getPosition().x + "," + data.getPosition().y + "," + data.getPosition().z;
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, selector + " #Button", EventData.of("Warp", posString), false);
            if (this.canEdit(data)) {
               commandBuilder.set(selector + " #EditButton.Visible", true);
               eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, selector + " #EditButton", EventData.of("Edit", posString), false);
            }
         }
      }
   }

   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("WaystoneListPage.ui");
      DropdownEntryInfo[] visibilityEntries = new DropdownEntryInfo[]{
         new DropdownEntryInfo(LocalizableString.fromString("All"), "all"),
         new DropdownEntryInfo(LocalizableString.fromString("Public"), "public"),
         new DropdownEntryInfo(LocalizableString.fromString("Private"), "private")
      };
      commandBuilder.set("#VisibilityFilter #VisibilityInput.Entries", visibilityEntries);
      commandBuilder.set("#VisibilityFilter #VisibilityInput.Value", this.visibilityFilter);
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.ValueChanged,
         "#VisibilityFilter #VisibilityInput",
         EventData.of("@VisibilityFilter", "#VisibilityFilter #VisibilityInput.Value")
      );
      if (this.networkService != null && this.networkService.isEnabled() && !this.availableNetworks.isEmpty()) {
         commandBuilder.set("#NetworkFilter.Visible", true);
         DropdownEntryInfo[] networkEntries = new DropdownEntryInfo[this.availableNetworks.size() + 1];
         networkEntries[0] = new DropdownEntryInfo(LocalizableString.fromString("All Networks"), "all");

         for (int i = 0; i < this.availableNetworks.size(); i++) {
            Network network = this.availableNetworks.get(i);
            networkEntries[i + 1] = new DropdownEntryInfo(LocalizableString.fromString(network.getDisplayName()), network.getId());
         }

         commandBuilder.set("#NetworkFilter #NetworkInput.Entries", networkEntries);
         commandBuilder.set("#NetworkFilter #NetworkInput.Value", this.networkFilter);
         eventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged, "#NetworkFilter #NetworkInput", EventData.of("@NetworkFilter", "#NetworkFilter #NetworkInput.Value")
         );
      }

      commandBuilder.append("#ListContainer", "WaystoneListWarpList.ui");
      this.buildWarpList(commandBuilder, eventBuilder);
      if (this.currencyPrice != null && this.currencyPrice.compareTo(BigDecimal.ZERO) > 0) {
         commandBuilder.append("WaystoneListPriceContainer.ui");
         commandBuilder.append("#PriceContainer", "WaystoneListPriceCurrency.ui");
         String currencyLabel = this.currencyType != null && !this.currencyType.isEmpty() ? this.currencyType : "coins";
         String formattedPrice = String.format("%.2f %s", this.currencyPrice, currencyLabel);
         commandBuilder.set("#PriceContainer #PriceText.Text", formattedPrice);
      } else if (!this.priceItems.isEmpty()) {
         commandBuilder.append("WaystoneListPriceContainer.ui");

         for (int i = 0; i < this.priceItems.size(); i++) {
            PriceItem priceItem = this.priceItems.get(i);
            commandBuilder.append("#PriceContainer", "WaystoneListPagePrice.ui");
            Item item = (Item)Item.getAssetMap().getAsset(priceItem.getItemId());
            if (item != null) {
               String selector = "#PriceContainer[" + i + "]";
               commandBuilder.set(selector + " #Price.Text", Message.translation(item.getTranslationKey()));
               commandBuilder.set(selector + " #ItemIcon.ItemId", priceItem.getItemId());
               commandBuilder.set(selector + " #QuantityLabel.Text", String.valueOf(priceItem.getAmount()));
            }
         }
      }

      if (!this.currentWaystoneName.isEmpty()) {
         commandBuilder.append("WaystoneListPageCurrent.ui");
         commandBuilder.set("#Current.Text", this.currentWaystoneName);
         Waystone currentWaystone = this.waystones.stream().filter(w -> w.getName().equals(this.currentWaystoneName)).findFirst().orElse(null);
         if (currentWaystone == null) {
            currentWaystone = this.waystoneService.getWaystoneByName(this.currentWaystoneName);
         }

         if (currentWaystone != null && this.canEdit(currentWaystone)) {
            commandBuilder.set("#EditCurrentButton.Visible", true);
            String posString = currentWaystone.getPosition().x + "," + currentWaystone.getPosition().y + "," + currentWaystone.getPosition().z;
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#EditCurrentButton", EventData.of("Edit", posString));
         }
      }
   }

   private Waystone getWaystoneFromPosString(String posString) {
      if (posString != null && !posString.isEmpty()) {
         try {
            String[] parts = posString.split(",");
            if (parts.length != 3) {
               return null;
            } else {
               int x = Integer.parseInt(parts[0]);
               int y = Integer.parseInt(parts[1]);
               int z = Integer.parseInt(parts[2]);
               return this.waystoneService.getWaystoneAt(new Vector3i(x, y, z));
            }
         } catch (Exception var6) {
            return null;
         }
      } else {
         return null;
      }
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull WaystoneListPage.WaystoneListPageEventData eventData) {
      Player player = (Player)store.getComponent(ref, Player.getComponentType());
      if (player != null) {
         if (eventData.getWarp() != null) {
            player.getPageManager().setPage(ref, store, Page.None);
            this.callback.accept(eventData.getWarp());
         } else if (eventData.getEdit() != null) {
            Waystone waystone = this.getWaystoneFromPosString(eventData.getEdit());
            if (waystone != null) {
               if (!this.canEdit(waystone)) {
                  return;
               }

               PlayerRef playerRef = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType());
               Ref<EntityStore> entityPlayerRef = player.getReference();
               if (playerRef != null && entityPlayerRef != null) {
                  player.getPageManager()
                     .openCustomPage(
                        entityPlayerRef,
                        store,
                        new WaystoneSettingsPage(playerRef, waystone, this.waystoneService, this.networkService, this.configurationService, this.isOp)
                     );
               }
            }
         } else if (eventData.getVisibilityFilter() != null) {
            this.visibilityFilter = eventData.getVisibilityFilter();
            this.rebuildList();
         } else if (eventData.getNetworkFilter() != null) {
            this.networkFilter = eventData.getNetworkFilter();
            this.rebuildList();
         }
      }
   }

   private void rebuildList() {
      UICommandBuilder commandBuilder = new UICommandBuilder();
      UIEventBuilder eventBuilder = new UIEventBuilder();
      this.buildWarpList(commandBuilder, eventBuilder);
      this.sendUpdate(commandBuilder, eventBuilder, false);
   }

   public static class WaystoneListPageEventData {
      @Nonnull
      public static final BuilderCodec<WaystoneListPage.WaystoneListPageEventData> CODEC = ((Builder)((Builder)((Builder)((Builder)BuilderCodec.builder(
                        WaystoneListPage.WaystoneListPageEventData.class, WaystoneListPage.WaystoneListPageEventData::new
                     )
                     .append(new KeyedCodec("Warp", Codec.STRING), (entry, s) -> entry.warp = s, entry -> entry.warp)
                     .add())
                  .append(new KeyedCodec("@VisibilityFilter", Codec.STRING), (entry, s) -> entry.visibilityFilter = s, entry -> entry.visibilityFilter)
                  .add())
               .append(new KeyedCodec("@NetworkFilter", Codec.STRING), (entry, s) -> entry.networkFilter = s, entry -> entry.networkFilter)
               .add())
            .append(new KeyedCodec("Edit", Codec.STRING), (entry, s) -> entry.edit = s, entry -> entry.edit)
            .add())
         .build();
      private String warp;
      private String visibilityFilter;
      private String networkFilter;
      private String edit;

      public String getWarp() {
         return this.warp;
      }

      public String getVisibilityFilter() {
         return this.visibilityFilter;
      }

      public String getNetworkFilter() {
         return this.networkFilter;
      }

      public String getEdit() {
         return this.edit;
      }
   }
}
