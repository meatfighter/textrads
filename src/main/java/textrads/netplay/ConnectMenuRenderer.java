package textrads.netplay;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.ui.common.Colors;
import textrads.ui.menu.BackExitRenderer;
import textrads.ui.menu.MenuColumnsRenderer;
import textrads.util.GraphicsUtil;

public class ConnectMenuRenderer {
    
    public static final TextColor BACKGROUND_COLOR = Colors.BLACK;
    public static final TextColor TITLE_COLOR = Colors.WHITE;
    
    private final HostPortRenderer hostPortRenderer = new HostPortRenderer();
    private final BackExitRenderer backExitRenderer = new BackExitRenderer();
    private final MenuColumnsRenderer menuColumnsRenderer = new MenuColumnsRenderer();    
    
    public void render(final TextGraphics g, final TerminalSize size, final ConnectMenuState connectMenuState) {
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fill(' ');

        final int oy = (size.getRows() - 12) / 2;
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, TITLE_COLOR);
        GraphicsUtil.centerString(g, size, oy, connectMenuState.getTitle());
        
        hostPortRenderer.render(g, size, connectMenuState.getHost(), connectMenuState.getPort(), oy + 2);
    }
}
