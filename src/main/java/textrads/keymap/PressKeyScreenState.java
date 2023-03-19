package textrads.keymap;

import com.googlecode.lanterna.input.KeyStroke;
import textrads.input.InputSource;
import static textrads.ui.menu.Menu.SELECTION_FRAMES;

public class PressKeyScreenState {
    
    private String action;
    private String defaultKey;
    
    private KeyStroke selection;
    private int selectionTimer;
    
    public void init(final int keyIndex) {
        this.action = KeyMapScreenState.ACTIONS[keyIndex];
        this.defaultKey = KeyMap.DEFAULT_KEYS[keyIndex].toString();
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

    public boolean isKeyPressed() {
        return selection != null;
    }
    
    public String getAction() {
        return action;
    }

    public String getDefaultKey() {
        return defaultKey;
    }
}
