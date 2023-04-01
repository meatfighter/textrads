package textrads.ui.menu;

import java.util.ArrayList;
import java.util.List;

public class Chooser {
    
    private static final int DEFAULT_DISPLAYED_ITEMS_PER_PAGE = 9;

    private final List<List<MenuColumn>> pages = new ArrayList<>();
    
    private final String title;
    private final int displayedItemsPerPage;
    private final BackExitState backExitState;
    
    private int width;
    private int height;
    private int itemsWidth;
    private int itemsHeight;
    
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
    
    public void init(final List<String> items) {

        pages.clear();
        itemsWidth = 0;
        itemsHeight = 0;
        List<MenuItem> page = new ArrayList<>();
        for (int i = 0, end = items.size() - 1, index = 1; i <= end; ++i) {            
            page.add(new MenuItem(items.get(i), Character.forDigit(index++, 10)));
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
            }
        }
        
        width = Math.max(itemsWidth, title.length());        
        height = 7 + itemsHeight;
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
}
