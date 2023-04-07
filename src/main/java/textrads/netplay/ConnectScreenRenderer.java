package textrads.netplay;

import textrads.ui.message.MessageState;
import textrads.ui.message.MessageRenderer;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.ui.common.Colors;
import textrads.ui.menu.BackExitRenderer;
import textrads.ui.menu.Menu;
import textrads.ui.menu.MenuColumnsRenderer;
import textrads.util.GraphicsUtil;

public class ConnectScreenRenderer {
    
    public static final TextColor BACKGROUND_COLOR = Colors.BLACK;
    public static final TextColor TITLE_COLOR = Colors.WHITE;
    
    private final HostPortRenderer hostPortRenderer = new HostPortRenderer();
    private final MenuColumnsRenderer menuColumnsRenderer = new MenuColumnsRenderer();
    private final MessageRenderer messageRenderer = new MessageRenderer();
    private final BackExitRenderer backExitRenderer = new BackExitRenderer();    
    
    public void render(final TextGraphics g, final TerminalSize size, final ConnectScreenState state) {
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fill(' ');

        final int oy = (size.getRows() - 15) / 2;
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, TITLE_COLOR);
        GraphicsUtil.centerString(g, size, oy, state.getTitle());
        
        hostPortRenderer.render(g, size, state.getHost(), state.getPort(), oy + 2);
        backExitRenderer.render(g, size, state.getBackExitState(), oy + 14);
        
        final MessageState messageState = state.getMessageState();
        
        if (messageState.getMessage() == null) {        
            menuColumnsRenderer.render(g, size, state.getStartMenu(), (size.getColumns() - 9) / 2, oy + 6);
            menuColumnsRenderer.render(g, size, state.getSetMenu(), (size.getColumns() - (Menu.COLUMN_SPACER + 23)) / 2, 
                    oy + 9);
            return;
        } 
        
        messageRenderer.render(g, size, messageState, oy + 7);
    }
}
