package dev.jvnm.plugin.repository;

import java.util.Optional;

public interface CooldownRepository {
   void load();

   void save();

   Optional<Long> getCooldown(String var1);

   void setCooldown(String var1, long var2);
}
