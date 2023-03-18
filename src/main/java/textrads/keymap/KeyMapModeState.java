package textrads.keymap;

import com.googlecode.lanterna.input.KeyStroke;
import textrads.InputEventSource;
import textrads.InputSource;
import textrads.db.Database;
import textrads.db.DatabaseSource;
import textrads.ui.menu.Menu;

public class KeyMapModeState {

    public static enum State {
        KEY_MAP_SCREEN,
        PRESS_KEY_SCREEN,
    }
    
    private final Database database = DatabaseSource.getDatabase();
    private final KeyMapScreenState keyMapScreenState = new KeyMapScreenState();
    private final PressKeyScreenState pressKeyScreenState = new PressKeyScreenState();
    
    private State state;
    private Key[] keys;
    private int keyIndex;
    private boolean escPressed;    
    
    public void reset() {
        state = State.KEY_MAP_SCREEN;
        keys = null;
        keyIndex = 0;
        escPressed = false;
        keyMapScreenState.init(database.get(Database.OtherKeys.KEY_MAP));
        InputSource.clear();
    }
    
    public void update() {
        if (escPressed) {
            return;
        }
        switch (state) {
            case KEY_MAP_SCREEN:
                updateKeyMapScreen();
                break;
            case PRESS_KEY_SCREEN:
                updatePressKeyScreen();
                break;
        }
    }
    
    private void updateKeyMapScreen() {
        final Menu menu = keyMapScreenState.getMenu();
        menu.update();
        final KeyStroke keyStroke = menu.getSelection();
        if (keyStroke == null) {
            return;
        }
        switch (keyStroke.getKeyType()) {
            case Escape:
                escPressed = true;
                break;
            case Character: {
                final Character c = keyStroke.getCharacter();
                if (c == null) {
                    break;
                }
                switch (Character.toUpperCase(c)) {
                    case 'S':
                        handleSet();
                        break;
                    case 'R':
                        handleReset();
                        break;
                }
                break;
            }
        }
    }
    
    private void handleSet() {        
        state = State.PRESS_KEY_SCREEN;
        keyIndex = 0;
        keys = new Key[KeyMap.DEFAULT_KEYS.length];
        pressKeyScreenState.init(0);
    }
    
    private void handleReset() {
        final KeyMap keyMap = new KeyMap();
        InputEventSource.setKeyMap(keyMap);
        keyMapScreenState.init(keyMap);
        database.saveAsync(Database.OtherKeys.KEY_MAP, keyMap);
    }
    
    private void updatePressKeyScreen() {
        pressKeyScreenState.update();
        final KeyStroke keyStroke = pressKeyScreenState.getSelection();
        if (keyStroke != null) {
            keys[keyIndex++] = new Key(keyStroke);
            if (keyIndex == keys.length) {
                final KeyMap keyMap = new KeyMap(keys);
                InputEventSource.setKeyMap(keyMap);
                keyMapScreenState.init(keyMap);
                database.saveAsync(Database.OtherKeys.KEY_MAP, keyMap);
                                
                state = State.KEY_MAP_SCREEN;                
                keys = null;
                keyIndex = 0;
                escPressed = false;
                keyMapScreenState.getMenu().reset();                
            } else {
                pressKeyScreenState.init(keyIndex);
            }
        }
    }

    public State getState() {
        return state;
    }

    public KeyMapScreenState getKeyMapScreenState() {
        return keyMapScreenState;
    }

    public PressKeyScreenState getPressKeyScreenState() {
        return pressKeyScreenState;
    }
    
    public boolean isEscPressed() {
        return escPressed;
    }
}
