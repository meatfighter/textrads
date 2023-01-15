package textrads;

public class App {
    
    private Mode mode;
    private boolean terminate;
    
    private GameEventSupplier gameEventSupplierP1;
    private GameEventSupplier gameEventSupplierP2;
    
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

    public GameEventSupplier getGameEventSupplierP1() {
        return gameEventSupplierP1;
    }

    public void setGameEventSupplierP1(final GameEventSupplier gameEventSupplierP1) {
        this.gameEventSupplierP1 = gameEventSupplierP1;
    }

    public GameEventSupplier getGameEventSupplierP2() {
        return gameEventSupplierP2;
    }

    public void setGameEventSupplierP2(final GameEventSupplier gameEventSupplierP2) {
        this.gameEventSupplierP2 = gameEventSupplierP2;
    }
}
