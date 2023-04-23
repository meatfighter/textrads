package textrads.ui.menu;

public class BackExitState {
    
    private static final String DEFAULT_BACK_KEY_NAME = "Esc";
    private static final String DEFAULT_BACK_LABEL = "Back";

    private final String backKeyName;
    private final String backLabel;
    private final boolean backEnabled;
    
    private boolean selected;
    
    public BackExitState() {
        this(DEFAULT_BACK_KEY_NAME, DEFAULT_BACK_LABEL, true);
    }
    
    public BackExitState(final boolean backEnabled) {
        this(DEFAULT_BACK_KEY_NAME, DEFAULT_BACK_LABEL, backEnabled);
    }
    
    public BackExitState(final String backLabel) {
        this(DEFAULT_BACK_KEY_NAME, backLabel, true);
    }
    
    public BackExitState(final String backKeyName, final String backLabel) {
        this(backKeyName, backLabel, true);
    }
    
    public BackExitState(final String backKeyName, final String backLabel, final boolean backEnabled) {
        this.backKeyName = backKeyName;
        this.backLabel = backLabel;
        this.backEnabled = backEnabled;
    }

    public void reset() {
        selected = false;
    }
    
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    public boolean isBackEnabled() {
        return backEnabled;
    }

    public String getBackKeyName() {
        return backKeyName;
    }

    public String getBackLabel() {
        return backLabel;
    }
}
