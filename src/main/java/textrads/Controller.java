package textrads;

public class Controller {
    
    private Mode mode;
    private boolean terminate;
    
    public void setMode(final Mode mode) {
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
}
