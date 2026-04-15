package dev.jvnm.plugin.model;

public class PriceItem {
   private final String itemId;
   private final int amount;

   public PriceItem(String itemId, int amount) {
      this.itemId = itemId;
      this.amount = amount;
   }

   public String getItemId() {
      return this.itemId;
   }

   public int getAmount() {
      return this.amount;
   }

   @Override
   public String toString() {
      return this.amount + "x " + this.itemId;
   }
}
