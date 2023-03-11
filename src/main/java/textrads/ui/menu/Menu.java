package textrads.ui.menu;

import com.googlecode.lanterna.input.KeyStroke;
import java.util.List;
import textrads.InputSource;

public class Menu {
    
    public static final int COLUMN_SPACER = 6;
    
    private final List<MenuColumn> menuColumns;
    private final int width;
    private final int height;
    private final String title;
    
    private final BackExitState backExitState = new BackExitState();
    
    private KeyStroke selection;
    
    public Menu(final String title, final List<MenuColumn> menuColumns) {
        this.menuColumns = menuColumns;
        this.title = title;
        
        int w = 0;
        int h = 0;
        for (int i = menuColumns.size() - 1; i >= 0; --i) {
            final MenuColumn menuColumn = menuColumns.get(i);
            if (i > 0) {
                w += COLUMN_SPACER;
            }
            w += menuColumn.getWidth();
            h = Math.max(h, menuColumn.getHeight());
        }
        h += 8;
        
        width = w;
        height = h;
    }
    
    public void reset() {
        selection = null;
        menuColumns.forEach(menuColumn -> menuColumn.reset());
        backExitState.reset();
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
        }
    }
    
    private void handleInput(final KeyStroke keyStroke) {
        switch (keyStroke.getKeyType()) {
            case Escape:
                selection = keyStroke;
                backExitState.setEscSelected(true);
                break;
            case Character: {
                final Character character = keyStroke.getCharacter();
                if (character == null) {
                    break;
                }
                final char c = Character.toUpperCase(character);
                for (final MenuColumn menuColumn : menuColumns) {
                    if (menuColumn.handleInput(c)) {
                        selection = keyStroke;
                        break;
                    }    
                }
                break;
            }
        }
    }
    
    public KeyStroke getSelection() {
        return selection;
    }

    public List<MenuColumn> getMenuColumns() {
        return menuColumns;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getTitle() {
        return title;
    }

    public BackExitState getBackExitState() {
        return backExitState;
    }
}
