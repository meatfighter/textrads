package textrads.keymap;

import java.util.ArrayList;
import java.util.List;
import textrads.ui.menu.Menu;
import textrads.ui.menu.MenuColumn;
import textrads.ui.menu.MenuItem;

public class KeyMapScreenState {
    
    public static final String[] KEYS = {
        "Shift Left",
        "Shift Right",
        "Soft Drop",
        "Rotate Counterclockwise",
        "Rotate Clockwise",
        "Pause",
    };

    private final Menu menu;
    private final String[] values = new String[KEYS.length];
    
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
        
        menu = new Menu(menuColumns, "Key Mapping", 9, 3);
    }
    
    public void init(final KeyMap keyMap) {
        final KeyDescription[] keyDescriptions = keyMap.getKeyDescriptions();
        width = 0;
        for (int i = KEYS.length - 1; i >= 0; --i) {
            values[i] = keyDescriptions[i].toString();
            width = Math.max(width, values[i].length());
        }
        width += 3 + KEYS[3].length();
        menu.reset();
    }

    public String[] getValues() {
        return values;
    }

    public int getWidth() {
        return width;
    }
    
    public Menu getMenu() {
        return menu;
    }
}
