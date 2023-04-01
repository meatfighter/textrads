package textrads.ui.menu;

import java.util.List;

public class Chooser {
    
    private static final int DEFAULT_DISPLAYED_ITEMS_PER_PAGE = 9;
    
    private final String title;
    private final int displayedItemsPerPage;
    private final BackExitState backExitState;
    
    private List<String> items;
    private int width;
    private int height;
    
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
        this.items = items;
        
        width = 0;
        items.forEach(item -> width = Math.max(width, item.length()));
        width = Math.max(width + 4, title.length());
        
        height = 6 + 2 * displayedItemsPerPage;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
