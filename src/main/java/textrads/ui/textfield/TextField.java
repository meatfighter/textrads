package textrads.ui.textfield;

import textrads.Textrads;

public class TextField {
    
    private static final long CURSOR_BLINK_MILLIS = 530;
    
    private static final float FRAMES_PER_BLINK = (float) (Textrads.FRAMES_PER_SECOND * CURSOR_BLINK_MILLIS / 1000.0);

    private final String prompt;
    private final TextFieldValidator validator;
    
    private float frameCounter;
    private boolean cursorVisible;
    
    public TextField(final String prompt, final TextFieldValidator validator) {
        this.prompt = prompt;
        this.validator = validator;
    }
    
    public void reset() {
        resetCursor();
    }
    
    private void resetCursor() {
        cursorVisible = true;
        frameCounter = 0f;
    }
    
    public void update() {
        if (frameCounter++ >= FRAMES_PER_BLINK) {
            frameCounter -= FRAMES_PER_BLINK;
            cursorVisible = !cursorVisible;
        } 
    }
}
