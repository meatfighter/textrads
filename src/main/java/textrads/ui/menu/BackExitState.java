package textrads.ui.menu;

public class BackExitState {

    private final boolean escapeEnabled;
    
    private boolean escSelected;
    
    public BackExitState() {
        this(true);
    }
    
    public BackExitState(final boolean escapeEnabled) {
        this.escapeEnabled = escapeEnabled;
    }

    public void reset() {
        escSelected = false;
    }
    
    public boolean isEscSelected() {
        return escSelected;
    }

    public void setEscSelected(final boolean escSelected) {
        this.escSelected = escSelected;
    }

    public boolean isEscapeEnabled() {
        return escapeEnabled;
    }
}
