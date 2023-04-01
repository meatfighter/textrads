package textrads.ui.menu;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.ui.common.Colors;
import textrads.util.GraphicsUtil;

public class MenuRenderer {
    
    public static final TextColor BACKGROUND_COLOR = Colors.BLACK;
    private static final TextColor TITLE_COLOR = Colors.WHITE;
    
    private final BackExitRenderer backExitRenderer = new BackExitRenderer();
    private final MenuColumnsRenderer menuColumnsRenderer = new MenuColumnsRenderer();
    
    public void render(final TextGraphics g, final TerminalSize size, final Menu menu) {
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fill(' ');
        
        final int ox = (size.getColumns() - menu.getWidth()) / 2;
        final int oy = (size.getRows() - menu.getHeight()) / 2;
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, TITLE_COLOR);
        GraphicsUtil.centerString(g, size, oy, menu.getTitle());
        
        menuColumnsRenderer.render(g, size, menu.getMenuColumns(), ox, oy + menu.getTitleMargin());
        
        backExitRenderer.render(g, size, menu.getBackExitState(), oy + menu.getHeight());
    }
}
