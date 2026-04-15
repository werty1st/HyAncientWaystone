package dev.jvnm.plugin.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.HytaleLogger.Api;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.AncientWaystone;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ConsumeWarpberryInteraction extends SimpleInstantInteraction {
   public static final BuilderCodec<ConsumeWarpberryInteraction> CODEC = BuilderCodec.builder(
         ConsumeWarpberryInteraction.class, ConsumeWarpberryInteraction::new, SimpleInstantInteraction.CODEC
      )
      .build();
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   @SuppressWarnings("removal")
   protected void firstRun(
      @NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler
   ) {
      CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
      if (commandBuffer == null) {
         interactionContext.getState().state = InteractionState.Failed;
         ((Api)LOGGER.atInfo()).log("CommandBuffer is null");
      } else {
         Store<EntityStore> store = ((EntityStore)commandBuffer.getExternalData()).getStore();
         Ref<EntityStore> ref = interactionContext.getEntity();
         Player player = (Player)commandBuffer.getComponent(ref, Player.getComponentType());
         if (player == null) {
            interactionContext.getState().state = InteractionState.Failed;
            ((Api)LOGGER.atInfo()).log("Player is null");
         } else {
            ItemStack itemStack = interactionContext.getHeldItem();
            if (itemStack == null) {
               interactionContext.getState().state = InteractionState.Failed;
               ((Api)LOGGER.atInfo()).log("ItemStack is null");
            } else {
               String itemId = itemStack.getItemId();
               Ref<EntityStore> entityPlayerRef = player.getReference();

               assert entityPlayerRef != null;

               PlayerRef playerRef = (PlayerRef)store.getComponent(entityPlayerRef, PlayerRef.getComponentType());

               assert playerRef != null;

               if (itemId.equals("Plant_Fruit_Warpberry")) {
                  AncientWaystone.getWaystoneService().openWaystoneListPage(store, entityPlayerRef, "", var3x -> {
                     Inventory inventory = player.getInventory();
                     short activeSlot = player.getInventory().getActiveHotbarSlot();
                     if (itemStack.getQuantity() > 1) {
                        inventory.getHotbar().setItemStackForSlot(activeSlot, new ItemStack(itemId, itemStack.getQuantity() - 1));
                     } else {
                        inventory.getHotbar().removeItemStackFromSlot(activeSlot);
                     }
                  });
               }
            }
         }
      }
   }
}
