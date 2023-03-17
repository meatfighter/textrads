package textrads.keymap;

import com.googlecode.lanterna.input.KeyStroke;
import textrads.InputSource;
import static textrads.ui.menu.Menu.SELECTION_FRAMES;

public class PressKeyScreenState {
    
    private String name;
    
    private KeyStroke selection;
    private int selectionTimer;
    
    public void init(final String name) {
        this.name = name;
        selection = null;
        selectionTimer = SELECTION_FRAMES;
        InputSource.clear();
    }

    public void update() {
        if (selection == null) {
            for (int i = InputSource.MAX_POLLS - 1; i >= 0; --i) {
                final KeyStroke keyStroke = InputSource.poll();
                if (keyStroke == null) {
                    break;
                }
                handleInput(keyStroke);
            }
        } else {            
            InputSource.clear();
            if (selectionTimer > 0) {
                --selectionTimer;
            }
        }
    }
    
    private void handleInput(final KeyStroke keyStroke) {
        selection = keyStroke;
    }
    
    public KeyStroke getSelection() {
        return (selectionTimer == 0) ? selection : null;
    }

    public boolean isHighlight() {
        return selection != null;
    }
    
    public String getName() {
        return name;
    }
}
