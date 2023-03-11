package textrads.ui.menu;

import java.util.List;

public class MenuColumn {
    
    private List<MenuItem> menuItems;
    private final int width;
    private final int height;
    
    public MenuColumn(final List<MenuItem> menuItems) {
        this.menuItems = menuItems;
        int w = 0;
        for (final MenuItem menuItem : menuItems) {
            if (!menuItem.isSpacer()) {
                w = Math.max(w, menuItem.getDescription().length());
            }
        }
        width = w + 4;
        height = 1 + 2 * (menuItems.size() - 1);
    }
    
    public void reset() {
        menuItems.forEach(menuItem -> menuItem.reset());
    }
    
    public boolean handleInput(final char c) {
        for (final MenuItem menuItem : menuItems) {
            if (menuItem.handleInput(c)) {
                return true;
            }
        }
        return false;
    }
    
    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }    
}
