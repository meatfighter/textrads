package textrads.ui.menu;

public class BackExitState {

    private boolean escSelected;

    public void reset() {
        escSelected = false;
    }
    
    public boolean isEscSelected() {
        return escSelected;
    }

    public void setEscSelected(final boolean escSelected) {
        this.escSelected = escSelected;
    }
}
