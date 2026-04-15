package dev.jvnm.plugin.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.jvnm.plugin.model.PriceItem;
import dev.jvnm.plugin.utils.FileUtil;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationService {
   private static final Logger logger = Logger.getLogger(ConfigurationService.class.getName());
   private final Path configFile;
   private final Gson gson;
   private final Map<String, Object> configData = new ConcurrentHashMap<>();

   public ConfigurationService(Path dataDirectory) {
      this.configFile = dataDirectory.resolve("config.json");
      this.gson = new GsonBuilder().setPrettyPrinting().create();
      this.load();
   }

   private void load() {
      if (!Files.exists(this.configFile)) {
         this.save();
      } else {
         try (Reader reader = Files.newBufferedReader(this.configFile)) {
            Map<String, Object> loaded = (Map<String, Object>)this.gson.fromJson(reader, (new TypeToken<Map<String, Object>>() {
               {
                  Objects.requireNonNull(ConfigurationService.this);
               }
            }).getType());
            if (loaded != null) {
               this.configData.clear();
               this.configData.putAll(loaded);
            }
         } catch (IOException var6) {
            logger.log(Level.SEVERE, "Failed to load configuration", (Throwable)var6);
         }
      }
   }

   public void save() {
      Map<String, Object> snapshot = new HashMap<>(this.configData);
      String json = this.gson.toJson(snapshot);
      FileUtil.writeAsync(this.configFile, json);
   }

   public void set(String key, Object value) {
      this.configData.put(key, value);
      this.save();
   }

   public <T> T get(String key) {
      return (T)this.configData.get(key);
   }

   public <T> Optional<T> get(String key, Class<T> type) {
      Object val = this.configData.get(key);
      return type.isInstance(val) ? Optional.of(type.cast(val)) : Optional.empty();
   }

   public <T> T get(String key, T defaultValue) {
      Object val = this.configData.get(key);

      try {
         return (T)(val != null ? val : defaultValue);
      } catch (ClassCastException var5) {
         logger.warning("Config type mismatch for key: " + key);
         return defaultValue;
      }
   }

   public int getInt(String key) {
      Object val = this.configData.get(key);
      return val instanceof Number ? ((Number)val).intValue() : -1000;
   }

   public List<PriceItem> getPriceItems() {
      if (this.configData.get("price_items") instanceof List<?> rawList) {
         List<PriceItem> priceItems = new ArrayList<>();

         for (Object item : rawList) {
            if (item instanceof Map) {
               @SuppressWarnings("unchecked")
               Map<String, Object> itemMap = (Map<String, Object>) item;
               String itemId = (String) itemMap.get("itemId");
               Object amountObj = itemMap.get("amount");
               if (itemId != null && amountObj instanceof Number) {
                  int amount = ((Number) amountObj).intValue();
                  priceItems.add(new PriceItem(itemId, amount));
               }
            }
         }

         return priceItems;
      } else {
         String oldItemId = this.get("price_item_id");
         Integer oldAmount = this.getInt("price_amount");
         if (oldItemId != null && oldAmount != null && oldAmount > 0) {
            List<PriceItem> migratedList = new ArrayList<>();
            migratedList.add(new PriceItem(oldItemId, oldAmount));
            this.setPriceItems(migratedList);
            return migratedList;
         } else {
            return new ArrayList<>();
         }
      }
   }

   public void setPriceItems(List<PriceItem> priceItems) {
      List<Map<String, Object>> jsonList = new ArrayList<>();

      for (PriceItem item : priceItems) {
         Map<String, Object> itemMap = new HashMap<>();
         itemMap.put("itemId", item.getItemId());
         itemMap.put("amount", item.getAmount());
         jsonList.add(itemMap);
      }

      this.configData.put("price_items", jsonList);
      this.configData.remove("price_item_id");
      this.configData.remove("price_amount");
      this.save();
   }

   public BigDecimal getCurrencyPrice() {
      Object priceObj = this.configData.get("currency_price");
      return priceObj instanceof Number ? BigDecimal.valueOf(((Number)priceObj).doubleValue()) : null;
   }

   public String getCurrencyType() {
      Object typeObj = this.configData.get("currency_type");
      return typeObj instanceof String && !((String)typeObj).isEmpty() ? (String)typeObj : null;
   }

   public void setCurrencyPrice(BigDecimal amount, String currencyType) {
      this.configData.put("currency_price", amount.doubleValue());
      if (currencyType != null && !currencyType.isEmpty()) {
         this.configData.put("currency_type", currencyType);
      } else {
         this.configData.remove("currency_type");
      }

      this.save();
   }

   public void clearCurrencyPrice() {
      this.configData.remove("currency_price");
      this.configData.remove("currency_type");
      this.save();
   }

   public boolean hasCurrencyPrice() {
      BigDecimal price = this.getCurrencyPrice();
      return price != null && price.compareTo(BigDecimal.ZERO) > 0;
   }
}
