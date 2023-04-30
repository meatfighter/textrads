package textrads.ui.menu;

import com.googlecode.lanterna.input.KeyStroke;
import java.util.List;
import textrads.input.InputSource;

public class Menu {
    
    public static final int COLUMN_SPACER = 6;

    public static final int DEFAULT_TITLE_MARGIN = 3;
    public static final int DEFAULT_BACK_EXIT_MARGIN = 5;
    
    public static final int SELECTION_FRAMES = 3;
    
    private final List<MenuColumn> menuColumns;
    private final int width;
    private final int height;
    private final String title;
    private final int titleMargin;
    private final int backExitMargin;
    
    private final BackExitState backExitState;
    
    private KeyStroke selection;
    private int selectionTimer;
    
    public Menu(final List<MenuColumn> menuColumns, final String title) {
        this(menuColumns, title, DEFAULT_TITLE_MARGIN, DEFAULT_BACK_EXIT_MARGIN, new BackExitState(true));
    }    

    public Menu(final List<MenuColumn> menuColumns, final String title, final boolean escapeEnabled) {
        this(menuColumns, title, DEFAULT_TITLE_MARGIN, DEFAULT_BACK_EXIT_MARGIN, new BackExitState(escapeEnabled));
    }
    
    public Menu(final List<MenuColumn> menuColumns, final String title, final BackExitState backExitState) {
        this(menuColumns, title, DEFAULT_TITLE_MARGIN, DEFAULT_BACK_EXIT_MARGIN, backExitState);
    }    
    
    public Menu(final List<MenuColumn> menuColumns, final String title, final int titleMargin) {
        this(menuColumns, title, titleMargin, DEFAULT_BACK_EXIT_MARGIN, new BackExitState(true));
    }
    
    public Menu(final List<MenuColumn> menuColumns, final String title, final int titleMargin, 
            final boolean escapeEnabled) {
        this(menuColumns, title, titleMargin, DEFAULT_BACK_EXIT_MARGIN, new BackExitState(escapeEnabled));
    }
    
    public Menu(final List<MenuColumn> menuColumns, final String title, final int titleMargin, 
            final int backExitMargin) {
        this(menuColumns, title, titleMargin, backExitMargin, new BackExitState(true));
    }
    
    public Menu(final List<MenuColumn> menuColumns, final String title, final int titleMargin, 
            final int backExitMargin, final boolean escapeEnabled) {
        this(menuColumns, title, titleMargin, backExitMargin, new BackExitState(escapeEnabled));
    }
    
    public Menu(final List<MenuColumn> menuColumns, final String title, final int titleMargin, 
            final int backExitMargin, final BackExitState backExitState) {
        
        this.menuColumns = menuColumns;
        this.title = title;
        this.titleMargin = titleMargin;
        this.backExitMargin = backExitMargin;        
        this.backExitState = backExitState;
        
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
        
        width = w;
        height = h + titleMargin + backExitMargin;
    }
    
    public void reset() {
        selection = null;
        selectionTimer = SELECTION_FRAMES;
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
            if (selectionTimer > 0) {
                --selectionTimer;
            }
        }
    }
    
    private void handleInput(final KeyStroke keyStroke) {
        switch (keyStroke.getKeyType()) {
            case Escape:
                if (backExitState.isBackEnabled()) {
                    selection = keyStroke;
                    backExitState.setSelected(true);
                }
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
        return (selectionTimer == 0) ? selection : null;
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

    public int getTitleMargin() {
        return titleMargin;
    }

    public int getBackExitMargin() {
        return backExitMargin;
    }

    public String getTitle() {
        return title;
    }

    public BackExitState getBackExitState() {
        return backExitState;
    }
}
