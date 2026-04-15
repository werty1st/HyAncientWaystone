package dev.jvnm.plugin.service;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import dev.jvnm.plugin.managers.PlayerManager;
import dev.jvnm.plugin.model.Network;
import dev.jvnm.plugin.model.PriceItem;
import dev.jvnm.plugin.model.Waystone;
import dev.jvnm.plugin.pages.WaystoneListPage;
import dev.jvnm.plugin.repository.CooldownRepository;
import dev.jvnm.plugin.repository.WaystoneRepository;
import dev.jvnm.plugin.utils.NameGenerator;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public class WaystoneService {
   private static final String SFX_TELEPORT = "SFX_Portal_Neutral_Teleport_Local";
   private final WaystoneRepository waystoneRepository;
   private final CooldownRepository cooldownRepository;
   private final ConfigurationService configurationService;
   private final FriendService friendService;
   private final DiscoveryService discoveryService;
   private final EconomyService economyService;
   private NetworkService networkService;

   public WaystoneService(
      WaystoneRepository waystoneRepository,
      CooldownRepository cooldownRepository,
      ConfigurationService configurationService,
      FriendService friendService,
      DiscoveryService discoveryService,
      EconomyService economyService
   ) {
      this.waystoneRepository = waystoneRepository;
      this.cooldownRepository = cooldownRepository;
      this.configurationService = configurationService;
      this.friendService = friendService;
      this.discoveryService = discoveryService;
      this.economyService = economyService;
   }

   public void setNetworkService(NetworkService networkService) {
      this.networkService = networkService;
   }

   public Waystone getWaystoneAt(Vector3i pos) {
      return this.waystoneRepository.findByPosition(pos).orElse(null);
   }

   public Waystone getWaystoneByName(String name) {
      return this.waystoneRepository.findByName(name).orElse(null);
   }

   public boolean isNameTaken(String name, Waystone waystoneToExclude) {
      Waystone existing = this.getWaystoneByName(name);
      return existing == null ? false : waystoneToExclude == null || !existing.getPosition().equals(waystoneToExclude.getPosition());
   }

   public Waystone getOrCreate(Vector3i pos, String ownerUuid, boolean isPublic, String worldName) {
      Waystone existing = this.getWaystoneAt(pos);
      if (existing != null) {
         return existing;
      } else {
         String funnyName;
         do {
            funnyName = NameGenerator.generate();
         } while (this.getWaystoneByName(funnyName) != null);

         Waystone newWaystone = new Waystone(pos, funnyName, ownerUuid, isPublic, worldName);
         this.waystoneRepository.add(newWaystone);
         return newWaystone;
      }
   }

   public List<Waystone> getWaystonesByOwner(String ownerUuid) {
      return this.waystoneRepository.findByOwner(ownerUuid);
   }

   public List<Waystone> getAllWaystones(String waystoneToExclude) {
      return this.waystoneRepository.findAll().stream().filter(w -> !w.getName().equalsIgnoreCase(waystoneToExclude)).toList();
   }

   public List<Waystone> getVisibleWaystones(String playerUuid, String waystoneToExclude, boolean isOp) {
      return this.getVisibleWaystones(playerUuid, waystoneToExclude, isOp, null);
   }

   public List<Waystone> getAllVisibleWaystones(String playerUuid, String waystoneToExclude, boolean isOp) {
      boolean globalDiscoveryEnabled = this.configurationService.get("discovery_enabled", false);
      boolean opBypassDiscovery = this.configurationService.get("opbypass_discovery", false);
      return this.waystoneRepository.findAll().stream().filter(w -> !w.getName().equalsIgnoreCase(waystoneToExclude)).filter(w -> {
         if (playerUuid.equals(w.getOwnerUuid())) {
            return true;
         } else {
            boolean baseVisible = false;
            if (w.isPublic()) {
               baseVisible = true;
            } else {
               String ownerUuid = w.getOwnerUuid();
               if (ownerUuid != null && this.friendService.isFriend(ownerUuid, playerUuid)) {
                  baseVisible = true;
               }

               if (isOp) {
                  baseVisible = true;
               }
            }

            if (!baseVisible) {
               return false;
            } else {
               if (this.networkService != null && this.networkService.isEnabled()) {
                  Network network = this.networkService.getNetwork(w.getNetworkId());
                  if (network != null) {
                     if (network.isPersonal()) {
                        if (!isOp && !playerUuid.equals(w.getOwnerUuid())) {
                           return false;
                        }
                     } else if (network.isSecret()) {
                        boolean canSeeNetwork = isOp || playerUuid.equals(network.getOwnerUuid()) || network.isMember(playerUuid);
                        if (!canSeeNetwork) {
                           return false;
                        }
                     }
                  }
               }

               Network waystoneNetwork = this.networkService != null ? this.networkService.getNetwork(w.getNetworkId()) : null;
               boolean isDiscoveryActive = this.getEffectiveDiscoveryEnabled(waystoneNetwork, globalDiscoveryEnabled);
               if (!isDiscoveryActive) {
                  return true;
               } else {
                  return isOp && opBypassDiscovery ? true : this.discoveryService.hasDiscovered(playerUuid, w.getName());
               }
            }
         }
      }).toList();
   }

   public List<Waystone> getVisibleWaystones(String playerUuid, String waystoneToExclude, boolean isOp, Waystone sourceWaystone) {
      boolean globalDiscoveryEnabled = this.configurationService.get("discovery_enabled", false);
      boolean opBypassDiscovery = this.configurationService.get("opbypass_discovery", false);
      boolean networksEnabled = this.networkService != null && this.networkService.isEnabled();
      String sourceNetworkId = sourceWaystone != null ? sourceWaystone.getNetworkId() : "global";
      Network sourceNetwork = networksEnabled && this.networkService != null ? this.networkService.getNetwork(sourceNetworkId) : null;
      boolean effectiveDiscoveryEnabled = this.getEffectiveDiscoveryEnabled(sourceNetwork, globalDiscoveryEnabled);
      return this.waystoneRepository.findAll().stream().filter(w -> !w.getName().equalsIgnoreCase(waystoneToExclude)).filter(w -> {
         if (networksEnabled && sourceNetwork != null) {
            String waystoneNetworkId = w.getNetworkId();
            boolean inSameNetwork = sourceNetworkId.equals(waystoneNetworkId);
            boolean inGlobal = "global".equals(waystoneNetworkId);
            boolean canSeeGlobal = sourceNetwork.canSeeGlobal();
            if (!inSameNetwork && (!inGlobal || !canSeeGlobal)) {
               return false;
            }
         }

         if (playerUuid.equals(w.getOwnerUuid())) {
            return true;
         } else {
            if (this.networkService != null && this.networkService.isEnabled()) {
               Network network = this.networkService.getNetwork(w.getNetworkId());
               if (network != null && !network.isPersonal() && network.isSecret()) {
                  boolean canSeeNetwork = isOp || playerUuid.equals(network.getOwnerUuid()) || network.isMember(playerUuid);
                  if (!canSeeNetwork) {
                     return false;
                  }
               }
            }

            boolean baseVisible = false;
            if (w.isPublic()) {
               baseVisible = true;
            } else {
               String ownerUuid = w.getOwnerUuid();
               if (ownerUuid != null && this.friendService.isFriend(ownerUuid, playerUuid)) {
                  baseVisible = true;
               }

               if (isOp) {
                  baseVisible = true;
               }
            }

            if (!baseVisible) {
               return false;
            } else {
               Network waystoneNetwork = this.networkService != null ? this.networkService.getNetwork(w.getNetworkId()) : null;
               boolean globalDiscovery = this.discoveryService.isEnabled();
               boolean isDiscoveryActive = this.getEffectiveDiscoveryEnabled(waystoneNetwork, globalDiscovery);
               if (!isDiscoveryActive) {
                  return true;
               } else {
                  return isOp && opBypassDiscovery ? true : this.discoveryService.hasDiscovered(playerUuid, w.getName());
               }
            }
         }
      }).toList();
   }

   private boolean getEffectiveDiscoveryEnabled(Network network, boolean globalSetting) {
      return network != null && network.getDiscoveryEnabled() != null ? network.getDiscoveryEnabled() : globalSetting;
   }

   public boolean isVisibleTo(Waystone waystone, String playerUuid) {
      if (waystone.isPublic()) {
         return true;
      } else if (playerUuid.equals(waystone.getOwnerUuid())) {
         return true;
      } else {
         String ownerUuid = waystone.getOwnerUuid();
         return ownerUuid != null && this.friendService.isFriend(ownerUuid, playerUuid);
      }
   }

   public void removeWaystone(Vector3i pos) {
      Waystone target = this.getWaystoneAt(pos);
      if (target != null) {
         this.waystoneRepository.remove(target);
      }
   }

   public void updateWaystoneName(Vector3i pos, String newName) {
      Waystone data = this.getWaystoneAt(pos);
      if (data != null) {
         Waystone updated = data.withName(newName);
         this.waystoneRepository.update(updated);
      }
   }

   public void updateWaystoneVisibility(Vector3i pos, boolean isPublic) {
      Waystone data = this.getWaystoneAt(pos);
      if (data != null) {
         Waystone updated = data.withVisibility(isPublic);
         this.waystoneRepository.update(updated);
      }
   }

   public void updateWaystoneNetwork(Vector3i pos, String networkId) {
      Waystone data = this.getWaystoneAt(pos);
      if (data != null) {
         Waystone updated = data.withNetwork(networkId);
         this.waystoneRepository.update(updated);
      }
   }

   public void updateWaystoneFacingYaw(Vector3i pos, float facingYaw) {
      Waystone data = this.getWaystoneAt(pos);
      if (data != null) {
         Waystone updated = data.withFacingYaw(facingYaw);
         this.waystoneRepository.update(updated);
      }
   }

   public void updateWaystoneCustomTeleportPos(Vector3i pos, Vector3i customTeleportPos) {
      Waystone data = this.getWaystoneAt(pos);
      if (data != null) {
         Waystone updated = data.withCustomTeleportPos(customTeleportPos);
         this.waystoneRepository.update(updated);
      }
   }

   public void warpPlayerToWaystone(Player player, PlayerRef playerRef, Waystone target, Consumer<String> callback) {
      this.warpPlayerToWaystone(player, playerRef, null, target, callback);
   }

   public void warpPlayerToWaystone(Player player, PlayerRef playerRef, Waystone sourceWaystone, Waystone target, Consumer<String> callback) {
      Network network = this.getNetworkForWaystone(sourceWaystone);
      if (this.checkCooldown(player, playerRef, network)) {
         if (this.deductWarpCost(player, playerRef, network)) {
            if (target.hasCustomTeleportPos()) {
               PlayerManager.teleportExact(player, target.getCustomTeleportPos(), target.getWorldName(), target.getFacingYaw());
            } else {
               PlayerManager.teleport(player, target.getPosition(), target.getWorldName(), target.getFacingYaw());
            }

            this.updateCooldown(playerRef, network);
            this.playTeleportEffects(player, playerRef, target.getName(), callback);
         }
      }
   }

   private Network getNetworkForWaystone(Waystone waystone) {
      return this.networkService != null && this.networkService.isEnabled() && waystone != null
         ? this.networkService.getNetwork(waystone.getNetworkId())
         : null;
   }

   private List<PriceItem> getEffectivePriceItems(Network network) {
      return network != null && !network.getCosts().isEmpty() ? network.getCosts() : this.configurationService.getPriceItems();
   }

   private int getEffectiveCooldown(Network network) {
      return network != null && network.getCooldown() > 0 ? network.getCooldown() : this.configurationService.getInt("cooldown");
   }

   private boolean deductWarpCost(Player player, PlayerRef playerRef, Network network) {
      return this.economyService.isAvailable() && this.configurationService.hasCurrencyPrice()
         ? this.deductCurrencyCost(playerRef)
         : this.deductItemCost(player, playerRef, network);
   }

   private boolean deductCurrencyCost(PlayerRef playerRef) {
      BigDecimal price = this.configurationService.getCurrencyPrice();
      String currencyType = this.configurationService.getCurrencyType();
      UUID playerUuid = playerRef.getUuid();
      if (!this.economyService.hasBalance(playerUuid, price, currencyType)) {
         String formatted = this.economyService.format(price, currencyType);
         playerRef.sendMessage(Message.raw("You need " + formatted + " to teleport."));
         return false;
      } else if (!this.economyService.withdraw(playerUuid, price, currencyType)) {
         playerRef.sendMessage(Message.raw("Failed to withdraw currency. Please try again."));
         return false;
      } else {
         return true;
      }
   }

   private boolean deductItemCost(Player player, PlayerRef playerRef, Network network) {
      List<PriceItem> priceItems = this.getEffectivePriceItems(network);
      if (priceItems.isEmpty()) {
         return true;
      } else {
         CombinedItemContainer container = player.getInventory().getCombinedEverything();

         for (PriceItem priceItem : priceItems) {
            ItemStackTransaction tx = container.removeItemStack(new ItemStack(priceItem.getItemId(), priceItem.getAmount()));
            if (!tx.succeeded()) {
               StringBuilder message = new StringBuilder("You need the following items to warp: ");

               for (int i = 0; i < priceItems.size(); i++) {
                  PriceItem item = priceItems.get(i);
                  message.append(item.getAmount()).append("x ").append(item.getItemId());
                  if (i < priceItems.size() - 1) {
                     message.append(", ");
                  }
               }

               playerRef.sendMessage(Message.raw(message.toString()));
               return false;
            }
         }

         return true;
      }
   }

   private boolean checkCooldown(Player player, PlayerRef playerRef, Network network) {
      int cooldownSeconds = this.getEffectiveCooldown(network);
      if (cooldownSeconds <= 0) {
         return true;
      } else {
         long lastTime = this.cooldownRepository.getCooldown(playerRef.getUsername()).orElse(0L);
         long currentTime = System.currentTimeMillis();
         long elapsed = currentTime - lastTime;
         long remaining = cooldownSeconds * 1000L - elapsed;
         if (remaining > 0L) {
            long remainingSeconds = (remaining + 999L) / 1000L;
            playerRef.sendMessage(Message.raw("You must wait " + remainingSeconds + "s before teleporting"));
            return false;
         } else {
            return true;
         }
      }
   }

   private void updateCooldown(PlayerRef playerRef, Network network) {
      int cooldownSeconds = this.getEffectiveCooldown(network);
      if (cooldownSeconds > 0) {
         this.cooldownRepository.setCooldown(playerRef.getUsername(), System.currentTimeMillis());
      }
   }

   public void playTeleportEffects(Player player, PlayerRef playerRef, String waystoneName, Consumer<String> callback) {
      World world = player.getWorld();
      if (world != null) {
         EntityStore entityStore = world.getEntityStore();
         Ref<EntityStore> entityPlayerRef = player.getReference();
         int index = SoundEvent.getAssetMap().getIndex("SFX_Portal_Neutral_Teleport_Local");
         world.execute(
            () -> {
               TransformComponent transform = (TransformComponent)entityStore.getStore()
                  .getComponent(entityPlayerRef, EntityModule.get().getTransformComponentType());
               if (transform != null) {
                  SoundUtil.playSoundEvent3dToPlayer(entityPlayerRef, index, SoundCategory.UI, transform.getPosition(), entityStore.getStore());
                  EventTitleUtil.showEventTitleToPlayer(playerRef, Message.raw(waystoneName), Message.raw("You have been teleported to"), true);
                  callback.accept(waystoneName);
               }
            }
         );
      }
   }

   public void openWaystoneListPage(@Nonnull Store<EntityStore> store, Ref<EntityStore> ref, String currentWaystoneName, Consumer<String> callback) {
      Player player = (Player)store.getComponent(ref, Player.getComponentType());
      PlayerRef playerRef = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType());
      if (player != null && playerRef != null) {
         Ref<EntityStore> entityPlayerRef = player.getReference();
         if (entityPlayerRef != null) {
            String playerUuid = playerRef.getUuid().toString();
            boolean isOp = PermissionsModule.get().getGroupsForUser(playerRef.getUuid()).contains("OP");
            Waystone source = null;
            if (currentWaystoneName != null && !currentWaystoneName.isEmpty()) {
               source = this.getWaystoneByName(currentWaystoneName);
            }

            Network sourceNetwork = this.getNetworkForWaystone(source);
            List<PriceItem> prices = this.getEffectivePriceItems(sourceNetwork);
            BigDecimal currencyPrice = this.configurationService.hasCurrencyPrice() ? this.configurationService.getCurrencyPrice() : null;
            String currencyType = this.configurationService.get("currency_type", "coins");
            player.getPageManager()
               .openCustomPage(
                  entityPlayerRef,
                  store,
                  new WaystoneListPage(
                     playerRef,
                     playerUuid,
                     isOp,
                     currentWaystoneName,
                     this.getAllVisibleWaystones(playerUuid, currentWaystoneName, isOp),
                     prices,
                     currencyPrice,
                     currencyType,
                     this,
                     this.networkService,
                     this.configurationService,
                     posString -> {
                        if (posString != null && !posString.isEmpty()) {
                           try {
                              String[] parts = posString.split(",");
                              if (parts.length != 3) {
                                 return;
                              }

                              int x = Integer.parseInt(parts[0]);
                              int y = Integer.parseInt(parts[1]);
                              int z = Integer.parseInt(parts[2]);
                              Waystone data = this.getWaystoneAt(new Vector3i(x, y, z));
                              if (data != null) {
                                 Waystone warpSource = null;
                                 if (currentWaystoneName != null && !currentWaystoneName.isEmpty()) {
                                    warpSource = this.getWaystoneByName(currentWaystoneName);
                                 }

                                 this.warpPlayerToWaystone(player, playerRef, warpSource, data, callback);
                              }
                           } catch (NumberFormatException var12x) {
                           }
                        }
                     }
                  )
               );
         }
      }
   }

   public ConfigurationService getConfigurationService() {
      return this.configurationService;
   }
}
