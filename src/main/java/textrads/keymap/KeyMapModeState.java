package textrads.keymap;

import com.googlecode.lanterna.input.KeyStroke;
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
    private boolean escPressed;
    
    public void reset() {
        state = State.KEY_MAP_SCREEN;
        escPressed = false;
        keyMapScreenState.init(database.get(Database.OtherKeys.KEY_MAP));
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
                        setKeyMap();
                        break;
                    case 'R':
                        resetKeyMap();
                        break;
                }
                break;
            }
        }
    }
    
    private void setKeyMap() {
        keyMapScreenState.getMenu().reset();
        state = State.PRESS_KEY_SCREEN;        
        pressKeyScreenState.init(KeyMapScreenState.KEYS[0]); // TODO
    }
    
    private void resetKeyMap() {
        final KeyMap keyMap = new KeyMap();
        keyMapScreenState.init(keyMap);
        database.saveAsync(Database.OtherKeys.KEY_MAP, keyMap);
    }
    
    private void updatePressKeyScreen() {
        pressKeyScreenState.update(); // TODO
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
