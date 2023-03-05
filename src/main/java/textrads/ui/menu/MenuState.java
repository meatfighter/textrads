package textrads.ui.menu;

import java.util.List;

public class MenuState {
    
    private List<MenuItem> menuItems;
    private int width;
    private int height;
    
    public void init(final List<MenuItem> menuItems) {
        this.menuItems = menuItems;
        
        width = 0;
        for (final MenuItem menuItem : menuItems) {
            width = Math.max(width, menuItem.getDescription().length());
        }
        width += 4;
        
        height = 2 + menuItems.size();
    }
    
    public void update() {
        
    }
}
