package textrads.netplay;

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
    public static final TextColor INFORM_COLOR = new TextColor.Indexed(223);    
    public static final TextColor WAITING_COLOR = new TextColor.Indexed(158);
    public static final TextColor ERROR_COLOR = new TextColor.Indexed(197);
    
    private static final String[] DOTS_STRINGS = new String[ConnectScreenState.MAX_DOTS + 1];
    static {
        for (int i = 0; i <= ConnectScreenState.MAX_DOTS; ++i) {
            final StringBuilder sb = new StringBuilder();
            for (int j = 0; j < i; ++j) {
                sb.append('.');
            }
            DOTS_STRINGS[i] = sb.toString();
        }
    }
    
    private final HostPortRenderer hostPortRenderer = new HostPortRenderer();
    private final BackExitRenderer backExitRenderer = new BackExitRenderer();
    private final MenuColumnsRenderer menuColumnsRenderer = new MenuColumnsRenderer();    
    
    public void render(final TextGraphics g, final TerminalSize size, final ConnectScreenState state) {
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fill(' ');

        final int oy = (size.getRows() - 15) / 2;
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, TITLE_COLOR);
        GraphicsUtil.centerString(g, size, oy, state.getTitle());
        
        hostPortRenderer.render(g, size, state.getHost(), state.getPort(), oy + 2);
        backExitRenderer.render(g, size, state.getBackExitState(), oy + 14);
        
        if (state.getMessage() == null) {        
            menuColumnsRenderer.render(g, size, state.getStartMenu(), (size.getColumns() - 9) / 2, oy + 6);
            menuColumnsRenderer.render(g, size, state.getSetMenu(), (size.getColumns() - (Menu.COLUMN_SPACER + 23)) / 2, 
                    oy + 9);
            return;
        } 
        
        switch (state.getMessageType()) {
            case INFORM:
                GraphicsUtil.setColor(g, BACKGROUND_COLOR, INFORM_COLOR);
                GraphicsUtil.centerString(g, size, oy + 7, state.getMessage());
                break;
            case WAITING: {
                GraphicsUtil.setColor(g, BACKGROUND_COLOR, WAITING_COLOR);
                g.putString(GraphicsUtil.centerString(g, size, oy + 7, state.getMessage(), ConnectScreenState.MAX_DOTS) 
                        + state.getMessage().length(), oy + 7, DOTS_STRINGS[state.getDots()]);
                break;
            }
            case ERROR:
                GraphicsUtil.setColor(g, BACKGROUND_COLOR, ERROR_COLOR);
                GraphicsUtil.centerString(g, size, oy + 7, state.getMessage());
                break;
        }
    }
}
