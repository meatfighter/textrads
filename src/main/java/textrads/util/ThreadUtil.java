package textrads.util;

public final class ThreadUtil {
    
    public static void interruptThread(final Thread thread) {
        if (thread != null) {
            thread.interrupt();
        }
    }    
    
    public static void joinThread(final Thread thread) {
        if (thread != null) {
            while (true) {
                try {
                    thread.join();
                    return;
                } catch (final InterruptedException ignored) {                
                }
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
