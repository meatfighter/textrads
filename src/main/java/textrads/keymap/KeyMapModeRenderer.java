package textrads.keymap;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;

public class KeyMapModeRenderer {
    
    private final KeyMapScreenRenderer keyMapScreenRenderer = new KeyMapScreenRenderer();
    private final PressKeyScreenRenderer pressKeyScreenRenderer = new PressKeyScreenRenderer();
    
    public void render(final TextGraphics g, final TerminalSize size, final KeyMapModeState state) {        
        switch (state.getState()) {
            case KEY_MAP_SCREEN:
                keyMapScreenRenderer.render(g, size, state.getKeyMapScreenState());
                break;
            case PRESS_KEY_SCREEN:
                pressKeyScreenRenderer.render(g, size, state.getPressKeyScreenState());
                break;
        }
    }
}
