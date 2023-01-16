package textrads;

public class App {

    private final GameEventSupplier[] eventSuppliers = new GameEventSupplier[2];
    
    private Mode mode;
    private boolean terminate;
    
    public void setMode(final Mode mode) throws Exception {
        this.mode = mode;
    }
    
    public Mode getMode() {
        return mode;
    }

    public boolean isTerminate() {
        return terminate;
    }

    public void setTerminate(final boolean terminate) {
        this.terminate = terminate;
    }

    public GameEventSupplier[] getEventSuppliers() {
        return eventSuppliers;
    }
}
