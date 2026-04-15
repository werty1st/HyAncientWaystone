package dev.jvnm.plugin.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.jvnm.plugin.permission.PermissionService;
import dev.jvnm.plugin.service.ConfigurationService;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class WaystonePriceClearCommand extends AbstractPlayerCommand {
   private final ConfigurationService configurationService;
   private final PermissionService permissionService;

   public WaystonePriceClearCommand(ConfigurationService configurationService, PermissionService permissionService) {
      super("clear", "Clear all waystone prices.");
      this.configurationService = configurationService;
      this.permissionService = permissionService;
   }

   protected void execute(
      @Nonnull CommandContext commandContext,
      @Nonnull Store<EntityStore> store,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nonnull World world
   ) {
      if (!this.permissionService.hasPermission(playerRef.getUuid(), "hytale.command.waystone.cmd.price")) {
         playerRef.sendMessage(Message.raw("You do not have permission to use this command."));
      } else if (this.configurationService.hasCurrencyPrice()) {
         playerRef.sendMessage(Message.raw("Cannot clear item prices while currency pricing is active. Use /waystone currency remove first."));
      } else {
         this.configurationService.setPriceItems(new ArrayList<>());
         playerRef.sendMessage(Message.raw("Price list cleared. Teleportation is now free."));
      }
   }
}
