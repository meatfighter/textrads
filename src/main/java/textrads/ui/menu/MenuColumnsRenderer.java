package textrads.ui.menu;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import java.util.List;

public class MenuColumnsRenderer {
    
    private final MenuItemRenderer menuItemRenderer = new MenuItemRenderer();
    
    public void render(final TextGraphics g, final TerminalSize size, final List<MenuColumn> menuColumns, 
            final int ox, final int oy) {
        
        for (int i = 0, x = ox, end = menuColumns.size(); i < end; ++i) {
            final MenuColumn menuColumn = menuColumns.get(i);
            final List<MenuItem> menuItems = menuColumn.getMenuItems();
            for (int j = menuItems.size() - 1; j >= 0; --j) {
                menuItemRenderer.render(g, size, menuItems.get(j), x, oy + 2 * j);
            }
            x += Menu.COLUMN_SPACER + menuColumn.getWidth();
        }
    }
}
