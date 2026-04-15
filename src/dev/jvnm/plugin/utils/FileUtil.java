package dev.jvnm.plugin.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtil {
   private static final Logger logger = Logger.getLogger(FileUtil.class.getName());
   private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "AncientWaystone-IO");
      t.setDaemon(true);
      return t;
   });

   public static void writeAsync(Path target, String content) {
      IO_EXECUTOR.submit(() -> writeAtomically(target, content));
   }

   public static void writeAtomically(Path target, String content) {
      Path tempFile = target.getParent().resolve(target.getFileName() + ".tmp");

      try {
         if (!Files.exists(target.getParent())) {
            Files.createDirectories(target.getParent());
         }

         Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8));

         try {
            Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (IOException var7) {
            try {
               Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException var6) {
               logger.log(Level.SEVERE, "Fallback move failed", (Throwable)var6);
               throw var6;
            }
         }

         logger.info("Successfully wrote to: " + target);
      } catch (Exception var8) {
         logger.log(Level.SEVERE, "Failed to write file atomically: " + target, (Throwable)var8);

         try {
            Files.deleteIfExists(tempFile);
         } catch (IOException var5) {
         }
      }
   }

   public static void await() {
      try {
         IO_EXECUTOR.submit(() -> {}).get();
      } catch (Exception var1) {
         throw new RuntimeException(var1);
      }
   }

   public static void shutdown() {
      IO_EXECUTOR.shutdown();
   }
}
