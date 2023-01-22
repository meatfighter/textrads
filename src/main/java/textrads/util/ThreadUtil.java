package textrads.util;

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
    
    private ThreadUtil() {        
    }
}
