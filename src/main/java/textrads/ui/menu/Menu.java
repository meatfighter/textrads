package textrads.ui.menu;

import java.util.List;
import textrads.InputSource;

public class Menu {
    
    public static final int COLUMN_SPACER = 6;
    
    private List<MenuColumn> menuColumns;
    private int width;
    private int height;
    private String title;
    
    public void init(final String title, final List<MenuColumn> menuColumns) {
        this.menuColumns = menuColumns;
        this.title = title;
        
        width = 0;
        height = 0;
        for (int i = menuColumns.size() - 1; i >= 0; --i) {
            final MenuColumn menuColumn = menuColumns.get(i);
            if (i > 0) {
                width += COLUMN_SPACER;
            }
            width += menuColumn.getWidth();
            height = Math.max(height, menuColumn.getHeight());
        }
        height += 8;
    }
    
    public void update() {
        InputSource.poll(); // TODO
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
}
