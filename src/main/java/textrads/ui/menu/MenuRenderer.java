package textrads.ui.menu;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import java.util.List;
import textrads.ui.common.Colors;
import textrads.util.GraphicsUtil;

public class MenuRenderer {
    
    public static final TextColor BACKGROUND_COLOR = Colors.BLACK;
    private static final TextColor TITLE_COLOR = Colors.WHITE;
    
    private final MenuItemRenderer menuItemRenderer = new MenuItemRenderer();
    private final BackExitRenderer backExitRenderer = new BackExitRenderer();
    
    public void render(final TextGraphics g, final TerminalSize size, final Menu menu) {
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fill(' ');
        
        final int ox = (size.getColumns() - menu.getWidth()) / 2;
        final int oy = (size.getRows() - menu.getHeight()) / 2;
        final int menuY = oy + menu.getTitleMargin();
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, TITLE_COLOR);
        GraphicsUtil.centerString(g, size, oy, menu.getTitle());
        
        final List<MenuColumn> menuColumns = menu.getMenuColumns();
        for (int i = 0, x = ox, end = menuColumns.size(); i < end; ++i) {
            final MenuColumn menuColumn = menuColumns.get(i);
            final List<MenuItem> menuItems = menuColumn.getMenuItems();
            for (int j = menuItems.size() - 1; j >= 0; --j) {
                menuItemRenderer.render(g, size, menuItems.get(j), x, menuY + 2 * j);
            }
            x += Menu.COLUMN_SPACER + menuColumn.getWidth();
        }
        
        backExitRenderer.render(g, size, menu.getBackExitState(), oy + menu.getHeight());
    }
}
