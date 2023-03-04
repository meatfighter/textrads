package textrads.attractmode;

import textrads.Textrads;

public class TitleScreenState {
    
    private static final double SECONDS_PER_FALL = 1.0;
    private static final double FLASHES_PER_SECOND = 1.25;
    
    private static final float FALL_PER_FRAME = (float) (1.0 / (Textrads.FRAMES_PER_SECOND * SECONDS_PER_FALL));
    private static final float FLASH_PER_FRAME = (float) (FLASHES_PER_SECOND / (double) Textrads.FRAMES_PER_SECOND);
    private static final int FLASH_FRAMES = AttractModeState.Durations.TITLE_FLASHING * Textrads.FRAMES_PER_SECOND;
    
    static enum Mode {
        TITLE_FALLING,
        PRESS_ENTER_FLASHING,
        DONE,
    }
    
    private Mode mode;
    private int landedLines;
    private float fallFraction;
    private float flashFraction;
    private int flashTimer;
    
    public void reset() {
        mode = Mode.DONE; // TODO Mode.TITLE_FALLING;
        landedLines = 0;
        fallFraction = 0f;
        flashFraction = 0f;        
    }
    
    public void update() {
        switch (mode) {
            case TITLE_FALLING:
                updateTitleFalling();
                break;
            case PRESS_ENTER_FLASHING:
                updatePressEnterFlashing();
                break;
        }
    }
    
    private void updateTitleFalling() {
        fallFraction += FALL_PER_FRAME;
        if (fallFraction >= 1f) {
            fallFraction = 0;
            if (++landedLines == 5) {
                landedLines = 0;
                flashFraction = 1f;
                flashTimer = FLASH_FRAMES;
                mode = Mode.PRESS_ENTER_FLASHING;
            }
        }
    }
    
    private void updatePressEnterFlashing() {
        if (--flashTimer == 0) {
            mode = Mode.DONE;
            return;
        }
        flashFraction -= FLASH_PER_FRAME;
        if (flashFraction <= 0f) {
            flashFraction += 1f;
        }        
    }
    
    public Mode getMode() {
        return mode;
    }

    public int getLandedLines() {
        return landedLines;
    }

    public float getFallFraction() {
        return fallFraction;
    }
    
    public boolean isFlash() {
        return flashFraction >= 0.5f;
    }
}
