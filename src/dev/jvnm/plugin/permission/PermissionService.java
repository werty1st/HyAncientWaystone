package dev.jvnm.plugin.permission;

import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import dev.jvnm.plugin.service.ConfigurationService;
import java.util.Set;
import java.util.UUID;

public class PermissionService {
   private final ConfigurationService configurationService;

   public PermissionService(ConfigurationService configurationService) {
      this.configurationService = configurationService;
   }

   public boolean isEnabled() {
      return this.configurationService.get("permission_enabled", false);
   }

   public void setEnabled(boolean enabled) {
      this.configurationService.set("permission_enabled", enabled);
   }

   public boolean hasPermission(UUID uuid, String permission) {
      if (!this.isEnabled()) {
         return true;
      } else {
         for (PermissionProvider provider : PermissionsModule.get().getProviders()) {
            if (provider.getUserPermissions(uuid).contains(permission)) {
               return true;
            }

            for (String group : provider.getGroupsForUser(uuid)) {
               Set<String> groupPerms = provider.getGroupPermissions(group);
               if (groupPerms.contains(permission) || groupPerms.contains("hytale.command.waystone.*")) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public boolean hasPermission(String uuidString, String permission) {
      try {
         return this.hasPermission(UUID.fromString(uuidString), permission);
      } catch (IllegalArgumentException var4) {
         return !this.isEnabled();
      }
   }

   public boolean isOp(UUID uuid) {
      return PermissionsModule.get().getGroupsForUser(uuid).contains("OP");
   }
}
