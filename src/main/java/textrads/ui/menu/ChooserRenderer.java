package textrads.ui.menu;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import java.util.List;
import textrads.ui.common.Colors;
import textrads.util.GraphicsUtil;

public class ChooserRenderer {
    
    public static final TextColor BACKGROUND_COLOR = Colors.BLACK;
    private static final TextColor TITLE_COLOR = Colors.WHITE;
    
    private final BackExitRenderer backExitRenderer = new BackExitRenderer();
    private final MenuColumnsRenderer menuColumnsRenderer = new MenuColumnsRenderer();    
    
    public void render(final TextGraphics g, final TerminalSize size, final Chooser chooser) {
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fill(' ');
        
        final int oy = (size.getRows() - chooser.getHeight()) / 2;
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, TITLE_COLOR);
        GraphicsUtil.centerString(g, size, oy, chooser.getTitle());

        menuColumnsRenderer.render(g, size, chooser.getPage(), 
                Math.max(0, (size.getColumns() - chooser.getItemsWidth()) / 2), oy + 2);
        
        final List<List<MenuColumn>> pages = chooser.getPages();
        if (pages.size() > 1) {
            final int pageIndex = chooser.getPageIndex();
            if (pageIndex == 0) {
                menuColumnsRenderer.render(g, size, chooser.getNextMenuColumns(), 
                        (size.getColumns() - chooser.getNextWidth()) / 2, oy + chooser.getHeight() - 4);
            } else if (pageIndex == pages.size() - 1) {
                menuColumnsRenderer.render(g, size, chooser.getPreviousMenuColumns(), 
                        (size.getColumns() - chooser.getPreviousWidth()) / 2, oy + chooser.getHeight() - 4);
            } else {
                menuColumnsRenderer.render(g, size, chooser.getPreviousNextMenuColumns(), 
                        (size.getColumns() - chooser.getPreviousNextWidth()) / 2, oy + chooser.getHeight() - 4);
            }
        }
        
        backExitRenderer.render(g, size, chooser.getBackExitState(), oy + chooser.getHeight() - 2);
    }
}
