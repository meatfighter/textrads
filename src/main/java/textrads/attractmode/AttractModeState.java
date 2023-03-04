package textrads.attractmode;

public class AttractModeState {
    
    private static interface Durations {
        int TITLE_FLASHING = 7;
        int DEMO = 25;
        int RECORDS = 7;
        int PSA = 3;        
    }
    
    public static enum Mode {
        TITLE_SCREEN,
        DEMO,
        RECORDS,
        PSA,
    }
    
    private TitleScreenState titleScreenState = new TitleScreenState();
    
    private Mode mode = Mode.TITLE_SCREEN;
    
    public AttractModeState() {
        titleScreenState.reset();
    }
    
    public void update() {
        switch (mode) {
            case TITLE_SCREEN:
                updateTitleScreen();
                break;
        }
    }
    
    private void updateTitleScreen() {
        titleScreenState.update();
    }

    public Mode getMode() {
        return mode;
    }

    public TitleScreenState getTitleScreenState() {
        return titleScreenState;
    }
}