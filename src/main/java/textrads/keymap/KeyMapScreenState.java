package textrads.keymap;

import java.util.ArrayList;
import java.util.List;
import textrads.ui.menu.Menu;
import textrads.ui.menu.MenuColumn;
import textrads.ui.menu.MenuItem;

public class KeyMapScreenState {
    
    public static final String[] ACTIONS = {
        "Shift Left",
        "Shift Right",
        "Soft Drop",
        "Rotate Counterclockwise",
        "Rotate Clockwise",
        "Pause",
        "Give Up",
    };
    
    public static final int MAX_ACTION_LENGTH;
    
    static {
        int length = 0;
        for (final String action : ACTIONS) {
            length = Math.max(length, action.length());
        }
        MAX_ACTION_LENGTH = length;
    }

    private final Menu menu;
    private final String[] keys = new String[ACTIONS.length];
    
    private int width;
    
    public KeyMapScreenState() {
        
        final List<MenuItem> setMenuColumnList = new ArrayList<>();
        setMenuColumnList.add(new MenuItem("Set"));
        final MenuColumn setMenuColumn = new MenuColumn(setMenuColumnList);
        
        final List<MenuItem> resetMenuColumnList = new ArrayList<>();
        resetMenuColumnList.add(new MenuItem("Reset"));
        final MenuColumn resetMenuColumn = new MenuColumn(resetMenuColumnList);
        
        final List<MenuColumn> menuColumns = new ArrayList<>();
        menuColumns.add(setMenuColumn);
        menuColumns.add(resetMenuColumn);
        
        menu = new Menu(menuColumns, "Keymapping", 10, 3);
    }
    
    public void init(final KeyMap keyMap) {
        final Key[] keyDescriptions = keyMap.getKeys();
        width = 0;
        for (int i = ACTIONS.length - 1; i >= 0; --i) {
            keys[i] = keyDescriptions[i].toString();
            width = Math.max(width, keys[i].length());
        }
        width += 3 + MAX_ACTION_LENGTH;
        menu.reset();
    }

    public String[] getKeys() {
        return keys;
    }

    public int getWidth() {
        return width;
    }
    
    public Menu getMenu() {
        return menu;
    }
}
