package textrads;

import textrads.Textrads;

public class PressEnterState {

    private static final double FLASHES_PER_SECOND = 1.25;
    
    private static final float FLASH_PER_FRAME = (float) (FLASHES_PER_SECOND / (double) Textrads.FRAMES_PER_SECOND);
    
    private float flashFraction;
    
    public void reset() {        
        flashFraction = 1f;
    }
    
    public void update() {
        flashFraction -= FLASH_PER_FRAME;
        if (flashFraction <= 0f) {
            flashFraction += 1f;
        }
    }
    
    public boolean isFlash() {
        return flashFraction >= 0.5f;
    }    
}
