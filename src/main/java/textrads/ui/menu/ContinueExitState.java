package textrads.ui.menu;

public class ContinueExitState {
    
    private boolean enterSelected;
    
    public void reset() {
        enterSelected = false;
    }
    
    public boolean isEnterSelected() {
        return enterSelected;
    }
    
    public void setEnterSelected(final boolean enterSelected) {
        this.enterSelected = enterSelected;
    }    
}
