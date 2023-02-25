package textrads.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class ThreadUtil {
    
    public static void interrupt(final Thread thread) {
        if (thread == null) {
            return;
        }
        thread.interrupt();
    }    
    
    public static void join(final Thread thread) {
        if (thread == null) {
            return;
        }
        while (true) {
            try {
                thread.join();
                return;
            } catch (final InterruptedException ignored) {                
            }
        }
    }
    
    public static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException ignored) {            
        }
    }
    
    public static void sleepOneSecond() {
        sleep(1000L);
    }
    
    public static void shutdownAndAwaitTermination(final ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (final InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private ThreadUtil() {        
    }
}
