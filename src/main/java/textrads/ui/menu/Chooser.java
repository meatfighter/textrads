package textrads.ui.menu;

import com.googlecode.lanterna.input.KeyStroke;
import java.util.ArrayList;
import java.util.List;
import textrads.input.InputSource;

public class Chooser<T> {
    
    private static final int DEFAULT_DISPLAYED_ITEMS_PER_PAGE = 9;

    private final List<MenuColumn> nextMenuColumns = createNextMenuColumns();
    private final List<MenuColumn> previousNextMenuColumns = createPreviousNextMenuColumns();
    private final List<MenuColumn> previousMenuColumns = createPreviousMenuColumns();
    
    private final int nextWidth = nextMenuColumns.get(0).getWidth();    
    private final int previousWidth = previousMenuColumns.get(0).getWidth();
    private final int previousNextWidth = nextWidth + Menu.COLUMN_SPACER + previousWidth;
    
    private final List<List<MenuColumn>> pages = new ArrayList<>();
    
    private final String title;
    private final int displayedItemsPerPage;
    private final BackExitState backExitState;

    private List<T> items;
    
    private int width;
    private int height;
    private int itemsWidth;
    private int itemsHeight;
    
    private int pageIndex;
    
    private T selectedItem;
    private boolean escPressed;
    private boolean nextPressed;
    private boolean previousPressed;
    private int selectionTimer;
    
    public Chooser(final String title) {
        this(title, DEFAULT_DISPLAYED_ITEMS_PER_PAGE, true);
    }
    
    public Chooser(final String title, final int displayedItemsPerPage) {
        this(title, displayedItemsPerPage, true);
    }

    public Chooser(final String title, final int displayedItemsPerPage, final boolean escapeEnabled) {
        this.title = title;
        this.displayedItemsPerPage = displayedItemsPerPage;
        backExitState = new BackExitState(escapeEnabled);
    }
    
    private List<MenuColumn> createNextMenuColumns() {
        final List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("Next"));
        
        final List<MenuColumn> menuColumns = new ArrayList<>();
        menuColumns.add(new MenuColumn(menuItems));
        
        return menuColumns;
    }
    
    private List<MenuColumn> createPreviousNextMenuColumns() {
        final List<MenuItem> menuItems0 = new ArrayList<>();
        menuItems0.add(new MenuItem("Previous"));
        
        final List<MenuItem> menuItems1 = new ArrayList<>();
        menuItems1.add(new MenuItem("Next"));
        
        final List<MenuColumn> menuColumns = new ArrayList<>();
        menuColumns.add(new MenuColumn(menuItems0));
        menuColumns.add(new MenuColumn(menuItems1));
        
        return menuColumns;
    }
    
    private List<MenuColumn> createPreviousMenuColumns() {
        final List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("Previous"));
        
        final List<MenuColumn> menuColumns = new ArrayList<>();
        menuColumns.add(new MenuColumn(menuItems));
        
        return menuColumns;        
    }
    
    public void init(final List<T> items) {
        
        backExitState.reset();
        nextMenuColumns.forEach(menuColumn -> menuColumn.reset());
        previousNextMenuColumns.forEach(menuColumn -> menuColumn.reset());
        previousMenuColumns.forEach(menuColumn -> menuColumn.reset());

        pages.clear();
        itemsWidth = 0;
        itemsHeight = 0;
        List<MenuItem> page = new ArrayList<>();
        for (int i = 0, end = items.size() - 1, index = 1; i <= end; ++i) {            
            page.add(new MenuItem(items.get(i).toString(), Character.forDigit(index, 10)));
            if (i == end || index == displayedItemsPerPage) {                
                final List<MenuColumn> menuColumnList = new ArrayList<>();
                final MenuColumn menuColumn = new MenuColumn(page);
                itemsWidth = Math.max(itemsWidth, menuColumn.getWidth());
                itemsHeight = Math.max(itemsHeight, menuColumn.getHeight());
                menuColumnList.add(menuColumn);
                pages.add(menuColumnList);
                if (i != end) {
                    index = 1;
                    page = new ArrayList<>();
                }
            } else {
                ++index;
            }
        }
        
        width = Math.max(itemsWidth, title.length());        
        height = 7 + 2 * displayedItemsPerPage;
        
        this.items = items;
        pageIndex = 0;
        selectedItem = null;
        escPressed = false;
        selectionTimer = Menu.SELECTION_FRAMES;
        
        InputSource.clear();
    }
    
    public void update() {
        if (nextPressed || previousPressed || escPressed || selectedItem != null) {
            InputSource.clear();
            if (selectionTimer > 0) {
                --selectionTimer;
            } else if (nextPressed) {
                handleNextPressed();
            } else if (previousPressed) {
                handlePreviousPressed();
            }
        } else {
            for (int i = InputSource.MAX_POLLS - 1; i >= 0; --i) {
                final KeyStroke keyStroke = InputSource.poll();
                if (keyStroke == null) {
                    break;
                }
                handleInput(keyStroke);
            }
        }
    }
    
    private void handleNextPressed() {
        nextPressed = false;
        selectionTimer = Menu.SELECTION_FRAMES;
        nextMenuColumns.forEach(menuColumn -> menuColumn.reset());
        previousNextMenuColumns.forEach(menuColumn -> menuColumn.reset());
        ++pageIndex;
    }
    
    private void handlePreviousPressed() {
        previousPressed = false;
        selectionTimer = Menu.SELECTION_FRAMES;
        previousNextMenuColumns.forEach(menuColumn -> menuColumn.reset());
        previousMenuColumns.forEach(menuColumn -> menuColumn.reset());
        --pageIndex;
    }
    
    private void handleInput(final KeyStroke keyStroke) {
        switch (keyStroke.getKeyType()) {
            case Escape:
                if (backExitState.isBackEnabled()) {
                    escPressed = true;
                    backExitState.setSelected(true);
                }
                break;
            case Character: {
                final Character character = keyStroke.getCharacter();
                if (character == null) {
                    break;
                }                
                final char c = Character.toUpperCase(character);
                
                if (pages.size() > 1) {
                    switch (c) {
                        case 'N':
                            if (pageIndex != pages.size() - 1) {
                                nextPressed = true;
                                if (pageIndex == 0) {
                                    nextMenuColumns.get(0).getMenuItems().get(0).setSelected(true);
                                } else {
                                    previousNextMenuColumns.get(1).getMenuItems().get(0).setSelected(true);
                                }
                            }
                            return;
                        case 'P':
                            if (pageIndex != 0) {
                                previousPressed = true;
                                if (pageIndex == pages.size() - 1) {
                                    previousMenuColumns.get(0).getMenuItems().get(0).setSelected(true);
                                } else {
                                    previousNextMenuColumns.get(0).getMenuItems().get(0).setSelected(true);
                                }
                            }
                            return;
                    }
                }
                
                final MenuColumn page = pages.get(pageIndex).get(0);
                if (page.handleInput(c)) {
                    selectedItem = items.get(displayedItemsPerPage * pageIndex + Character.getNumericValue(c) - 1);
                }    
                
                break;
            }
        }
    }

    public T getSelectedItem() {
        return (selectionTimer == 0) ? selectedItem : null;
    }

    public boolean isEscPressed() {
        return (selectionTimer == 0) ? escPressed : false;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getItemsWidth() {
        return itemsWidth;
    }

    public int getItemsHeight() {
        return itemsHeight;
    }

    public List<MenuColumn> getNextMenuColumns() {
        return nextMenuColumns;
    }

    public List<MenuColumn> getPreviousNextMenuColumns() {
        return previousNextMenuColumns;
    }

    public List<MenuColumn> getPreviousMenuColumns() {
        return previousMenuColumns;
    }

    public int getNextWidth() {
        return nextWidth;
    }

    public int getPreviousWidth() {
        return previousWidth;
    }

    public int getPreviousNextWidth() {
        return previousNextWidth;
    }

    public List<List<MenuColumn>> getPages() {
        return pages;
    }

    public List<MenuColumn> getPage() {
        return pages.get(pageIndex);
    }
    
    public int getPageIndex() {
        return pageIndex;
    }

    public BackExitState getBackExitState() {
        return backExitState;
    }

    public String getTitle() {
        return title;
    }
}
