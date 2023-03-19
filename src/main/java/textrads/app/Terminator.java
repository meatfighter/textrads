package textrads.app;

public final class Terminator {
    
    private static volatile boolean terminate;

    public static boolean isTerminate() {
        return terminate;
    }

    public static void setTerminate(final boolean terminate) {
        Terminator.terminate = terminate;
    }
    
    private Terminator() {        
    }
}