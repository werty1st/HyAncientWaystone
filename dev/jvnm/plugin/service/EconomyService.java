package dev.jvnm.plugin.service;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.HytaleLogger.Api;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

public class EconomyService {
   private static HytaleLogger loggerInstance;
   private Object economyProvider;
   private boolean available = false;
   private boolean hookAttempted;

   private static HytaleLogger getLogger() {
      if (loggerInstance == null) {
         try {
            loggerInstance = HytaleLogger.forEnclosingClass();
         } catch (Exception var1) {
            loggerInstance = null;
         }
      }

      return loggerInstance;
   }

   private static void log(String message) {
      HytaleLogger l = getLogger();
      if (l != null) {
         ((Api)l.atInfo()).log(message);
      }
   }

   private static void log(String format, Object... args) {
      HytaleLogger l = getLogger();
      if (l != null) {
         ((Api)l.atInfo()).log(format, args);
      }
   }

   private static void logWarning(String format, Exception e, Object... args) {
      HytaleLogger l = getLogger();
      if (l != null) {
         ((Api)((Api)l.atWarning()).withCause(e)).log(format, args);
      }
   }

   private static Object unwrapOptional(Object obj) {
      if (obj == null) {
         return null;
      } else if (obj.getClass().getName().equals("java.util.Optional")) {
         try {
            Boolean isPresent = (Boolean)obj.getClass().getMethod("isPresent").invoke(obj);
            return isPresent ? obj.getClass().getMethod("get").invoke(obj) : null;
         } catch (Exception var2) {
            log("Failed to unwrap Optional: %s", var2.getMessage());
            return null;
         }
      } else {
         return obj;
      }
   }

   public EconomyService() {
      this.economyProvider = null;
      this.hookAttempted = false;
      log("EconomyService created - VaultUnlocked hook will be attempted on first use");
   }

   private void ensureHooked() {
      if (!this.hookAttempted) {
         this.hookAttempted = true;
         this.tryHookVaultUnlocked();
      }
   }

   private void logEconomyMethods(Object economy) {
      StringBuilder economyMethods = new StringBuilder("Economy object methods: ");

      for (Method m : economy.getClass().getMethods()) {
         economyMethods.append(m.getName());
         economyMethods.append("(");
         Class<?>[] params = m.getParameterTypes();

         for (int i = 0; i < params.length; i++) {
            if (i > 0) {
               economyMethods.append(",");
            }

            economyMethods.append(params[i].getSimpleName());
         }

         economyMethods.append("), ");
      }

      log(economyMethods.toString());
      log("Economy object class: %s", economy.getClass().getName());
   }

   private void tryHookVaultUnlocked() {
      log("Attempting to hook VaultUnlocked economy...");
      String[] possibleVaultClasses = new String[]{
         "net.cfh.vault.VaultUnlocked",
         "net.milkbowl.vault2.VaultUnlocked",
         "net.milkbowl.vault2.Vault",
         "net.milkbowl.vault2.VaultUnlocked2",
         "net.milkbowl.vault.Vault",
         "net.milkbowl.vault.VaultUnlocked",
         "net.tnemc.vaultunlocked.VaultUnlocked",
         "net.tnemc.vault.VaultUnlocked"
      };
      String[] possibleEconomyClasses = new String[]{
         "net.milkbowl.vault2.economy.Economy",
         "net.milkbowl.vault.economy.Economy",
         "net.tnemc.vaultunlocked.economy.Economy",
         "net.tnemc.vault.economy.Economy"
      };
      Class<?> economyClass = null;

      for (String className : possibleEconomyClasses) {
         try {
            economyClass = Class.forName(className);
            log("Found Economy class: %s", className);
            break;
         } catch (ClassNotFoundException var41) {
         }
      }

      if (economyClass == null) {
         log("No VaultUnlocked Economy class found - economy integration disabled.");
      } else {
         for (String vaultClassName : possibleVaultClasses) {
            try {
               Class<?> vaultUnlockedClass = Class.forName(vaultClassName);
               log("Found VaultUnlocked class: %s", vaultClassName);
               StringBuilder methods = new StringBuilder("Available static methods: ");

               for (Method m : vaultUnlockedClass.getMethods()) {
                  if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 0) {
                     methods.append(m.getName()).append("(), ");
                  }
               }

               log(methods.toString());
               Object vaultInstance = null;

               try {
                  vaultInstance = vaultUnlockedClass.getMethod("getInstance").invoke(null);
                  log("Got instance via getInstance()");
               } catch (NoSuchMethodException var35) {
                  try {
                     vaultInstance = vaultUnlockedClass.getMethod("instance").invoke(null);
                     log("Got instance via instance()");
                  } catch (NoSuchMethodException var34) {
                     try {
                        vaultInstance = vaultUnlockedClass.getMethod("get").invoke(null);
                        log("Got instance via get()");
                     } catch (NoSuchMethodException var33) {
                        log("No getInstance(), instance() or get() method found");
                     }
                  }
               }

               try {
                  Object services = vaultUnlockedClass.getMethod("services").invoke(null);
                  if (services != null) {
                     log("services() returned: %s", services.getClass().getName());
                     StringBuilder servicesMethods = new StringBuilder("Services methods: ");

                     for (Method mx : services.getClass().getMethods()) {
                        if (mx.getParameterCount() <= 1) {
                           servicesMethods.append(mx.getName()).append("(");
                           Class<?>[] params = mx.getParameterTypes();

                           for (int i = 0; i < params.length; i++) {
                              if (i > 0) {
                                 servicesMethods.append(",");
                              }

                              servicesMethods.append(params[i].getSimpleName());
                           }

                           servicesMethods.append("), ");
                        }
                     }

                     log(servicesMethods.toString());

                     try {
                        Object providerNames = services.getClass().getMethod("economyProviderNames").invoke(services);
                        log("economyProviderNames() returned: %s", providerNames);
                        if (providerNames instanceof Collection) {
                           for (Object name : (Collection)providerNames) {
                              if (name != null) {
                                 String nameStr = name.toString();
                                 log("Trying economyObj('%s')", nameStr);

                                 try {
                                    Object economy = services.getClass().getMethod("economyObj", String.class).invoke(services, nameStr);
                                    if (economy != null) {
                                       economy = unwrapOptional(economy);
                                       if (economy != null) {
                                          this.logEconomyMethods(economy);
                                          this.economyProvider = economy;
                                          this.available = true;
                                          log("VaultUnlocked economy integration ENABLED via economyObj('%s')!", nameStr);
                                          return;
                                       }
                                    }
                                 } catch (Exception var32) {
                                    log("economyObj('%s') failed: %s", nameStr, var32.getMessage());
                                 }
                              }
                           }
                        }
                     } catch (NoSuchMethodException var37) {
                        log("No economyProviderNames() method");
                     }

                     try {
                        Object economy = services.getClass().getMethod("economyObj").invoke(services);
                        log("services.economyObj() returned: %s", economy != null ? economy.getClass().getName() : "null");
                        if (economy != null) {
                           economy = unwrapOptional(economy);
                           if (economy != null) {
                              this.logEconomyMethods(economy);
                              this.economyProvider = economy;
                              this.available = true;
                              log("VaultUnlocked economy integration ENABLED via services().economyObj()!");
                              return;
                           }
                        }
                     } catch (NoSuchMethodException var31) {
                        try {
                           Object economyx = services.getClass().getMethod("economy").invoke(services);
                           log("services.economy() returned: %s", economyx != null ? economyx.getClass().getName() : "null");
                           if (economyx != null) {
                              economyx = unwrapOptional(economyx);
                              if (economyx != null) {
                                 this.logEconomyMethods(economyx);
                                 this.economyProvider = economyx;
                                 this.available = true;
                                 log("VaultUnlocked economy integration ENABLED via services().economy()!");
                                 return;
                              }
                           }
                        } catch (NoSuchMethodException var30) {
                           log("No economy method on services manager");
                        }
                     }
                  }
               } catch (NoSuchMethodException var38) {
                  log("No services() method");
               }

               String[] staticEconomyMethods = new String[]{"economyObj", "economy"};

               for (String methodName : staticEconomyMethods) {
                  try {
                     Object result = vaultUnlockedClass.getMethod(methodName).invoke(null);
                     log("Static %s() returned: %s", methodName, result != null ? result.getClass().getName() : "null");
                     if (result != null) {
                        Object economy = unwrapOptional(result);
                        if (economy != null) {
                           this.logEconomyMethods(economy);
                           this.economyProvider = economy;
                           this.available = true;
                           log("VaultUnlocked economy integration ENABLED via static %s()!", methodName);
                           return;
                        }

                        log("Static %s() returned empty Optional", methodName);
                     }
                  } catch (NoSuchMethodException var29) {
                     log("No static %s() method found", methodName);
                  }
               }

               if (vaultInstance != null) {
                  StringBuilder instanceMethods = new StringBuilder("Instance methods: ");

                  for (Method mxx : vaultInstance.getClass().getMethods()) {
                     if (mxx.getParameterCount() == 0) {
                        instanceMethods.append(mxx.getName()).append("(), ");
                     }
                  }

                  log(instanceMethods.toString());
                  Object economy = null;
                  String[] economyGetterNames = new String[]{"economyObj", "getEconomy", "economy", "getEcon", "econ"};

                  for (String methodName : economyGetterNames) {
                     try {
                        Object result = vaultInstance.getClass().getMethod(methodName).invoke(vaultInstance);
                        if (result != null) {
                           log("Got result via %s(): %s", methodName, result.getClass().getName());
                           economy = unwrapOptional(result);
                           if (economy != null) {
                              log("Got economy via %s() (after unwrap if needed)", methodName);
                              StringBuilder economyMethods = new StringBuilder("Economy methods: ");

                              for (Method mxxx : economy.getClass().getMethods()) {
                                 economyMethods.append(mxxx.getName());
                                 economyMethods.append("(");
                                 Class<?>[] params = mxxx.getParameterTypes();

                                 for (int i = 0; i < params.length; i++) {
                                    if (i > 0) {
                                       economyMethods.append(",");
                                    }

                                    economyMethods.append(params[i].getSimpleName());
                                 }

                                 economyMethods.append("), ");
                              }

                              log(economyMethods.toString());
                              log("Economy class: %s", economy.getClass().getName());
                              break;
                           }
                        }
                     } catch (NoSuchMethodException var36) {
                     }
                  }

                  if (economy != null) {
                     this.economyProvider = economy;
                     this.available = true;
                     log("VaultUnlocked economy integration ENABLED!");
                     return;
                  }

                  log("VaultUnlocked found but no economy provider registered.");
               }
            } catch (ClassNotFoundException var39) {
            } catch (Exception var40) {
               logWarning("Error trying %s", var40, vaultClassName);
            }
         }

         try {
            Class<?> vault2Class = Class.forName("net.milkbowl.vault2.VaultUnlocked2");
            log("Found VaultUnlocked2 class, trying static getEconomy()");
            Object economy = vault2Class.getMethod("getEconomy").invoke(null);
            if (economy != null) {
               this.economyProvider = economy;
               this.available = true;
               log("VaultUnlocked economy integration ENABLED via VaultUnlocked2!");
               return;
            }
         } catch (ClassNotFoundException var27) {
         } catch (Exception var28) {
            log("VaultUnlocked2 static getEconomy() failed: %s", var28.getMessage());
         }

         log("VaultUnlocked not detected or could not hook economy.");
      }
   }

   public boolean isAvailable() {
      this.ensureHooked();
      return this.available && this.economyProvider != null;
   }

   public BigDecimal getBalance(UUID playerUuid) {
      return this.getBalance(playerUuid, null);
   }

   public BigDecimal getBalance(UUID playerUuid, String currencyType) {
      if (!this.isAvailable()) {
         return BigDecimal.ZERO;
      } else {
         try {
            String pluginName = "AncientWaystone";
            Object result;
            if (currencyType != null && !currencyType.isEmpty()) {
               try {
                  result = this.economyProvider
                     .getClass()
                     .getMethod("balance", String.class, UUID.class, String.class, String.class)
                     .invoke(this.economyProvider, pluginName, playerUuid, "default", currencyType);
                  log("balance('%s', UUID, 'default', '%s') returned: %s", pluginName, currencyType, result);
               } catch (NoSuchMethodException var6) {
                  result = this.economyProvider
                     .getClass()
                     .getMethod("getBalance", UUID.class, String.class)
                     .invoke(this.economyProvider, playerUuid, currencyType);
               }
            } else {
               try {
                  result = this.economyProvider.getClass().getMethod("balance", String.class, UUID.class).invoke(this.economyProvider, pluginName, playerUuid);
                  log("balance('%s', UUID) returned: %s", pluginName, result);
               } catch (NoSuchMethodException var7) {
                  result = this.economyProvider.getClass().getMethod("getBalance", UUID.class).invoke(this.economyProvider, playerUuid);
                  log("getBalance(UUID) returned: %s", result);
               }
            }

            result = unwrapOptional(result);
            if (result instanceof BigDecimal) {
               return (BigDecimal)result;
            } else if (result != null) {
               log("Balance result type: %s, value: %s", result.getClass().getName(), result);
               return new BigDecimal(result.toString());
            } else {
               return BigDecimal.ZERO;
            }
         } catch (Exception var8) {
            logWarning("Error getting player balance", var8);
            return BigDecimal.ZERO;
         }
      }
   }

   public boolean hasBalance(UUID playerUuid, BigDecimal amount) {
      return this.hasBalance(playerUuid, amount, null);
   }

   public boolean hasBalance(UUID playerUuid, BigDecimal amount, String currencyType) {
      if (!this.isAvailable()) {
         return false;
      } else {
         BigDecimal balance = this.getBalance(playerUuid, currencyType);
         return balance.compareTo(amount) >= 0;
      }
   }

   public boolean withdraw(UUID playerUuid, BigDecimal amount) {
      return this.withdraw(playerUuid, amount, null);
   }

   public boolean withdraw(UUID playerUuid, BigDecimal amount, String currencyType) {
      if (!this.isAvailable()) {
         return false;
      } else {
         try {
            Object response = null;
            String pluginName = "AncientWaystone";
            if (currencyType != null && !currencyType.isEmpty()) {
               try {
                  response = this.economyProvider
                     .getClass()
                     .getMethod("withdraw", String.class, UUID.class, String.class, String.class, BigDecimal.class)
                     .invoke(this.economyProvider, pluginName, playerUuid, "default", currencyType, amount);
                  log("withdraw('%s', UUID, 'default', '%s', %s) returned: %s", pluginName, currencyType, amount, response);
               } catch (NoSuchMethodException var8) {
                  response = this.economyProvider
                     .getClass()
                     .getMethod("withdrawPlayer", UUID.class, BigDecimal.class, String.class)
                     .invoke(this.economyProvider, playerUuid, amount, currencyType);
               }
            } else {
               try {
                  response = this.economyProvider
                     .getClass()
                     .getMethod("withdraw", String.class, UUID.class, BigDecimal.class)
                     .invoke(this.economyProvider, pluginName, playerUuid, amount);
                  log("withdraw('%s', UUID, %s) returned: %s", pluginName, amount, response);
               } catch (NoSuchMethodException var10) {
                  try {
                     response = this.economyProvider
                        .getClass()
                        .getMethod("withdrawPlayer", UUID.class, BigDecimal.class)
                        .invoke(this.economyProvider, playerUuid, amount);
                     log("withdrawPlayer(UUID, %s) returned: %s", amount, response);
                  } catch (NoSuchMethodException var9) {
                     logWarning("No withdraw method found on economy provider", var9);
                     return false;
                  }
               }
            }

            if (response == null) {
               return false;
            } else {
               response = unwrapOptional(response);
               if (response instanceof Boolean) {
                  return (Boolean)response;
               } else {
                  try {
                     Boolean success = (Boolean)response.getClass().getMethod("transactionSuccess").invoke(response);
                     return success != null && success;
                  } catch (NoSuchMethodException var11) {
                     log("Withdraw response type: %s, assuming success", response.getClass().getName());
                     return true;
                  }
               }
            }
         } catch (Exception var12) {
            logWarning("Error withdrawing from player", var12);
            return false;
         }
      }
   }

   public String format(BigDecimal amount) {
      return this.format(amount, null);
   }

   public String format(BigDecimal amount, String currencyType) {
      if (!this.isAvailable()) {
         return amount.toPlainString();
      } else {
         try {
            return currencyType != null && !currencyType.isEmpty()
               ? (String)this.economyProvider.getClass().getMethod("format", BigDecimal.class, String.class).invoke(this.economyProvider, amount, currencyType)
               : (String)this.economyProvider.getClass().getMethod("format", BigDecimal.class).invoke(this.economyProvider, amount);
         } catch (Exception var4) {
            return amount.toPlainString();
         }
      }
   }
}
